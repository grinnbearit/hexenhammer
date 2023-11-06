(ns hexenhammer.controller.core
  (:require [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.movement :as cm]))


(defn to-setup
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (tsb/reset-battlemap)
      (update :game/battlemap tb/set-presentation :silent-selectable)))


(defn to-movement
  [{:keys [game/battlefield game/units] :as state}]
  (let [player-cubes (tu/unit-cubes units 1)
        movable-cubes (filter #(lbu/movable? battlefield %) player-cubes)
        movable-keys (map #(lbu/unit-key battlefield %) movable-cubes)]
    (-> (assoc state
               :game/player 1
               :game/phase [:movement :select-hex]
               :game/movement {:movable-keys (set movable-keys)
                               :movable-cubes (set movable-cubes)})
        (tsb/reset-battlemap movable-cubes)
        (update :game/battlemap tb/set-presentation :selectable))))
