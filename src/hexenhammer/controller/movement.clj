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
          (dissoc state :movement/moved?)
          (assoc state :movement/moved? true))
        (assoc :game/battlemap (merge (:movement/battlemap state)
                                      (get-in state [:movement/breadcrumbs pointer]))
               :movement/selected pointer)
        (update :game/battlemap set-mover-selected pointer))))
