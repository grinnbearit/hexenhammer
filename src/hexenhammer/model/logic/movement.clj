(ns hexenhammer.model.logic.movement
  (:require [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.cube :as mc]
            [clojure.set :as set]))


(defrecord Pointer [cube facing])


(defn reform-facings
  "Given a cube, returns a set of allowed facings after a reform,
  includes the original facing"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (set
     (for [facing #{:n :ne :se :s :sw :nw}
           :let [shadow (assoc unit :unit/facing facing)
                 new-battlefield (assoc battlefield cube shadow)]
           :when (not (mlc/battlefield-engaged? new-battlefield cube))]
       facing))))


(defn show-reform
  "Given a cube, returns a mover with the set of allowed facings
  selecting the unit's facing"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (me/gen-mover cube (:unit/player unit)
                  :marked (:unit/facing unit)
                  :options (reform-facings battlefield cube)
                  :presentation :selected)))


(defn forward-step
  "Given a pointer, returns a set of pointers reachable in a single forward step"
  [pointer]
  (let [facing->pivots {:n [:nw :ne] :ne [:n :se] :se [:ne :s]
                        :s [:se :sw] :sw [:s :nw] :nw [:sw :n]}
        [lp rp] (facing->pivots (:facing pointer))
        forward-cube (mc/step (:cube pointer) (:facing pointer))]
    #{(->Pointer (:cube pointer) lp)
      (->Pointer (:cube pointer) rp)
      (->Pointer forward-cube (:facing pointer))
      (->Pointer forward-cube lp)
      (->Pointer forward-cube rp)}))


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
  "A untility wrapper for forward-steps, mocks out the moving unit and
  includes the starting pointer in every path"
  [battlefield pointer hexes]
  (for [cube (:cube pointer)
        shadow-terrain (me/gen-terrain cube)
        shadow-battlefield (assoc battlefield cube shadow-terrain)
        path (forward-steps shadow-battlefield #{} pointer hexes)]
    (conj path pointer)))


(defn forward-paths
  "A untility wrapper for forward-steps, mocks out the moving unit and
  includes the starting pointer in every path"
  [battlefield pointer hexes]
  (let [cube (:cube pointer)
        shadow-terrain (me/gen-terrain cube)
        shadow-battlefield (assoc battlefield cube shadow-terrain)]
    (for [path (forward-steps shadow-battlefield #{} pointer hexes)]
      (conj path pointer))))


(defn M->hexes
  [M]
  (Math/round (float (/ M 3))))


(defn collect-facings
  "Given a collection of pointers, groups their facings by cube"
  [pointers]
  (letfn [(reducer [cube->facings pointer]
            (update cube->facings (:cube pointer) (fnil conj #{}) (:facing pointer)))]

    (reduce reducer {} pointers)))


(defn show-moves
  "Given a cube, returns a map of cube->mover that the unit
  can reach, selects the original position"
  [battlefield cube]
  (let [unit (battlefield cube)
        start (->Pointer cube (:unit/facing unit))
        hexes (M->hexes (:unit/M unit))
        pointers (->> (forward-paths battlefield start hexes)
                      (apply concat))]
    (->> (for [[pointer-cube pointer-facings] (collect-facings pointers)]
           [pointer-cube
            (cond-> (me/gen-mover pointer-cube (:unit/player unit) :options pointer-facings)

              (= cube pointer-cube)
              (assoc :mover/marked (:unit/facing unit)
                     :entity/presentation :selected))])
         (into {}))))
