(ns poolgp.simulation.demo.manager
  (:require [poolgp.simulation.structs :as pgp-protocols])
  (:import poolgp.simulation.structs.Ball)
  (:import poolgp.simulation.structs.GameState)
  (:import poolgp.simulation.structs.Player)
  (:gen-class))

(def ex-state
  (GameState.
    (Player. :interactive nil 0)
    (Player. :genetic (list) 0)
    :p1
    (list (Ball. 10 10 ""))
    (list (Ball. 20 20 ""))
    (list)))

(defn demo-render
  "Render scene on graphics 2D object and state
  returns nothing"
  [state g]
  (let [render #(pgp-protocols/render % nil g)]
  (do
    (doall (map render (:stripes state)))
    (doall (map render (:solids state))))))

(defn demo-update
  "update game state, returns game state"
  [state]
  state)

(defn demo-init
  "load from path, return state"
  [state-path]
  ;returns GameState
  )
