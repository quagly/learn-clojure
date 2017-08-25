(ns learn-clojure.413parallelizing_file_processing_with_reducers)
;;;; from clojure cookbook 4.13
;;;; second example of parallel file processing
;;;; use reducers for parallel processing  without loading the file into memory
;;;; unlike receipe 412 this example parallelizes the file read as well
;;;; and is intended to be simpler, you be the judge
;;;; depends on the external iota library
;;;; https://github.com/thebusby/iota
;;;; not to be confused with the clojure testing iota library
;;;; https://github.com/juxt/iota
;;;; iota is designed to work with reduce to for handling large text files

; odd to use str as an alias for clojure.string when str is a keyword
; but I found multiple examples of it, so just going with it for now
(require '[iota                  :as io]
         '[clojure.core.reducers :as r]
         '[clojure.string        :as str]) 

;;; count words in a file

;; word counting funcitons
; nothing ioto about this one
(defn count-map
  "Returns a map of words to occurence count in the given string"
  [s]
  ; why use fn here?  Ah fn is the longhand of #() anonymous function short hand.  Either would work
  ; reduce takes a function of two parameters and goes through a collection in pairs.
  ; fnil returns 0 if (partial inc) 0 returns nil
  ; don't know what partial is for here.  returns a fuction that is inc with some parameter hard coded
  ; so it needs fewer parameters.  Not understanding how it is used here.
  ; why bare {}  I think it coersed the results of str/split into a map of word keys.  sometimes I see into {} for this
  (reduce (fn [m w] (update-in m [w] (fnil (partial inc) 0)))
          ;stick result in a map maybe?
          {}
          ;split string on space.  #" " is the syntax for regex
          (str/split s #" ")))

