(ns hexenhammer.controller.charge.reaction
  (:require [hexenhammer.logic.battlefield.movement.flee :as lbmf]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]))


(defn unselect
  [{:keys [game/charge] :as state}]
  (let [charger (:charger charge)
        targets (:targets charge)]
    (if (empty? targets)
      (-> (assoc state :game/phase [:charge :reaction :finish-reaction])
          (dissoc :game/cube :game/charge :game/battlemap))
      (-> (assoc state :game/phase [:charge :reaction :select-hex])
          (dissoc :game/cube)
          (tsb/reset-battlemap (conj targets charger))
          (update :game/battlemap tb/set-presentation targets :selectable)
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


(defn select-hex
  [state cube]
  (set-hold state cube))


(defn select-hold
  [state _]
  (unselect state))


(defn select-flee
  [state _]
  (unselect state))


(defn hold
  [{:keys [game/cube] :as state}]
  (-> (update-in state [:game/charge :targets] disj cube)
      (unselect)))


(defn switch-reaction
  [{:keys [game/cube] :as state} reaction]
  (cond-> (unselect state)

    (= :hold reaction)
    (set-hold cube)

    (= :flee reaction)
    (set-flee cube)))
