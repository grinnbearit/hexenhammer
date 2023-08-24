(ns hexenhammer.model.logic.entity)


(defn unit?
  [entity]
  (= :unit (:entity/class entity)))


(defn terrain?
  [entity]
  (= :terrain (:entity/class entity)))


(defn onto-terrain
  "Places an object entity onto a terrain entity
  removes it's entity/state since it's no longer relevant"
  [object terrain]
  (->> (dissoc terrain :entity/state)
       (assoc object :object/terrain)))
