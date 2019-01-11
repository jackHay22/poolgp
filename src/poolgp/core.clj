(ns poolgp.core
  (:require [poolgp.peripherals.window.demowindow :as window]
            [poolgp.peripherals.server :as server]
            [poolgp.peripherals.tablebuilder :as tablebuilder]
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
   ["-e" "--edit FILENAME" "Edit filename"
    :default false
    :validate [utils/path? "Must be a valid filepath"]]
   ["-b" "--blank FILENAME" "Write blank state"
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
        (:edit options)   (tablebuilder/edit-tables (:edit options))
        (:blank options) (utils/write-json-file (:blank options) resources/EMPTY-CONFIG-STATE)
        :else            (server/start-server (:port options)))))
