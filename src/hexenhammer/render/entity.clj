(ns hexenhammer.render.entity
  (:require [hexenhammer.render.bit :as rb]
            [hexenhammer.render.svg :as rs]))


(defn render-base
  "Generates a base for an entity"
  [terrain]
  (let [type-str (name (:terrain/type terrain))
        presentation-str (name (:entity/presentation terrain))]
    (-> (rs/hexagon)
        (rs/add-classes ["terrain" type-str presentation-str]))))


(defmulti render (fn [entity phase cube] (:entity/class entity)))


(defmethod render :terrain
  [terrain phase cube]
  (-> (render-base terrain)
      (rs/translate cube)
      (rs/if-selectable (:entity/presentation terrain) phase cube)))


(defmethod render :unit
  [unit phase cube]
  (let [base (merge (:unit/terrain unit)
                    (select-keys unit [:entity/presentation]))]
    (-> [:g {}
         (render-base base)
         (-> [:g {}
              (-> (rs/hexagon)
                  (rs/add-classes ["unit" (str "player-" (:unit/player unit))]))
              (rs/chevron (:unit/facing unit))
              (rs/text (:unit/name unit) -1)
              (rs/text (format "%d x %d" (:unit/F unit) (:unit/ranks unit)) 0)
              (when (pos? (:unit/damage unit))
                (rs/text (format "[%d]" (:unit/damage unit)) 1))
              (rs/text (rb/int->roman (:unit/id unit)) 2)]
             (rs/scale 9/10))]

        (rs/translate cube)
        (rs/if-selectable (:entity/presentation unit) phase cube))))
