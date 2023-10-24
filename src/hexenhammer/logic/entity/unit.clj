(ns hexenhammer.logic.entity.unit)


(defn gen-infantry
  "Returns a generic infantry entity"
  [player id facing M Ld R]
  {:entity/class :unit
   :entity/presentation :default
   :entity/los 1

   :unit/player player
   :unit/name "infantry"
   :unit/id id
   :unit/facing facing
   :unit/M M
   :unit/Ld Ld
   :unit/W 1
   :unit/F 4
   :unit/R R
   :unit/model-strength 1
   :unit/ranks R
   :unit/damage 0})


(defn unit-key
  "Returns a unique identifier for the passed unit"
  [unit]
  (select-keys unit [:unit/player :unit/name :unit/id]))


(defn models
  "Calculates the number of models (wounded or not) in the unit"
  [unit]
  (- (* (:unit/F unit)
        (:unit/ranks unit))
     (quot (:unit/damage unit)
           (:unit/W unit))))


(defn unit-strength
  [unit]
  (* (:unit/model-strength unit)
     (models unit)))