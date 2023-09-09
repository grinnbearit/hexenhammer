(ns hexenhammer.model.unit)


(defn models
  [unit]
  (- (* (:unit/F unit)
        (:unit/ranks unit))
     (quot (:unit/damage unit)
           (:unit/W unit))))


(defn unit-strength
  [unit]
  (* (:unit/model-strength unit)
     (models unit)))
