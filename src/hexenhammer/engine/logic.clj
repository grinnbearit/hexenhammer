(ns hexenhammer.engine.logic
  (:require [hexenhammer.cube :as cube]
            [hexenhammer.engine.component :as component]
            [clojure.set :as set]))


(defn M->hexes
  "Converts a move stat to a number of hexes"
  [M]
  (Math/round (float (/ M 3))))


(defn make-pivots
  "Given a shadow, returns the set of `shadow` pivots reacheable in a single hex step"
  [shadow]
  (let [pivot->facings {:n [:nw :ne]
                        :ne [:n :se]
                        :se [:ne :s]
                        :s [:se :sw]
                        :sw [:s :nw]
                        :nw [:sw :n]}]

    (set
     (for [facing (pivot->facings (:shadow/facing shadow))]
       (component/gen-shadow (:shadow/cube shadow) facing)))))


(defn terrain?
  [component]
  (= (:hexenhammer/class component) :terrain))


(defn make-forward-step
  "Given a battlefield and a shadow, returns a set of `shadows` reachable in a single hex step"
  [battlefield shadow]
  (let [forward-cube (cube/step (:shadow/cube shadow) (:shadow/facing shadow))]

    (if (and (contains? battlefield forward-cube)
             (terrain? (battlefield forward-cube)))
      (let [forward-shadow (component/gen-shadow forward-cube (:shadow/facing shadow))]
        (set/union (make-pivots shadow)
                   (make-pivots forward-shadow)
                   #{forward-shadow}))
      (make-pivots shadow))))


(defn make-forward-steps
  "Given a battlefield, shadow and a number of hexes, returns a set of `shadows` reachable"
  [battlefield shadow hexes]
  (if (zero? hexes)
    #{shadow}
    (->> (for [shadow-node (make-forward-steps battlefield shadow (dec hexes))]
           (make-forward-step battlefield shadow-node))
         (apply set/union #{shadow}))))
