(ns poolgp.simulation.demo.interactionutils
  (:gen-class))

;Various state transforms to GameState on player interaction

;ControllerInterface
; {
;   :mouse-entered? true/false
;   :mouse (Vector.)
;   :force int
;   :release? true/false
;   :cue path -> img
; }

(defn clicked [e state] state)
(defn entered [e state] state)
(defn exited [e state] state)
