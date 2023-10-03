(ns hexenhammer.model.event)


(defn dangerous
  [cube player name id]
  {:event/class :dangerous
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
  [cube target-player target-name target-id source-player source-name source-id wounds]
  {:event/class :opportunity-attack
   :event/cube cube
   :unit/player target-player
   :entity/name target-name
   :unit/id target-id
   :event/source {:unit/player source-player
                  :entity/name source-name
                  :unit/id source-id}
   :event/wounds wounds})
