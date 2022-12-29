;;
;; Socos - PVS/Lisp interface
;;
;; TODO
;; * Should also report the name of the file in which an error occured.
;;   Currently not done because it seems difficult to get access to the
;;   file name (*current-file* does not work, since the throw-catch 
;;   mechanism causes the stack to unwind)

(in-package :pvs)

(defvar *call-stack* nil)

(define-condition socos-runtime-error (error) nil)


(excl:set-signal-handler 
 15 
 '(lambda (signal &optional ignore) 
    (error (format nil "got signal ~d~%" signal)) 
    (bye 1)))


(defun socos-print-sexp (sexp)
  (format *standard-output* "~%-*-SOCOS-*-~%")
  (format *standard-output* "~s~%" sexp)
  (format *standard-output* "-*-*-*-*-*-~%~%"))


(defun socos-error (msg)
  (socos-print-sexp `(error ,msg)))


;;; Disable compilation of pvs-lib
(defun load-pvs-lib-lisp-file (lib-path)
  ;; Set up *default-pathname-defaults* and sys:*load-search-list*
  ;; so that simple loads from the pvs-lib.lisp file work.
  (let* ((*default-pathname-defaults* (merge-pathnames lib-path))
	 #+allegro (sys:*load-search-list* *pvs-library-path*)
	 (lfile "pvs-lib.lisp")
	 (*suppress-printing* t))
    (if (file-exists-p (merge-pathnames *default-pathname-defaults* lfile))
	(let ((bfile (format nil "pvs-lib.~a" *pvs-binary-type*)))
	  (setq bfile nil)
; 	  (when (or (not (file-exists-p bfile))
; 		    (compiled-file-older-than-source? lfile bfile))
; 	    (multiple-value-bind (ignore error)
; 		(ignore-errors (compile-file lfile))
; 	      (declare (ignore ignore))
; 	      (cond (error
; 		     (pvs-message "Compilation error - ~a" error)
; 		     (pvs-message "Loading lib file ~a interpreted"
; 		       (shortname lfile))
; 		     (setq bfile nil))
; 		    (t
; 		     (chmod "ug+w" (namestring bfile))))))
	  (pvs-message "Loading ~a..." (or bfile lfile))
	  (let ((*libloads* nil))
	    (multiple-value-bind (ignore error)
		(ignore-errors (load (or bfile lfile)))
	      (declare (ignore ignore))
	      (cond (error
		     (pvs-message "Error loading ~a:~%  ~a"
		       (or bfile lfile) error)
		     (when bfile
		       (pvs-message "Trying source ~a:" lfile)
		       (multiple-value-bind (ignore lerror)
			   (ignore-errors (load lfile))
			 (declare (ignore ignore))
			 (cond (lerror
				(pvs-message "Error loading ~a:~%  ~a"
				  lfile error))
			       (t (pvs-message "~a loaded" lfile)
				  (cons lfile *libloads*))))))
		    (t (pvs-message "~a loaded" (or bfile lfile))
		       (cons lfile *libloads*))))))
	(pvs-message "~a not found relative to ~a"
	  lfile *default-pathname-defaults*))))


(defun escape-string (s)
  (with-output-to-string (out)
			 (loop for i from 0 below (length s) do
			   (let* ((c (char s i))
				  (x (cond ((eq c #\Newline) "\\n")
					   ((eq c #\\) "\\\\")
					   (t (string (char s i))))))
			     (write-string x out)))))


(defun socos-hello ()
  (socos-print-sexp `(hello ,(excl::getpid))))


(defun socos-set-context (ctx)
  (unless (file-equal *pvs-context-path* 
		      (make-pathname :directory ctx))
    (change-context ctx))
  (load-prelude-library "socos"))


(defmacro with-socos-errors (&rest body)
  `(let ((*type-error-catch* 'type-error)
	 (*parse-error-catch* 'parse-error)
         ;;; hook into debugger (occurs from within PVS when something fails) 
	 (*debugger-hook* #'(lambda (c h) (socos-error (format nil "~a" c)))))
    (multiple-value-bind 
	(x msg obj)
	(catch 'type-error
	  (multiple-value-bind 
	      (y msg obj)
	      (catch 'parse-error
		(progn ,@body)
		t)
	    (unless y (socos-print-sexp `(parse-error ,(line-begin (place obj)) ,(col-begin (place obj)) ,msg))))
	  t)
      (unless x 
	    (if (place obj)
		(socos-print-sexp `(typecheck-error ,(line-begin (place obj)) ,(col-begin (place obj)) ,msg))
	      (socos-print-sexp `(error ,msg)))))))
      

