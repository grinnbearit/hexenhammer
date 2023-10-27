(ns hexenhammer.controller.movement
  (:require [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement :as lbm]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.battlemap :as tb]))


(defn unselect
  [state]
  (let [movable-cubes (get-in state [:game/movement :movable-cubes])]
    (-> (assoc state
               :game/phase [:movement :select-hex])
        (dissoc :game/cube)
        (update :game/movement select-keys
                [:movable-keys :movable-cubes])
        (t/reset-battlemap movable-cubes)
        (update :game/battlemap tb/set-presentation :selectable))))


(defn select-reform
  [{:keys [game/battlefield] :as state} cube]
  (if (= cube (:game/cube state))
    (unselect state)
    (let [{:keys [cube->enders]} (lbm/reform battlefield cube)]
      (-> (assoc state
                 :game/cube cube
                 :game/battlemap cube->enders)
          (update :game/movement assoc
                  :cube->enders cube->enders)
          (update :game/battlemap tb/set-presentation [cube] :selected)))))


(defn move-reform
  [state pointer]
  (let [cube (:game/cube state)]
    (-> (assoc state :game/pointer pointer)
        (assoc-in [:game/movement :moved?] true)
        (update-in [:game/battlemap cube] assoc
                   :mover/selected (:facing pointer)))))


(defn select-hex
  [state cube]
  (-> (assoc state :game/phase [:movement :reform])
      (select-reform cube)))


(defn skip-movement
  [{:keys [game/cube game/battlefield] :as state}]
  (let [unit-key (lbu/unit-key battlefield cube)]
    (-> (update-in state [:game/movement :movable-cubes] disj cube)
        (update-in [:game/movement :movable-keys] disj unit-key)
        (unselect))))


(defn finish-movement
  [{:keys [game/cube game/pointer] :as state}]
  (-> (update state :game/battlefield lbu/move-unit cube pointer)
      (skip-movement)))
