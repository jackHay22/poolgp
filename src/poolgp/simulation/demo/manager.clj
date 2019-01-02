(ns poolgp.simulation.demo.manager
  (:require [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.resources :as resources]
            [poolgp.simulation.pool.physics :as physics]
            [poolgp.simulation.demo.interactionutils :as interaction])
  (:gen-class))

(defn demo-render
  "Render scene on graphics 2D object and state
  returns nothing"
  [state g]
  (let [render #(structs/render % true g)] ;true for shadows
    (do
      (utils/draw-image g 0 0 (:surface (:table state)))
      (doall (map render (:balls state)))
      (utils/draw-image g 0 0 (:raised (:table state)))
      (interaction/render-interaction state g))))

(defn demo-update
  "update game state, returns game state"
  [state]
  (physics/update-state
    (interaction/update-interaction state)))

(defn demo-init
  "load from path, return state"
  [state-path]
  (let [loaded (utils/read-state state-path)]
    (if (record? loaded)
      loaded ;return
      (doall
        (utils/write-log
          (str "File \"" state-path "\" does not appear to contain game state"))
        (System/exit 1)))))
