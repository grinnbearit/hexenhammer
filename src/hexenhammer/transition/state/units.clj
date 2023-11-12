(ns hexenhammer.transition.state.units
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
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
  [{:keys [game/battlefield] :as state} unit-cube source-cube models]
  (let [unit (battlefield unit-cube)
        damaged-unit (leu/destroy-models unit models)
        damaged-bf (assoc battlefield unit-cube damaged-unit)]

    (cond-> (assoc state :game/battlefield damaged-bf)

      (lbu/heavy-casualties? damaged-bf unit-cube)
      (update :game/events conj
              (lev/heavy-casualties source-cube (leu/unit-key unit))))))


(defn damage-unit
  "Removes a number of wounds from the unit"
  [{:keys [game/battlefield] :as state} unit-cube source-cube damage]
  (let [unit (battlefield unit-cube)
        damaged-unit (leu/damage-unit unit damage)
        damaged-bf (assoc battlefield unit-cube damaged-unit)]

    (cond-> (assoc state :game/battlefield damaged-bf)

      (lbu/heavy-casualties? damaged-bf unit-cube)
      (update :game/events conj
              (lev/heavy-casualties source-cube (leu/unit-key unit))))))


(defn escape-unit
  "Destroys the unit at `unit-cube`, the unit escapes the battlfield at `end-cube`"
  [{:keys [game/battlefield] :as state} unit-cube end-cube]
  (let [unit-key (lbu/unit-key battlefield unit-cube)]
    (-> (update state :game/units tu/remove-unit unit-key)
        (update :game/battlefield lbu/remove-unit unit-cube))))
