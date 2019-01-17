(ns poolgp.simulation.analysis.game.rules
  (:require [poolgp.simulation.resources :as resources]
            [poolgp.simulation.analysis.game.table.physics :as physics])
  (:gen-class))

;TODO
;

;
; (defn do-game-state
;   "check game status and ball type assignments"
;   [state]
;   (if (not (empty? (:pocketed state)))
;     (cond
;       (= (:ball-type ((:current state) state)) :unassigned)
;         ;do ball type assignments (based on first ball in pocketed list)
;         (let [pocketed-type (:type (first (:pocketed state)))]
;           (assoc-in
;             (assoc-in state
;               [(:current state) :ball-type] pocketed-type)
;               [(:waiting state) :ball-type] (pocketed-type NOT-BALL-TYPE)))
;       :else state)
;     state))
;
; (defn do-turn-state
;   "check criteria for changing turns"
;   [state]
;   (if (balls-stopped? (:balls state))
;     ;change turns
;     (let [current (:current state)
;           waiting (:waiting state)]
;           ;TODO don't change if current pocketed balls on current turn
;           (assoc state :current waiting :waiting current))
;     state))

(defn pocketed?
  "determine if ball is in a pocket"
  [b table]
  (reduce #(if (> (:r table) (physics/distance (:center b) %2))
               (reduced true) %1)
          false (:pockets table)))

(defn move-ball-check-scoring
  "check any score updates"
  [gamestate]
  (let [current-balltype (:ball-type (:current gamestate))
        current-pocketed (:pocketed (:table-state gamestate))]
  ))

(defn move-pocketed
  "take gamestate, check for balls in
  pockets"
  [gamestate]
  (update-in gamestate [:table-state]
    (fn [ts]
      (reduce (fn [s b]
                (if (pocketed? b (:table ts))
                  (if (not (= (:id b) :cue))
                    ;regular ball pocketed
                    (update-in
                      (update-in s
                        [:pocketed] conj b)
                        [:balls] (fn [balls]
                                    (filter
                                        #(not (= (:id %) (:id b)))
                                        balls)))
                    ;move cue to break point
                    (update-in s [:balls]
                      (fn [balls] (map #(if (= (:id %) :cue)
                                          (assoc % :center resources/BREAK-PT) %)
                                        balls))))
                 s))
              ts (:balls ts)))))

(defn rules-update
  "check rules and gamestate"
  [gamestate]
  ;Check if any balls are in pockets
  ;Check if pocketed ball is type of current player (assign if unassigned)
  ; if balls stopped and no score during turn, set ready and change current
    gamestate
  )
