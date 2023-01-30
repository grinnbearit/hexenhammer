(ns hexenhammer.cube)
;; inspired by https://www.redblobgames.com/grids/hexagons/


(defrecord Cube [q r s])


(defn cube-add
  [cx cy]
  (->Cube (+ (.q cx) (.q cy))
          (+ (.r cx) (.r cy))
          (+ (.s cx) (.s cy))))
