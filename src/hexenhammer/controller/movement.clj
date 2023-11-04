(ns hexenhammer.controller.movement
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement :as lbm]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.dice :as td]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.event :as ce]))


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
  (let [{:keys [cube->enders pointer->cube->tweeners pointer->events]} (logic-fn battlefield cube)
        battlemap (cond-> cube->enders
                    (not (contains? cube->enders cube))
                    (assoc cube (battlefield cube)))]
    (-> (assoc state
               :game/cube cube
               :game/phase phase
               :game/battlemap battlemap)
        (update :game/movement assoc
                :battlemap battlemap
                :pointer->cube->tweeners pointer->cube->tweeners
                :pointer->events pointer->events)
        (update :game/battlemap tb/set-presentation [cube] :selected))))


(defn move-movement
  [state pointer]
  (let [battlemap (get-in state [:game/movement :battlemap])
        cube->tweeners (get-in state [:game/movement :pointer->cube->tweeners pointer])
        events (get-in state [:game/movement :pointer->events pointer])]
    (-> (assoc state
               :game/pointer pointer
               :game/battlemap (merge battlemap cube->tweeners))
        (update :game/movement assoc
                :moved? true
                :events events)
        (update-in [:game/battlemap (:cube pointer)] assoc
                   :entity/presentation :selected
                   :mover/presentation :present
                   :mover/selected (:facing pointer)))))


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
  [{:keys [game/battlefield] :as state} cube]
  (let [threats (lbm/list-threats battlefield cube)
        new-state (set-movement state lbm/march [:movement :march] cube)
        unit (battlefield cube)]

    (if (seq threats)
      (-> (if (get-in unit [:unit/flags :marched?])
            (let [roll (get-in unit [:unit/state :movement :roll])]
              (if (get-in unit [:unit/state :movement :passed?])
                (update new-state :game/movement assoc
                        :march :passed
                        :roll roll)
                (update new-state :game/movement assoc
                        :march :failed
                        :roll roll)))
            (assoc-in new-state [:game/movement :march] :required))

          (assoc-in [:game/movement :threats] threats)
          (t/refresh-battlemap threats)
          (update :game/battlemap tb/set-presentation threats :marked))

      (assoc-in new-state [:game/movement :march] :unnecessary))))


(defn select-march
  [state _]
  (unselect state))


(defn move-march
  [state pointer]
  (let [new-state (move-movement state pointer)
        threats (get-in state [:game/movement :threats])]

    (-> (t/refresh-battlemap new-state threats)
        (update :game/battlemap tb/set-presentation threats :marked))))


(defn select-hex
  [state cube]
  (set-reform state cube))


(defn skip-movement
  [{:keys [game/cube game/battlefield] :as state}]
  (let [unit-key (lbu/unit-key battlefield cube)]
    (-> (update-in state [:game/movement :movable-cubes] disj cube)
        (update-in [:game/movement :movable-keys] disj unit-key)
        (unselect))))


(defn reset-movement
  [{:keys [game/battlefield game/units] :as state}]
  (letfn [(movable-key? [unit-key]
            (when-let [unit-cube (tu/get-unit units unit-key)]
              (let [unit (battlefield unit-cube)]
                (not (leu/fleeing? unit)))))]

    (let [unmoved-keys (get-in state [:game/movement :movable-keys])
          movable-keys (filter movable-key? unmoved-keys)
          movable-cubes (map #(tu/get-unit units %) movable-keys)]

      (-> (assoc state :game/movement
                 {:movable-keys (set movable-keys)
                  :movable-cubes (set movable-cubes)})
          (unselect)))))


(defn finish-movement
  [{:keys [game/cube game/pointer] :as state}]
  (let [events (get-in state [:game/movement :pointer->events pointer])]
    (-> (update state :game/battlefield lbu/move-unit cube pointer)
        (update :game/events into events)
        (skip-movement)
        (ce/trigger reset-movement))))


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


(defn test-leadership
  [state]
  (let [cube (:game/cube state)
        pointer (:game/pointer state)
        unit (get-in state [:game/battlefield cube])
        roll (td/roll! 2)]
    (-> (assoc-in state [:game/battlefield cube]
                  (-> (assoc-in unit [:unit/flags :marched?] true)
                      (assoc-in [:unit/state :movement]
                                {:roll roll
                                 :passed? (<= (apply + roll) (:unit/Ld unit))})))
        (set-march cube)
        (move-march pointer))))
