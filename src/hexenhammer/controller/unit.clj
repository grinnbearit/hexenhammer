(ns hexenhammer.controller.unit
  (:require [hexenhammer.logic.unit :as lu]
            [hexenhammer.logic.terrain :as lt]))


(defn unit-cubes
  "Returns all unit cubes for the passed player, if no player is passed returns all unit cubes"
  ([state]
   (concat (unit-cubes state 1) (unit-cubes state 2)))
  ([state player]
   (->> (get-in state [:game/units player])
        (vals)
        (mapcat (comp vals :cubes)))))


(defn destroy-unit
  [state unit]
  (-> (update-in state [:game/units (:unit/player unit) (:entity/name unit) :cubes] dissoc
                 (:unit/id unit))
      (update-in [:game/battlefield (:entity/cube unit)] lt/pickup)))


(defn damage-unit
  [state unit damage]
  (assoc-in state [:game/battlefield (:entity/cube unit)] (lu/damage-unit unit damage)))


(defn destroy-models
  [state unit models]
  (assoc-in state [:game/battlefield (:entity/cube unit)] (lu/destroy-models unit models)))
