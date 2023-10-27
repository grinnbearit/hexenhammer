(ns hexenhammer.logic.battlefield.movement
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.mover :as lem]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :as lbu]))


(defn valid-move?
  "Returns true if the unit at `unit-cube` can have pointer as an interim part of movement"
  [battlefield unit-cube pointer]
  (let [new-bf (lbu/remove-unit battlefield unit-cube)
        entity (new-bf (:cube pointer))]
    (let/passable? entity)))


(defn valid-end?
  "Returns true if the unit at `unit-cube` can end its movement at `pointer`"
  [battlefield unit-cube pointer]
  (let [new-bf (lbu/move-unit battlefield unit-cube pointer)]
    (not (lbu/engaged? new-bf (:cube pointer)))))


(defn reform-paths
  "Returns all reform paths, each consisting of two steps
  start is the starting pointer"
  [battlefield unit-cube]
  (let [start (lbu/unit-pointer battlefield unit-cube)]
    (for [option (disj #{:n :ne :se :s :sw :nw} (:facing start))
          :let [end (lc/->Pointer unit-cube option)]
          :when (valid-end? battlefield unit-cube end)]
      [start end])))


(defn forward-step
  "Given a pointer, returns a set of pointers reachable in a single forward step
  The order of pointers in the list determines the priority"
  [pointer]
  (let [facing->pivots {:n [:nw :ne] :ne [:n :se] :se [:ne :s]
                        :s [:se :sw] :sw [:s :nw] :nw [:sw :n]}
        [lp rp] (facing->pivots (:facing pointer))
        forward-cube (lc/step (:cube pointer) (:facing pointer))]
    [(lc/->Pointer (:cube pointer) lp)
     (lc/->Pointer (:cube pointer) rp)
     (lc/->Pointer forward-cube (:facing pointer))
     (lc/->Pointer forward-cube lp)
     (lc/->Pointer forward-cube rp)]))


(defn forward-paths
  "Returns all sequences of steps reachable in hexes from the passed pointer,
  does not include duplicate paths to the same pointer
  removes paths that end as engaged
  prioritises by shortest path and the order in `forward-step`"
  [battlefield unit-cube hexes]
  (let [start (lbu/unit-pointer battlefield unit-cube)]

    (loop [queue (conj (clojure.lang.PersistentQueue/EMPTY) [start])
           paths []
           seen #{start}]

      (if (empty? queue)
        (rest paths)

        (let [path (peek queue)
              pointer (peek path)]

          (cond (and (= (inc hexes) (count path))
                     (valid-end? battlefield unit-cube pointer))
                (recur (pop queue) (conj paths path) seen)

                (= (inc hexes) (count path))
                (recur (pop queue) paths seen)

                :else
                (let [steps (->> (forward-step pointer)
                                 (filter #(valid-move? battlefield unit-cube %))
                                 (remove seen))
                      next-queue (->> (map #(conj path %) steps)
                                      (into (pop queue)))
                      next-seen (into seen steps)]

                  (if (valid-end? battlefield unit-cube pointer)
                    (recur next-queue (conj paths path) next-seen)
                    (recur next-queue paths next-seen)))))))))


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
  [battlefield unit-cube]
  (let [paths (reform-paths battlefield unit-cube)
        cube->enders (paths->enders battlefield unit-cube paths)]
    {:cube->enders cube->enders}))


(defn forward
  [battlefield unit-cube]
  (let [hexes (lc/hexes (get-in battlefield [unit-cube :unit/M]))
        paths (forward-paths battlefield unit-cube hexes)
        cube->enders (paths->enders battlefield unit-cube paths)]
    {:cube->enders cube->enders}))
