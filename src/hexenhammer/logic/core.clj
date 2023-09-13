(ns hexenhammer.logic.core
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]))


(defn battlefield-visible?
  "Returns true if every cube between cx and cy has an los value
  less than either of their los"
  [battlefield cx cy]
  (let [max-los (max (:entity/los (battlefield cx))
                     (:entity/los (battlefield cy)))]
    (->> (mc/cubes-between cx cy)
         (map (comp :entity/los battlefield))
         (remove #(< % max-los))
         (empty?))))


(defn field-of-view
  "Returns the list of cubes in the unit's forward cone, visible to it"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (letfn [(reducer [acc d]
              (let [slice (->> (mc/forward-slice cube (:unit/facing unit) d)
                               (filter #(and (contains? battlefield %)
                                             (battlefield-visible? battlefield cube %))))]
                (if (empty? slice)
                  (reduced acc)
                  (concat acc slice))))]

      (reduce reducer [] (drop 1 (range))))))


(defn remove-unit
  "Returns a new battlefield with the unit removed from the old cube
  assumes the cube points to a unit"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (assoc battlefield cube (lt/pickup unit))))


(defn move-unit
  "Returns a new battlefield with the unit removed from the old cube and moved to the new pointer
  assumes the cube points to a unit and the pointer points to passable terrain

  this function is useful in testing if new battlefield states are valid"
  [battlefield cube pointer]
  (let [unit (battlefield cube)
        old-terrain (lt/pickup unit)
        new-terrain (battlefield (:cube pointer))
        new-unit (-> (assoc unit
                            :entity/cube (:cube pointer)
                            :unit/facing (:facing pointer))
                     (lt/place new-terrain))]

    (assoc battlefield
           cube old-terrain
           (:cube pointer) new-unit)))


(defn set-state
  "sets all entities on the battlefield to the passed entity state
  if a list of cubes is passed, sets only those cubes instead"
  ([battlefield state]
   (update-vals battlefield #(assoc % :entity/state state)))
  ([battlefield cubes state]
   (->> (set-state (select-keys battlefield cubes) state)
        (merge battlefield))))


(defn show-cubes
  "Returns a new battlemap with the cubes set to the passed state"
  [battlefield cubes state]
  (-> (select-keys battlefield cubes)
      (set-state state)))
