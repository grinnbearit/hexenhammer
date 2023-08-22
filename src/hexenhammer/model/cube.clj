(ns hexenhammer.model.cube
  (:require [clojure.math.combinatorics :refer [permutations]]))
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


(defn forward-cone
  "Given a cube, a facing and a distance returns all the cubes in the forward cone
  up to `distance` hexes away"
  [cube facing distance]
  (let [facing->turns (zipmap [:n :ne :se :s :sw :nw] (range 7))]
    (for [d (range 1 (inc distance))
          q (range (- d) (inc d))
          :let [r (if (neg? q) (- (+ d q)) (- d))
                s (- (+ q r))]]

      (->> (facing->turns facing)
           (rotate (->Cube q r s))
           (add cube)))))
