(ns hexenhammer.transition.state.units
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.units :as tu]))


(defn move-unit
  "Moves the unit and updates the required fields"
  [{:keys [game/battlefield] :as state} unit-cube pointer]
  (let [unit-key (lbu/unit-key battlefield unit-cube)]
    (-> (update state :game/units tu/set-unit unit-key (:cube pointer))
        (update :game/battlefield lbu/move-unit unit-cube pointer))))


(defn destroy-unit
  "Destroys the unit at unit-cube"
  [{:keys [game/battlefield] :as state} unit-cube]
  (let [unit-key (lbu/unit-key battlefield unit-cube)]
    (-> (update state :game/units tu/remove-unit unit-key)
        (update :game/battlefield lbu/remove-unit unit-cube))))


(defn destroy-models
  "Destroys a number of models in the unit"
  [state unit-cube models]
  (update-in state [:game/battlefield unit-cube] leu/destroy-models models))
