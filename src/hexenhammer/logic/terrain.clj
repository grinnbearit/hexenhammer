(ns hexenhammer.logic.terrain
  (:require [hexenhammer.logic.entity :as le]))


(defn place
  "Places an object entity onto a terrain entity, updating the entity/cube for the placed object
  resets the entity state of the terrain but keeps all other keys"
  [terrain object]
  (let [cube (:entity/cube terrain)]
    (assoc object
           :entity/cube cube
           :object/terrain (dissoc terrain :entity/state))))


(defn pickup
  "Returns the terrain object under the object or the terrain directly"
  [entity]
  (if (le/terrain? entity)
    entity
    (:object/terrain entity)))


(defn swap
  "Places object onto the entity after picking up what's on it"
  [entity object]
  (place (pickup entity) object))


(defn passable?
  "Returns true if the passed entity can be moved trough"
  [entity]
  (and (le/terrain? entity)
       (not= :impassable (:terrain/type entity))))


(defn dangerous?
  "Returns true if the passed terrain triggers a dangerous terrain test when moved through"
  [entity]
  (contains? #{:dangerous :impassable} (:terrain/type entity)))
