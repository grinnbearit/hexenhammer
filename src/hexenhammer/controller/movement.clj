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


(defn set-reform
  [{:keys [game/battlefield] :as state} cube]
  (let [{:keys [cube->enders]} (lbm/reform battlefield cube)]
    (-> (assoc state
               :game/cube cube
               :game/phase [:movement :reform]
               :game/battlemap cube->enders)
        (update :game/battlemap tb/set-presentation [cube] :selected))))


(defn select-reform
  [state _]
  (unselect state))


(defn move-reform
  [state pointer]
  (let [cube (:game/cube state)]
    (-> (assoc state :game/pointer pointer)
        (assoc-in [:game/movement :moved?] true)
        (update-in [:game/battlemap cube] assoc
                   :mover/selected (:facing pointer)))))


(defn set-forward
  [{:keys [game/battlefield] :as state} cube]
  (let [{:keys [cube->enders]} (lbm/forward battlefield cube)]
    (-> (assoc state
               :game/cube cube
               :game/phase [:movement :forward]
               :game/battlemap cube->enders)
        (update :game/movement assoc
                :cube->enders cube->enders)
        (update :game/battlemap tb/set-presentation [cube] :selected))))


(defn select-forward
  [state _]
  (unselect state))


(defn move-forward
  [state pointer]
  (let [cube->enders (get-in state [:game/movement :cube->enders])]
    (-> (assoc state
               :game/pointer pointer
               :game/battlemap cube->enders)
        (assoc-in [:game/movement :moved?] true)
        (update-in [:game/battlemap (:cube pointer)] assoc
                   :mover/selected (:facing pointer))
        (update :game/battlemap tb/set-presentation [(:cube pointer)] :selected))))


(defn select-hex
  [state cube]
  (set-reform state cube))


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


(defn switch-movement
  [{:keys [game/cube] :as state} movement]
  (cond-> (unselect state)

    (= :reform movement)
    (set-reform cube)

    (= :forward movement)
    (set-forward cube)))
