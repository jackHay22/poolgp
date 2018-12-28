(ns poolgp.simulation.resources
  (:require [poolgp.config :as config])
  (:import poolgp.simulation.structs.GameState)
  (:import poolgp.simulation.structs.Player)
  (:import poolgp.simulation.structs.Table)
  (:import poolgp.simulation.structs.Vector)
  (:import poolgp.simulation.structs.Ball)
  (:import poolgp.simulation.structs.Wall)
  (:gen-class))

(def BALL-IMAGES
  {:1 "pool/test_ball.png"
   :2 "pool/test_ball.png"
   :3 "pool/test_ball.png"
   :4 "pool/test_ball.png"
   :5 "pool/test_ball.png"
   :6 "pool/test_ball.png"
   :7 "pool/test_ball.png"
   :8 "pool/test_ball.png"
   :9 "pool/test_ball.png"
   :10 "pool/test_ball.png"
   :11 "pool/test_ball.png"
   :12 "pool/test_ball.png"
   :13 "pool/test_ball.png"
   :14 "pool/test_ball.png"
   :15 "pool/test_ball.png"})

(def TABLE-IMAGES
  {:bg "pool/surface.png"

  })

(def BALLS
  (list
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :1 :solid (:1 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :2 :solid (:2 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :3 :solid (:3 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :4 :solid (:4 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :5 :solid (:5 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :6 :solid (:6 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :7 :solid (:7 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :8 :solid (:8 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :9 :striped (:9 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :10 :striped (:10 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :11 :striped (:11 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :12 :striped (:12 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :13 :striped (:13 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :14 :striped (:14 BALL-IMAGES))
    (Ball. (Vector. (rand-int 1200) (rand-int 600)) config/BALL-RADIUS-PX (Vector. (- (rand-int 3) 1) (- (rand-int 3) 1)) config/BALL-MASS-G :15 :striped (:15 BALL-IMAGES))))

(def CUE (Ball. (Vector. 600 600) config/BALL-RADIUS-PX (Vector. 0 0) config/CUE-MASS-G :cue :cue "pool/test_ball.png"))

(def EXAMPLE-STATE
  (GameState.
    (Player. :genetic (list) 0)
    (Player. :genetic (list) 0)
    :p1
    CUE
    BALLS
    (list)
    (Table. 10 (list (Vector. 10 10))
      (list (Wall. (list (Vector. 10 10)))) "pool/surface.png")))
