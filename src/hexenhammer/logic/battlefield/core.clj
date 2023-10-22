(ns hexenhammer.logic.battlefield.core
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.terrain :as let]))


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


(defn gen-initial-state
  "Returns the initial hexenhammer state given a list of rows and columns"
  [rows columns]
  {:game/setup {:rows rows
                :columns columns}
   :game/battlefield (-> (gen-battlefield-cubes rows columns)
                         (zipmap (repeat (let/gen-open-ground))))
   :game/events (clojure.lang.PersistentQueue/EMPTY)})
