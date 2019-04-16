(ns poolgp.simulation.analysis.game.displayutils
  (:require [poolgp.config :as config]
            [poolgp.simulation.utils :as utils]
            [poolgp.simulation.resources :as resources])
  (:gen-class))

(def STATIC-BALL-IMAGES
  (atom resources/BALL-IMAGES))

(def LOADED-STATIC? (atom false))

(defn load-static!
  []
  (if (not @LOADED-STATIC?)
      (do
      (swap! STATIC-BALL-IMAGES
        (fn [static-map]
          (reduce #(update-in %1 [%2]
                      utils/load-image)
                  static-map
                  (keys static-map))))
        (reset! LOADED-STATIC? true))))

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
  [gr p1-score p2-score current p1-type p2-type]
  (let [rect-x config/INSET-MARGIN
        rect-y (+ config/POOL-HEIGHT-PX config/INSET-MARGIN)
        rect-width (int (/ config/POOL-WIDTH-PX 4))
        rect-height (- config/INFO-HEIGHT-PX (* config/INSET-MARGIN 2))
        p1-score-x (- (+ rect-x (int (/ rect-width 4))) 15)
        p1-score-y (+ rect-y (int (/ rect-height 2)) 15)
        p2-score-x (+ rect-x (* (int (/ rect-width 4)) 3))
        p2-score-y (+ rect-y (int (/ rect-height 2)) 15)]
    (round-rect gr rect-x rect-y rect-width rect-height
                    config/INSET-MARGIN config/INSET-MARGIN)
    (doto gr
      (.setColor config/PANEL-BG-COLOR)
      (.setFont config/PANEL-SCORE-FONT)
      (.fillRect (- (+ rect-x (int (/ rect-width 2))) 10)
                 (+ rect-y (int (/ rect-height 2)))
                 20 5)
      (.drawString (str p1-score) p1-score-x p1-score-y)
      (utils/draw-image (- p1-score-x 52) (- p1-score-y 24)
                        (p1-type @STATIC-BALL-IMAGES))
      (.drawString (str p2-score) p2-score-x p2-score-y)
      (utils/draw-image (+ p2-score-x 40) (- p2-score-y 24)
                        (p2-type @STATIC-BALL-IMAGES))
      (.fillRect (if (= current :p1)
                    (- (+ rect-x (int (/ rect-width 4))) 17)
                    (+ rect-x (* (int (/ rect-width 4)) 3) -2))
                (+ rect-y (int (* rect-height 0.75))) 20 2))))

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
