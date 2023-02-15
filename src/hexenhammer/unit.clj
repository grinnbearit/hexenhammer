(ns hexenhammer.unit)


(defn gen-infantry
  [id & {:keys [facing] :or {facing :n}}]
  {:hexenhammer/class :unit
   :unit/name "infantry"
   :unit/id id
   :unit/models 12
   :unit/facing facing})
