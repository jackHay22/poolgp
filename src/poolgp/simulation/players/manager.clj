(ns poolgp.simulation.players.manager
  (:require [poolgp.simulation.players.push.interp :as push]
            [poolgp.peripherals.interactionutils :as interaction])
  (:import poolgp.simulation.structs.Player)
  (:gen-class))

(defn init-player
  "initialize a player record from the json structure"
  [player-json id]
  (Player.
    id
    (if (:genetic player-json) :genetic :interactive)
    (push/load-push (:strategy player-json))))

(defn update-operations
  "update the gamestate of an analysis state
  with the current player for that gamestate
  return: gamestate with any decisions made"
  [gamestate current-player]
  (if (:ready? gamestate)
      (assoc
        (if (= (:type current-player) :genetic)
            ;do push evaluation
            (update-in gamestate
                [:table-state] push/eval-push (:strategy current-player)
                                              (:max-push-iterations gamestate)
                                              (:push-inputs gamestate))
            ;allow controller interaction
            (interaction/do-interactive-turn gamestate))
        :ready? false)
      gamestate))

(defn display-operations
  [gamestate gr])
