(ns hexenhammer.controller.movement)


(defn set-mover-selected
  [battlemap pointer]
  (update battlemap (:cube pointer) assoc
          :mover/selected (:facing pointer)
          :mover/state :present
          :entity/presentation :selected))


(defn reset-state
  "Removes all keys associated with movement"
  [state]
  (dissoc state
          :game/battlemap
          :movement/moved?
          :movement/battlemap
          :movement/breadcrumbs))
