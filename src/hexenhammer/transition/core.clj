(ns hexenhammer.transition.core
  (:require [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.core :as lb]))


(defn gen-initial-state
  "Returns the initial hexenhammer state given a list of rows and columns"
  [rows columns]
  {:game/setup {:rows rows
                :columns columns}
   :game/units {1 {} 2 {}}
   :game/battlefield (-> (lb/gen-battlefield-cubes rows columns)
                         (zipmap (repeat let/OPEN-GROUND)))
   :game/events (clojure.lang.PersistentQueue/EMPTY)})
