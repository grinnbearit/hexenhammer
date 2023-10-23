(ns hexenhammer.render.core
  (:require [hexenhammer.render.entity :as re]
            [hexenhammer.render.svg :as rs]))


(defn entity->z
  "Returns the z index value for the passed entity depending on the presentation status"
  [entity]
  (let [presentation->rank {:default 0 :selectable 1 :marked 2 :selected 3}]
    (-> (:entity/presentation entity)
        (presentation->rank))))


(defn render-battlefield
  [{:keys [game/setup game/battlefield game/battlemap game/phase]}]
  (let [{:keys [rows columns]} setup]
    [:svg (rs/size->dim rows columns)
     (for [[cube entity] (->> (merge battlefield battlemap)
                              (sort-by (comp entity->z last)))]
       (re/render entity phase cube))]))
