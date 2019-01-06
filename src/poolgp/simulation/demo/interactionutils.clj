(ns poolgp.simulation.demo.interactionutils
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.pool.physics :as physics]
            [poolgp.simulation.structs :as structs]
            [poolgp.config :as config])
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
  "update cue ball velocity if hit"
  ;TODO: should this be an event handler?
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

(defn do-cue-draw-loc
  "calculate the x,y pt to draw the cue image"
  [state]
  ;TODO
  (let [controller (:controller state)
        angle (:angle controller)
        mouse (:mouse controller)
        end-x (- (:x mouse) (* config/CUE-HOLD-DIST (Math/cos angle)))
        end-y (- (:y mouse) (* config/CUE-HOLD-DIST (Math/sin angle)))
        half-img-size (/ (.getWidth (:cue controller)) 2)
        offset-x (- half-img-size (* half-img-size (Math/cos angle)))
        offset-y (- half-img-size (* half-img-size (Math/sin angle)))]
        ;TODO
        (assoc-in state [:controller :cue-draw]
          (Vector. (- end-x offset-x) (- end-y offset-y)))))

(defn update-interaction
  "update user interaction state"
  [state]
  (if (:mouse-entered? (:controller state))
      (let [controller (:controller state)
            cue-ball-loc (:center (reduce
                                    #(if (= (:id %2) :cue) (reduced %2) %1)
                                    nil (:balls state)))
            mouse-pt (.getLocation (MouseInfo/getPointerInfo))
            mouse-loc (Vector. (int (.getX mouse-pt)) (int (.getY mouse-pt)))
            dist (physics/distance mouse-loc cue-ball-loc)
            angle (physics/pts-angle-radians cue-ball-loc mouse-loc)
            rotate-op (utils/get-rotation-op angle
                        (/ (.getWidth (:cue controller)) 2)
                        (/ (.getHeight (:cue controller)) 2))]
            ;(println (Math/toDegrees angle))
          (cue-strike
            (do-cue-draw-loc
              (reduce #(assoc-in %1 (first %2) (second %2)) state
                    (partition 2
                      (list [:controller :angle]
                              angle
                            [:controller :mouse]
                              mouse-loc
                            [:controller :force]
                            ;TODO
                              (int (* (- dist 230) 0.1))
                            [:controller :rotate-op]
                              rotate-op))))))
      state))

(defn render-interaction
  "render cue if mouse on table"
  [state g]
  (if (:mouse-entered? (:controller state))
      (let [controller (:controller state)]
      (if (not (= (:rotate-op controller) nil))
          (utils/draw-image-rotate g (:x (:cue-draw controller))
                                     (:y (:cue-draw controller))
                                     (:cue controller)
                                     (:rotate-op controller))))))
