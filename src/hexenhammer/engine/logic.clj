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
       (component/gen-shadow (:shadow/player shadow) (:shadow/position shadow) facing)))))


(defn terrain?
  [component]
  (= (:hexenhammer/class component) :terrain))


(defn make-forward-step
  "Given a battlefield and a shadow, returns a set of `shadows` reachable in a single hex step"
  [battlefield shadow]
  (let [forward-cube (cube/step (:shadow/position shadow) (:shadow/facing shadow))]

    (if (and (contains? battlefield forward-cube)
             (terrain? (battlefield forward-cube)))
      (let [forward-shadow (component/gen-shadow (:shadow/player shadow)
                                                 forward-cube
                                                 (:shadow/facing shadow))]
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


(defn enemies?
  "Returns true if the shadow and the unit are owned by different players"
  [shadow unit]
  (not= (:shadow/player shadow)
        (:unit/player unit)))


(defn engaged?
  "Returns true if the shadow and the unit are engaged"
  [shadow unit]
  (and (enemies? shadow unit)
       (or (contains? (set (cube/forward-arc (:unit/position unit) (:unit/facing unit)))
                      (:shadow/position shadow))
           (contains? (set (cube/forward-arc (:shadow/position shadow) (:shadow/facing shadow)))
                      (:unit/position unit)))))


(defn unit?
  [component]
  (= (:hexenhammer/class component) :unit))


(defn engaged-battlefield?
  "Returns true if the passed shadow is engaged to any units on the battlefield"
  [battlefield shadow]
  (->> (for [cube (cube/neighbours (:shadow/position shadow))]
         (and (contains? battlefield cube)
              (unit? (battlefield cube))
              (engaged? shadow (battlefield cube))))
       (some true?)
       (boolean)))


(defn move-unit-forwards
  [battlefield unit]
  (let [hexes (M->hexes (:unit/M unit))
        unit-shadow (component/gen-shadow (:unit/player unit)
                                          (:unit/position unit)
                                          (:unit/facing unit))]
    (->> (disj (make-forward-steps battlefield unit-shadow hexes) unit-shadow)
         (remove (partial engaged? battlefield)))))
