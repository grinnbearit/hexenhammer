(ns hexenhammer.logic.unit
  (:require [hexenhammer.model.unit :as mu]))


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
