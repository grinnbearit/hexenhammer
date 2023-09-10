(ns hexenhammer.model.event)


(defn dangerous
  [player id]
  {:event/class :dangerous
   :unit/player player
   :unit/id id})
