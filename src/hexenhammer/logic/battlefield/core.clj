(ns hexenhammer.logic.battlefield.core
  (:require [hexenhammer.logic.cube :as lc]))


(defn gen-battlefield-cubes
  "Returns a list of cube coordinates for a battlefield of size rows x columns"
  [rows columns]
  (let [hop-right (partial lc/add (lc/->Cube 2 -1 -1))
        hop-down (partial lc/add (lc/->Cube 0 1 -1))]
    (->> (interleave (iterate hop-right (lc/->Cube 0 0 0))
                     (iterate hop-right (lc/->Cube 1 0 -1)))
         (take columns)
         (iterate #(map hop-down %))
         (take rows)
         (apply concat))))


(defn visible?
  "Returns true if every cube between cx and cy has a los value
  less than either of their los"
  [battlefield cx cy]
  (let [max-los (max (:entity/los (battlefield cx))
                     (:entity/los (battlefield cy)))]
    (->> (lc/cubes-between cx cy)
         (map (comp :entity/los battlefield))
         (remove #(< % max-los))
         (empty?))))
