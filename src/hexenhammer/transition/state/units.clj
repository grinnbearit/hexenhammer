(ns hexenhammer.transition.state.units
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.event :as lbv]
            [hexenhammer.transition.units :as tu]))


(defn move-unit
  "Moves the unit and updates the required fields"
  [{:keys [game/battlefield] :as state} unit-cube pointer]
  (let [unit-key (lbu/unit-key battlefield unit-cube)]
    (-> (update state :game/units tu/set-unit unit-key (:cube pointer))
        (update :game/battlefield lbu/move-unit unit-cube pointer))))


(defn remove-unit
  "A helper function for destroy-unit and escape unit"
  [{:keys [game/player game/battlefield] :as state} unit-cube source-cube]
  (let [unit (battlefield unit-cube)
        unit-key (leu/unit-key unit)
        removed-bf (lbu/remove-unit battlefield unit-cube)]

    (cond-> (-> (assoc state :game/battlefield removed-bf)
                (update :game/units tu/remove-unit unit-key))

      (<= 8 (leu/unit-strength unit))
      (update :game/events into
              (lbv/nearby-friend-annihilated removed-bf source-cube player)))))


(defn reduce-unit
  "A helper function for destroy-models and damage-unit"
  [{:keys [game/battlefield] :as state} unit-cube source-cube reducer-fn]
  (let [unit (battlefield unit-cube)
        reduced-bf (update battlefield unit-cube reducer-fn)]

    (cond-> (assoc state :game/battlefield reduced-bf)

      (lbu/heavy-casualties? reduced-bf unit-cube)
      (update :game/events conj
              (lev/heavy-casualties source-cube (leu/unit-key unit))))))


(defn destroy-unit
  "Destroys the unit at unit-cube"
  [state unit-cube]
  (remove-unit state unit-cube unit-cube))


(defn escape-unit
  "Destroys the unit at `unit-cube`, the unit escapes the battlfield at `end-cube`"
  [state unit-cube end-cube]
  (remove-unit state unit-cube end-cube))


(defn destroy-models
  "Destroys a number of models in the unit"
  [state unit-cube source-cube models]
  (reduce-unit state unit-cube source-cube #(leu/destroy-models % models)))


(defn damage-unit
  "Removes a number of wounds from the unit"
  [state unit-cube source-cube damage]
  (reduce-unit state unit-cube source-cube #(leu/damage-unit % damage)))
