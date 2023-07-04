(ns hexenhammer.engine.logic
  (:require [hexenhammer.cube :as cube]
            [hexenhammer.engine.component :as component]
            [clojure.set :as set]))


(defn M->hexes
  "Converts a move stat to a number of hexes"
  [M]
  (Math/round (float (/ M 3))))


(defn show-pivots
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


(defn show-forward-step
  "Given a battlefield and a shadow, returns a set of `shadows` reachable in a single hex step"
  [battlefield shadow]
  (let [forward-cube (cube/step (:shadow/position shadow) (:shadow/facing shadow))]

    (if (and (contains? battlefield forward-cube)
             (terrain? (battlefield forward-cube)))
      (let [forward-shadow (component/gen-shadow (:shadow/player shadow)
                                                 forward-cube
                                                 (:shadow/facing shadow))]
        (set/union (show-pivots shadow)
                   (show-pivots forward-shadow)
                   #{forward-shadow}))
      (show-pivots shadow))))


(defn show-forward-steps
  "Given a battlefield, shadow and a number of hexes, returns a set of `shadows` reachable"
  [battlefield shadow hexes]
  (if (zero? hexes)
    #{shadow}
    (->> (for [shadow-node (show-forward-steps battlefield shadow (dec hexes))]
           (show-forward-step battlefield shadow-node))
         (apply set/union #{shadow}))))


(defn shadow-enemies?
  "Returns true if the shadow and the unit are owned by different players"
  [unit shadow]
  (not= (:shadow/player shadow)
        (:unit/player unit)))


(defn shadow-engaged?
  "Returns true if the shadow and the unit are engaged"
  [unit shadow]
  (and (shadow-enemies? unit shadow)
       (or (contains? (set (cube/forward-arc (:unit/position unit) (:unit/facing unit)))
                      (:shadow/position shadow))
           (contains? (set (cube/forward-arc (:shadow/position shadow) (:shadow/facing shadow)))
                      (:unit/position unit)))))


(defn unit?
  [component]
  (= (:hexenhammer/class component) :unit))


(defn shadow-battlefield-engaged?
  "Returns true if the passed shadow is engaged to any units on the battlefield"
  [battlefield shadow]
  (->> (for [cube (cube/neighbours (:shadow/position shadow))]
         (and (contains? battlefield cube)
              (unit? (battlefield cube))
              (shadow-engaged? (battlefield cube) shadow)))
       (some true?)
       (boolean)))


(defn show-move-unit-forwards
  "Given a battlefield and a unit, returns a list of shadows reachable by that unit"
  [battlefield unit]
  (let [hexes (M->hexes (:unit/M unit))
        unit-shadow (component/gen-shadow (:unit/player unit)
                                          (:unit/position unit)
                                          (:unit/facing unit))]
    (->> (disj (show-forward-steps battlefield unit-shadow hexes) unit-shadow)
         (remove (partial shadow-battlefield-engaged? battlefield)))))


(defn enemies?
  "Returns true if the units are owned by different players"
  [unit-1 unit-2]
  (not= (:unit/player unit-1)
        (:unit/player unit-2)))


(defn engaged?
  "Returns true if the two units are engaged"
  [unit-1 unit-2]
  (and (enemies? unit-1 unit-2)
       (or (contains? (set (cube/forward-arc (:unit/position unit-1) (:unit/facing unit-1)))
                      (:unit/position unit-2))
           (contains? (set (cube/forward-arc (:unit/position unit-2) (:unit/facing unit-2)))
                      (:unit/position unit-1)))))


(defn battlefield-engaged?
  "Returns true if the passed unit is engaged to any units on the battlefield"
  [battlefield unit]
  (->> (for [cube (cube/neighbours (:unit/position unit))]
         (and (contains? battlefield cube)
              (unit? (battlefield cube))
              (engaged? (battlefield cube) unit)))
       (some true?)
       (boolean)))


(defn movable?
  "Given a battlefield and a unit, returns true if the unit is allowed to move"
  [battlefield unit]
  (not (battlefield-engaged? battlefield unit)))
