(ns hexenhammer.unit)


(defn gen-warrior
  [id & {:keys [facing] :or {facing :n}}]
  {:hexenhammer/class :unit
   :unit/name "warrior"
   :unit/id id
   :unit/models 12
   :unit/facing facing})
