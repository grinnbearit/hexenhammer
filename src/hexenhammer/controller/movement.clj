(ns hexenhammer.controller.movement
  (:require [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]))


(defn reset-movement
  [{:keys [game/player game/battlefield game/units] :as state}]
  (let [player-cubes (tu/unit-cubes units player)
        movable-cubes (filter #(lbu/movable? battlefield %) player-cubes)]
      (-> (assoc state
                 :game/phase [:movement :select-hex]
                 :game/movement {:movers movable-cubes})
          (t/reset-battlemap movable-cubes)
          (update :game/battlemap tb/set-presentation :marked))))
