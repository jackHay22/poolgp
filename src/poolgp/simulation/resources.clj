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
  {:solid "images/balls/red_solid_blank.png"
   :striped "images/balls/blue_solid_blank.png"
   :cue "images/balls/white_cue.png"
   :unassigned "images/balls/unassigned.png"})

(def TABLE-IMAGES
  {:surface "images/table_surface.png"
   :raised "images/table_raised.png"
   :holder "images/ball_selected_holder.png"
   :cue "images/cue.png"})

(def LEFT-WALL
  (Wall.
    (list
      (Vector. 28 50)
      (Vector. 47 76)
      (Vector. 47 76)
      (Vector. 47 524)
      (Vector. 47 524)
      (Vector. 28 546))))

(def TOP-LEFT-WALL
  (Wall.
    (list
      (Vector. 53 28)
      (Vector. 71 48)
      (Vector. 71 48)
      (Vector. 572 48)
      (Vector. 572 48)
      (Vector. 580 28))))

(def TOP-RIGHT-WALL
  (Wall.
    (list
      (Vector. 620 28)
      (Vector. 628 48)
      (Vector. 628 48)
      (Vector. 1130 48)
      (Vector. 1130 48)
      (Vector. 1146 28))))

(def RIGHT-WALL
  (Wall.
    (list
      (Vector. 1172 52)
      (Vector. 1154 76)
      (Vector. 1154 76)
      (Vector. 1154 524)
      (Vector. 1154 524)
      (Vector. 1172 548))))

(def BOTTOM-RIGHT-WALL
  (Wall.
    (list
      (Vector. 1146 572)
      (Vector. 1126 552)
      (Vector. 1126 552)
      (Vector. 628 552)
      (Vector. 628 552)
      (Vector. 620 572))))

(def BOTTOM-LEFT-WALL
  (Wall.
    (list
      (Vector. 580 572)
      (Vector. 572 552)
      (Vector. 572 552)
      (Vector. 71 552)
      (Vector. 71 552)
      (Vector. 52 572))))

(def TABLE (Table. 20,
            (list (Vector. 52.0 52.0) (Vector. 600.0 52.0) (Vector. 1147.0 52.0)
                  (Vector. 1147.0 546.0) (Vector. 600.0 546.0) (Vector. 52.0 546.0))
            (list LEFT-WALL RIGHT-WALL
                  BOTTOM-LEFT-WALL BOTTOM-RIGHT-WALL
                  TOP-LEFT-WALL TOP-RIGHT-WALL)
            (:surface TABLE-IMAGES)
            (:raised TABLE-IMAGES)))

(def EMPTY-CONFIG-STATE
  {:simulation {
    :analysis []
    :p1 {:genetic true :strategy "()"}
    :p2 {:genetic true :strategy "()"}}
    :eval-worker {
      :indiv-ingress-p 9999
      :indiv-egress-p 8000
      :opp-pool-req-p 8888
      :engine-hostname "engine"}})

(def CONTROLLER (ControllerInterface. false false (:cue TABLE-IMAGES)))
