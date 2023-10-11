(ns hexenhammer.view.widget
  (:require [hexenhammer.view.css :refer [STYLESHEET]]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.unit :as vu]
            [hexenhammer.view.entity :as ve]
            [clojure.string :as str]))


(defn entity->z
  "Returns the z index value for the passed entity depending on the presentation status"
  [entity]
  (let [presentation->rank {:default 0 :selectable 1 :marked 2 :selected 3}]
    (-> (:entity/state entity)
        (presentation->rank))))


(defn render-battlefield
  [{:keys [game/rows game/columns game/battlefield game/battlemap]}]
  [:svg (svg/size->dim rows columns)
   (for [entity (->> (merge battlefield battlemap)
                     (vals)
                     (sort-by entity->z))]
     (ve/render entity))])


(defn unit-str
  [unit]
  (format "P%d - %s (%s)" (:unit/player unit) (:entity/name unit) (vu/int->roman (:unit/id unit))))


(defn render-profile
  [unit]
  [:div
   [:h3 (unit-str unit)]
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
      [:td (mu/models unit)]
      [:td (mu/unit-strength unit)]]]]])


(defn render-events
  [events]
  (when (seq events)
    [:table
     [:thead
      [:tr [:th "Order"] [:th "Event"] [:th "Unit"]]]
     (for [[index event] (zipmap (range) events)]
       (case (:event/class event)
         :dangerous
         [:tr [:td (inc index)] [:td "Dangerous Terrain"] [:td (unit-str event)]]

         :opportunity-attack
         [:tr [:td (inc index)] [:td "Opportunity Attack"] [:td (unit-str event)]]

         :heavy-casualties
         [:tr [:td (inc index)] [:td "Heavy Casualties"] [:td (unit-str event)]]

         :panic
         [:tr [:td (inc index)] [:td "Panic!"] [:td (unit-str event)]]))]))


(defn render-movement
  [state movement]
  (let [player (:game/player state)
        cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        moved? (get-in state [:game/movement :moved?])
        pointer (get-in state [:game/movement :pointer])
        events (get-in state [:game/movement :pointer->events pointer])]

    [:html
     [:head
      [:h1 (str "Player - " player)]
      [:h2 (str "Movement - " (str/capitalize (name movement)))]
      [:style STYLESHEET]
      [:body
       (render-battlefield state)
       (render-profile unit) [:br]
       (render-events events) [:br]

       [:form {:action "/movement/skip-movement" :method "post"}
        [:input {:type "submit" :value "Skip Movement"}]

        (when moved?
          [:input {:type "submit" :value "Finish Movement"
                   :formaction "/movement/finish-movement"}])]

       [:table
        [:tr
         (for [option [:reform :forward :reposition :march]]
           [:td
            (if (= movement option)
              (str/capitalize (name movement))
              [:a {:href (str "/movement/" (name option))}
               (str/capitalize (name option))])])]]]]]))
