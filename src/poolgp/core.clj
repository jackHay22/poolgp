(ns poolgp.core
  (:require [poolgp.peripherals.window.demowindow :as window]
            [poolgp.peripherals.server :as server]
            [poolgp.peripherals.testbuilder :as testbuilder]
            [poolgp.simulation.resources :as resources]
            [poolgp.simulation.utils :as utils]
            [poolgp.config :as config]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def opts
  [["-p" "--port PORT" "Port number"
    :default config/DEFAULT-PORT
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-d" "--demo PATH" "Demo file"
    :default false
    :validate [utils/path? "Must be a valid filepath"]]
   ["-n" "--new FILENAME" "Save filename"
    :default false]
   ["-s" "--state" "Write State"
    :default false]
   ["-h" "--help"]])

(defn -main
  "start system on cmd line args"
  [& args]
  (let [run-args (parse-opts args opts)
        options (:options run-args)
        errors (:errors run-args)]
      (cond
        (> (count errors) 0) (println (first errors))
        (:demo options)  (window/start-window (:demo options))
        ;(:new options)   (testbuilder/make-test (:new options))
        :else            (server/start-server (:port options)))))
