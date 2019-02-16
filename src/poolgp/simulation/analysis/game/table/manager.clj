(ns poolgp.simulation.analysis.game.table.manager
  (:require [poolgp.peripherals.window.renderutils :as renderutils]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.analysis.game.table.physics :as physics]
            [poolgp.simulation.resources :as resources]
            [poolgp.config :as config])
  (:import poolgp.simulation.structs.TableState)
  (:import poolgp.simulation.structs.Ball)
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

(defn table-init
  "init table from configuration"
  [table-json images?]
  (TableState.
    (map #(Ball.
            (Vector. (float (:x %)) (float (:y %)))
            config/BALL-RADIUS-PX
            (Vector. 0.0 0.0)
            config/BALL-MASS-G
            (keyword (str (:id %)))
            (keyword (:type %))
            (if images?
              (utils/load-image ((keyword (str (:id %))) resources/BALL-IMAGES))
              nil))
          (:balls table-json))
    (list)
    (if images?
      (utils/load-structure-images resources/TABLE
          [:raised] [:surface]) resources/TABLE)))

(defn table-update
  "update table"
  [tablestate]
  (physics/update-state tablestate))

(defn table-render
  "render table view"
  [tablestate gr]
  (do
    (utils/draw-image gr 0 0 (:surface (:table tablestate)))
    (doall (map #(let [center (:center %)
                       r (:r %)]
                    (utils/draw-image gr
                          (- (:x center) r)
                          (- (:y center) r) (:img %)))
                (:balls tablestate)))
    (utils/draw-image gr 0 0 (:raised (:table tablestate)))))
