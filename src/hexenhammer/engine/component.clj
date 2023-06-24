(ns hexenhammer.engine.component)


(defn gen-grass
  [position]
  {:hexenhammer/class :terrain
   :terrain/name "grass"
   :terrain/position position})


(defn gen-infantry
  [player id position facing]
  {:hexenhammer/class :unit
   :unit/player player
   :unit/name "infantry"
   :unit/id id
   :unit/files 4
   :unit/ranks 4
   :unit/position position
   :unit/facing facing
   :unit/M 4})


(defn gen-shadow
  [player position facing]
  {:hexenhammer/class :shadow
   :shadow/player player
   :shadow/position position
   :shadow/facing facing})
