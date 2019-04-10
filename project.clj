(defproject poolgp "1.2.0-SNAPSHOT"
  :description "Pool genetic programming simulation"
  :url "https://github.com/jackHay22/poolgp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/data.json "0.2.6"]
                 [clojush.poolgp "3.17.1-1-SNAPSHOT"]]
  :main ^:skip-aot poolgp.core
  :jvm-opts ["-Xdock:name=PoolGP"
             "-server"
             "-Xdock:icon=resources/images/icon.png"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
