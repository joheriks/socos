;;; File defining major mode for the ibp language
;;; Syntax highlighting and indentation.
;;; For XEmacs.

;;; How to use this file:

;;; For best performance, you should compile this file with M-x
;;; byte-compile-file

;;; Put this file in some directory in your load-path,
;;; and add the following line to your .emacs file:
;;; (require 'ibp-mode)

;;; You may also wish to modify the *socos-command*  depending
;;; on how you have installed socos.
;;; Default is to use socos found on PATH with no flags.

(defvar socos-command "socos" "* Socos command to be executed")
(defvar socos-arguments nil "* Socos arguments")

(require 'comint)

;; Syntax highlighting

(defconst *ibp-fontlock-list*
  (append
   '(("\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *:\\s *\\(var\\|pvar\\)\\s +\\([a-zA-Z][a-zA-Z_?0-9]*\\)"
     (3 'font-lock-type-face))
    ("\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *\\(\\[.*\\]\\)*\\s *:\\s *procedure\\b"
     (1 'font-lock-function-name-face))
    ("\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *:\\s *module\\b"
     (1 'font-lock-type-face))
    ("\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *:\\s *situation\\b"
     (1 'font-lock-constant-face))
    ("\\bdefine\\s *\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *("
     (1 'font-lock-builtin-face)))

   (mapcar '(lambda (kw) (list (concat "\\b" kw "\\b") 0 'font-lock-keyword-face))
	   '("AND" "ANDTHEN" "ARRAY" "ASSUMING"
	     "ASSUMPTION" "AUTOREWRITE" "AXIOM"
	     "BEGIN" "BUT" "BY" 
	     "CASES" "CHALLENGE" "CHOICE"
	     "CLAIM" "CLOSURE" "COND" "CONTEXT"
	     "CONJECTURE" "CONTAINING" "CONVERSION"
	     "COROLLARY" "DATATYPE" "DERIVATION"
	     "ELSE" "ELSIF" 
	     "END" "ENDASSUMING" "ENDCALL" "ENDCASES" 
	     "ENDCHOICE" "ENDCOND" "ENDIF" "ENDTABLE"
	     "EXISTS" "EXIT" "EXTENDING"
	     "FACT" "FALSE"
	     "FORALL" "FORMULA" "FROM"
	     "FUNCTION" "HAS_TYPE" "IF" "IFF" 
	     "IMPLIES" "IMPORTING" "IN"
	     "INDUCTIVE" "JUDGEMENT" "LAMBDA" "LAW"
	     "LEMMA" "LET" "LIBRARY" "MACRO"
	     "MEASURE" "NONEMPTY_TYPE" "NOT" "O"
	     "OBLIGATION" "OF" "OR" "ORELSE"
	     "POSTULATE" "PROPOSITION"  "PROOF"
	     "RECURSIVE" "SUBLEMMA" "SUBTYPES"
	     "SUBTYPES_OF" "TABLE" "THEN" "THEOREM"
	     "THEORY" "TRUE" "TYPE"
	     "TYPEPLUS" "VAR" "WHEN" "WHERE" "WITH"
	     "XOR" "AUTOREWRITEPLUS"
	     "AUTOREWRITEMINUS" "CONVERSIONPLUS"
	     "CONVERSIONMINUS" "ABORT" "CASE"
	     "DECREASING" "GOTO" "USE"
	     "POST" "PRE" "PROCEDURE" "PVAR" "RESULT"
	     "SITUATION" "STRATEGY"
	     "THEN" "VALRES" "CALL"))))

;; Indentation related things

(defun ibp-back-to-non-empty-line ()
  (let ((line (forward-line -1)))
    (while (let ((blp (point))
                 (elp (progn (end-of-line) (point))))
             (and (= 0 line)
                  (= 0 (string-match "\\s *" (buffer-substring-no-properties blp elp)))
                  (<= (- elp blp) (match-end 0))))
      (setq line (forward-line -1)))
    (beginning-of-line)
    line))

(defun ibp-count-columns (begin end)
  (- (progn (goto-char end) (current-column))
     (progn (goto-char begin) (current-column))))

(defun ibp-get-indent-more (pos endpos)
  (let ((begin-rgx "\\s *\\(begin\\|if\\|choice\\|call\\(\\s +.*\\)?\\)\\s *$")
        (invariant-rgx "^\\s *\\(define\\|pre\\|post\\|invariant\\|extern\\|importing\\|using\\).*[^;]$"))
    (if (re-search-backward begin-rgx pos t)
        tab-width
      (progn (beginning-of-line)
             (if (re-search-forward invariant-rgx endpos t)
                 (1+ (length (match-string 1)))
               nil)))))

(defun ibp-indent-fun ()
  "Indent function for ibp programs"
  (interactive)
  (let ((indent-more nil)
        (indent-less nil)
        (prev-indent nil)
        (next-indent 0)
        (blp nil)
        (elp nil)
	(indent-less-rgx "^\\s *\\(end\\(\\s +[a-zA-Z][a-zA-Z_?0-9]*\\)?\\|endif\\|endchoice\\|endcall\\)\\s *$"))

    (setq elp (progn (end-of-line) (point))
          blp (progn (beginning-of-line) (point))
          indent-less (re-search-forward indent-less-rgx elp t))

    (save-excursion
      (when (= 0 (ibp-back-to-non-empty-line))
        (setq blp (point)
              elp (progn (end-of-line) (point))
              indent-more (ibp-get-indent-more blp elp)
              prev-indent (progn (beginning-of-line)
                                 (re-search-forward "^\\s *" elp t)
                                 (match-end 0))))
      (when prev-indent
        (setq next-indent (+ (ibp-count-columns blp prev-indent)
                             (cond (indent-less (- tab-width))
                                   (indent-more indent-more)
                                   (t 0))))))
    (indent-line-to (max next-indent 0))))

;; Proving related things

(defun ibp-forward-to-non-comment-line ()
  (let ((line 0))
    (while (let ((blp (point))
                 (elp (progn (end-of-line) (point))))
             (and (= 0 line)
                  (= 0 (string-match "\\s *\\(%.*\\)*"
                                     (buffer-substring-no-properties blp elp)))
                  (<= (- elp blp) (match-end 0))))
      (setq line (forward-line 1)))
    (beginning-of-line)
    line))

(defun ibp-get-context-name ()
  (save-excursion
    (beginning-of-buffer)
    (let* ((line (ibp-forward-to-non-comment-line))
           (elp (progn (end-of-line) (point)))
           (blp (progn (beginning-of-line) (point))))
      (when (and line
                 (re-search-forward
                  "\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *:\\s *context\\b" elp t))
        (match-string 1)))))

(defun ibp-get-procedure-name ()
  (save-excursion
    (let ((elp (progn (end-of-line) (point)))
          (blp (progn (beginning-of-line) (point))))
      (when (re-search-forward
           "\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *\\(\\[.*\\]\\)*\\s *:\\s *procedure\\b" elp t)
        (match-string 1)))))

(defun ibp-get-situation-name ()
  (save-excursion
    (let ((elp (progn (end-of-line) (point)))
          (blp (progn (beginning-of-line) (point))))
      (if (re-search-forward
           "\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *:\\s *situation\\b" elp t)
          (match-string 1)
        "PRE"))))

(defun ibp-get-enclosing-procedure-name ()
  (save-excursion
    (let ((p-rgx "\\b\\([a-zA-Z][a-zA-Z_?0-9]*\\)\\s *\\(\\[.*\\]\\)*\\s *:\\s *procedure\\b")
          (line 0)
          (result nil)
          (done nil))
      (while (and (not done) (= line 0))
        (let ((elp (progn (end-of-line) (point)))
              (blp (progn (beginning-of-line) (point))))
          (if (re-search-forward p-rgx elp t)
              (progn
                (setq result (match-string 1))
                (setq done t))
            (setq line (forward-line -1)))))
      result)))


(defun ibp-verify-buffer ()
  "Runs proofs for current buffer"
  (interactive)
  (save-buffer)
  (ibp-verify-file (buffer-file-name)))


(defun ibp-verify-file (filename)
  (message (format "Verifying %s..." filename))
  (let ((buf (get-buffer-create "*SOCOS*")))
    (erase-buffer buf)
    (save-excursion
      (set-buffer buf)
      (comint-mode))
    (comint-exec buf "*SOCOS*" socos-command nil (append socos-arguments 
							 (list filename)))
    (display-buffer "*SOCOS*")))


;; The following two methods are currently out of date...

(defun ibp-prove-procedure ()
  "Runs proofs for procedure at point"
  (interactive)
  (let* ((file-name (buffer-file-name))
         (dirname (file-name-directory file-name))
         (modulename (ibp-get-module-name))
         (procedurename (ibp-get-procedure-name)))
    (cond ((not modulename)    (error "Unable to determine name of module"))
          ((not procedurename) (error "Point is not at a procedure declaration"))
          (t (ibp-recompile-and-prove file-name
                                      (format "(ibp-prover-prove-theory \"%s\" \"%s_%s\")"
                                              dirname modulename procedurename))))))

(defun ibp-prove-lemma ()
  "Prove lemma at point (point should be at a situation declaration)"
  (interactive)
  (let ((file-name (buffer-file-name))
        (procedurename (ibp-get-enclosing-procedure-name))
        (situation-name (ibp-get-situation-name)))
    (cond ((not procedurename)  (error "Unable to determine name of enclosing procedure"))
          ((not situation-name) (error "Point is not at a situation declaration"))
          (t (and (or (not (string= situation-name "PRE"))
                      (y-or-n-p "Point is not at a situation \
declaration. Prove lemmas for transitions from PRE?"))
                  (ibp-recompile-and-prove file-name
                                           (format "(ibp-prover-prove-lemma\"%s\" \"%s\")"
                                                   file-name
                                                   (format "pc_%s_%s"
                                                           procedurename
                                                           situation-name))))))))

;; Keymap

(defvar ibp-mode-map () "ibp-mode keymap")
(if ibp-mode-map
    ()
  (setq ibp-mode-map (make-sparse-keymap))
  (define-key ibp-mode-map "\C-c\C-v" 'ibp-verify-buffer))



;; Create ibp mode

(put 'ibp-mode 'font-lock-defaults '(*ibp-fontlock-list*))

(defun ibp-mode ()
  (interactive)
  (kill-all-local-variables)
  (setq major-mode 'ibp-mode)
  (setq mode-name "IBP")

  (make-local-variable 'comment-start)
  (setq comment-start "%")
  (make-local-variable 'comment-start-skip)
  (setq comment-start-skip "%+ *")

  (make-local-variable 'font-lock-defaults)
  (setq font-lock-defaults '(*ibp-fontlock-list*))

  (make-local-variable 'indent-line-function)
  (setq indent-line-function 'ibp-indent-fun)

  (setq case-fold-search t)
  (modify-syntax-entry ?_ "w")
  (modify-syntax-entry ?? "w")
  (modify-syntax-entry ?%  "< b")
  (modify-syntax-entry ?\n "> b")
  (setq font-lock-keywords-case-fold-search t)
  (setq indent-tabs-mode nil)
  (setq tab-width 4)

  (use-local-map ibp-mode-map)

  (run-hooks 'ibp-mode-hook))

;;;###autoload
(add-to-list 'auto-mode-alist '("\\.ibp$" . ibp-mode))

(provide 'ibp-mode)
