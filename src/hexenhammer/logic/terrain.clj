(ns hexenhammer.logic.terrain
  (:require [hexenhammer.logic.entity :as le]))


(defn place
  "Places an object entity onto a terrain entity
  removes it's entity/state since it's no longer relevant"
  [object terrain]
  (->> (dissoc terrain :entity/state)
       (assoc object :object/terrain)))


(defn pickup
  "Returns the terrain object under the object or the terrain directly"
  [entity]
  (if (le/terrain? entity)
    entity
    (:object/terrain entity)))


(defn swap
  "Places object onto the entity after picking up what's on it"
  [object entity]
  (place object (pickup entity)))


(defn passable?
  "Returns true if the passed entity can be moved trough"
  [entity]
  (and (le/terrain? entity)
       (not= :impassable (:terrain/type entity))))
