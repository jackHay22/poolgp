(ns poolgp.simulation.players.manager
  (:require [poolgp.simulation.players.push :as push]
            [poolgp.peripherals.interactionutils :as interaction])
  (:import poolgp.simulation.structs.Player)
  (:gen-class))

(defn init-player
  "initialize a player record from the json structure"
  [player-json id]
  (Player.
    id
    nil ;clojush-indiv
    (if (:genetic player-json) :genetic :interactive)
    (push/load-push (:strategy player-json))))

(defn init-clojush-player
  "initialize a player from a
   network received clojush packet"
   [clojush-p id]
   (Player.
     id
     clojush-p
     :genetic
     (:program (:indiv clojush-p))))

(defn update-operations
  "update the gamestate of an analysis state
  with the current player for that gamestate
  return: gamestate with any decisions made
  (controller is nil when not in demo)"
  [gamestate current-player controller]
  (if (:ready? gamestate)
        (if (= (:type current-player) :genetic)
            ;do push evaluation
            (assoc
              (update-in gamestate
                [:table-state] push/eval-push (:strategy current-player)
                                              (:max-push-iterations gamestate)
                                              (:push-inputs gamestate))
              :ready? false)
            ;allow controller interaction
            (interaction/update-interaction gamestate controller))
      gamestate))

(defn display-operations
  "display controller"
  [gr gs controller]
  (interaction/render-interaction gr gs controller))
