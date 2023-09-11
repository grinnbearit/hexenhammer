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
