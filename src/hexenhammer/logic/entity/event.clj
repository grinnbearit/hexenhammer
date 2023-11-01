(ns hexenhammer.logic.entity.event)


(defn dangerous
  [cube unit-key]
  {:entity/class :event
   :event/type :dangerous
   :event/cube cube
   :event/unit-key unit-key})
