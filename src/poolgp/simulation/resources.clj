(ns poolgp.simulation.resources
  (:require [poolgp.config :as config])
  (:import poolgp.simulation.structs.GameState)
  (:import poolgp.simulation.structs.Player)
  (:import poolgp.simulation.structs.Table)
  (:import poolgp.simulation.structs.Vector)
  (:import poolgp.simulation.structs.Ball)
  (:import poolgp.simulation.structs.Wall)
  (:import poolgp.simulation.structs.ControllerInterface)
  (:gen-class))

(def BALL-IMAGES
  {:1 "images/balls/red_solid_blank.png"
   :2 "images/balls/red_solid_blank.png"
   :3 "images/balls/red_solid_blank.png"
   :4 "images/balls/red_solid_blank.png"
   :5 "images/balls/red_solid_blank.png"
   :6 "images/balls/red_solid_blank.png"
   :7 "images/balls/red_solid_blank.png"
   :8 "images/balls/red_solid_blank.png"
   :9 "images/balls/blue_solid_blank.png"
   :10 "images/balls/blue_solid_blank.png"
   :11 "images/balls/blue_solid_blank.png"
   :12 "images/balls/blue_solid_blank.png"
   :13 "images/balls/blue_solid_blank.png"
   :14 "images/balls/blue_solid_blank.png"
   :15 "images/balls/blue_solid_blank.png"
   :cue "images/balls/white_cue.png"})

(def TABLE-IMAGES
  {:surface "images/table_surface.png"
   :raised "images/table_raised.png"
   :holder "images/ball_selected_holder.png"
   :cue "images/cue.png"})


(def BALLS
  (list
    (Ball. (Vector. 273 225) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :1 :solid (:1 BALL-IMAGES))
    (Ball. (Vector. 273 250) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :2 :solid (:2 BALL-IMAGES))
    (Ball. (Vector. 273 275) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :3 :solid (:3 BALL-IMAGES))
    (Ball. (Vector. 273 300) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :4 :solid (:4 BALL-IMAGES))
    (Ball. (Vector. 273 325) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :5 :solid (:5 BALL-IMAGES))
    (Ball. (Vector. 298 240) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :6 :solid (:6 BALL-IMAGES))
    (Ball. (Vector. 298 265) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :7 :solid (:7 BALL-IMAGES))
    (Ball. (Vector. 298 290) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :8 :solid (:8 BALL-IMAGES))
    (Ball. (Vector. 298 315) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :9 :striped (:9 BALL-IMAGES))
    (Ball. (Vector. 325 255) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :10 :striped (:10 BALL-IMAGES))
    (Ball. (Vector. 325 280) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :11 :striped (:11 BALL-IMAGES))
    (Ball. (Vector. 325 305) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :12 :striped (:12 BALL-IMAGES))
    (Ball. (Vector. 350 270) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :13 :striped (:13 BALL-IMAGES))
    (Ball. (Vector. 350 295) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :14 :striped (:14 BALL-IMAGES))
    (Ball. (Vector. 375 285) config/BALL-RADIUS-PX (Vector. 0 0) config/BALL-MASS-G :15 :striped (:15 BALL-IMAGES))
    (Ball. (Vector. 880 285) config/BALL-RADIUS-PX (Vector. -8 0) config/CUE-MASS-G :cue :cue (:cue BALL-IMAGES))))

(def LEFT-WALL
  (Wall.
    (list
      (Vector. 28 53)
      (Vector. 51 88)
      (Vector. 51 88)
      (Vector. 51 512)
      (Vector. 51 512)
      (Vector. 28 549))))

(def TOP-LEFT-WALL
  (Wall.
    (list
      (Vector. 53 28)
      (Vector. 78 50)
      (Vector. 78 50)
      (Vector. 567 50)
      (Vector. 567 50)
      (Vector. 580 28))))

(def TOP-RIGHT-WALL
  (Wall.
    (list
      (Vector. 622 28)
      (Vector. 633 50)
      (Vector. 633 50)
      (Vector. 1119 50)
      (Vector. 1119 50)
      (Vector. 1146 28))))

(def RIGHT-WALL
  (Wall.
    (list
      (Vector. 1172 52)
      (Vector. 1150 86)
      (Vector. 1150 86)
      (Vector. 1150 511)
      (Vector. 1150 511)
      (Vector. 1172 548))))

(def BOTTOM-RIGHT-WALL
  (Wall.
    (list
      (Vector. 1145 572)
      (Vector. 1122 550)
      (Vector. 1122 550)
      (Vector. 632 550)
      (Vector. 632 550)
      (Vector. 621 572))))

(def BOTTOM-LEFT-WALL
  (Wall.
    (list
      (Vector. 580 572)
      (Vector. 568 550)
      (Vector. 568 550)
      (Vector. 79 550)
      (Vector. 79 550)
      (Vector. 53 572))))

(def TABLE (Table. 20,
            (list (Vector. 32 32) (Vector. 600 32) (Vector. 1167 32)
                  (Vector. 1167 571) (Vector. 600 571) (Vector. 32 571))
            (list LEFT-WALL RIGHT-WALL
                  BOTTOM-LEFT-WALL BOTTOM-RIGHT-WALL
                  TOP-LEFT-WALL TOP-RIGHT-WALL)
            (:surface TABLE-IMAGES)
            (:raised TABLE-IMAGES)))

(def CONTROLLER (ControllerInterface. false (Vector. 0 0) 0 0 false (:cue TABLE-IMAGES) nil (Vector. 0 0)))

; (def EXAMPLE-STATE
;   (GameState.
;     (Player. :p1 :genetic (list) 0 0 :unassigned)
;     (Player. :p2 :genetic (list) 0 0 :unassigned)
;     :p1
;     :p2
;     BALLS
;     (list)
;     TABLE
;     CONTROLLER))

;_________________Analytics______________
(def std-analytics (list))
