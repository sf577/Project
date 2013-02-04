;; jindent.el, used by $PTII/util/testsuite/jindent to
;; indent Java files to the Ptolemy II standard
;; Version: $Id: jindent.el,v 1.1 2002/05/04 20:57:09 cxh Exp $

(load (expand-file-name
       (concat (getenv "PTII") "/util/lisp/ptjavastyle.el")))

(defun jindent ()
  (java-mode)
  (indent-region (point-min) (point-max) 'nil)
  (save-buffer)
)
