(ns hexenhammer.view.widget
  (:require [hexenhammer.view.css :refer [STYLESHEET]]
            [hexenhammer.view.svg :as svg]
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


(defn render-profile
  [unit]
  [:table
   [:h3 (str (:entity/name unit) " (" (:unit/id unit) ")")]
   [:thead
    [:tr [:th "M"][:th "Ld"]]]
   [:tbody
    [:tr [:td (:unit/M unit)] [:td (:unit/Ld unit)]]]])


(defn render-movement
  [state movement]
  (let [player (:game/player state)
        cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        moved? (get-in state [:game/movement :moved?])]

    [:html
     [:head
      [:h1 (str "Player - " player)]
      [:h2 (str "Movement - " (str/capitalize (name movement)))]
      [:style STYLESHEET]
      [:body
       (render-battlefield state)
       (render-profile unit) [:br]
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
