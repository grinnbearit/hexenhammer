(ns hexenhammer.controller.charge.reaction
  (:require [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]))


(defn unselect
  [state]
  (let [targets (get-in state [:game/charge :targets])]
    (-> (assoc state :game/phase [:charge :reaction :select-hex])
        (dissoc :game/cube)
        (tsb/reset-battlemap targets)
        (update :game/battlemap tb/set-presentation :selectable))))


(defn select-hex
  [state cube]
  (-> (assoc state
             :game/cube cube
             :game/phase [:charge :reaction :react])
      (tsb/reset-battlemap [cube])
      (update :game/battlemap tb/set-presentation :selected)))


(defn select-react
  [state _]
  (unselect state))
