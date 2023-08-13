(ns hexenhammer.view.entity
  (:require [hexenhammer.view.svg :as svg]))


(defmulti render :entity/class)


(defn render-terrain
  "Generates a base, selectable svg terrain entity"
  [terrain]
  (cond-> (-> (svg/hexagon)
              (svg/add-classes ["terrain"]))

    (= :selected (:entity/presentation terrain))
    (svg/add-classes ["selected"])

    (= :highlighted (:entity/presentation terrain))
    (svg/add-classes ["highlighted"])))


(defmethod render :terrain
  [terrain]
  (cond-> (-> (render-terrain terrain)
              (svg/translate (:entity/cube terrain)))

    (= :selectable (:entity/interaction terrain))
    (svg/selectable (:entity/cube terrain))))


(defmethod render :unit
  [unit]
  (let [int->roman ["0" "i" "ii" "iii" "iv" "v" "vi" "vii" "viii" "ix" "x"]]
    (cond-> (-> [:g {}
                 (render-terrain unit)
                 (-> [:g {}
                      (-> (svg/hexagon)
                          (svg/add-classes ["unit" (str "player-" (:unit/player unit))]))
                      (svg/chevron (:unit/facing unit))
                      (svg/text (:entity/name unit) -1)
                      (svg/text (int->roman (:unit/id unit)) 2)]
                     (svg/scale 9/10))]

                (svg/translate (:entity/cube unit)))

      (= :selectable (:entity/interaction unit))
      (svg/selectable (:entity/cube unit)))))


(defmethod render :mover
  [mover]
  (-> [:g {}
       (render-terrain mover)
       (-> [:g {}
            (cond-> (-> (svg/hexagon)
                        (svg/add-classes ["mover" (str "player-" (:unit/player mover))]))

              (= :selected (:entity/presentation mover))
              (svg/add-classes ["selected"]))

            (for [option (:mover/options mover)
                  :when (not= option (:mover/marked mover))]
              (-> (svg/arrow option)
                  (svg/add-classes ["arrow"])
                  (svg/movable (:entity/cube mover) option)))
            (when-let [marked (:mover/marked mover)]
              (-> (svg/arrow marked)
                  (svg/add-classes ["arrow" "marked"])))]

           (svg/scale 9/10))]

      (svg/translate (:entity/cube mover))))
