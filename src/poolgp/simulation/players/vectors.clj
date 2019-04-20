(ns poolgp.simulation.players.vectors
  (:gen-class))

(defn- ** [x] (* x x))

(defn- r2? [v] (>= (count v) 2))

(defn- safe-div
  "safe division"
  [a b]
  (if (= 0 b)
    a (/ a b)))

(defn normal
  "normalize vector and optionally scale"
  ([v]
  (if (r2? v)
      (let
        [mag (Math/sqrt (+ (** (first v))
                           (** (second v))))]
        [(safe-div (first v) mag) (safe-div (second v) mag)])
      v))
  ([v scale]
    (update
      (update (normal v) 0 * scale)
                         1 * scale)))

(defn sub
  "subtraction on two vectors"
  [v1 v2]
  (if (and (r2? v1)
           (r2? v2))
      [(- (first v1) (first v2))
       (- (second v1) (second v2))]
      v1))

(defn add
  "addition of two vectors"
  [v1 v2]
  (if (and (r2? v1)
           (r2? v2))
    [(+ (first v1) (first v2))
     (+ (second v1) (second v2))]
    v1))

(defn scale
  "scale vector"
  [v s]
  (if (r2? v)
    [(* (first v) s) (* (second v) s)]
    v))

(defn dot
  "dot product of two vectors"
  [v1 v2]
  (if (and (r2? v1)
           (r2? v2))
      (+ (* (first v1) (first v2))
         (* (second v1) (second v2)))
      1))

(defn len
  "length of vector"
  [v]
  (if (r2? v)
      (Math/sqrt (+ (** (first v))
                    (** (second v))))
      1))

(defn proj
  "projection on two vectors"
  [v1 v2]
  (scale v1
    (safe-div (dot v1 v2) (** (len v1)))))
