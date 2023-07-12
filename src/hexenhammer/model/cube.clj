(ns hexenhammer.model.cube)
;; inspired by https://www.redblobgames.com/grids/hexagons/


(defrecord Cube [q r s])


(defn add
  "adds two cubes together and returns a new cube"
  [cx cy]
  (->Cube (+ (.q cx) (.q cy))
          (+ (.r cx) (.r cy))
          (+ (.s cx) (.s cy))))