(ns hexenhammer.model.logic.movement
  (:require [hexenhammer.model.logic.core :as mlc]
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
  "Given a cube, returns a mover with the set of allowed facings
  selecting the unit's facing"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (me/gen-mover cube (:unit/player unit)
                  :selected (:unit/facing unit)
                  :options (reform-facings battlefield cube)
                  :presentation :selected)))


(defn forward-step
  "Given a pointer, returns a set of pointers reachable in a single forward step"
  [pointer]
  (let [facing->pivots {:n [:nw :ne] :ne [:n :se] :se [:ne :s]
                        :s [:se :sw] :sw [:s :nw] :nw [:sw :n]}
        [lp rp] (facing->pivots (:facing pointer))
        forward-cube (mc/step (:cube pointer) (:facing pointer))]
    #{(mc/->Pointer (:cube pointer) lp)
      (mc/->Pointer (:cube pointer) rp)
      (mc/->Pointer forward-cube (:facing pointer))
      (mc/->Pointer forward-cube lp)
      (mc/->Pointer forward-cube rp)}))


(defn valid-pointer?
  "Returns true if the pointer is on the battlefield and on a terrain hex"
  [battlefield pointer]
  (let [cube (:cube pointer)]
    (and (contains? battlefield cube)
         (= :terrain (get-in battlefield [cube :entity/class])))))


(defn forward-steps
  "Returns all sequences of steps reachable in hexes from the passed pointer
  seen is a set containing all pointers that shouldn't be included in paths"
  [battlefield seen pointer hexes]
  (if (zero? hexes)
    [()]
    (let [steps (forward-step pointer)]
      (for [next-pointer steps
            :when (and (not (seen next-pointer))
                       (valid-pointer? battlefield next-pointer))
            next-path (forward-steps battlefield (set/union seen steps)
                                     next-pointer (dec hexes))]
        (conj next-path next-pointer)))))


(defn forward-paths
  "A untility wrapper for forward-steps adds the starting pointer to every path"
  [battlefield pointer hexes]
  (for [path (forward-steps battlefield #{} pointer hexes)]
    (vec (conj path pointer))))


(defn paths->mover-map
  "Given a player and a list of paths, returns a map of cube -> mover, removes invalid options"
  [battlefield player paths]
  (letfn [(reducer [mover-acc pointer]
            (let [cube (:cube pointer)
                  shadow (pointer->shadow player pointer)
                  shadow-battlefield (assoc battlefield cube shadow)]
              (if (not (mlc/battlefield-engaged? shadow-battlefield cube))
                (update mover-acc cube (fnil conj #{}) (:facing pointer))
                mover-acc)))]

    (->> (for [[cube options] (reduce reducer {} (apply concat paths))]
           [cube (me/gen-mover cube player :options options :state :future)])
         (into {}))))


(defn compress-path
  "Returns a subset of path containing only the last pointer for every cube in path
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


(defn paths->breadcrumbs-map
  "combines all compressed maps for all paths into a single breadcrumb map,"
  [player mover-map paths]
  (letfn [(pointer->breadcrumb [pointer]
            (if-let [mover (mover-map (:cube pointer))]
              (assoc mover
                     :mover/highlighted (:facing pointer)
                     :mover/state :past)
              (me/gen-mover (:cube pointer) player
                            :highlighted (:facing pointer)
                            :state :past)))]

    (->> (for [[pointer path] (->> (map path->compressed-map paths)
                                   (apply merge))]
           [pointer (map pointer->breadcrumb path)])
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
  "Given a battlefield and cube, returns
  :mover-map,  cube->mover that the unit can reach, selects the original position
  :breadcrumb-map, pointer->cube->mover that the unit needs to pass through to reach the mover"
  [battlefield cube]
  (let [unit (battlefield cube)
        new-battlefield (remove-unit battlefield cube)
        start (mc/->Pointer cube (:unit/facing unit))
        hexes (M->hexes (:unit/M unit))
        paths (forward-paths new-battlefield start hexes)
        mover-map (paths->mover-map new-battlefield (:unit/player unit) paths)
        breadcrumbs-map (paths->breadcrumbs-map (:unit/player unit) mover-map paths)]
    {:moves (update mover-map cube assoc
                    :mover/selected (:unit/facing unit)
                    :mover/state :present
                    :entity/presentation :selected)
     :breadcrumbs breadcrumbs-map}))
