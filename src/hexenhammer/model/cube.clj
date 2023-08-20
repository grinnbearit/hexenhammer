(ns hexenhammer.model.cube
  (:require [clojure.math.combinatorics :refer [permutations]]))
;; inspired by https://www.redblobgames.com/grids/hexagons/


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
  "Given a cube and a facing, teturns the 3 cubes making up the forward arc"
  [cube facing]
  (let [facing->arc {:n [:nw :n :ne] :ne [:n :ne :se] :se [:ne :se :s]
                     :s [:se :s :sw] :sw [:s :sw :nw] :nw [:sw :nw :n]}]
    (map (partial step cube) (facing->arc facing))))


(defn neighbours
  "Returns the 6 adjacent cubes around the passed cube"
  [cube]
  (map (partial step cube) [:n :ne :se :s :sw :nw]))


(defn neighbours-at
  "Returns all cubes at a distance of `distance` from cube"
  [cube distance]
  (let [q distance]
    (->> (for [r (map - (range q))
               :let [s (- (+ q r))]]
           [[q r s]
            (map - [q r s])])
         (apply concat)
         (mapcat permutations)
         (distinct)
         (map (partial apply ->Cube))
         (map (partial add cube)))))


(defn neighbours-within
  "Returns all cubes within `distance` of cube"
  [cube distance]
  (mapcat (partial neighbours-at cube) (range 1 (inc distance))))
