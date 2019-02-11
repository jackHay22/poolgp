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

(def BREAK-PT (Vector. 883 303))

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

(def EMPTY-CONFIG-STATE
  {:simulation {
    :analysis []
    :p1 {:genetic true :strategy "()"}
    :p2 {:genetic true :strategy "()"}
    :eval-worker {
      :indiv-ingress-p 9999
      :indiv-egress-p 8000
      :opp-pool-req-p 8888
      :engine-hostname "eval"}}})

(def CONTROLLER (ControllerInterface. false false (:cue TABLE-IMAGES)))
