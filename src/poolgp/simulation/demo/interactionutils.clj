(ns poolgp.simulation.demo.interactionutils
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.pool.physics :as physics])
  (:import java.awt.MouseInfo)
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

;Various state transforms to GameState on player interaction

;ControllerInterface
; {
;   :mouse-entered? true/false
;   :mouse (Vector.)
;   :force int
;   :release? true/false
;   :cue path -> img
; }

(defn clicked
  "set clicked (release)"
  [e state]
  (assoc-in state [:controller :release?] true))

(defn entered
  "set entered panel"
  [e state]
  (assoc-in state [:controller :mouse-entered?] true))

(defn exited
  "set exited panel"
  [e state]
  (assoc-in state [:controller :mouse-entered?] false))

(defn cue-strike
  [state]
  (if (:release? (:controller state))
      (let [force (:force (:controller state))
            angle (:angle (:controller state))]
      (assoc-in
        (update-in state [:balls]
          #(map (fn [b]
                    (if (= (:id b) :cue)
                        (assoc b :vector
                            (physics/vector-from-angle angle force))
                        b))
                %))
        [:controller :release?] false))
      state))

(defn update-interaction
  "update user interaction state"
  [state]
  (if (:mouse-entered? (:controller state))
      (let [cue-ball-loc (:center (reduce
                                    #(if (= (:id %2) :cue) (reduced %2) %1)
                                    nil (:balls state)))
            mouse-pt (.getLocation (MouseInfo/getPointerInfo))
            mouse-loc (Vector. (int (.getX mouse-pt)) (int (.getY mouse-pt)))
            dist (physics/distance mouse-loc cue-ball-loc)]
            (cue-strike
              (reduce #(assoc-in %1 (first %2) (second %2)) state
                    (partition 2
                      (list [:controller :angle]
                              (physics/pts-angle-radians cue-ball-loc mouse-loc)
                            [:controller :mouse]
                              mouse-loc
                            [:controller :force]
                              (int (* (- dist 230) 0.1)))))))
      state))

(defn render-interaction
  "render cue if mouse on table"
  [state g]
  (if (:mouse-entered? (:controller state))
      (let [mouse-loc (:mouse (:controller state))
            half-cue-dim (int (/ (.getWidth (:cue (:controller state))) 2))
            cue-x (if (:release? (:controller state))
            ;TODO: move cue towards ball
                      (- (:x mouse-loc) (int (/ half-cue-dim 2)))
                      (- (:x mouse-loc) half-cue-dim))
            cue-y (if (:release? (:controller state))
            ;TODO: move cue towards ball
                      (- (:y mouse-loc) (int (/ half-cue-dim 2)))
                      (- (:y mouse-loc) half-cue-dim))]
      (utils/draw-image-rotate g cue-x cue-y
                                 (:cue (:controller state))
                                 (:angle (:controller state))))))
