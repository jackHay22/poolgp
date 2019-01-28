(ns poolgp.peripherals.interactionutils
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.analysis.game.table.physics :as physics]
            [poolgp.simulation.structs :as structs]
            [poolgp.config :as config])
  (:import java.awt.MouseInfo)
  (:import poolgp.simulation.structs.Vector)
  (:gen-class))

;ControllerInterface
; {
;   :mouse-entered? true/false
;   :mouse (Vector.)
;   :force int
;   :angle int (radians)
;   :release? true/false
;   :cue path -> img
;   :rotate-op (AffineTransformOp.)/nil
;   :cue-draw (Vector.)
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

(defn- get-cue-velocity
  "calculate dx/dy vector for cue
  based on mouse loc and cue loc"
  [mouse-loc cue-loc]
  (let [dist (physics/distance mouse-loc cue-loc)
        angle (physics/pts-angle-radians cue-loc mouse-loc)]
        ;TODO
        (Vector. 0 0)
  ))

(defn- get-cue-loc
  "get cue location from gamestate"
  [gs]
  (:center (reduce
              #(if (= (:id %2) :cue) (reduced %2) %1)
              (Vector. 0 0) (:balls (:table-state gs)))))

(defn- get-mouse-loc
  "return mouse location"
  []
  (let [m-pt (.getLocation (MouseInfo/getPointerInfo))]
      (Vector. (int (.getX m-pt)) (int (.getY m-pt)))))

(defn render-interaction
  "render cue if mouse on table"
  [g gs controller]
  (if (:mouse-entered? controller)
      (utils/draw-image-rotate g 0 0
                   (:cue controller)
                   (utils/get-rotation-op
                              (physics/pts-angle-radians
                                (get-cue-loc gs) (get-mouse-loc))
                               (/ (.getWidth (:cue controller)) 2)
                               (/ (.getHeight (:cue controller)) 2)))
                               ))

(defn update-interaction
  "on ready and interactive player up,
  do interactive turn (allow cue strike)
  this also updates the controller until
  not ready? (returns gamestate)"
  [gamestate controller]
  (if (and
        (:mouse-entered? controller)
        (:release? controller))

        ;TODO: can't UNRELEASE
      (assoc
        (update-in gamestate [:table-state :balls]
          #(map (fn [b] (if (= (:id b) :cue)
                            (assoc b :vector
                              (get-cue-velocity (get-mouse-loc)
                                                (get-cue-loc gamestate)))
                            b)) %))
        :ready? false)
      ;else
      gamestate))
