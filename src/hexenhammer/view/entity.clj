(ns hexenhammer.view.entity
  (:require [hexenhammer.view.svg :as svg]))


(defmulti render :entity/class)


(defn render-terrain
  "Generates a base, selectable svg terrain entity"
  [terrain]
  (cond-> (-> (svg/hexagon)
              (svg/add-classes ["terrain"]))

    (= :selected (:entity/presentation terrain))
    (svg/add-classes ["selected"])))


(defmethod render :terrain
  [terrain]
  (cond-> (-> (render-terrain terrain)
              (svg/translate (:entity/cube terrain)))

    (= :selectable (:entity/interaction terrain))
    (svg/selectable (:entity/cube terrain))))


(defmethod render :unit
  [unit]
  (cond-> (-> [:g {}
               (render-terrain unit)
               (-> [:g {}
                    (-> (svg/hexagon)
                        (svg/add-classes ["unit" (str "player-" (:unit/player unit))]))
                    (svg/chevron (:unit/facing unit))]
                   (svg/scale 9/10))]

              (svg/translate (:entity/cube unit)))

    (= :selectable (:entity/interaction unit))
    (svg/selectable (:entity/cube unit))))
