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


(defn set-movement
  [{:keys [game/battlefield] :as state} logic-fn phase cube]
  (let [{:keys [cube->enders]} (logic-fn battlefield cube)
        battlemap (cond-> cube->enders
                    (not (contains? cube->enders cube))
                    (assoc cube (battlefield cube)))]
    (-> (assoc state
               :game/cube cube
               :game/phase phase
               :game/battlemap battlemap)
        (assoc-in [:game/movement :battlemap] battlemap)
        (update :game/battlemap tb/set-presentation [cube] :selected))))


(defn move-movement
  [state pointer]
  (let [battlemap (get-in state [:game/movement :battlemap])]
    (-> (assoc state
               :game/pointer pointer
               :game/battlemap battlemap)
        (assoc-in [:game/movement :moved?] true)
        (update-in [:game/battlemap (:cube pointer)] assoc
                   :mover/selected (:facing pointer))
        (update :game/battlemap tb/set-presentation [(:cube pointer)] :selected))))


(defn set-reform
  [state cube]
  (set-movement state lbm/reform [:movement :reform] cube))


(defn select-reform
  [state _]
  (unselect state))


(defn move-reform
  [state pointer]
  (move-movement state pointer))


(defn set-forward
  [state cube]
  (set-movement state lbm/forward [:movement :forward] cube))


(defn select-forward
  [state _]
  (unselect state))


(defn move-forward
  [state pointer]
  (move-movement state pointer))


(defn set-reposition
  [state cube]
  (set-movement state lbm/reposition [:movement :reposition] cube))


(defn select-reposition
  [state _]
  (unselect state))


(defn move-reposition
  [state pointer]
  (move-movement state pointer))


(defn set-march
  [state cube]
  (set-movement state lbm/march [:movement :march] cube))


(defn select-march
  [state _]
  (unselect state))


(defn move-march
  [state pointer]
  (move-movement state pointer))


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
    (set-forward cube)

    (= :reposition movement)
    (set-reposition cube)

    (= :march movement)
    (set-march cube)))
