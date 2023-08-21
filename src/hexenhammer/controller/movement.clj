(ns hexenhammer.controller.movement)


(defn set-mover-selected
  [battlemap pointer]
  (update battlemap (:cube pointer) assoc
          :mover/selected (:facing pointer)
          :mover/state :present
          :entity/interaction :selectable
          :entity/presentation :selected))


(defn show-moves
  "Updates the battlemap and breadcrumbs for all moves that change hexes"
  [state pointer]
  (let [unit (get-in state [:game/battlefield (:cube pointer)])]
    (-> (if (and (= (:cube pointer) (:entity/cube unit))
                 (= (:facing pointer) (:unit/facing unit)))
          (update state :game/movement dissoc :moved?)
          (assoc-in state [:game/movement :moved?] true))
        (assoc :game/battlemap (merge (get-in state [:game/movement :battlemap])
                                      (get-in state [:game/movement :breadcrumbs pointer])))
        (assoc-in [:game/movement :pointer] pointer)
        (update :game/battlemap set-mover-selected pointer))))
