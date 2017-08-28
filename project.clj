(defproject learn-clojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 ; for example 88 logging 
                 [com.taoensso/timbre "4.10.0"]
                 ; for example 413 parallel reducer file processing
                 [iota "1.1.3"]
                 ; for json encoding
                 ; ETL pipeline with tranducers 
                 ; cloure.data.json seems to be the most common approach
                 ; see cookbook 4.23
                 [cheshire "5.8.0"]]
  :main ^:skip-aot learn-clojure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
