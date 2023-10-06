(ns hexenhammer.model.event)


(defn dangerous
  [cube player name id]
  {:event/class :dangerous
   :event/cube cube
   :unit/player player
   :entity/name name
   :unit/id id})


(defn heavy-casualties
  [cube player name id]
  {:event/class :heavy-casualties
   :event/cube cube
   :unit/player player
   :entity/name name
   :unit/id id})


(defn panic
  [cube player name id]
  {:event/class :panic
   :event/cube cube
   :unit/player player
   :entity/name name
   :unit/id id})


(defn opportunity-attack
  [cube player name id wounds]
  {:event/class :opportunity-attack
   :event/cube cube
   :unit/player player
   :entity/name name
   :unit/id id
   :event/wounds wounds})
