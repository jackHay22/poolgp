(ns poolgp.simulation.analysis.game.manager
  (:require [poolgp.simulation.analysis.game.table.manager :as table-manager]
            [poolgp.simulation.analysis.game.rules :as rules])
  (:import poolgp.simulation.structs.GameState)
  (:gen-class))

(defn game-init
  "init gamestate"
  [gamestate-json images?]
  (GameState.
    ;table state
    (table-manager/table-init (:table gamestate-json) images?)
    :p1 :p2 false
    nil ;TODO: controller
    ))

(defn game-update
  "update gamestate"
  [gamestate]
  ;TODO update gamestate, controller?
  (rules/rules-update
    (update-in gamestate [:table-state] table-manager/table-update)))

(defn game-render
  "render game state"
  [gamestate gr]
  ;TODO: render score and interaction
  (do
    (table-manager/table-render (:table-state gamestate) gr)))
