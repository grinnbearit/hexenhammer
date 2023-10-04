(ns hexenhammer.model.cube)
;; inspired and sometimes directly copied from https://www.redblobgames.com/grids/hexagons/


(defrecord Cube [q r s])
(defrecord Pointer [cube facing])


(defn add
  "adds two cubes together and returns a new cube"
  [cx cy]
  (->Cube (+ (.q cx) (.q cy))
          (+ (.r cx) (.r cy))
          (+ (.s cx) (.s cy))))


(defn step
  "return the adjacent cube in the direction given by facing"
  [cube facing]
  (let [facing->offset {:n (->Cube 0 -1 1) :ne (->Cube 1 -1 0) :se (->Cube 1 0 -1)
                        :s (->Cube 0 1 -1) :sw (->Cube -1 1 0) :nw (->Cube -1 0 1)}]
    (add cube (facing->offset facing))))


(defn forward-arc
  "Given a cube and a facing, returns the 3 cubes making up the forward arc"
  [cube facing]
  (let [facing->arc {:n [:nw :n :ne] :ne [:n :ne :se] :se [:ne :se :s]
                     :s [:se :s :sw] :sw [:s :sw :nw] :nw [:sw :nw :n]}]
    (map (partial step cube) (facing->arc facing))))


(defn neighbours
  "Returns the 6 adjacent cubes around the passed cube"
  [cube]
  (map (partial step cube) [:n :ne :se :s :sw :nw]))


(defn neighbours-within
  "Returns all cubes within `distance` of cube"
  [cube distance]
  (for [q (range (- distance) (inc distance))
        r (range (max (- distance) (- (+ q distance)))
                 (inc (min distance (- distance q))))
        :let [s (- (+ q r))]
        :when (not (and (zero? q) (zero? r) (zero? s)))]
    (add cube (->Cube q r s))))


(defn rotate
  "Rotates a cube around the origin in 60° increments"
  ([cube]
   (let [{:keys [q r s]} cube]
     (->Cube (- r) (- s) (- q))))
  ([cube n]
   (->> (iterate rotate cube)
        (drop n)
        (first))))


(defn forward-slice
  "Given a cube, a facing and a distance returns all the cubes in the forward arc at
  `distance` hexes away"
  [cube facing distance]
  (let [facing->turns (zipmap [:n :ne :se :s :sw :nw] (range 7))]
    (for [q (range (- distance) (inc distance))
          :let [r (if (neg? q) (- (+ distance q)) (- distance))
                s (- (+ q r))]]

      (->> (facing->turns facing)
           (rotate (->Cube q r s))
           (add cube)))))


(defn distance
  "Given two cubes, returns the number of hops on a hex grid to get from
  one to the other"
  [cx cy]
  (/ (+ (abs (- (:q cx) (:q cy)))
        (abs (- (:r cx) (:r cy)))
        (abs (- (:s cx) (:s cy))))
     2))


(defn round
  "Rounds a fractional cube to the closest valid whole cube"
  [cube]
  (let [q (Math/round (float (:q cube)))
        r (Math/round (float (:r cube)))
        s (Math/round (float (:s cube)))
        q-delta (abs (- q (:q cube)))
        r-delta (abs (- r (:r cube)))
        s-delta (abs (- s (:s cube)))]

    (cond (and (> q-delta r-delta)
               (> q-delta s-delta))
          (->Cube (- (+ r s)) r s)

          (> r-delta s-delta)
          (->Cube q (- (+ q s)) s)

          :else
          (->Cube q r (- (+ q r))))))


(defn cubes-between
  "Given two cubes, returns all cubes intersected by a line drawn
  connecting the centres of each"
  [cx cy]
  (let [cuts (inc (distance cx cy))
        delta (->Cube (/ (- (:q cy) (:q cx)) cuts)
                      (/ (- (:r cy) (:r cx)) cuts)
                      (/ (- (:s cy) (:s cx)) cuts))]

    (->> (iterate #(add delta %) cx)
         (drop 1)
         (take (dec cuts))
         (map round)
         (remove #{cx cy})
         (distinct))))


(defn direction
  "Given a source and a target cube, returns the facing that a line drawn
  from the centres of the two cubes would pass, oriented around the source cube"
  [cx cy]
  (let [closest-cube (or (first (cubes-between cx cy)) cy)]
    (->> (drop-while #(not= (step cx %) closest-cube) [:n :ne :se :s :sw :nw])
         (first))))


(defn pointer-step
  "Given a pointer, returns the next pointer moving in the same facing"
  [pointer]
  (->Pointer (step (:cube pointer) (:facing pointer))
             (:facing pointer)))
