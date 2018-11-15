(ns poolgp.structs
  (:require [poolgp.utils :as utils])
  (:gen-class))

(defprotocol StateInterface (init [s c]))

;render on object, Graphics2D object
(defprotocol Renderable (render [o g]))

(defrecord SystemState [init-handler]
  StateInterface
  (init [state context]
    (init-handler context)))

(defrecord Ball [x y img]
  Renderable
  (render [b g]
    (utils/draw-image g (:x b) (:y b) (:img b))))

; trace: path->list of actions, stripes: list of Balls, solids: list of Balls
; pocketed: list of Balls
(defrecord GameState [trace stripes solids pocketed repaint])
