(ns hexenhammer.model.event)


(defn dangerous
  [player name id]
  {:event/class :dangerous
   :unit/player player
   :entity/name name
   :unit/id id})


(defn panic
  [player name id]
  {:event/class :panic
   :unit/player player
   :entity/name name
   :unit/id id})
