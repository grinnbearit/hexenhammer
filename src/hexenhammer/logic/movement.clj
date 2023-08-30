(ns hexenhammer.logic.movement
  (:require [hexenhammer.logic.core :as l]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.cube :as mc]
            [clojure.set :as set]))


(defn reform-facings
  "Given a cube, returns a set of allowed facings after a reform,
  includes the original facing"
  [battlefield cube]
  (set
   (for [facing #{:n :ne :se :s :sw :nw}
         :let [pointer (mc/->Pointer cube facing)
               shadow-battlefield (l/move-unit battlefield cube pointer)]
         :when (not (l/battlefield-engaged? shadow-battlefield cube))]
     facing)))


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


(defn forward-paths
  "Returns all sequences of steps reachable in hexes from the passed pointer,
  does not include duplicate paths to the same pointer
  removes paths that end as engaged
  prioritises by shortest path and the order in `forward-step`"
  [battlefield start hexes]
  (loop [queue (conj (clojure.lang.PersistentQueue/EMPTY) [start])
         paths []
         seen #{start}]

    (if (empty? queue)
      paths

      (let [path (peek queue)
            pointer (peek path)]

        (cond (and (= (inc hexes) (count path))
                   (l/valid-end? battlefield (:cube start) pointer))
              (recur (pop queue) (conj paths path) seen)

              (= (inc hexes) (count path))
              (recur (pop queue) paths seen)

              :else
              (let [steps (->> (forward-step pointer)
                               (filter #(l/valid-move? battlefield (:cube start) %))
                               (remove seen))
                    next-queue (->> (map #(conj path %) steps)
                                    (into (pop queue)))
                    next-seen (into seen steps)]

                (if (l/valid-end? battlefield (:cube start) pointer)
                  (recur next-queue (conj paths path) next-seen)
                  (recur next-queue paths next-seen))))))))


(defn reposition-paths
  "returns all valid repositioning paths given the starting pointer and a number of hexes
  removes paths that end as engaged"
  [battlefield start hexes]
  (->> (for [facing (disj #{:n :ne :se :s :sw :nw} (:facing start))
             :let [steps (->> (iterate #(mc/step % facing) (:cube start))
                              (drop 1)
                              (map #(mc/->Pointer % (:facing start)))
                              (take-while #(l/valid-move? battlefield (:cube start) %))
                              (take hexes)
                              (cons start)
                              (vec))]
             path-length (range 2 (+ 2 hexes))
             :when (<= path-length (count steps))
             :let [path (subvec steps 0 path-length)]
             :when (l/valid-end? battlefield (:cube start) (peek path))]
         path)
       (cons [start])))


(defn show-battlemap
  "Given a battlefield, player and path-map returns a new battlemap with the movers representing those paths"
  [battlefield player paths]
  (letfn [(reducer [mover-acc pointer]
            (update mover-acc (:cube pointer) (fnil conj #{}) (:facing pointer)))]

    (->> (for [[cube options] (reduce reducer {} (map peek paths))]
           [cube (-> (me/gen-mover cube player :options options)
                     (lt/swap (battlefield cube)))])
         (into {}))))


(defn M->hexes
  [M]
  (Math/round (float (/ M 3))))


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


(defn path->breadcrumbs
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


(defn show-breadcrumbs
  "returns a map of pointer->breadcrumbs for all paths"
  [battlefield battlemap player paths]
  (->> (for [path paths]
         [(peek path) (path->breadcrumbs battlefield battlemap player path)])
       (into {})))


(defn show-moves
  "Given a battlefield, cube, hexes and a path-fn returns
  :battlemap, cube->mover that the unit can reach when moving
  :path-map, pointer->cube->path that the unit needs to pass through to reach the pointer"
  [battlefield cube hexes path-fn]
  (let [unit (battlefield cube)
        start (mc/->Pointer cube (:unit/facing unit))
        paths (path-fn battlefield start hexes)
        battlemap (show-battlemap battlefield (:unit/player unit) paths)
        breadcrumbs (show-breadcrumbs battlefield battlemap (:unit/player unit) paths)]
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
          :when (and (le/unit? entity)
                     (l/enemies? unit entity))]
      neighbour)))


(defn show-threats
  "Returns a battlemap of cubes that 'threaten' this unit on the battlefield"
  [battlefield cube]
  (->> (for [threat (list-threats battlefield cube)]
         [threat (assoc (battlefield threat) :entity/state :marked)])
       (into {})))
