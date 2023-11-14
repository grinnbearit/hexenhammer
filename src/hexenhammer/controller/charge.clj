(ns hexenhammer.controller.charge
  (:require [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]))


(defn unselect
  [state]
  (if-let [charger-cubes (seq (get-in state [:game/charge :charger-cubes]))]
    (-> (assoc state
               :game/phase [:charge :select-hex])
        (dissoc :game/cube)
        (update :game/charge select-keys
                [:charger-keys :charger-cubes])
        (tsb/reset-battlemap charger-cubes)
        (update :game/battlemap tb/set-presentation :selectable))
    (-> (assoc state :game/phase [:charge :to-movement])
        (dissoc :game/cube :game/battlemap))))


(defn select-hex
  [state cube]
  (-> (assoc state
             :game/cube cube
             :game/phase [:charge :skip-charge])
      (tsb/reset-battlemap [cube])
      (update :game/battlemap tb/set-presentation :selected)))


(defn select-skip
  [state _]
  (unselect state))


(defn skip-charge
  [{:keys [game/cube game/battlefield] :as state}]
  (let [unit-key (lbu/unit-key battlefield cube)]
    (-> (update-in state [:game/charge :charger-cubes] disj cube)
        (update-in [:game/charge :charger-keys] disj unit-key)
        (unselect))))
