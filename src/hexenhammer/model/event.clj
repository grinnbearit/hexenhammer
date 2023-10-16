(ns hexenhammer.model.event)


(defn dangerous
  [cube unit-key]
  {:event/class :dangerous
   :event/cube cube
   :event/unit-key unit-key})


(defn heavy-casualties
  [cube unit-key]
  {:event/class :heavy-casualties
   :event/cube cube
   :event/unit-key unit-key})


(defn panic
  [cube unit-key]
  {:event/class :panic
   :event/cube cube
   :event/unit-key unit-key})


(defn opportunity-attack
  [cube unit-key wounds]
  {:event/class :opportunity-attack
   :event/cube cube
   :event/unit-key unit-key
   :event/wounds wounds})
