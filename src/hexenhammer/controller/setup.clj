(ns hexenhammer.controller.setup
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.battlemap :as tb]))


(defn select-hex
  [state cube]
  (let [entity (get-in state [:game/battlefield cube])
        subphase (if (let/terrain? entity) :add-unit :remove-unit)]
    (-> (assoc state
               :game/phase [:setup subphase]
               :game/cube cube)
        (tb/reset-battlemap [cube])
        (update :game/battlemap tb/set-presentation [cube] :selected))))


(defn unselect
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (dissoc :game/cube)
      (tb/reset-battlemap)
      (update :game/battlemap tb/set-presentation :silent-selectable)))


(defn select-add-unit
  [state _]
  (unselect state))


(defn select-remove-unit
  [state _]
  (unselect state))


(defn add-unit
  [state player facing M Ld R]
  (let [cube (:game/cube state)
        prev-id (get-in state [:game/units player "infantry" :counter] 0)
        next-id (inc prev-id)
        unit (leu/gen-infantry player next-id facing M Ld R)]
    (-> (update-in state [:game/battlefield cube] let/place unit)
        (assoc-in [:game/units player "infantry" :cubes next-id] cube)
        (assoc-in [:game/units player "infantry" :counter] next-id)
        (unselect))))


(defn remove-unit
  [state]
  (let [unit-cube (:game/cube state)
        {:keys [unit/player unit/name unit/id]} (get-in state [:game/battlefield unit-cube])]
    (-> (update state :game/battlefield lbu/remove-unit unit-cube)
        (update-in [:game/units player name :cubes] dissoc id)
        (unselect))))


(defn swap-terrain
  [state terrain]
  (let [cube (:game/cube state)
        entity (get-in state [:game/battlefield cube])
        new-terrain (case terrain
                      :open let/OPEN-GROUND
                      :dangerous let/DANGEROUS-TERRAIN
                      :impassable let/IMPASSABLE-TERRAIN)
        new-entity (if (let/terrain? entity) new-terrain (let/place new-terrain entity))]
    (-> (assoc-in state [:game/battlefield cube] new-entity)
        (unselect))))
