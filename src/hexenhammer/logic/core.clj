(ns hexenhammer.logic.core
  (:require [hexenhammer.model.cube :as cube]
            [hexenhammer.logic.entity :as le]))


(defn enemies?
  "Returns true if the two units have different owners"
  [unit-1 unit-2]
  (not= (:unit/player unit-1)
        (:unit/player unit-2)))


(defn engaged?
  "Returns true if the two units are engaged to each other"
  [unit-1 unit-2]
  (and (enemies? unit-1 unit-2)
       (or (contains? (set (cube/forward-arc (:entity/cube unit-1) (:unit/facing unit-1)))
                      (:entity/cube unit-2))
           (contains? (set (cube/forward-arc (:entity/cube unit-2) (:unit/facing unit-2)))
                      (:entity/cube unit-1)))))


(defn battlefield-engaged?
  "Returns true if the passed cube is currently engaged on the battlefield
  assumes the cube is on the battlefield and a unit"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (->> (for [neighbour (cube/neighbours cube)
               :when (contains? battlefield neighbour)
               :let [entity (battlefield neighbour)]
               :when (and (le/unit? entity)
                          (engaged? unit entity))]
           entity)
         ((comp boolean seq)))))


(defn battlefield-visible?
  "Returns true if every cube between cx and cy is terrain"
  [battlefield cx cy]
  (->> (cube/cubes-between cx cy)
       (map battlefield)
       (remove le/terrain?)
       (empty?)))


(defn field-of-view
  "Returns the list of cubes in the unit's forward cone, visible to it"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (letfn [(reducer [acc d]
              (let [slice (->> (cube/forward-slice cube (:unit/facing unit) d)
                               (filter #(and (contains? battlefield %)
                                             (battlefield-visible? battlefield cube %))))]
                (if (empty? slice)
                  (reduced acc)
                  (concat acc slice))))]

      (reduce reducer [] (drop 1 (range))))))
