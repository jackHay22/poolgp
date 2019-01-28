(ns poolgp.config
  (:import java.awt.Color)
  (:import java.awt.BasicStroke)
  (:import java.awt.Font)
  (:gen-class))

(def POOL-WIDTH-PX 1200)
(def POOL-HEIGHT-PX 600)
(def INFO-HEIGHT-PX 100)

;demo window configuration values
(def WINDOW-WIDTH-PX 1200)
(def WINDOW-HEIGHT-PX (+ POOL-HEIGHT-PX INFO-HEIGHT-PX))
(def INSET-MARGIN 10)
(def WINDOW-FPS 60)
(def WINDOW-TITLE "Pool GP")
(def EDIT-WINDOW-TITLE "PoolGP Test Builder")

;Table dimensions in meters
(def TABLE-WIDTH-M 2.7)
(def TABLE-HEIGHT-M 1.4)
(def TABLE-RUNNER-M 0.15)
(def SURFACE-FRICTION 0.99)

;pixels per meter ~400 at 1200 px width
(def M-PX-RATIO (/ WINDOW-WIDTH-PX (+ TABLE-WIDTH-M
                                      (* 2 TABLE-RUNNER-M))))

(def TABLE-RUNNER-WIDTH-PX (* TABLE-RUNNER-M M-PX-RATIO))
(def TABLE-WIDTH-PX (* TABLE-WIDTH-M M-PX-RATIO))
(def TABLE-HEIGHT-PX (* TABLE-HEIGHT-M M-PX-RATIO))

;ball radius in meters
(def BALL-RADIUS-M 0.0286)
(def BALL-RADIUS-PX (* BALL-RADIUS-M M-PX-RATIO))

;ball masses in grams
(def BALL-MASS-G 160) ;160
(def CUE-MASS-G 170) ;170

(def CUE-HOLD-DIST 100)

;default configuration values
(def DEFAULT-PORT 9999)
(def DEFAULT-MAX-ITERATIONS 10000)
(def DEFAULT-MAX-PUSH-ITERATIONS 1000)
(def DEFAULT-PUSH-INPUTS (list :cue :balls :pockets))

;max ball velocity before it skips walls
(def MAX-VELOCITY 10)

(def PANEL-BG-COLOR (Color. 76 68 71))
(def PANEL-INFO-COLOR (Color. 253 177 87))
(def PANEL-INFO-STROKE-COLOR (Color. 255 200 134))
(def PANEL-INFO-STROKE (BasicStroke. 2))

(def PANEL-SCORE-FONT (Font. "TimesRoman" Font/BOLD 30))

(def PAUSED? (atom true))

(def TEXT-COLOR? true)
