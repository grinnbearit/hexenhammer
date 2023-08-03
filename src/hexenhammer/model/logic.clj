(ns hexenhammer.model.logic
  (:require [hexenhammer.model.cube :as cube]))


(defn engaged?
  "Returns true if the two units are engaged to each other"
  [unit-1 unit-2]
  (and (not= (:unit/player unit-1)
             (:unit/player unit-2))
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
               :when (and (= (:entity/class entity) :unit)
                          (engaged? unit entity))]
           entity)
         ((comp boolean seq)))))
