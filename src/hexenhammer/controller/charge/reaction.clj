(ns hexenhammer.controller.charge.reaction
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.flee :as lbmf]
            [hexenhammer.transition.dice :as td]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.units :as tsu]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.event :as ce]))


(defn unselect
  [{:keys [game/charge] :as state}]
  (let [charger (:charger charge)
        target-cubes (:target-cubes charge)]
    (if (empty? target-cubes)
      (-> (assoc state :game/phase [:charge :reaction :finish-reaction])
          (dissoc :game/cube :game/charge :game/battlemap))
      (-> (assoc state :game/phase [:charge :reaction :select-hex])
          (dissoc :game/cube)
          (tsb/reset-battlemap (conj target-cubes charger))
          (update :game/battlemap tb/set-presentation target-cubes :selectable)
          (update :game/battlemap tb/set-presentation [charger] :marked)))))


(defn set-hold
  [{:keys [game/charge] :as state} cube]
  (let [charger (:charger charge)]
    (-> (assoc state
               :game/cube cube
               :game/phase [:charge :reaction :hold])
        (tsb/reset-battlemap [cube charger])
        (update :game/battlemap tb/set-presentation [cube] :selected)
        (update :game/battlemap tb/set-presentation [charger] :marked))))


(defn set-flee
  [{:keys [game/battlefield game/charge] :as state} cube]
  (let [charger (:charger charge)
        {:keys [cube->tweeners events]} (lbmf/flee battlefield cube charger 12)]
    (-> (assoc state
               :game/cube cube
               :game/phase [:charge :reaction :flee]
               :game/battlemap cube->tweeners)
        (update :game/charge assoc
                :events events)
        (tsb/refresh-battlemap [charger])
        (update :game/battlemap tb/set-presentation [cube] :selected)
        (update :game/battlemap tb/set-presentation [charger] :marked))))


(defn set-fled
  [state cube]
  (-> (set-hold state cube)
      (assoc :game/phase [:charge :reaction :fled])))


(defn set-fleeing
  [state cube]
  (-> (set-flee state cube)
      (assoc :game/phase [:charge :reaction :fleeing])))


(defn select-hex
  [{:keys [game/battlefield] :as state} cube]
  (let [unit (battlefield cube)]
    (if (leu/fleeing? unit)
      (if (leu/fled? unit)
        (set-fled state cube)
        (set-fleeing state cube))
      (set-hold state cube))))


(defn select-hold
  [state _]
  (unselect state))


(defn select-flee
  [state _]
  (unselect state))


(defn hold
  [{:keys [game/battlefield game/cube] :as state}]
  (let [unit-key (lbu/unit-key battlefield cube)]
    (-> (update-in state [:game/charge :target-cubes] disj cube)
        (update-in [:game/charge :target-keys] disj unit-key)
        (unselect))))


(defn fled
  [state]
  (hold state))


(defn switch-reaction
  [{:keys [game/cube] :as state} reaction]
  (cond-> (unselect state)

    (= :hold reaction)
    (set-hold cube)

    (= :flee reaction)
    (set-flee cube)))


(defn flee
  [{:keys [game/battlefield game/cube game/charge] :as state}]
  (let [unit (battlefield cube)
        unit-key (leu/unit-key unit)
        charger (:charger charge)
        roll (td/roll! 2)
        {:keys [end cube->tweeners edge? events]} (lbmf/flee battlefield
                                                             cube
                                                             charger
                                                             (apply + roll))]
    (-> (if edge?
          (tsu/escape-unit state cube (:cube end))
          (-> (update-in state [:game/battlefield cube] leu/set-flee)
              (tsu/move-unit cube end)))
        (assoc :game/phase [:charge :reaction :flee :roll])
        (update-in [:game/charge :target-keys] disj unit-key)
        (update-in [:game/charge :target-cubes] disj cube)
        (update :game/events into events)
        (update :game/charge assoc
                :edge? edge?
                :unit unit
                :roll roll)
        (tsb/reset-battlemap [charger])
        (update :game/battlemap merge cube->tweeners)
        (update :game/battlemap tb/set-presentation [charger (:cube end)] :marked))))


(defn fleeing
  [state]
  (flee state))


(defn reset-reaction
  [{:keys [game/units game/charge] :as state}]
  (letfn [(reducer [target-acc unit-key]
            (if-let [unit-cube (tu/get-unit units unit-key)]
              (-> (update target-acc :target-keys (fnil conj #{}) unit-key)
                  (update :target-cubes (fnil conj #{}) unit-cube))
              target-acc))]

    (let [target-keys (:target-keys charge)
          new-charge (reduce reducer
                             (select-keys charge [:charger])
                             target-keys)]

      (-> (assoc state :game/charge new-charge)
          (unselect)))))


(defn trigger
  [state]
  (ce/trigger state reset-reaction))
