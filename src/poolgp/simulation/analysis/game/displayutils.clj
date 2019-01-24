(ns poolgp.simulation.analysis.game.displayutils
  (:require [poolgp.config :as config]
            [poolgp.simulation.utils :as utils])
  (:gen-class))

(defn- round-rect
  "create rounded rectangle with border
  using config defaults"
  [gr x y w h a-w a-h]
  (doto gr
    (.setStroke config/PANEL-INFO-STROKE)
    (.setColor config/PANEL-INFO-COLOR)
    (.fillRoundRect x y w h a-w a-h)
    (.setColor config/PANEL-INFO-STROKE-COLOR)
    (.drawRoundRect x y w h a-w a-h)))

(defn render-score
  "take graphics, render score"
  [gr p1-score p2-score]
  (let [rect-x config/INSET-MARGIN
        rect-y (+ config/POOL-HEIGHT-PX config/INSET-MARGIN)
        rect-width (int (/ config/POOL-WIDTH-PX 4))
        rect-height (- config/INFO-HEIGHT-PX (* config/INSET-MARGIN 2))]
    (round-rect gr rect-x rect-y rect-width rect-height
                    config/INSET-MARGIN config/INSET-MARGIN)
    (doto gr
      (.setColor config/PANEL-BG-COLOR)
      (.setFont config/PANEL-SCORE-FONT)
      (.fillRect (- (+ rect-x (int (/ rect-width 2))) 10)
                 (+ rect-y (int (/ rect-height 2)))
                 20 5)
      (.drawString (str p1-score) (- (+ rect-x (int (/ rect-width 4))) 15)
                                  (+ rect-y (int (/ rect-height 2)) 15))
      (.drawString (str p2-score) (+ rect-x (* (int (/ rect-width 4)) 3))
                                  (+ rect-y (int (/ rect-height 2)) 15)))))

(defn render-info
  "render information about gamestate"
  [gr gamestate]
  ;TODO: display relevant information about gamestate
  ;display current player
  )

(defn render-pocketed
  "render the pocketed balls"
  [gr pocketed]
  (let [ball-total (count pocketed)
        holder-width (+ (* 2 config/INSET-MARGIN) (* 28 ball-total))
        holder-height (+ 23 (* 2 config/INSET-MARGIN))
        holder-x (+ (* 2 config/INSET-MARGIN) (int (/ config/POOL-WIDTH-PX 4)))
        holder-y (- config/WINDOW-HEIGHT-PX holder-height config/INSET-MARGIN)]
        (if (> ball-total 0)
            (do
              (round-rect gr holder-x holder-y holder-width holder-height
                          config/INSET-MARGIN config/INSET-MARGIN)
              (doall
                (map #(utils/draw-image gr
                          %2 (+ holder-y config/INSET-MARGIN) (:img %1))
                      pocketed (iterate (partial + 28)
                                        (+ holder-x config/INSET-MARGIN))))))))
