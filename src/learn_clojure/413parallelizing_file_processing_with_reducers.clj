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
(defn count-map
  "Returns a map of words to occurence count in the given string"
  [s]
  ; why use fn here?  Ah fn is the longhand of #() anonymous function short hand.  Either would work
  ; fn is nice here because another set of () doesn't help me with clarity
  ; reduce
  ; it took me a while to figure out {} is for so extended explaination here
  ; reduce takes two or three parameters.  Here we are using the three parameter mode.  
  ; (reduce fn {} [])
  ; str/split returns a vector but our function works with maps parameter m and word parameter w
  ; when an additional parameter is given {} it becomes the first element processed.
  ; so on its first 'iteration' reduce gets an empty map and a the first word from the vector
  ; this is an idiom use for converting collection types, for example
  ; (reduce conj #{} [:a :b :c])
  ; converts a vector into a set
  ; fn
  ; update-in is a slick function for updating values in a map
  ; compare with assoc-in
  ; signature: (update-in m ks f & args)
  ; it takes a map, a sequence of keys, and a function that transforms the old value to a new value for a key
  ; here we are using a one element sequence for the second parameter, [w]
  ; the update function introduced in clojure 1.7 takes a single value, so we could just do w instead of [w].  hey it works! 
  ; we want to count the words, so we want to increment the value or initialize it to 1 if the key is new, i.e., its value is nil
  ; we can just inc, we need a function that increments so
  ; (partial inc) returns a function that increments its parameter
  ; we don't want to pass nil to inc since that will throw a null pointer exception
  ; (inc nil) ; oops!
  ; so we intercept nils before they get to inc and convert them to 0 so that they increment to 1
  ; (fnil (partial inc) 0)
  ; both partial and fnil are higher-order functions since they take a function as argument and return a function
  ; here we can try passing nil and see we get a 1 back
  ; ((fnil (partial inc) 0) nil)
  (reduce (fn [m w] (update m w (fnil (partial inc) 0)))
          ;make empty map first parameter of reduce
          {}
          ;split string on space.  #" " is the syntax for regex
          (str/split s #" ")))

(defn add-maps
  "Returns a map where each key is the sum of vals of that key in m1 and m2."
  ;; instead of using the general reduce, it appears that this use case is what (merge-with f & maps) is for
  ;; where the f function is simply +.  this works.  See exmaple below
  ;; from the fold doc "when called with no arguments, must return its identity element"
  ;; this function is called from fold in keyword-count
  ;; is it best practice to always include a no argument when reducing so that it can be called from fold? 
  ;; Ah, I get it now.  Because fold executes in parallel you cannot define a starting value because there is
  ;; no starting place. And each "branch" partition has it's own start.
  ;; The no argument form is used by folk to initialize m1 map.  Contrast this with the reduce in
  ;; count-map where we are reading a line sequences from beginning to end and initializing with {}
  ([] {}) ;; Necessary base case for use a combiner in fold
  ([m1 m2]
   ;; reduce function takes two parameters, a map and a key value pair
   ;; here partial is used to create a '+' function that always adds v
   ;; as above I have converted the example to use the new update in clojure 1.7 instead of update-in
   ;; here is a runnable example 
   ;; (reduce (fn [m [k v]] (update m k (fnil (partial + v) 0))) {:a 1 :b 2} {:b 1 :c 2} ) 
   ;; unlike in the function count map where the first map is empty for initialization, now we start with 
   ;; a populated map {:a 1 :b 2}, then each key value pair of the second map is passed in one pair at a time
   ;; to the funciton that takes a map and key value pair and returns a map to be the first element of the next call
   ;; example using merge-with.  Maybe merge-with doesn't perform as well?
   ;; (merge-with + {:a 1 :b 2} {:b 1 :c 2} )
   (reduce (fn [m [k v]] (update m k (fnil (partial + v) 0))) m1 m2)))

;; main file processing
(defn keyword-count
  "Returns a map of the word counts"
  [filename]
  ;; convert file to sequences optimized for use with reduce in parallel
  ;; iota uses java nio to provide a memory mapped view of the file for random access
  ;; and structures the resulting sequences internally optimized for reduce's parallelism. 
  (->> (iota/seq filename) 
       ;; filter out nil values 
       ;; this is a clojure idiom, filter takes out everything that evaluates to false
       ;; identity just returns whatever it is passed
       ;; nil is false so gets filtered out
       ;; could also use a nil check here I belive (nil?)
       (r/filter identity)
       ;; count word occurences in parallel for each line
       ;; use r/map for parallel processing 
       ;; note that one of the benefits of the reduce framework is that it will combine
       ;; filter/map into a single operation.  No need for filter to output and manage a sequence to pass to map
       (r/map count-map)
       ;; combine the resulting maps in parallel
       (r/fold add-maps)))
