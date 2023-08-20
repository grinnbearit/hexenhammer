(ns hexenhammer.model.logic.movement
  (:require [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.logic.entity :as mle]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.cube :as mc]
            [clojure.set :as set]))


(defn pointer->shadow
  "Takes a player and a pointer and returns a shadow unit with that cube and facing"
  [player pointer]
  (me/gen-shadow (:cube pointer) player (:facing pointer)))


(defn reform-facings
  "Given a cube, returns a set of allowed facings after a reform,
  includes the original facing"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (set
     (for [facing #{:n :ne :se :s :sw :nw}
           :let [pointer (mc/->Pointer cube facing)
                 shadow (pointer->shadow (:unit/player unit) pointer)
                 shadow-battlefield (assoc battlefield cube shadow)]
           :when (not (mlc/battlefield-engaged? shadow-battlefield cube))]
       facing))))


(defn show-reform
  "Given a cube, returns a battlemap with the set of allowed facings"
  [battlefield cube]
  (let [unit (battlefield cube)]
    {cube (me/gen-mover cube (:unit/player unit) :options (reform-facings battlefield cube))}))


(defn forward-step
  "Given a pointer, returns a set of pointers reachable in a single forward step
  The order of pointers in the list determines the priority"
  [pointer]
  (let [facing->pivots {:n [:nw :ne] :ne [:n :se] :se [:ne :s]
                        :s [:se :sw] :sw [:s :nw] :nw [:sw :n]}
        [lp rp] (facing->pivots (:facing pointer))
        forward-cube (mc/step (:cube pointer) (:facing pointer))]
    [(mc/->Pointer (:cube pointer) lp)
     (mc/->Pointer (:cube pointer) rp)
     (mc/->Pointer forward-cube (:facing pointer))
     (mc/->Pointer forward-cube lp)
     (mc/->Pointer forward-cube rp)]))


(defn valid-pointer?
  "Returns true if the pointer is on the battlefield and on a terrain hex"
  [battlefield pointer]
  (let [cube (:cube pointer)]
    (and (contains? battlefield cube)
         (mle/terrain? (battlefield cube)))))


