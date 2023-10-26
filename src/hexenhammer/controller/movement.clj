(ns hexenhammer.controller.movement
  (:require [hexenhammer.transition.core :as t]
            [hexenhammer.transition.battlemap :as tb]))


(defn unselect
  [state]
  (let [movable-cubes (get-in state [:game/movement :movable-cubes])]
    (-> (assoc state
               :game/phase [:movement :select-hex])
        (dissoc :game/cube)
        (t/reset-battlemap movable-cubes)
        (update :game/battlemap tb/set-presentation :selectable))))


(defn select-reform
  [state cube]
  (if (= cube (:game/cube state))
    (unselect state)
    (-> (assoc state :game/cube cube)
        (t/reset-battlemap [cube])
        (update :game/battlemap tb/set-presentation :selected))))


(defn select-hex
  [state cube]
  (-> (assoc state :game/phase [:movement :reform])
      (select-reform cube)))
