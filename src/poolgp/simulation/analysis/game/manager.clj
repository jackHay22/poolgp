(ns poolgp.simulation.analysis.game.manager
  (:require [poolgp.simulation.analysis.game.table.manager :as table-manager]
            [poolgp.simulation.analysis.game.rules :as rules]
            [poolgp.simulation.analysis.game.displayutils :as displayutils]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.GameState)
  (:import poolgp.simulation.structs.GamePlayer)
  (:gen-class))

(defn game-init
  "init gamestate"
  [gamestate-json images?]
  (GameState.
    ;table state
    (table-manager/table-init (:table gamestate-json) images?)
    ;current
    (GamePlayer. :p1 :unassigned 0)
    ;waiting
    (GamePlayer. :p2 :unassigned 0)
    ;ready? current-scored? scratched?
    true    false           false
    (if (:max-push-iterations gamestate-json)
        (:max-push-iterations gamestate-json)
        config/DEFAULT-MAX-PUSH-ITERATIONS)
    (if (:push-inputs gamestate-json)
        (map keyword (:push-inputs gamestate-json))
        config/DEFAULT-PUSH-INPUTS)))

(defn game-update
  "update gamestate"
  [gamestate]
  ;TODO update gamestate, controller?
  (rules/rules-update
    (update-in gamestate [:table-state] table-manager/table-update)))

(defn game-render
  "render game state"
  [gamestate gr]
  (let [p1-state (if (= (:id (:current gamestate)) :p1) :current :waiting)
        p2-state (if (= p1-state :current) :waiting :current)]
    (do
      (table-manager/table-render (:table-state gamestate) gr)
      (displayutils/render-score gr
            (:score (p1-state gamestate))
            (:score (p2-state gamestate)))
      (displayutils/render-pocketed gr
        (:pocketed (:table-state gamestate))))))

(defn game-log
  "write game level logs"
  [gamestate]
  (rules/rules-log gamestate))
