(ns hexenhammer.model.unit)


(defn models
  "Calculates the number of models (wounded or not) in the unit"
  [unit]
  (- (* (:unit/F unit)
        (:unit/ranks unit))
     (quot (:unit/damage unit)
           (:unit/W unit))))


(defn wounds
  "Calculate the number of wounds remaining on this unit"
  [unit]
  (- (* (:unit/F unit)
        (:unit/ranks unit)
        (:unit/W unit))
     (:unit/damage unit)))


(defn set-wounds
  "Returns a new unit with the passed wounds set, doesn't check for bounds"
  [unit wounds]
  (let [rank-wounds (* (:unit/F unit) (:unit/W unit))
        full-ranks (quot wounds rank-wounds)
        extra-wounds (rem wounds rank-wounds)]
    (if (zero? extra-wounds)
      (assoc unit
             :unit/ranks full-ranks
             :unit/damage 0)
      (assoc unit
             :unit/ranks (inc full-ranks)
             :unit/damage (- rank-wounds extra-wounds)))))


(defn set-models
  "Returns a new unit with the passed models set, doesn't check for bounds
  removes all pre-existing damage for multiwound units"
  [unit models]
  (let [full-ranks (quot models (:unit/F unit))
        extra-models (rem models (:unit/F unit))
        rank-wounds (* (:unit/F unit) (:unit/W unit))
        extra-wounds (* extra-models (:unit/W unit))]
    (if (zero? extra-models)
      (assoc unit
             :unit/ranks full-ranks
             :unit/damage 0)
      (assoc unit
             :unit/ranks (inc full-ranks)
             :unit/damage (- rank-wounds extra-wounds)))))


(defn unit-strength
  [unit]
  (* (:unit/model-strength unit)
     (models unit)))


(defn unit-key
  "Returns a unique identifier for the passed unit"
  [unit]
  (select-keys unit [:unit/player :entity/name :unit/id]))
