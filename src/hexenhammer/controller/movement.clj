(ns hexenhammer.controller.movement)


(defn set-mover-selected
  [battlemap pointer]
  (update battlemap (:cube pointer) assoc
          :mover/selected (:facing pointer)
          :mover/state :present
          :entity/interaction :selectable
          :entity/presentation :selected))
