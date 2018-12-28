(ns poolgp.config (:gen-class))

;demo window configuration values
(def WINDOW-WIDTH-PX 1200)
(def WINDOW-HEIGHT-PX 640)
(def WINDOW-FPS 60)
(def WINDOW-TITLE "Pool GP")

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
(def BALL-MASS-G 160)
(def CUE-MASS-G 170)

(def DEFAULT-PORT 9999)
