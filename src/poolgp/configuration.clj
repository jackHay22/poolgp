(ns poolgp.configuration
  (:require [poolgp.structs :as stateprotocol]
            [poolgp.evalserver.server :as eval]
            [poolgp.simulation.window :as demo])
  (:import poolgp.structs.SystemState)
  (:import poolgp.structs.Ball)
  (:import poolgp.structs.GameState)
  (:gen-class))

(def eval-mode (SystemState. #(eval/init-server %)))
(def demo-mode (SystemState. #(demo/init-demo %)))

(defn starting-gamestate
  [trace]
  (GameState. trace
    (list (Ball. 10 10 ""))
    (list (Ball. 20 20 ""))
    (list)
    nil ;repaint fn
    )
  )

(def default-listen-port 9999)

(defn start
  "start system on cmd line args"
  [args]
  (if
    (empty? args) (stateprotocol/init demo-mode (starting-gamestate ""))

    ;(stateprotocol/init eval-mode default-listen-port)

    ))
