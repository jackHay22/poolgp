(ns poolgp.simulation.analysis.game.rules
  (:gen-class))

;TODO
;
; (defn pocketed?
;   "determine if ball is in a pocket"
;   [b table]
;   (reduce #(if (> (:r table) (distance (:center b) %2))
;                (reduced true) %1)
;           false (:pockets table)))
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
;
; (defn do-pockets
;   "if ball pocketed, remove from table"
;   [state]
;   (reduce (fn [s b]
;     (if (and (pocketed? b (:table s))
;              (not (= (:id b) :cue)))
;         (update-in
;           (update-in s
;             [:pocketed] conj b)
;             [:balls] (fn [b-list]
;                           (filter #(not (= (:id b) (:id %)))
;                           b-list)))
;         s))
;     state (:balls state)))

(defn rules-update
  [gamestate]
gamestate
  )
