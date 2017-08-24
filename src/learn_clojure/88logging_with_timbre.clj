(ns learn-clojure.88logging_with_timbre)
; from clojure cookbook 

(require '[taoensso.timbre :as log])

(defn div-4 [n]
  (log/info "Starting")
  (try 
    (/ 4 n)
    (catch Throwable t
      (log/error t "oh no!"))
    (finally 
      (log/info "Ending"))))
