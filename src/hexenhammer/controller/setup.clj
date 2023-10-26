(ns hexenhammer.controller.setup
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]))


(defn select-hex
  [state cube]
  (let [entity (get-in state [:game/battlefield cube])
        subphase (if (let/terrain? entity) :add-unit :remove-unit)]
    (-> (assoc state
               :game/phase [:setup subphase]
               :game/cube cube)
        (t/reset-battlemap [cube])
        (update :game/battlemap tb/set-presentation [cube] :selected))))


(defn unselect
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (dissoc :game/cube)
      (t/reset-battlemap)
      (update :game/battlemap tb/set-presentation :silent-selectable)))


(defn select-add-unit
  [state _]
  (unselect state))


(defn select-remove-unit
  [state _]
  (unselect state))


(defn add-unit
  [{:keys [game/units] :as state} player facing M Ld R]
  (let [cube (:game/cube state)
        id (tu/next-id units player "infantry")
        unit (leu/gen-infantry player id facing M Ld R)
        unit-key (leu/unit-key unit)]
    (-> (update-in state [:game/battlefield cube] let/place unit)
        (update :game/units tu/inc-id player "infantry")
        (update :game/units tu/set-unit unit-key cube)
        (unselect))))


(defn remove-unit
  [{:keys [game/battlefield] :as state}]
  (let [unit-cube (:game/cube state)
        unit-key (lbu/unit-key battlefield unit-cube)]
    (-> (update state :game/battlefield lbu/remove-unit unit-cube)
        (update :game/units tu/remove-unit unit-key)
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
