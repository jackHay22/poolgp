(ns poolgp.manager
  (:require [poolgp.simulation.structs :as stateprotocol]
            [poolgp.simulation.eval.manager :as eval]
            [poolgp.simulation.demo.manager :as demo]
            [poolgp.simulation.demo.window :as demo-window]
            [poolgp.simulation.utils :as utils]
            [clojure.tools.cli :refer [parse-opts]])
  (:import poolgp.simulation.structs.SystemState)
  (:gen-class))

(def eval-mode (SystemState.
                  #(eval/eval-init %)
                  #(eval/eval-update %)
                  #(eval/eval-render %1 %2)))
(def demo-mode (SystemState.
                  #(do
                    (demo-window/start-window
                      (demo/demo-init %) "Pool GP" 1400 700 60))
                  #(demo/demo-update %)
                  #(demo/demo-render %1 %2)))

(def opts
  [["-p" "--port PORT" "Port number"
    :default 9999
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-d" "--demo PATH" "Demo file"
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
        (:demo config) (stateprotocol/init-state demo-mode "")
        :else (stateprotocol/init-state eval-mode (:port config)))))
