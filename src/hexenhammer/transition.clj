(ns hexenhammer.transition
  (:require [hexenhammer.cube :as cube]))


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
  {:map/rows rows
   :map/columns columns
   :map/battlefield (zipmap (gen-battlefield-cubes rows columns)
                            (repeat {:hexenhammer/class :terrain
                                     :terrain/name :grass}))})


(defn !init-state
  [state rows columns]
  (reset! state (gen-initial-state rows columns)))


(defn place-unit
  [state cube unit]
  (assoc-in state [:map/battlefield cube] unit))


(defn !place-unit
  [state cube unit]
  (swap! state place-unit cube unit))
