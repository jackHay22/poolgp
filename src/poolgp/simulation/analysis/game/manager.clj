(ns poolgp.simulation.analysis.game.manager
  (:require [poolgp.simulation.analysis.game.table.manager :as table-manager]
            [poolgp.simulation.analysis.game.rules :as rules]
            [poolgp.simulation.analysis.game.gameutils :as gameutils]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.GameState)
  (:gen-class))

(defn game-init
  "init gamestate"
  [gamestate-json images?]
  (GameState.
    ;table state
    (table-manager/table-init (:table gamestate-json) images?)
    :p1 :p2 true false
    :unassigned :unassigned
    0 0
    (if (:max-push-iterations gamestate-json)
        (:max-push-iterations gamestate-json)
        config/MAX-PUSH-ITERATIONS)
    (if (:push-inputs gamestate-json)
        (map keyword (:push-inputs gamestate-json))
        config/DEFAULT-PUSH-INPUTS)
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
  [gamestate gr demo?]
  ;TODO: render score and interaction
  (if demo?
    (do
      (table-manager/table-render (:table-state gamestate) gr)
      (gameutils/render-score gr
            (:p1-score gamestate) (:p2-score gamestate)))))
