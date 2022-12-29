(load-prelude-library "exec")
(load-prelude-library "ds")

(load-pvs-attachments)

(setq param (list "cur" "a" "m" "k" "v1" "v2" "v3" "counter"))
(setf pvs_name "exec__minvector")
(setf value "(ini__,vector_val((:8,5:)),0,0,0,0,0,0)")
(setf pvs_th "exec_minvector")

(exec pvs_name param value pvs_th)

(bye)

