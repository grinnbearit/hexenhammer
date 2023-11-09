(ns hexenhammer.logic.entity.unit
  (:require [hexenhammer.logic.cube :as lc]))


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


(defn unit?
  [entity]
  (= :unit (:entity/class entity)))


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


(defn set-models
  "Returns a new unit with the passed models `m` set, doesn't check for bounds
  removes all pre-existing damage for multiwound units"
  [unit m]
  (let [full-ranks (quot m (:unit/F unit))
        extra-models (rem m (:unit/F unit))
        rank-wounds (* (:unit/F unit) (:unit/W unit))
        extra-wounds (* extra-models (:unit/W unit))]
    (if (zero? extra-models)
      (assoc unit
             :unit/ranks full-ranks
             :unit/damage 0)
      (assoc unit
             :unit/ranks (inc full-ranks)
             :unit/damage (- rank-wounds extra-wounds)))))


(defn destroy-models
  "Given a unit and a number of models `m`, returns the new unit with the
  destroyed models removed, assumes the unit has sufficient models"
  [unit m]
  (set-models unit (- (models unit) m)))


(defn unit-strength
  [unit]
  (* (:unit/model-strength unit)
     (models unit)))


(defn enemies?
  "Returns true if the two units have different owners"
  [unit-1 unit-2]
  (not= (:unit/player unit-1)
        (:unit/player unit-2)))


(defn fleeing?
  "Returns true if the unit is fleeing"
  [unit]
  (boolean (get-in unit [:unit/flags :fleeing?])))


(defn panicked?
  "Returns true if the has taken a panic test this phase"
  [unit]
  (boolean (get-in unit [:unit/state :phase :panicked?])))


(defn reset-phase
  "Resets the phase state"
  [unit]
  (assoc-in unit [:unit/state :phase] {:strength (unit-strength unit)}))


(defn phase-strength
  "Returns the strength this unit started the phase with"
  [unit]
  (get-in unit [:unit/state :phase :strength]))


(defn set-panicked
  "Updates the unit at to have attempted a panic test"
  [unit]
  (assoc-in unit [:unit/state :phase :panicked?] true))


(defn set-flee
  "Updates the unit to be fleeing"
  [unit]
  (assoc-in unit [:unit/flags :fleeing?] true))
