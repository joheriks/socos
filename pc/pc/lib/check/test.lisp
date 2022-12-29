;(load "exec__min.lisp")
;(load "exec__minvector.lisp")
;(load "test__exec__minvector.lisp")
(load-prelude-library "ds")
(load-pvs-attachments)

(setq pvsfile "exec_minvector")
(change-context ".")

(typecheck-file pvsfile nil nil nil t)

;(evaluation-mode-pvsio "test__exec__minvector")
;(bye)
;(print (help_pvs_attachment "query_line"))
;(print (pvsio_studio_query_line_1))
(defun run(st param r fin)

 
 ;	(format t "~a ~%" r)
	(tCheck param r)
	
  (loop until (member (aref r 0) fin) do

   ;  	(format t "~a ~%" (coerce r 'list))

	(setf r (apply st (list r)))
	(tCheck param r)
)
)	

(defun tCheck(param r)
  (setf i 0)
  (setq m '())
  
  (dolist (item (coerce r 'list))
   (cond ((integerp item) t
;	 (format t "~#[~;:~]~a" (nth i param))
;	 (format t "~#[~; ~; ~]~a " item)
	  (setq x (list (nth i param) item))
	  (setq m (append m x))
	  (setf i (+ i 1)))
	 ((arrayp item) t
	   ;(format t "~#[~;:~;~]~a" (nth i param))
	   ;(format t "~#[~; ~; ~]~a " item)
	   (setq y (list (nth i param) (aref item 0)))
	   (setq m (append m y))
	   (setf i (+ i 1)))
	 (t
	  (cond ((or (equal item t) (equal item nil))
	   (setq v (list  (nth i param) item)))
	  (t
	   (setq v (list  (nth i param) "Unknown"))))
	   (setq m (append m v))
	   (setf i (+ i 1))
	 ))
   
   )
   	(format t "~a ~%" m)
  ; (format t "~#[~;:~;~]~a" (nth i param))
  ; (format t "~#[~; ~; ~]~a " item)
  ; (format t ")~%") 
   

 )



;(print (funcall #'b))

;(setf r (coerce (list 0 5 (b) 0 0) 'array))
(setq param (list "cur" "a" "m" "k" "v1" "v2" "v3" "counter"))

;(run #'_step r (list #'fin__?_0 #'loop?_0 ))

;(run #'_step param r (list #'fin__?_0))


(defun eval-pvs (pvs)
  (eval (pvs2cl (pc-typecheck (pc-parse pvs 'expr)))))
(defun frompvs (pvs)
  (pvs2cl (pc-typecheck (pc-parse pvs 'expr))))
(let* ((theory (get-typechecked-theory "test__exec__minvector"))
       (*current-theory* theory)
       (*generate-tccs* (if nil 'all 'none))
       (*current-context* (or (saved-context theory)
                              (context nil)))
       (*suppress-msg* t)
       (*in-evaluator* t)
       (*destructive?* t)
       (*eval-verbose* nil)
       (*compile-verbose* nil)
       (*convert-back-to-pvs* nil))
 ; (defun run(st param r fin)

 
 ;	(format t "~a ~%" r)
;	(tCheck param r)
;	(setf r (apply st (list r)))
;	(print (aref r 5))
  ;(loop until (member (aref r 0) fin) do

   ;  	(format t "~a ~%" (coerce r 'list))
	
	;(setf r (apply st (list r)))
;	(print (frompvs (aref r 5)))
;	(format t "~a ~%" r)
;	(format t "~a ~%" (coerce r 'list))

;	(tCheck param r)
  
;)
  
;)
    ;  (print (generate-lisp-for-theory "test__exec__minvector"))
  ;  (print (car (coerce (pvs2cl_record b) 'list)))
  
  ;(print (eval-pvs "len(vector_val((:3,15:)))"))
   ; (print (eval-pvs "update(update(vector_val((:1,2,5:)),1,3),2,8)"))
   ;   (print (list (frompvs "fin__")))

          (run (apply (car (frompvs "step")) nil)
           param 
	  (eval-pvs "(ini__,vector_val((:8,5:)),0,0,0,0,0,0)")
      	   (list (frompvs "fin__")))
	
      	 )

(bye)

