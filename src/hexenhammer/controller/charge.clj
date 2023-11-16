(ns hexenhammer.controller.charge
  (:require [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.charge :as lbmc]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]))


(defn unselect
  [state]
  (if-let [chargers (seq (get-in state [:game/charge :chargers]))]
    (-> (assoc state
               :game/phase [:charge :select-hex])
        (dissoc :game/cube)
        (update :game/charge select-keys [:chargers])
        (tsb/reset-battlemap chargers)
        (update :game/battlemap tb/set-presentation :selectable))
    (-> (assoc state :game/phase [:charge :to-movement])
        (dissoc :game/cube :game/battlemap))))


(defn select-hex
  [{:keys [game/battlefield] :as state} cube]
  (let [{:keys [cube->enders pointer->cube->tweeners pointer->events
                pointer->targets pointer->range]}
        (lbmc/charge battlefield cube)]
    (-> (assoc state
               :game/cube cube
               :game/phase [:charge :pick-targets]
               :game/battlemap cube->enders)
        (update :game/charge assoc
                :cube->enders cube->enders
                :pointer->cube->tweeners pointer->cube->tweeners
                :pointer->events pointer->events
                :pointer->targets pointer->targets
                :pointer->range pointer->range)
        (tsb/refresh-battlemap [cube])
        (update :game/battlemap tb/set-presentation [cube] :selected))))


(defn select-pick-targets
  [state _]
  (unselect state))


(defn move-pick-targets
  [{:keys [game/charge] :as state} pointer]
  (let [cube->enders (:cube->enders charge)
        cube->tweeners (get-in charge [:pointer->cube->tweeners pointer])
        events (get-in charge [:pointer->events pointer])
        targets (get-in charge [:pointer->targets pointer])
        charge-range (get-in charge [:pointer->range pointer])]
    (-> (assoc state
               :game/pointer pointer
               :game/phase [:charge :declare-targets]
               :game/battlemap (merge cube->enders cube->tweeners))
        (update :game/charge assoc
                :events events
                :targets targets
                :charge-range charge-range)
        (update-in [:game/battlemap (:cube pointer)] assoc
                   :entity/presentation :selected
                   :mover/presentation :present
                   :mover/selected (:facing pointer))
        (tsb/refresh-battlemap targets)
        (update :game/battlemap tb/set-presentation targets :marked))))


(defn select-declare-targets
  [state _]
  (unselect state))


(defn move-declare-targets
  [state pointer]
  (move-pick-targets state pointer))


(defn skip-charge
  [{:keys [game/cube game/battlefield] :as state}]
  (let [unit-key (lbu/unit-key battlefield cube)]
    (-> (update-in state [:game/charge :chargers] disj cube)
        (unselect))))
