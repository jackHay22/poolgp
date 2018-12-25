(ns poolgp.manager
  (:require [poolgp.simulation.demo.manager :as demo]
            [poolgp.simulation.demo.window :as demo-window]
            [poolgp.simulation.eval.manager :as eval]
            [poolgp.simulation.eval.server :as eval-server]
            [poolgp.simulation.utils :as utils]
            [clojure.tools.cli :refer [parse-opts]])
  (:import poolgp.simulation.structs.SystemState)
  (:gen-class))

(def WINDOW-SETUP
    {:title "Pool GP"
     :width 1400
     :height 700
     :fps 60})

(def EVAL-SETUP
    {:id 1
      })

(def eval-mode (SystemState.
                  #(eval/eval-init %)
                  #(eval/eval-update %)
                  #(eval/eval-render %1 %2)))
(def demo-mode (SystemState.
                  #(demo/demo-init %)
                  #(demo/demo-update %)
                  #(demo/demo-render %1 %2)))

(def opts
  [["-p" "--port PORT" "Port number"
    :default 9999
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-d" "--demo PATH" "Demo file"
    :default false
    :validate [utils/path? "Must be a valid filepath"]]
   ["-h" "--help"]])

(defn start
  "start system on cmd line args"
  [args]
  (let [run-args (parse-opts args opts)
        config (:options run-args)
        errors (:errors run-args)]
      (cond
        (> (count errors) 0) (println (first errors))
        (:demo config) (demo-window/start-window demo-mode (:demo config) WINDOW-SETUP)
        :else          (eval-server/start-server eval-mode (:port config) EVAL-SETUP))))
