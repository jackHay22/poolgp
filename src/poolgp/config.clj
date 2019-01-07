(ns poolgp.config (:gen-class))

(def POOL-WIDTH-PX 1200)
(def POOL-HEIGHT-PX 600)
(def INFO-HEIGHT-PX 100)

;demo window configuration values
(def WINDOW-WIDTH-PX 1200)
(def WINDOW-HEIGHT-PX (+ POOL-HEIGHT-PX INFO-HEIGHT-PX))
(def WINDOW-FPS 60)
(def WINDOW-TITLE "Pool GP")
(def EDIT-WINDOW-TITLE "PoolGP Test Builder")

;Table dimensions in meters
(def TABLE-WIDTH-M 2.7)
(def TABLE-HEIGHT-M 1.4)
(def TABLE-RUNNER-M 0.15)
(def SURFACE-FRICTION 0.995)

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

(def CUE-HOLD-DIST 10)

(def DEFAULT-PORT 9999)

;max ball velocity before it skips walls
(def MAX-VELOCITY 14)

;STATE VARS

(def PAUSED? (atom true))
