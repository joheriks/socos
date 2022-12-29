;(load-pvs-attachments)


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
  step
  state
  final?
  return)


(defun run (stack)

  ; (tCheck param r)
  ; (format t "~a ~%" r)



   (loop for n from 1 to 20 do

        (let* ((frame (nth 0 stack))
	 (step (frame-step frame))
	 (final? (frame-final? frame))
	 (ret (frame-return frame)))

      
      (cond ((or (apply-pvs-fun "final" (frame-state frame))
		 (equal (frame-state frame) nil))
	     (print "return")
	     (setf exitstate (eval (frompvs "exec__state__minvector2_adt.fin__(99)")))
	     (print exitstate)
	     (pop stack)
	     (when (not stack) (loop-finish))
	     (setf newframe (nth 0 stack))
	     (setf (frame-state newframe) (apply ret (list exitstate))))
	     
	     
	    ((apply-pvs-fun "call" (frame-state frame))
	     (setf step2 (get-pvs-dt-comp "ini__trs_0__call_0?" "st__" (frame-state frame)))
	     (setf cs (get-pvs-dt-comp "ini__trs_0__call_0?" "cs__" (frame-state frame)))
	     (setf fi (get-pvs-dt-comp "ini__trs_0__call_0?" "fi__" (frame-state frame)))
	     (setf rs (get-pvs-dt-comp "ini__trs_0__call_0?" "rs__" (frame-state frame)))
	     (print "call")
	     (push (make-frame :step step2 :state nil :final? fi :return rs) stack))
	    

	    (t
	     (setf (frame-state frame) (apply step (list (frame-state frame))))
	     (format t "~a ~%" (frame-state frame))
	     )
	    )))
 	 
  ; (tCheck param r))
  ; (format t "~a ~%" r)
)

(defun tCheck(param r)
  (setf i 0)
  (setf U "Unknown")
  (setq l '())
  
  (dolist (item (coerce r 'list))
   (cond ((integerp item) t
      (setq l1 (list (nth i param) item))
      (setq l (append l l1))
      (setf i (+ i 1)))
	 ((arrayp item) t
	   (setq l2 (list (nth i param) (aref item 0)))
	   (setq l (append l l2))
	   (setf i (+ i 1)))
	 (t
	  (cond ((or (equal item t) (equal item nil))
	   (setq l3 (list  (nth i param) item)))
		(t
		  (setq l3 (list  (nth i param) U))))
		  (setq l (append l l3))
		  (setf i (+ i 1))
	 ))
   )
   (format t "~a ~%" l)
 )

(defun eval-pvs (pvs)
  (eval (frompvs pvs)))

(defun frompvs (pvs)
  (pvs2cl (pc-typecheck (pc-parse pvs 'expr))))

(defun apply-pvs-fun (fname x)
  (apply (apply (car (frompvs fname)) nil) (list x)))

(defun get-pvs-dt-comp (rname cname x)
  (apply (nth 1 (frompvs (format nil "lambda (x:(~a)): ~a(x)" rname cname))) (list x)))

(defun exec (pvs_th pvs_file)

  (setq pvsfile pvs_file)
  (change-context ".")

  (typecheck-file pvsfile nil nil nil t)

  (pvs2cl-theory (get-typechecked-theory pvs_th))
  
  (with-context pvs_th
		(setf stack (list (make-frame
				   :step (eval-pvs (format nil "~a.~a" pvs_th "step"))
				   :state (eval-pvs (format nil "~a.~a" pvs_th "enter"))
				   :final? (eval-pvs (format nil "~a.~a" pvs_th "final"))
				   :return nil)))

		(run stack)))





