(ns hexenhammer.logic.battlefield.unit
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.core :as lb]))


(defn remove-unit
  "Returns a new battlefield with the unit removed from the old cube"
  [battlefield unit-cube]
  (update battlefield unit-cube let/clear))


(declare engaged?)


(defn list-engaged
  "Returns a list of enemy cubes engaged to the passed unit-cube"
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (for [neighbour (lc/neighbours unit-cube)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (leu/unit? entity)
                     (engaged? battlefield unit-cube neighbour))]
      neighbour)))


(defn engaged?
  "With 2 unit cubes, returns true if they're engaged on the battlefield
  With 1 unit cube, returns true if it's engaged to any other unit on the battlefield"
  ([battlefield unit-cube-1 unit-cube-2]
   (let [unit-1 (battlefield unit-cube-1)
         unit-2 (battlefield unit-cube-2)]
     (and (leu/enemies? unit-1 unit-2)
          (or (contains? (set (lc/forward-arc unit-cube-1 (:unit/facing unit-1)))
                         unit-cube-2)
              (contains? (set (lc/forward-arc unit-cube-2 (:unit/facing unit-2)))
                         unit-cube-1)))))

  ([battlefield unit-cube]
   (-> (list-engaged battlefield unit-cube)
       (seq)
       (boolean))))


(defn unit-key
  "Returns the unit-key for the passed unit-cube"
  [battlefield unit-cube]
  (leu/unit-key (battlefield unit-cube)))


(defn move-unit
  "Returns a new battlefield with the unit moved to the new pointer"
  [battlefield unit-cube pointer]
  (let [unit (-> (battlefield unit-cube)
                 (assoc :unit/facing (:facing pointer)))]
    (-> (remove-unit battlefield unit-cube)
        (update (:cube pointer) let/place unit))))


(defn unit-pointer
  "Returns the pointer for the passed unit-cube"
  [battlefield unit-cube]
  (let [facing (get-in battlefield [unit-cube :unit/facing])]
    (lc/->Pointer unit-cube facing)))


(defn reset-phase
  "Resets the phase for the passed unit-cube"
  [battlefield unit-cube]
  (update battlefield unit-cube leu/reset-phase))


(defn panickable?
  "Returns true if the unit needs to take a panic test"
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (not (or (leu/panicked? unit)
             (leu/fleeing? unit)
             (engaged? battlefield unit-cube)))))


(defn heavy-casualties?
  "Returns true if the unit needs to take a heavy casualties test"
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (and (<= (/ (leu/unit-strength unit)
                (leu/phase-strength unit))
             3/4)
         (panickable? battlefield unit-cube))))


(defn closest-enemy
  "Returns the closest enemy cube to unit-cube from enemy-cubes,
  if tied returns the strongest"
  [battlefield unit-cube enemy-cubes]
  (letfn [(keyfn [enemy-cube]
            (let [enemy (battlefield enemy-cube)]
              (- (* (lc/distance unit-cube enemy-cube) 100)
                 (leu/unit-strength enemy))))]

    (apply min-key keyfn enemy-cubes)))


(defn reset-movement
  "Resets the movement state for the passed unit-cube"
  [battlefield unit-cube]
  (update battlefield unit-cube leu/reset-movement))


(defn field-of-view
  "Returns the list of cubes in the unit's forward cone, visible to it"
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (letfn [(reducer [acc d]
              (let [slice (->> (lc/forward-slice unit-cube (:unit/facing unit) d)
                               (filter #(and (contains? battlefield %)
                                             (lb/visible? battlefield unit-cube %))))]
                (if (empty? slice)
                  (reduced acc)
                  (concat acc slice))))]

      (reduce reducer [] (drop 1 (range))))))
