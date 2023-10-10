(ns hexenhammer.logic.unit
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]))


(defn enemies?
  "Returns true if the two units have different owners"
  [unit-1 unit-2]
  (not= (:unit/player unit-1)
        (:unit/player unit-2)))


(defn allies?
  "Returns true if the two units have the same owner"
  [unit-1 unit-2]
  (= (:unit/player unit-1)
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
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (for [neighbour (mc/neighbours unit-cube)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (le/unit? entity)
                     (engaged? unit entity))]
      neighbour)))


(defn battlefield-engaged?
  "Returns true if the passed cube is currently engaged on the battlefield
  assumes the cube is on the battlefield and a unit"
  [battlefield unit-cube]
  (not (empty? (engaged-cubes battlefield unit-cube))))


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
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (letfn [(reducer [acc d]
              (let [slice (->> (mc/forward-slice unit-cube (:unit/facing unit) d)
                               (filter #(and (contains? battlefield %)
                                             (battlefield-visible? battlefield unit-cube %))))]
                (if (empty? slice)
                  (reduced acc)
                  (concat acc slice))))]

      (reduce reducer [] (drop 1 (range))))))


(defn remove-unit
  "Returns a new battlefield with the unit removed from the old cube
  assumes the cube points to a unit"
  [battlefield unit-cube]
  (update battlefield unit-cube lt/pickup))


(defn move-unit
  "Returns a new battlefield with the unit removed from the old cube and moved to the new pointer
  assumes the cube points to a unit and the pointer points to passable terrain"
  [battlefield unit-cube pointer]
  (let [unit (-> (battlefield unit-cube)
                 (assoc :unit/facing (:facing pointer)))]

    (-> (remove-unit battlefield unit-cube)
        (update (:cube pointer) lt/swap unit))))


(defn destroyed?
  "Given a unit and damage, returns true if the unit would be destroyed"
  [unit damage]
  (<= (mu/wounds unit) damage))


(defn damage-unit
  "Given a unit and damage, returns the new unit with the damage taken
  assumes the damage isn't enought to destroy the unit"
  [unit damage]
  (mu/set-wounds unit (- (mu/wounds unit) damage)))


(defn destroy-models
  "Given a unit and a number of models, returns the new unit with the
  destroyed models removed, assumes the unit has sufficient models"
  [unit models]
  (mu/set-models unit (- (mu/models unit) models)))


(defn phase-reset
  "Resets different statuses that happen on a phase transition"
  [battlefield unit-cubes]
  (letfn [(reducer [battlefield cube]
            (let [unit (battlefield cube)]
              (assoc-in battlefield [cube :unit/phase]
                        {:strength (mu/unit-strength unit)})))]

    (reduce reducer battlefield unit-cubes)))


(defn panickable?
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (not (or (get-in unit [:unit/phase :panicked?])
             (get-in unit [:unit/movement :fleeing?])
             (battlefield-engaged? battlefield unit-cube)))))


(defn heavy-casualties?
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (and (<= (/ (mu/unit-strength unit) (get-in unit [:unit/phase :strength]))
             3/4)
         (panickable? battlefield unit-cube))))
