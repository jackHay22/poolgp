(ns poolgp.simulation.analysis.game.manager
  (:require [poolgp.simulation.analysis.game.table.manager :as table-manager]
            [poolgp.simulation.analysis.game.playeroperations :as operations])
  (:import poolgp.simulation.structs.GameState)
  (:gen-class))

(defn game-init
  "init gamestate"
  [gamestate-json images?]
  (GameState.
    ;table state
    (table-manager/table-init (:table gamestate-json) images?)
    (operations/init-player (:p1 gamestate-json) :p1)
    (operations/init-player (:p2 gamestate-json) :p2)
    :p1 :p2 false
    nil ;TODO: controller
    ))

(defn game-update
  "update gamestate"
  [gamestate]
  ;TODO update gamestate, controller?
  (operations/update-operations
    (update-in gamestate [:table-state] table-manager/table-update)))

(defn game-render
  "render game state"
  [gamestate gr]
  ;TODO: render score and interaction
  (do
    (table-manager/table-render (:table-state gamestate) gr)
    (operations/display-operations gamestate gr)))
