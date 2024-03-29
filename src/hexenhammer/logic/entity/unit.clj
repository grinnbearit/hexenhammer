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


(defn damage-unit
  "Given a unit and damage, returns the new unit with the damage taken
  assumes the damage isn't enought to destroy the unit"
  [unit damage]
  (set-wounds unit (- (wounds unit) damage)))


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
  (get-in unit [:unit/state :game :fleeing?] false))


(defn panicked?
  "Returns true if the has taken a panic test this phase"
  [unit]
  (get-in unit [:unit/state :phase :panicked?] false))


(defn reset-phase
  "Resets the phase state"
  [unit]
  (assoc-in unit [:unit/state :phase] {:strength (unit-strength unit)}))


(defn phase-strength
  "Returns the strength this unit started the phase with"
  [unit]
  (get-in unit [:unit/state :phase :strength]))


(defn set-panicked
  "Updates the unit to have attempted a panic test"
  [unit]
  (assoc-in unit [:unit/state :phase :panicked?] true))


(defn set-flee
  "Updates the unit to be fleeing"
  [unit]
  (-> (assoc-in unit [:unit/state :game :fleeing?] true)
      (assoc-in [:unit/state :phase :fled?] true)))


(defn fled?
  "Returns true if the unit has already fled in this phase"
  [unit]
  (get-in unit [:unit/state :phase :fled?] false))


(defn friendly?
  "True if the unit is owned by the passed player"
  [unit player]
  (= (:unit/player unit) player))


(defn reset-movement
  [unit]
  (update unit :unit/state dissoc :movement))


(defn set-declared
  "Updates to unit to have declared a charge"
  [unit]
  (assoc-in unit [:unit/state :charge :declared?] true))


(defn declared?
  "Returns true if the unit has declared a charge in the charge phase"
  [unit]
  (get-in unit [:unit/state :charge :declared?] false))


(defn set-marched
  "Updates the unit to have marched"
  [unit]
  (assoc-in unit [:unit/state :turn :marched?] true))


(defn marched?
  "Return true if the unit marched this turn"
  [unit]
  (get-in unit [:unit/state :turn :marched?] false))
