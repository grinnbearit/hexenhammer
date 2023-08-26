(ns hexenhammer.logic.movement
  (:require [hexenhammer.logic.core :as lc]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
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
           :when (not (lc/battlefield-engaged? shadow-battlefield cube))]
       facing))))


(defn show-reform
  "Given a cube, returns a battlemap with the set of allowed facings"
  [battlefield cube]
  (let [unit (battlefield cube)]
    {cube (-> (me/gen-mover cube (:unit/player unit)
                            :options (reform-facings battlefield cube))
              (lt/swap unit))}))


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
  "Returns true if the pointer is on the battlefield and not on an impassable hex"
  [battlefield pointer]
  (let [cube (:cube pointer)]
    (and (contains? battlefield cube)
         (lt/passable? (battlefield cube)))))


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
  [battlefield start hexes]
  (for [facing (disj #{:n :ne :se :s :sw :nw} (:facing start))]
    (->> (iterate #(mc/step % facing) (:cube start))
         (drop 1)
         (map #(mc/->Pointer % (:facing start)))
         (take-while #(valid-pointer? battlefield %))
         (take hexes)
         (cons start)
         (vec))))


(defn paths->path-map
  "Given a vector of paths, returns a map of pointer->subpath for every path in paths"
  [paths]
  (->> (for [path paths
             n (range (count path))
             :let [subpath (subvec path 0 (inc n))]]
         [(peek subpath) subpath])
       (into {})))


(defn path-map->battlemap
  "Given a player and path map returns a new battlemap with the movers representing those paths
  removes invalid pointers from paths"
  [battlefield player path-map]
  (letfn [(reducer [mover-acc pointer]
            (let [cube (:cube pointer)
                  shadow (pointer->shadow player pointer)
                  shadow-battlefield (assoc battlefield cube shadow)]
              (if (not (lc/battlefield-engaged? shadow-battlefield cube))
                (update mover-acc cube (fnil conj #{}) (:facing pointer))
                mover-acc)))]

    (->> (for [[cube options] (reduce reducer {} (apply concat (vals path-map)))]
           [cube (-> (me/gen-mover cube player :options options)
                     (lt/swap (battlefield cube)))])
         (into {}))))


(defn M->hexes
  [M]
  (Math/round (float (/ M 3))))


(defn remove-unit
  "Returns a new battlefield with the unit at cube removed"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (assoc battlefield cube (lt/pickup unit))))


(defn show-moves
  "Given a battlefield, cube, hexes and a path-fn returns
  :battlemap, cube->mover that the unit can reach when moving
  :path-map, pointer->cube->path that the unit needs to pass through to reach the pointer"
  [battlefield cube hexes path-fn]
  (let [unit (battlefield cube)
        new-battlefield (remove-unit battlefield cube)
        start (mc/->Pointer cube (:unit/facing unit))
        paths (path-fn new-battlefield start hexes)
        path-map (paths->path-map paths)
        battlemap (path-map->battlemap new-battlefield (:unit/player unit) path-map)]
    {:battlemap battlemap
     :path-map path-map}))


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


(defn show-breadcrumbs
  "Given a path, returns the battlemap of breadcrumbs representing it"
  [battlefield battlemap player path]
  (->> (for [pointer (compress-path path)
             :let [cube (:cube pointer)]]
         [cube
          (if-let [mover (battlemap cube)]
            (assoc mover
                   :mover/highlighted (:facing pointer)
                   :mover/state :past)
            (-> (me/gen-mover (:cube pointer) player
                              :highlighted (:facing pointer)
                              :state :past)
                (lt/swap (battlefield cube))))])
       (into {})))


(defn list-threats
  "Returns a list of cubes that 'threaten' this unit on the battlefield
  i.e. enemy units within 3 hexes
  assumes the passed cube is on the battlefield and a unit"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (for [neighbour (mc/neighbours-within cube 3)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (le/unit? entity)
                     (lc/enemies? unit entity))]
      neighbour)))


(defn show-threats
  "Returns a battlemap of cubes that 'threaten' this unit on the battlefield"
  [battlefield cube]
  (->> (for [threat (list-threats battlefield cube)]
         [threat (assoc (battlefield threat) :entity/state :marked)])
       (into {})))
