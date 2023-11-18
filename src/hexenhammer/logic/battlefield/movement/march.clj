(ns hexenhammer.logic.battlefield.movement.march
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]))


(defn path-events
  "Returns a list of dangerous events for the passed path"
  [battlefield unit-cube path]
  (let [unit-key (lbu/unit-key battlefield unit-cube)
        new-bf (lbu/remove-unit battlefield unit-cube)
        path-cubes (map :cube (lbm/compress-path path))]

    (for [cube (rest path-cubes)
          :let [terrain (new-bf cube)]
          :when (let/dangerous? terrain)]
      (lev/dangerous-terrain cube unit-key))))


(defn paths-events
  "Returns a map of pointer->events for all paths"
  [battlefield unit-cube paths]
  (->> (for [path paths]
         [(peek path) (path-events battlefield unit-cube path)])
       (into {})))


(defn list-threats
  "Returns a list of cubes that 'threaten' this unit on the battlefield
  i.e. enemy units within 3 hexes"
  [battlefield unit-cube]
  (let [unit (battlefield unit-cube)]
    (for [neighbour (lc/neighbours-within unit-cube 3)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (leu/unit? entity)
                     (leu/enemies? unit entity))]
      neighbour)))


(defn march
  [battlefield unit-cube]
  (let [hexes (lc/hexes (* 2 (get-in battlefield [unit-cube :unit/M])))
        paths (lbm/forward-paths battlefield unit-cube hexes)
        cube->enders (lbm/paths->enders battlefield unit-cube paths)
        pointer->cube->tweeners (lbm/paths->tweeners battlefield unit-cube cube->enders paths)
        pointer->events (paths-events battlefield unit-cube paths)]
    {:cube->enders cube->enders
     :pointer->cube->tweeners pointer->cube->tweeners
     :pointer->events pointer->events}))
