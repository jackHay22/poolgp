(ns poolgp.core
  (:require [poolgp.peripherals.window.demowindow :as window]
            [poolgp.peripherals.server :as server]
            [poolgp.peripherals.tablebuilder :as tablebuilder]
            [poolgp.peripherals.tournament :as tournament]
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
   ["-t" "--tournament PATH" "Game task definition (including tournament file)"
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
        (> (count errors) 0)  (log/write-error (first errors))
        (:demo options)       (window/start-window (utils/read-json-file (:demo options)))
        (:builder options)    (tablebuilder/edit-tables (:builder options))
        (:tournament options) (tournament/run-tournament (utils/read-json-file (:tournament options)))
        (:new options)        (utils/write-json-file (:new options) resources/EMPTY-CONFIG-STATE)
        (:eval options)       (server/start-server (utils/read-json-file (:eval options)))
        :no-opts              (do
                               (log/write-error "please use one of the following options:")
                               (println (:summary run-args))))))
