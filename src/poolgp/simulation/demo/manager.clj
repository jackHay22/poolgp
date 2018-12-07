(ns poolgp.simulation.demo.manager
  (:require [poolgp.simulation.structs :as pgp-protocols])
  (:gen-class))

;Game State
(def STATE (atom nil))

(defn render
  "Render scene on graphics 2D object and state"
  [g]
  (let [render #(pgp-protocols/render % g)
        state @STATE]
  (do
    (doall (map render (:stripes state)))
    (doall (map render (:solids state))))))

(defn init
  "take starting state"
  [starting-state]
  )
