(ns hexenhammer.logic.entity.event)


(defn dangerous-terrain
  [cube unit-key]
  {:entity/class :event
   :event/type :dangerous-terrain
   :event/cube cube
   :event/unit-key unit-key})


(defn heavy-casualties
  [cube unit-key]
  {:entity/class :event
   :event/type :heavy-casualties
   :event/cube cube
   :event/unit-key unit-key})
