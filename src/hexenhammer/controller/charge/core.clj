(ns hexenhammer.controller.charge.core
  (:require [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.charge :as lbmc]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.charge.reaction :as ccr]))


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


(defn reset-charge
  [{:keys [game/player game/battlefield game/units] :as state}]
  (let [player-cubes (tu/unit-cubes units player)
        charger-cubes (filter #(lbmc/charger? battlefield %) player-cubes)]
    (-> (assoc state :game/charge {:chargers (set charger-cubes)})
        (unselect))))


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


(defn declare-targets
  [{:keys [game/battlefield game/cube] :as state}]
  (let [target-cubes (get-in state [:game/charge :targets])
        target-keys (set (map #(lbu/unit-key battlefield %) target-cubes))]
    (-> (assoc state :game/charge {:target-cubes target-cubes
                                   :target-keys target-keys
                                   :charger cube})
        (assoc-in [:game/battlefield cube :unit/state :charge]
                  {:declared? true
                   :target-keys target-keys})
        (ccr/unselect))))


(defn skip-charge
  [{:keys [game/cube game/battlefield] :as state}]
  (let [unit-key (lbu/unit-key battlefield cube)]
    (-> (update-in state [:game/charge :chargers] disj cube)
        (unselect))))


(defn finish-reaction
  [state]
  (reset-charge state))
