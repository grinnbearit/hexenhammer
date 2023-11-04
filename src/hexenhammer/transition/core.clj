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


(defn reset-battlemap
  "Sets the battlemap to the current battlefield (or a subset of it)"
  ([state]
   (assoc state :game/battlemap (:game/battlefield state)))
  ([state cubes]
   (->> (select-keys (:game/battlefield state) cubes)
        (assoc state :game/battlemap))))


(defn refresh-battlemap
  "Updates the battlemap with cubes pulled from the battlefield"
  [state cubes]
  (->> (select-keys (:game/battlefield state) cubes)
       (update state :game/battlemap merge)))
