(ns hexenhammer.model.probability
  (:require [hexenhammer.model.core :as m]))


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
                          {(m/M->hexes (+ M roll)) prob})
                        (apply merge-with +))
         min-hexes (m/M->hexes (+ M 1))
         max-hexes (m/M->hexes (+ M 6))]
     (->> (for [hexes (range min-hexes (inc max-hexes))]
            [hexes (->> (map hex-probs (range hexes (inc max-hexes)))
                        (apply +))])
          (into {}))))
  ([M distance]
   (if (<= distance (m/M->hexes (+ 1 M)))
     1
     (get (charge M) distance 0))))
