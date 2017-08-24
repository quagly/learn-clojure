(ns learn-clojure.44accessing_resource_files)
; from clojure cookbook 

(require '[clojure.java.io :as io]
         '[clojure.edn :as edn])

(->> "people.edn"
     io/resource
     slurp
     edn/read-string
     (map :language))

