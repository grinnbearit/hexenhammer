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
  [state unit-cube]
  (let [{:keys [unit/player entity/name unit/id]} (get-in state [:game/battlefield unit-cube])]
    (-> (update-in state [:game/units player name :cubes] dissoc id)
        (update-in [:game/battlefield unit-cube] lt/pickup))))


(defn damage-unit
  [state unit-cube source-cube damage]
  (let [unit (get-in state [:game/battlefield unit-cube])
        damaged-unit (lu/damage-unit unit damage)
        damaged-bf (assoc (:game/battlefield state) unit-cube damaged-unit)]
    (cond-> (assoc state :game/battlefield damaged-bf)

      (lu/heavy-casualties? damaged-bf unit-cube)
      (update :game/events conj
              (mv/heavy-casualties source-cube (:unit/player unit) (:entity/name unit) (:unit/id unit))))))


(defn destroy-models
  [state unit-cube source-cube models]
  (let [unit (get-in state [:game/battlefield unit-cube])
        damaged-unit (lu/destroy-models unit models)
        damaged-bf (assoc (:game/battlefield state) unit-cube damaged-unit)]
    (cond-> (assoc state :game/battlefield damaged-bf)

      (lu/heavy-casualties? damaged-bf unit-cube)
      (update :game/events conj
              (mv/heavy-casualties source-cube (:unit/player unit) (:entity/name unit) (:unit/id unit))))))


(defn move-unit
  [state unit pointer]
  (let [unit-cube (:entity/cube unit)
        dest-cube (:cube pointer)]
    (-> (assoc-in state [:units (:unit/player unit) (:entity/name unit) (:unit/id unit)] dest-cube)
        (update :game/battlefield lu/move-unit unit-cube pointer))))
