(ns hexenhammer.logic.battlefield.movement.charge
  (:require [clojure.set :as set]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]))


(defn list-targets
  "Returns a set of visible enemy cubes to the passed unit cube"
  [battlefield cube]
  (let [unit (battlefield cube)
        max-charge (lc/hexes (+ 6 (:unit/M unit)))]
    (->> (for [viewed (lbu/field-of-view battlefield cube)
               :let [entity (battlefield viewed)]
               :when (and (leu/unit? entity)
                          (leu/enemies? unit entity)
                          (<= (lc/distance cube viewed) max-charge))]
           viewed)
         (set))))


(defn charge-step
  "Given a pointer, returns a set of pointers reachable in a single charge step
  The order of pointers in the list determines the priority
  guide is the facing of the start pointer and limits possible facings"
  [pointer guide]
  (let [facing->pivots {:n [:nw :ne] :ne [:n :se] :se [:ne :s]
                        :s [:se :sw] :sw [:s :nw] :nw [:sw :n]}
        [lp rp] (facing->pivots (:facing pointer))
        forward-cube (lc/step (:cube pointer) (:facing pointer))]
    (if (= guide (:facing pointer))
      [(lc/->Pointer (:cube pointer) lp)
       (lc/->Pointer (:cube pointer) rp)
       (lc/->Pointer forward-cube (:facing pointer))
       (lc/->Pointer forward-cube lp)
       (lc/->Pointer forward-cube rp)]
      [(lc/->Pointer (:cube pointer) guide)
       (lc/->Pointer forward-cube (:facing pointer))
       (lc/->Pointer forward-cube guide)])))


(defn charge-paths
  "Returns a map of path->targets with a charge path to every target cube
  always returns the shortest path to each target cube prioritised by the order in `charge-step`
  each shortest path to a target _could_ contain other targets that have shorter engagement paths
  but no path can end engaged to a non target"
  [battlefield unit-cube targets]
  (let [start (lbu/unit-pointer battlefield unit-cube)]

    (loop [queue (conj (clojure.lang.PersistentQueue/EMPTY) [start])
           path->targets {}
           seen #{start}
           remaining targets]

      (if (or (empty? queue)
              (empty? remaining))
        path->targets

        (let [path (peek queue)
              pointer (peek path)
              engaged (-> (lbu/move-unit battlefield unit-cube pointer)
                          (lbu/list-engaged (:cube pointer))
                          (set))
              steps (->> (charge-step pointer (:facing start))
                         (filter #(lbm/valid-move? battlefield unit-cube %))
                         (remove seen))
              next-queue (->> (map #(conj path %) steps)
                              (into (pop queue)))
              next-seen (into seen steps)]

          (if (and (set/subset? engaged targets)
                   (seq (set/intersection engaged remaining)))
            (recur next-queue
                   (assoc path->targets path engaged)
                   next-seen
                   (set/difference remaining engaged))
            (recur next-queue
                   path->targets
                   next-seen
                   remaining)))))))


(defn charger?
  "Returns true if this unit is movable and has any viable charge targets"
  [battlefield cube]
  (and (lbm/movable? battlefield cube)
       (let [targets (list-targets battlefield cube)
             paths (charge-paths battlefield cube targets)]
         (boolean (seq paths)))))
