(ns hexenhammer.controller.unit
  (:require [hexenhammer.model.unit :as mu]
            [hexenhammer.model.event :as mv]
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


(defn remove-unit
  "Removes the unit at `unit-cube`"
  [state unit-cube]
  (let [{:keys [unit/player entity/name unit/id] :as unit} (get-in state [:game/battlefield unit-cube])]
    (-> (update-in state [:game/units player name :cubes] dissoc id)
        (update :game/battlefield lu/remove-unit unit-cube))))


(defn destroy-unit
  "Destroys the unit at `unit-cube`"
  [state unit-cube]
  (let [unit (get-in state [:game/battlefield unit-cube])
        new-state (remove-unit state unit-cube)]
    (cond-> new-state

      (<= 8 (get-in unit [:unit/phase :strength]))
      (update :game/events into
              (lu/nearby-friend-annihilated-events (:game/battlefield new-state) unit-cube (:unit/player unit))))))


(defn escape-unit
  "Destroys the unit at `unit-cube`, the unit escapes the battlfield at `end-cube`"
  [state unit-cube end-cube]
  (let [unit (get-in state [:game/battlefield unit-cube])
        new-state (remove-unit state unit-cube)]
    (cond-> new-state

      (<= 8 (get-in unit [:unit/phase :strength]))
      (update :game/events into
              (lu/nearby-friend-annihilated-events (:game/battlefield new-state) end-cube (:unit/player unit))))))


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
  [state unit-cube pointer]
  (let [target-cube (:cube pointer)
        {:keys [unit/player entity/name unit/id]} (get-in state [:game/battlefield unit-cube])]
    (-> (assoc-in state [:game/units player name :cubes id] target-cube)
        (update :game/battlefield lu/move-unit unit-cube pointer))))
