(in-package :pvs)

(libload "pvs-strategies")

; The following works around a bug in PVS 5.0 by patching
; the latest source from the repository.
(load "translate-to-yices.lisp")

