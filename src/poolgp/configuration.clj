(ns poolgp.configuration
  (:require [poolgp.simulation.structs :as stateprotocol]
            [poolgp.simulation.eval.server :as eval]
            [poolgp.simulation.demo.window :as demo]
            [clojure.tools.cli :refer [parse-opts]])
  (:import poolgp.simulation.structs.SystemState)
  (:import poolgp.simulation.structs.Ball)
  (:import poolgp.simulation.structs.GameState)
  (:gen-class))

(def eval-mode (SystemState. #(eval/init-server %)))
(def demo-mode (SystemState. #(demo/init-demo % "Pool GP" 1400 700)))

(defn starting-gamestate
  [trace]
  (GameState. trace
    (list (Ball. 10 10 ""))
    (list (Ball. 20 20 ""))
    (list)
    nil ;repaint fn
    )
  )

(def opts
  [["-p" "--port PORT" "Port number"
    :default 9999
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-d" nil "Demo mode"
    :id :demo
    :default false]
   ["-h" "--help"]])

(defn start
  "start system on cmd line args"
  [args]
  (let [run-config (:options (parse-opts args opts))]
      (cond
        (:demo run-config) (stateprotocol/init demo-mode (starting-gamestate ""))
        :else (stateprotocol/init eval-mode (:port run-config)))))
