(ns hexenhammer.logic.entity.terrain)


(def OPEN-GROUND
  {:entity/class :terrain
   :entity/presentation :default
   :entity/los 0
   :terrain/type :open})


(def DANGEROUS-TERRAIN
  (assoc OPEN-GROUND
         :terrain/type :dangerous))


(def IMPASSABLE-TERRAIN
  (assoc OPEN-GROUND
         :terrain/type :impassable
         :entity/los 5))


(defn terrain?
  [entity]
  (= :terrain (:entity/class entity)))


(defn place
  "Places a unit entity onto a terrain entity
  returns the combined entity"
  [terrain unit]
  (assoc unit :unit/terrain terrain))


(defn clear
  "Pops the unit off the terrain, returns the underlying terrain"
  [unit]
  (:unit/terrain unit))


(defn pickup
  "Pops the unit off the terrain, returns the unit"
  [unit]
  (dissoc unit :unit/terrain))


(defn dangerous?
  "Returns true if the passed terrain triggers a dangerous terrain test when moved through"
  [terrain]
  (= :dangerous (:terrain/type terrain)))


(defn impassable?
  "Returns true if the passed terrain is impassable"
  [terrain]
  (= :impassable (:terrain/type terrain)))


(defn passable?
  "Returns true if the passed entity can be moved through"
  [entity]
  (and (terrain? entity)
       (not (impassable? entity))))
