(ns poolgp.peripherals.interactionutils
  (:require [poolgp.simulation.utils :as utils]
            [poolgp.simulation.analysis.game.table.physics :as physics]
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

(defn- cue-strike
  "update cue ball velocity if hit"
  ;TODO: should this be an event handler?
  [state]
  (if (:release? (:controller (:gs state)))
      (let [force (:force (:controller (:gs state)))
            angle (:angle (:controller (:gs state)))]
      (assoc-in
        (update-in state [:gs :balls]
          #(map (fn [b]
                    (if (= (:id b) :cue)
                        (assoc b :vector
                            (physics/vector-from-angle angle force))
                        b))
                %))
        [:gs :controller :release?] false))
      state))

(defn- do-cue-draw-loc
  "calculate the x,y pt to draw the cue image"
  [state]
  ;TODO
  (let [controller (:controller (:gs state))
        angle (:angle controller)
        mouse (:mouse controller)
        end-x (- (:x mouse) (* config/CUE-HOLD-DIST (Math/cos angle)))
        end-y (- (:y mouse) (* config/CUE-HOLD-DIST (Math/sin angle)))
        half-img-size (/ (.getWidth (:cue controller)) 2)
        offset-x (- half-img-size (* half-img-size (Math/cos angle)))
        offset-y (- half-img-size (* half-img-size (Math/sin angle)))]
        ;TODO
        (assoc-in state [:gs :controller :cue-draw]
          (Vector. (- end-x offset-x) (- end-y offset-y)))))

(defn update-interaction
  "update user interaction state"
  [state]
  (if (:mouse-entered? (:controller (:gs state)))
      (let [controller (:controller (:gs state))
            cue-ball-loc (:center (reduce
                                    #(if (= (:id %2) :cue) (reduced %2) %1)
                                    nil (:balls (:gs state))))
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
                      (list [:gs :controller :angle]
                              angle
                            [:gs :controller :mouse]
                              mouse-loc
                            [:gs :controller :force]
                            ;TODO
                              (int (* (- dist 230) 0.1))
                            [:gs :controller :rotate-op]
                              rotate-op))))))
      state))

(defn render-interaction
  "render cue if mouse on table"
  [g controller]
  (if (:mouse-entered? controller)
      (if (not (= (:rotate-op controller) nil))
          (utils/draw-image-rotate g (:x (:cue-draw controller))
                                     (:y (:cue-draw controller))
                                     (:cue controller)
                                     (:rotate-op controller)))))

(defn do-interactive-turn
  "on ready and interactive player up,
  do interactive turn (allow cue strike)
  this also updates the controller until
  not ready?"
  [gamestate controller]
  (println "controller update")
  ;TODO: once cue strike complete, set ready? false to progress turn
  (assoc gamestate :ready? false)
  )
