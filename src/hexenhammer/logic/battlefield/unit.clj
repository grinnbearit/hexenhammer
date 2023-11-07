(ns hexenhammer.logic.battlefield.unit
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.terrain :as let]))


(defn remove-unit
  "Returns a new battlefield with the unit removed from the old cube"
  [battlefield unit-cube]
  (update battlefield unit-cube let/clear))


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
   (->> (lc/neighbours unit-cube)
        (filter #(contains? battlefield %))
        (filter #(leu/unit? (battlefield %)))
        (filter #(engaged? battlefield unit-cube %))
        ((comp boolean seq)))))


(defn movable?
  "Returns true if the unit isn't engaged in combat or fleeing"
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (not (or (engaged? battlefield unit-cube)
             (leu/fleeing? unit)))))


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
