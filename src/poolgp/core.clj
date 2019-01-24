(ns poolgp.core
  (:require [poolgp.peripherals.window.demowindow :as window]
            [poolgp.peripherals.server :as server]
            [poolgp.peripherals.tablebuilder :as tablebuilder]
            [poolgp.simulation.resources :as resources]
            [poolgp.simulation.utils :as utils]
            [poolgp.config :as config]
            [poolgp.log :as log]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def opts
  [["-e" "--eval PATH" "Evaluation task definition"
    :default false
    :validate [utils/path? "Must be a valid filepath"]]
   ["-d" "--demo PATH" "Demo file"
    :default false
    :validate [utils/path? "Must be a valid filepath"]]
   ["-b" "--builder PATH" "Edit filename"
    :default false
    :validate [utils/path? "Must be a valid filepath"]]
   ["-n" "--new FILENAME" "Write blank state"
    :default false]
   ["-h" "--help"]])

(defn -main
  "start system on cmd line args"
  [& args]
  (let [run-args (parse-opts args opts)
        options (:options run-args)
        errors (:errors run-args)]
      (cond
        (> (count errors) 0) (log/write (first errors))
        (:demo options)      (window/start-window (:demo options))
        (:builder options)   (tablebuilder/edit-tables (:builder options))
        (:new options)       (utils/write-json-file (:new options) resources/EMPTY-CONFIG-STATE)
        (:eval options)      (server/start-server (:eval options))
        :else                (log/write "ERROR" "please specify a run configuration"))))
