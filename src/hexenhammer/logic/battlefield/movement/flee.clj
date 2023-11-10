(ns hexenhammer.logic.battlefield.movement.flee
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.mover :as lem]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]))


(defn flee-direction
  "Returns a pointer for the direction this unit will flee in"
  [pointer source-cube]
  (if (= (:cube pointer) source-cube)
    pointer
    (lc/->Pointer (:cube pointer)
                  (lc/direction source-cube (:cube pointer)))))


(defn flee-step
  "Given a pointer, returns the next pointer in the same direction"
  [pointer]
  (lc/->Pointer (lc/step (:cube pointer) (:facing pointer))
                (:facing pointer)))


(defn flee-path
  "Given a unit-cube, source-cube and a number of hexes, returns {:path :edge?}
  path contains the pointers the unit moves through
  edge? is true if the unit runs off the battlefield edge

  unlike core, keeps the start pointer"
  [battlefield unit-cube source-cube hexes]
  (let [start (lbu/unit-pointer battlefield unit-cube)
        flee-pointer (flee-direction start source-cube)]

    (loop [path (if (= start flee-pointer)
                  [start]
                  [start flee-pointer])]

      (let [pointer (peek path)]

        (if (and (<= (inc hexes) (count path))
                 (lbm/valid-move? battlefield unit-cube pointer)
                 (lbm/valid-end? battlefield unit-cube pointer))
          {:path path :edge? false}

          (let [next-step (flee-step pointer)]
            (if (contains? battlefield (:cube next-step))
              (recur (conj path next-step))
              {:path path :edge? true})))))))


(defn compress-path
  "Returns a compressed flee path which only modifies the first 2 pointers"
  [path]
  (cond-> path

    (and (<= 2 (count path))
         (= (get-in path [0 :cube])
            (get-in path [1 :cube])))
    (-> (rest) (vec))))


(defn path->tweeners
  "Given a path, returns a new map of cube->mover for the passable in between steps in the path"
  [battlefield unit-cube path edge?]
  (let [player (get-in battlefield [unit-cube :unit/player])
        new-bf (lbu/remove-unit battlefield unit-cube)
        tween-path (cond-> (compress-path path)
                     (not edge?) (pop))]

    (->> (for [pointer tween-path
               :let [cube (:cube pointer)
                     entity (new-bf cube)]
               :when (let/passable? entity)
               :let [mover (lem/gen-mover player
                                          :highlighted (:facing pointer)
                                          :presentation :past)]]
           [cube (let/place entity mover)])
         (into {}))))


(defn path-events
  "Returns a list of events for the passed path"
  [battlefield unit-cube path]
  (let [unit (battlefield unit-cube)
        unit-key (leu/unit-key unit)
        new-bf (lbu/remove-unit battlefield unit-cube)
        path-cubes (map :cube (compress-path path))]

    (->> (for [cube (rest path-cubes)
               :let [entity (new-bf cube)]]

           (cond (let/terrain? entity)
                 (when (or (let/dangerous? entity)
                           (let/impassable? entity))
                   (lev/dangerous-terrain cube unit-key))

                 (leu/enemies? unit entity)
                 (when (not (leu/fleeing? entity))
                   (lev/opportunity-attack cube unit-key (leu/unit-strength entity)))

                 :else
                 (when (and (<= 8 (leu/unit-strength unit))
                            (lbu/panickable? new-bf cube))
                   (lev/panic (leu/unit-key entity)))))

         (remove nil?))))


(defn flee
  [battlefield unit-cube source-cube roll]
  (let [hexes (lc/hexes roll)
        {:keys [path edge?] :as fp} (flee-path battlefield unit-cube source-cube hexes)
        end (last path)
        cube->tweeners (path->tweeners battlefield unit-cube path edge?)
        events (path-events battlefield unit-cube path)]
    {:end end
     :cube->tweeners cube->tweeners
     :edge? edge?
     :events events}))
