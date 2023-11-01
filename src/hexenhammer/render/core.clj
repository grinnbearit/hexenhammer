(ns hexenhammer.render.core
  (:require [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.render.svg :as rs]
            [hexenhammer.render.entity :as re]))


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


(defn render-profile
  [unit]
  [:div
   [:h3 (rb/unit-key->str unit)]
   [:table.profile
    [:thead
     [:tr [:th "M"][:th "Ld"] [:th "W"] [:th "Formation"] [:th "Damage"] [:th "Models"] [:th "Unit Strength"]]]
    [:tbody
     [:tr
      [:td (:unit/M unit)]
      [:td (:unit/Ld unit)]
      [:td (:unit/W unit)]
      [:td (format "%d x %d" (:unit/F unit) (:unit/ranks unit))]
      [:td (:unit/damage unit)]
      [:td (leu/models unit)]
      [:td (leu/unit-strength unit)]]]]])


(defn render-events
  [events]
  (when (seq events)
    [:table
     [:thead
      [:tr [:th "Order"] [:th "Event"] [:th "Unit"]]]
     [:tbody
      (for [[index event] (zipmap (range) events)
            :let [unit-key (:event/unit-key event)]]
        (case (:event/type event)
          :dangerous
          [:tr [:td (inc index)] [:td "Dangerous Terrain"] [:td (rb/unit-key->str unit-key)]]

          :opportunity-attack
          [:tr [:td (inc index)] [:td "Opportunity Attack"] [:td (rb/unit-key->str unit-key)]]

          :heavy-casualties
          [:tr [:td (inc index)] [:td "Heavy Casualties"] [:td (rb/unit-key->str unit-key)]]

          :panic
          [:tr [:td (inc index)] [:td "Panic!"] [:td (rb/unit-key->str unit-key)]]))]]))
