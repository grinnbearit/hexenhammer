(ns hexenhammer.logic.unit
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.logic.entity :as le]))


(defn enemies?
  "Returns true if the two units have different owners"
  [unit-1 unit-2]
  (not= (:unit/player unit-1)
        (:unit/player unit-2)))


(defn engaged?
  "Returns true if the two units are engaged to each other"
  [unit-1 unit-2]
  (and (enemies? unit-1 unit-2)
       (or (contains? (set (mc/forward-arc (:entity/cube unit-1) (:unit/facing unit-1)))
                      (:entity/cube unit-2))
           (contains? (set (mc/forward-arc (:entity/cube unit-2) (:unit/facing unit-2)))
                      (:entity/cube unit-1)))))


(defn engaged-cubes
  "Returns a list of cubes engaged to the passed cube
  assumes the cube is on the battlefield and a unit"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (for [neighbour (mc/neighbours cube)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (le/unit? entity)
                     (engaged? unit entity))]
      neighbour)))


(defn battlefield-engaged?
  "Returns true if the passed cube is currently engaged on the battlefield
  assumes the cube is on the battlefield and a unit"
  [battlefield cube]
  (not (empty? (engaged-cubes battlefield cube))))


(defn destroyed?
  "Given a unit and damage, returns true if the unit would be destroyed"
  [unit damage]
  (<= (mu/wounds unit) damage))


(defn damage-unit
  "Given a unit and damage, returns the new unit with the damage taken
  assumes the damage isn't enought to destroy the unit"
  [unit damage]
  (mu/set-wounds unit (- (mu/wounds unit) damage)))


(defn destroy-models
  "Given a unit and a number of models, returns the new unit with the
  destroyed models removed, assumes the unit has sufficient models"
  [unit models]
  (mu/set-models unit (- (mu/models unit) models)))


(defn phase-reset
  "Resets different statuses that happen on a phase transition"
  [battlefield unit-cubes]
  (letfn [(reducer [battlefield cube]
            (let [unit (battlefield cube)]
              (update battlefield cube assoc
                      :unit/phase-strength (mu/unit-strength unit))))]

    (reduce reducer battlefield unit-cubes)))
