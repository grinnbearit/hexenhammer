(ns hexenhammer.logic.cube)
;; inspired and sometimes directly copied from https://www.redblobgames.com/grids/hexagons/


(defrecord Cube [q r s])


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


(defn neighbours
  "Returns the 6 adjacent cubes around the passed cube"
  [cube]
  (map (partial step cube) [:n :ne :se :s :sw :nw]))


(defn forward-arc
  "Given a cube and a facing, returns the 3 cubes making up the forward arc"
  [cube facing]
  (let [facing->arc {:n [:nw :n :ne] :ne [:n :ne :se] :se [:ne :se :s]
                     :s [:se :s :sw] :sw [:s :sw :nw] :nw [:sw :nw :n]}]
    (map (partial step cube) (facing->arc facing))))
