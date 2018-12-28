(ns poolgp.simulation.demo.manager
  (:require [poolgp.simulation.structs :as structs]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.resources :as resources]
            [poolgp.simulation.pool.physics :as physics])
  (:gen-class))

(defn demo-render
  "Render scene on graphics 2D object and state
  returns nothing"
  [state g]
  (let [render #(structs/render % nil g)]
    (do
      (structs/render (:table state) nil g)
      (doall (map render (:balls state))))))

(defn demo-update
  "update game state, returns game state"
  [state]
  (update-in state
    [:balls] #(physics/update-balls % (:table state))))

(defn demo-init
  "load from path, return state"
  [state-path]
    ;(utils/write-state resources/EXAMPLE-STATE))
  (let [loaded (utils/read-state state-path)]
    (if (record? loaded)
      loaded ;return
      (doall
        (utils/write-log
          (str "File \"" state-path "\" does not appear to contain game state"))
        (System/exit 1)))))
