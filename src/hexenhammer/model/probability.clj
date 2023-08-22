(ns hexenhammer.model.probability)


(def ROLL-2D6
  (-> (for [d1 (range 1 7)
            d2 (range 1 7)]
        (+ d1 d2))
      (frequencies)
      (update-vals #(/ % 36))))


(defn march
  [Ld]
  (->> (map ROLL-2D6 (range 2 (inc Ld)))
       (apply +)))
