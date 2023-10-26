(ns hexenhammer.logic.battlefield.movement
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.mover :as lem]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]))


(defn valid-end?
  "Returns true if the unit at `unit-cube` can end its movement at `pointer`"
  [battlefield unit-cube pointer]
  (let [new-bf (lbu/move-unit battlefield unit-cube pointer)]
    (not (lbu/engaged? new-bf (:cube pointer)))))


(defn reform-paths
  "Returns all reform paths, each consisting of two steps
  start is the starting pointer"
  [battlefield unit-cube]
  (let [facing (get-in battlefield [unit-cube :unit/facing])
        start (lc/->Pointer unit-cube facing)]
    (for [option (disj #{:n :ne :se :s :sw :nw} facing)
          :let [end (lc/->Pointer unit-cube option)]
          :when (valid-end? battlefield unit-cube end)]
      [start end])))


(defn paths->enders
  "Given a battlefield, player and path-list returns a new map of cube->mover for each end cube"
  [battlefield unit-cube paths]
  (let [player (get-in battlefield [unit-cube :unit/player])
        new-bf (lbu/remove-unit battlefield unit-cube)]

    (letfn [(collect-facings [mover-acc pointer]
              (update mover-acc (:cube pointer) (fnil conj #{}) (:facing pointer)))]

      (->> (for [[cube options] (reduce collect-facings {} (map peek paths))
                 :let [terrain (new-bf cube)
                       mover (lem/gen-mover player :options options)]]
             [cube (let/place terrain mover)])
           (into {})))))


(defn reform
  "Returns,"
  [battlefield unit-cube]
  (let [paths (reform-paths battlefield unit-cube)
        cube->enders (paths->enders battlefield unit-cube paths)]
    {:cube->enders cube->enders}))
