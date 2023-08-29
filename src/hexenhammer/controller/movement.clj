(ns hexenhammer.controller.movement
  (:require [hexenhammer.logic.movement :as lm]))


(defn set-mover-selected
  [battlemap pointer]
  (update battlemap (:cube pointer) assoc
          :mover/selected (:facing pointer)
          :mover/state :present
          :entity/state :selected))


(defn show-moves
  "Updates the battlemap and breadcrumbs for all moves that change hexes"
  [state pointer]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        {:keys [battlemap breadcrumbs]} (:game/movement state)]

    (-> (if (and (= (:cube pointer) (:entity/cube unit))
                 (= (:facing pointer) (:unit/facing unit)))
          (update state :game/movement dissoc :moved?)
          (assoc-in state [:game/movement :moved?] true))

        (assoc :game/battlemap (merge battlemap (breadcrumbs pointer)))
        (assoc-in [:game/movement :pointer] pointer)
        (update :game/battlemap set-mover-selected pointer))))
