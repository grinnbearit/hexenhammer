(ns hexenhammer.controller.setup
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.core :as t]))


(defn select-hex
  [state cube]
  (let [entity (get-in state [:game/battlefield cube])
        subphase (if (let/terrain? entity) :add-unit :remove-unit)]
    (-> (assoc state
               :game/phase [:setup subphase]
               :game/selected cube)
        (t/reset-battlemap [cube])
        (update :game/battlemap t/set-presentation [cube] :selected))))


(defn unselect
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (dissoc :game/selected)
      (t/reset-battlemap)
      (update :game/battlemap t/set-presentation :silent-selectable)))


(defn select-add-unit
  [state _]
  (unselect state))


(defn select-remove-unit
  [state _]
  (unselect state))


(defn add-unit
  [state player facing M Ld R]
  (let [cube (:game/selected state)
        prev-id (get-in state [:game/units player "infantry" :counter] 0)
        next-id (inc prev-id)
        unit (leu/gen-infantry player next-id facing M Ld R)]
    (-> (update-in state [:game/battlefield cube] let/place unit)
        (assoc-in [:game/units player "infantry" :cubes next-id] cube)
        (assoc-in [:game/units player "infantry" :counter] next-id)
        (unselect))))


(defn remove-unit
  [state]
  (let [unit-cube (:game/selected state)
        {:keys [unit/player unit/name unit/id]} (get-in state [:game/battlefield unit-cube])]
    (-> (update state :game/battlefield lbu/remove-unit unit-cube)
        (update-in [:game/units player name :cubes] dissoc id)
        (unselect))))
