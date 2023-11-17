(ns hexenhammer.controller.core
  (:require [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]
            [hexenhammer.logic.battlefield.movement.charge :as lbmc]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.battlefield :as tf]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.charge.core :as cc]))


(defn to-setup
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (tsb/reset-battlemap)
      (update :game/battlemap tb/set-presentation :silent-selectable)))


(declare to-charge)


(defn to-start
  [state]
  (-> (assoc state :game/player 1)
      (to-charge)))


(defn to-charge
  [{:keys [game/battlefield game/units] :as state}]
  (let [unit-cubes (tu/unit-cubes units)
        player-cubes (tu/unit-cubes units 1)
        charger-cubes (filter #(lbmc/charger? battlefield %) player-cubes)]
    (-> (assoc state
               :game/phase [:charge :select-hex]
               :game/charge {:chargers (set charger-cubes)})
        (update :game/battlefield tf/reset-phase unit-cubes)
        (cc/unselect))))


(defn to-movement
  [{:keys [game/battlefield game/units] :as state}]
  (let [player-cubes (tu/unit-cubes units 1)
        movable-cubes (filter #(lbm/movable? battlefield %) player-cubes)
        movable-keys (map #(lbu/unit-key battlefield %) movable-cubes)]
    (-> (assoc state
               :game/phase [:movement :select-hex]
               :game/movement {:movable-keys (set movable-keys)
                               :movable-cubes (set movable-cubes)})
        (cm/unselect))))


(defn to-close-combat
  [{:keys [game/units game/player] :as state}]
  (let [unit-cubes (tu/unit-cubes units)
        player-cubes (tu/unit-cubes units player)]
    (-> (update state :game/battlefield tf/reset-phase unit-cubes)
        (update :game/battlefield tf/reset-movement player-cubes)
        (dissoc :game/movement)
        (assoc :game/phase [:close-combat]))))
