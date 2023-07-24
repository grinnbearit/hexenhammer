(ns hexenhammer.model.core
  (:require [hexenhammer.model.cube :as cube]
            [hexenhammer.model.entity :as entity]))


(defn gen-battlefield-cubes
  "Returns a list of cube coordinates for a battlefield of size rows x columns"
  [rows columns]
  (let [hop-right (partial cube/add (cube/->Cube 2 -1 -1))
        hop-down (partial cube/add (cube/->Cube 0 1 -1))]
    (->> (interleave (iterate hop-right (cube/->Cube 0 0 0))
                     (iterate hop-right (cube/->Cube 1 0 -1)))
         (take columns)
         (iterate #(map hop-down %))
         (take rows)
         (apply concat))))


(defn gen-initial-state
  "Returns the initial hexenhammer state given a list of rows and columns"
  [rows columns]
  {:game/phase :setup
   :game/subphase :select-hex
   :game/player 1
   :game/rows rows
   :game/columns columns
   :game/units {1 {:counter 0 :cubes {}}
                2 {:counter 0 :cubes {}}}
   :game/battlefield (->> (for [cube (gen-battlefield-cubes rows columns)]
                            [cube (entity/gen-terrain cube :interaction :selectable)])
                          (into {}))})
