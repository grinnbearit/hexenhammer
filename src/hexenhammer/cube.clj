(ns hexenhammer.cube)
;; inspired by https://www.redblobgames.com/grids/hexagons/


(defrecord Cube [q r s])


(defn add
  "adds two cubes together and returns a new cube"
  [cx cy]
  (->Cube (+ (.q cx) (.q cy))
          (+ (.r cx) (.r cy))
          (+ (.s cx) (.s cy))))


(defn subtract
  "subtracts cy from cx and returns a new cube"
  [cx cy]
  (->Cube (- (.q cx) (.q cy))
          (- (.r cx) (.r cy))
          (- (.s cx) (.s cy))))


(defn distance
  "returns the distance in hexes between cx and cy"
  [cx cy]
  (let [cube (subtract cx cy)]
    (max (abs (.q cube))
         (abs (.r cube))
         (abs (.s cube)))))


(defn step
  "return the adjacent cube in the direction given by facing"
  [position facing]
  (let [facing->offset {:n (->Cube 0 -1 1) :ne (->Cube 1 -1 0) :se (->Cube 1 0 -1)
                        :s (->Cube 0 1 -1) :sw (->Cube -1 1 0) :nw (->Cube -1 0 1)}]
    (add position (facing->offset facing))))
