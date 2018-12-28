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

(def TABLE-BG "pool/table.png")

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

(def LEFT-WALL
  (Wall.
    (list
      (Vector. 28 53)
      (Vector. 51 88)
      (Vector. 51 512)
      (Vector. 28 549))))

(def TOP-LEFT-WALL
  (Wall.
    (list
      (Vector. 53 28)
      (Vector. 78 50)
      (Vector. 567 50)
      (Vector. 580 28))))

(def TOP-RIGHT-WALL
  (Wall.
    (list
      (Vector. 622 28)
      (Vector. 633 50)
      (Vector. 1119 50)
      (Vector. 1146 28))))

(def RIGHT-WALL
  (Wall.
    (list
      (Vector. 1172 52)
      (Vector. 1150 86)
      (Vector. 1150 511)
      (Vector. 1172 548))))

(def BOTTOM-RIGHT-WALL
  (Wall.
    (list
      (Vector. 1145 572)
      (Vector. 1122 550)
      (Vector. 632 550)
      (Vector. 621 572))))

(def BOTTOM-LEFT-WALL
  (Wall.
    (list
      (Vector. 580 572)
      (Vector. 568 550)
      (Vector. 79 550)
      (Vector. 53 572))))

(def TABLE (Table. 20,
            (list (Vector. 32 32) (Vector. 600 32) (Vector. 1167 32)
                  (Vector. 1167 571) (Vector. 600 571) (Vector. 32 571))
            (list LEFT-WALL RIGHT-WALL
                  BOTTOM-LEFT-WALL BOTTOM-RIGHT-WALL
                  TOP-LEFT-WALL TOP-RIGHT-WALL)
            TABLE-BG))

(def EXAMPLE-STATE
  (GameState.
    (Player. :genetic (list) 0)
    (Player. :genetic (list) 0)
    :p1
    CUE
    BALLS
    (list)
    TABLE))
