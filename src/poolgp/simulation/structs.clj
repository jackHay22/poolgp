(ns poolgp.simulation.structs
  (:require [poolgp.simulation.utils :as utils])
  (:gen-class))

(defprotocol StateInterface
  (init-state [s c])
  (update-state [s c]))

;render on object, context Graphics2D object
(defprotocol Renderable (render [o c g]))

(defrecord SystemState [init-handler update-handler render-handler]
  StateInterface
  (init-state [state context]
    (init-handler context))
  (update-state [state context]
    (update-handler context))
  Renderable
  (render [state context graphics]
    (render-handler context graphics)))

;GameState
; {
;   :p1 (Player.)
;   :p2 (Player.)
;   :current :p1/:p2
;   :stripes [(Ball.) (Ball.)]
;   :solids [(Ball.) (Ball.)]
;   :pocketed [(Ball.) (Ball.)]
; }

(defrecord GameState [p1 p2 current stripes solids pocketed])

;Player
; {
;   :type :genetic/:interactive
;   :strategy (push code)
;   :fitness int
; }

(defrecord Player [type strategy fitness])

;Ball
; {
;   :x int
;   :y int
;   :img "" -> image
; }

(defrecord Ball [x y img]
  Renderable
  (render [b c g]
    (utils/draw-image g (:x b) (:y b) (:img b))))
