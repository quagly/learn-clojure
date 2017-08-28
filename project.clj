(defproject learn-clojure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 ; for example 88 logging 
                 [com.taoensso/timbre "4.10.0"]
                 ; for example 413 parallel reducer file processing
                 [iota "1.1.3"]]
  :main ^:skip-aot learn-clojure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
