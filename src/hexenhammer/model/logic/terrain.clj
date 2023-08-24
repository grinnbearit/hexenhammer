(ns hexenhammer.model.logic.terrain
  (:require [hexenhammer.model.logic.entity :as mle]))


(defn place
  "Places an object entity onto a terrain entity
  removes it's entity/state since it's no longer relevant"
  [object terrain]
  (->> (dissoc terrain :entity/state)
       (assoc object :object/terrain)))


(defn pickup
  "Returns the terrain object under the object or the terrain directly"
  [entity]
  (if (mle/terrain? entity)
    entity
    (:object/terrain entity)))


(defn swap
  "Places object onto the entity after picking up what's on it"
  [object entity]
  (place object (pickup entity)))
