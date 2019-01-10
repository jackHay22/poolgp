(ns poolgp.simulation.analysis.game.playeroperations
  (:import poolgp.simulation.structs.Player)
  (:gen-class))

(defn init-player
  "initialize a player record from the json structure"
  [player-json id]
  (Player.
    id
    (if (:genetic player-json) :genetic :interactive)
    (:strategy player-json)
    0 0
    :unassigned))

(defn update-operations
  [gamestate])

(defn display-operations
  [gamestate gr])