(defun socos-check (pvsfile timeout)
  (handler-case
      (let ((*print-readably* nil)
	    (*noninteractive* t)
	    (*pvs-verbose* 3))
	(with-socos-errors
	  (typecheck-file pvsfile nil nil nil t)
		      
	  (let* ((theorynames (theories-in-file pvsfile))
		 (pvstheories (mapcar #'get-typechecked-theory theorynames)))
	    (dolist (theory pvstheories)
	      (install-prooflite-scripts (filename theory) (id theory) 0 t))
	    (if timeout
		(mp:with-timeout (timeout (socos-error "timeout"))
				 (prove-pvs-file pvsfile t))
				 ;;(proveit-theories pvstheories t nil t))
	      ;;(proveit-theories pvstheories t nil t))
	      (prove-pvs-file pvsfile t))
	    (status-proof-theories pvstheories)
	    (save-context))))

    (error (condition)
	   (socos-print-sexp `(error ,(format nil "~a" condition)))
	   (values nil condition)))
	     
;;; Always signal that proof checking has terminated. This is important, since the
;;; ProofChecker waits until this line has been output.
  
  (socos-print-sexp '(check-finished)))


(defun eval-pvs (pvs)
  (eval (frompvs pvs)))

(defun frompvs (pvs)
  (pvs2cl (pc-typecheck (pc-parse pvs 'expr))))

(defun update-dt-comp (x c v)
  (setf (slot-value x c) v))



(defmacro with-context (theoryname &rest body)
  `(let* ((*current-theory* (get-typechecked-theory ,theoryname))
          (*current-context* (saved-context *current-theory*))
          (*generate-tccs* 'none)
          (*suppress-msg* t)
          (*in-evaluator* t)
          (*destructive?* t)
          (*eval-verbose* nil)
          (*compile-verbose* nil)
          (*convert-back-to-pvs* nil))
     ,@body))


(defstruct frame
  theory
  state)

(defun get-entry-fun (theory)
  (eval-pvs (format nil "~a.~a" theory "enter__")))

(defun get-step-fun (theory)
  (eval-pvs (format nil "~a.~a" theory "step__")))

(defun get-exit-fun (theory)
  (eval-pvs (format nil "~a.~a" theory "exit__")))


(defun socos-start-trace (file theory)
  (load-prelude-library "ds")
  (load-pvs-attachments)

  (with-socos-errors
    (typecheck-file file))

  (pvs2cl-theory (get-typechecked-theory theory))
  
  (setf *call-stack* nil)
  
  (with-context theory
    (setf *call-stack* (list (make-frame
			      :theory theory
			      :state (eval-pvs "main__"))))))



(defun vector-to-list (a)
  (let ((len (aref a 1))
        (vec (aref a 0))
        (list nil))
    (dotimes (i len)
      (push (funcall vec i) list))
    (nreverse list)))


(defun value-to-string (v)
  (cond
   ((stringp v) v)
   ;((arrayp v) (format nil "[~{~a~^, ~}]" (vector-to-list v)))
   (t (format nil "~a" v))))


(defun socos-get-state ()
  (socos-print-sexp
   `(state
     ,(loop for frame in *call-stack* collect
	(let* ((state (frame-state frame))
	       (name (class-name (class-of state)))
	       (slots (class-slots (class-of state))))
	  (list (loop for slot in slots
		  collect (format nil "~a" (slot-definition-name slot)))
		(loop for slot in slots
		  collect (value-to-string (slot-value state (slot-definition-name slot))))))))))


(defun is-final (state)
  (not (eq nil (search "fin__" (symbol-name (class-name (class-of state)))))))



(defun is-call (state)
  (not (eq nil (search "__ret" (symbol-name (class-name (class-of state)))))))



(defun socos-step ()

  (when (not *call-stack*)
    (socos-error "attempt to call step with empty stack")
    (return-from socos-step))

  ; TODO: configurable stack depth
  (when (> (length *call-stack*) 100)
    (socos-error "maximum stack depth (100) reached"))

  (setf frame (first *call-stack*)) 
  (setf theory (frame-theory frame))

  (cond
   ((is-final (frame-state frame))
    (pop *call-stack*)
    (when (not *call-stack*) (return-from socos-step))
    (with-context 
     theory
     (setf (slot-value (frame-state (first *call-stack*)) 'cs__) 
	   (funcall (get-exit-fun theory) (frame-state frame))))
    (setf frame (first *call-stack*))
    (setf theory (frame-theory frame)))
   
   ((is-call (frame-state frame))
    (setf theory (slot-value (frame-state frame) 'th__))
    (setf calleestate (slot-value (frame-state frame) 'cs__))
    (with-context 
     theory
     (setf frame (make-frame :theory theory
			     :state (funcall (get-entry-fun theory) calleestate)))
     (push frame *call-stack*))))
  
  (handler-case
   (progn 
     (with-context
      theory
      (setf (frame-state frame) (funcall (get-step-fun theory) (frame-state frame)))))
   
   (error (condition)
	  (socos-error (format nil "~a" condition)))))
	      
