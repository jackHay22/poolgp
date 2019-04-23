(ns poolgp.simulation.players.manager
  (:require [poolgp.simulation.players.push :as push]
            [poolgp.peripherals.interactionutils :as interaction]
            [clojush.globals :as clojush-globals]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.Player)
  (:gen-class))

(defn init-player
  "initialize a player record from the json structure"
  [player-json id]
  (Player.
    id
    nil ;clojush-indiv
    (read-string
      (:strategy player-json))))

(defn init-clojush-player
  "initialize a player from a
   network received clojush packet"
   [clojush-p id]
   (Player.
     id
     clojush-p
     (:program clojush-p)))

(defn configure-clojush!
  "configure clojush global values"
  []
  (do
    (reset! clojush-globals/global-evalpush-limit
            config/CLOJUSH-EVALPUSH-LIMIT)
    (reset! clojush-globals/global-max-points
            config/CLOJUSH-MAX-PTS)))

(defn update-operations
  "update the gamestate of an analysis state
  with the current player for that gamestate
  return: gamestate with any decisions made
  (controller is nil when not in demo)"
  [gamestate current-player controller]
  (if (:ready? gamestate)
    ;do push evaluation
    (assoc
      (update-in gamestate
        [:table-state] push/eval-push (:strategy current-player)
                                      (:push-inputs gamestate)
                                      (:ball-type
                                          (:current gamestate)))
      :ready? false)
      gamestate))

(defn display-operations
  "display controller"
  [gr gs controller]
  ;(interaction/render-interaction gr gs controller)
  )
