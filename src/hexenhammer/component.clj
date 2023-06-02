(ns hexenhammer.component)


(defn gen-grass
  []
  {:hexenhammer/class :terrain
   :terrain/name "grass"})


(defn gen-infantry
  [player id & {:keys [facing] :or {facing :n}}]
  {:hexenhammer/class :unit
   :unit/player player
   :unit/name "infantry"
   :unit/id id
   :unit/files 4
   :unit/ranks 4
   :unit/facing facing})
