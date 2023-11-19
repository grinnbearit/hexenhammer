(ns hexenhammer.logic.probability
  (:require [hexenhammer.logic.cube :as lc]))


(def ROLL-2D6
  (-> (for [d1 (range 1 7)
            d2 (range 1 7)]
        (+ d1 d2))
      (frequencies)
      (update-vals #(/ % 36))))


(def ADVANTAGE-2D6
  (-> (for [d1 (range 1 7)
            d2 (range 1 7)]
        (max d1 d2))
      (frequencies)
      (update-vals #(/ % 36))))


(defn march
  [Ld]
  (->> (map ROLL-2D6 (range 2 (inc Ld)))
       (apply +)))


(defn charge
  ([M]
   (let [hex-probs (->> (for [[roll prob] ADVANTAGE-2D6]
                          {(lc/hexes (+ M roll)) prob})
                        (apply merge-with +))

         min-hexes (lc/hexes (+ M 1))
         max-hexes (lc/hexes (+ M 6))]

     (->> (for [hexes (range min-hexes (inc max-hexes))]
            [hexes (->> (range hexes (inc max-hexes))
                        (map hex-probs)
                        (apply +))])
          (into {}))))

  ([M distance]
   (if (<= distance (lc/hexes (+ 1 M)))
     1
     (get (charge M) distance 0))))


(def FLEE
  (->> (for [[roll prob] ROLL-2D6]
         {(lc/hexes roll) prob})
       (apply merge-with +)))
