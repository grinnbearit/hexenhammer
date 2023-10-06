(ns hexenhammer.controller.unit
  (:require [hexenhammer.model.event :as mv]
            [hexenhammer.logic.unit :as lu]
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
  [state source unit damage]
  (let [unit-cube (:entity/cube unit)
        damaged-unit (lu/damage-unit unit damage)
        damaged-bf (assoc (:game/battlefield state) unit-cube damaged-unit)]
    (cond-> (assoc state :game/battlefield damaged-bf)

      (lu/heavy-casualties? damaged-bf unit-cube)
      (update :game/events conj
              (mv/heavy-casualties source (:unit/player unit) (:entity/name unit) (:unit/id unit))))))


(defn destroy-models
  [state source unit models]
  (let [unit-cube (:entity/cube unit)
        damaged-unit (lu/destroy-models unit models)
        damaged-bf (assoc (:game/battlefield state) unit-cube damaged-unit)]
    (cond-> (assoc state :game/battlefield damaged-bf)

      (lu/heavy-casualties? damaged-bf unit-cube)
      (update :game/events conj
              (mv/heavy-casualties source (:unit/player unit) (:entity/name unit) (:unit/id unit))))))


(defn move-unit
  [state unit pointer]
  (let [unit-cube (:entity/cube unit)
        dest-cube (:cube pointer)]
    (-> (assoc-in state [:units (:unit/player unit) (:entity/name unit) (:unit/id unit)] dest-cube)
        (update :game/battlefield lu/move-unit unit-cube pointer))))
