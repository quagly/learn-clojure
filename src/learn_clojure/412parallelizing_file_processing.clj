(ns learn-clojure.412parallelizing_file_processing)
;;;; from clojure cookbook 4.12
;;;; first example of parallel file processing
;;;; read file line by line and use all cores without loading the file into memory
;;;; I added a bunch of comments to the code since I don't know the bulit-ins yet
(require ['clojure.java.io :as 'jio])

;;; this does process the function in parallel for each line if the file
;;; but both reading and writing are single threaded
;;; this approach is only suitable when the computation is long running 
;;; vs. reading and writing a line
(defn pmap-file
  "Process input-file inparallel, applying procssing-fn to each row
  in parallel and output into output-file"
  [processing-fn input-file output-file]
  (with-open [rdr (jio/reader input-file)
              wtr (jio/writer output-file)]
    ; read lines into a lazy sequence of strings named 'lines'
    (let [lines (line-seq rdr)]
      ; need dorun for side-effect of writing to a file
      ; otherwise lazy sequence processing will cause files to close
      ; during read and write
      (dorun
        ; writes must be coordinated to write whole lines at a time
        ; otherwise parallel writes make a mess
        (map #(.write wtr %)
             ; built in pmap for parallel processing of sequences
             (pmap processing-fn lines))))))

;;; set up to try pmap-file
;; clojure convention seems to be double semi-colons for comments
;; see https://stackoverflow.com/questions/5084191/what-is-the-difference-between-and-in-clojure-code-comments
; clojure idiom for creating a sequence 
; atoms hold immutable data
; for shared, sychronous state
(def accumulator (atom 0))

;; defn- with a dash defines a private function
;; I think that means cannot be called outside this namespace, like java's package private
(defn- example-row-fn 
  "trivial function to pass into pmap-file.  Adds a comma and sequence number at the end of the row"
  [row-string]
  ; note that swap! .. inc is the idiomatic way to increment the atom sequence
  (str row-string "," (swap! accumulator inc) "\n"))

;; Call it
;; note that I put my input files in the resources directory
;; from REPL all paths relative to project root
(pmap-file example-row-fn "resources/input.txt" "resources/output.txt")
; note that this is not the right way to use the resources directory.  
; this will fail when running from an uberjar which puts resources in the root
; see example 44 for how to read resource files
; writing should go outside the project perhaps?  

