(defproject poolgp "0.1.0-SNAPSHOT"
  :description "Pool genetic programming simulation"
  :url "https://poolgp.jackhay.io"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot poolgp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
