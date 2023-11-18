(ns hexenhammer.controller.charge.reaction
  (:require [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]))


(defn unselect
  [state]
  (let [targets (get-in state [:game/charge :targets])]
    (if (empty? targets)
      (-> (assoc state :game/phase [:charge :reaction :finish-reaction])
          (dissoc :game/cube :game/charge :game/battlemap))
      (-> (assoc state :game/phase [:charge :reaction :select-hex])
          (dissoc :game/cube)
          (tsb/reset-battlemap targets)
          (update :game/battlemap tb/set-presentation :selectable)))))


(defn set-hold
  [state cube]
  (-> (assoc state
             :game/cube cube
             :game/phase [:charge :reaction :hold])
      (tsb/reset-battlemap [cube])
      (update :game/battlemap tb/set-presentation :selected)))


(defn set-flee
  [state cube]
  (-> (assoc state
             :game/cube cube
             :game/phase [:charge :reaction :flee])
      (tsb/reset-battlemap [cube])
      (update :game/battlemap tb/set-presentation :selected)))


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
