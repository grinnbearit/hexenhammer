(ns hexenhammer.logic.core
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]))


(defn enemies?
  "Returns true if the two units have different owners"
  [unit-1 unit-2]
  (not= (:unit/player unit-1)
        (:unit/player unit-2)))


(defn engaged?
  "Returns true if the two units are engaged to each other"
  [unit-1 unit-2]
  (and (enemies? unit-1 unit-2)
       (or (contains? (set (mc/forward-arc (:entity/cube unit-1) (:unit/facing unit-1)))
                      (:entity/cube unit-2))
           (contains? (set (mc/forward-arc (:entity/cube unit-2) (:unit/facing unit-2)))
                      (:entity/cube unit-1)))))


(defn engaged-cubes
  "Returns a list of cubes engaged to the passed cube
  assumes the cube is on the battlefield and a unit"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (for [neighbour (mc/neighbours cube)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (le/unit? entity)
                     (engaged? unit entity))]
      neighbour)))


(defn battlefield-engaged?
  "Returns true if the passed cube is currently engaged on the battlefield
  assumes the cube is on the battlefield and a unit"
  [battlefield cube]
  (not (empty? (engaged-cubes battlefield cube))))


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