(defn forward-paths
  "Returns all sequences of steps reachable in hexes from the passed pointer,
  does not include duplicate paths to the same pointer
  prioritises by shortest path and the order in `forward-steps`"
  [battlefield start hexes]
  (loop [queue (conj (clojure.lang.PersistentQueue/EMPTY) [start])
         paths []
         seen #{start}]

    (cond (empty? queue)
          paths

          (= (inc hexes) (count (peek queue)))
          (recur (pop queue)
                 (conj paths (peek queue))
                 seen)

          :else
          (let [path (peek queue)
                pointer (peek path)
                steps (->> (forward-step pointer)
                           (filter #(valid-pointer? battlefield %))
                           (remove seen))]

            (if (empty? steps)
              (recur (pop queue) (conj paths path) seen)
              (recur (->> (map #(conj path %) steps)
                          (into (pop queue)))
                     paths
                     (into seen steps)))))))


(defn reposition-paths
  "returns all valid repositioning paths given the starting pointer and a number of hexes
  ensures the returned paths are vectors"
  [battlefield pointer hexes]
  (for [facing (disj #{:n :ne :se :s :sw :nw} (:facing pointer))]
    (->> (iterate #(mc/step % facing) (:cube pointer))
         (drop 1)
         (map #(mc/->Pointer % (:facing pointer)))
         (take-while #(valid-pointer? battlefield %))
         (take hexes)
         (cons pointer)
         (vec))))


(defn paths->battlemap
  "Given a player and a list of paths returns a new battlemap with the movers representing those paths
  removes invalid pointers from paths"
  [battlefield player paths]
  (letfn [(reducer [mover-acc pointer]
            (let [cube (:cube pointer)
                  shadow (pointer->shadow player pointer)
                  shadow-battlefield (assoc battlefield cube shadow)]
              (if (not (mlc/battlefield-engaged? shadow-battlefield cube))
                (update mover-acc cube (fnil conj #{}) (:facing pointer))
                mover-acc)))]

    (->> (for [[cube options] (reduce reducer {} (apply concat paths))]
           [cube (me/gen-mover cube player :options options)])
         (into {}))))


(defn compress-path
  "Returns a subset of path containing only the last pointer for every cube in the path
  assumes the last pointer is the end step so excludes it"
  [path]
  (letfn [(reducer [acc pointer]
            (if (= (:cube (peek acc))
                   (:cube pointer))
              (conj (pop acc) pointer)
              (conj acc pointer)))]

    (pop (reduce reducer [] path))))


(defn path->compressed-map
  "Returns a map of pointer -> compressed path where every pointer in the path
  points to the relevant compressed path"
  [path]
  (->> (for [cutoff (range 1 (inc (count path)))
             :let [sub-path (subvec path 0 cutoff)]]
         [(peek sub-path) (compress-path sub-path)])
       (into {})))


(defn paths->breadcrumbs
  "combines all compressed maps for all paths into a single breadcrumbs object
  pointer -> battlemap"
  [player mover-map paths]
  (letfn [(pointer->breadcrumb [pointer]
            [(:cube pointer)
             (if-let [mover (mover-map (:cube pointer))]
               (assoc mover
                      :mover/highlighted (:facing pointer)
                      :mover/state :past)
               (me/gen-mover (:cube pointer) player
                             :highlighted (:facing pointer)
                             :state :past))])]

    (->> (for [[pointer path] (->> (map path->compressed-map paths)
                                   (apply merge))]
           [pointer (->> (map pointer->breadcrumb path)
                         (into {}))])
         (into {}))))


(defn M->hexes
  [M]
  (Math/round (float (/ M 3))))


(defn remove-unit
  "Returns a new battlefield with the unit at cube removed"
  [battlefield cube]
  (let [terrain (me/gen-terrain cube)]
    (assoc battlefield cube terrain)))


(defn show-moves
  "Given a battlefield, cube, hexes and a path-fn returns
  :battlemap,  cube->mover that the unit can reach when moving
  :breadcrumb, pointer->cube->mover that the unit needs to pass through to reach the pointer"
  [battlefield cube hexes path-fn]
  (let [unit (battlefield cube)
        new-battlefield (remove-unit battlefield cube)
        start (mc/->Pointer cube (:unit/facing unit))
        paths (path-fn new-battlefield start hexes)
        battlemap (paths->battlemap new-battlefield (:unit/player unit) paths)
        breadcrumbs (paths->breadcrumbs (:unit/player unit) battlemap paths)]
    {:battlemap battlemap
     :breadcrumbs breadcrumbs}))


(defn show-forward
  "Given a battlefield and cube, returns
  :battlemap,  cube->mover that the unit can reach when moving forward
  :breadcrumb, pointer->cube->mover that the unit needs to pass through to reach the pointer"
  [battlefield cube]
  (let [M (get-in battlefield [cube :unit/M])
        hexes (M->hexes M)]
    (show-moves battlefield cube hexes forward-paths)))


(defn show-reposition
  "Given a battlefield and cube, returns
  :battlemap,  cube->mover that the unit can reach when repositioning
  :breadcrumb, pointer->cube->mover that the unit needs to pass through to reach the pointer"
  [battlefield cube]
  (let [M (get-in battlefield [cube :unit/M])
        hexes (M->hexes (/ M 2))]
    (show-moves battlefield cube hexes reposition-paths)))


(defn show-march
  "Given a battlefield and cube, returns
  :battlemap,  cube->mover that the unit can reach when marching
  :breadcrumb, pointer->cube->mover that the unit needs to pass through to reach the pointer"
  [battlefield cube]
  (let [M (get-in battlefield [cube :unit/M])
        hexes (M->hexes (* M 2))]
    (show-moves battlefield cube hexes forward-paths)))


(defn list-threats
  "Returns a list of cubes that 'threaten' this unit on the battlefield
  i.e. enemy units within 3 hexes
  assumes the passed cube is on the battlefield and a unit"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (for [neighbour (mc/neighbours-within cube 3)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (mle/unit? entity)
                     (mlc/enemies? unit entity))]
      neighbour)))
