(ns hexenhammer.view.html
  (:require [garden.core :refer [css]]
            [hiccup.core :refer [html]]
            [hexenhammer.view.colour :refer [PALETTE]]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :as entity]
            [clojure.string :as str]))


(def STYLESHEET
  (css
   [:polygon
    [:&.terrain {:fill (PALETTE "dark green 1") :stroke "black"}
     [:&.selected {:stroke "yellow"}]
     [:&.marked {:stroke "white"}]
     [:&.highlighted {:stroke "orange"}]]
    [:&.unit
     [:&.player-1 {:fill (PALETTE "dark red berry 2") :stroke "black"}]
     [:&.player-2 {:fill (PALETTE "dark cornflower blue 2") :stroke "black"}]]
    [:&.mover
     [:&.future
      [:&.player-1 {:fill (PALETTE "light red berry 1") :stroke "black"}]
      [:&.player-2 {:fill (PALETTE "light cornflower blue 1") :stroke "black"}]]
     [:&.present
      [:&.player-1 {:fill (PALETTE "dark red berry 2") :stroke "black"}]
      [:&.player-2 {:fill (PALETTE "dark cornflower blue 2") :stroke "black"}]]
     [:&.past
      [:&.player-1 {:fill (PALETTE "light red berry 2") :stroke "black"}]
      [:&.player-2 {:fill (PALETTE "light cornflower blue 2") :stroke "black"}]]]
    [:&.arrow
     [:&.selected {:stroke "yellow" :fill "yellow"}]
     [:&.highlighted {:stroke "orange" :fill "orange"}]]]
   [:table :th :td {:border "1px solid"}]))


(defn entity->z
  "Returns the z index value for the passed entity depending on the presentation status"
  [entity]
  (let [presentation->rank {:default 0 :highlighted 1 :marked 2 :selected 3}]
    (-> (:entity/presentation entity)
        (presentation->rank))))


(defn render-battlefield
  [{:keys [game/rows game/columns game/battlefield game/battlemap]}]
  [:svg (svg/size->dim rows columns)
   (for [entity (->> (merge battlefield battlemap)
                     (vals)
                     (sort-by entity->z))]
     (entity/render entity))])


(defmulti render (juxt :game/phase :game/subphase))


(defmethod render [:setup :select-hex]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state) [:br] [:br]
     [:form {:action "/setup/to-movement" :method "post"}
      [:input {:type "submit" :value "To Movement"}]]]]))


(defmethod render [:setup :add-unit]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Add Unit"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state) [:br] [:br]
     [:form {:action "/setup/add-unit" :method "post"}
      [:table
       [:tr
        [:td
         [:label {:for "player"} "Player"]]
        [:td
         [:input {:type "radio" :id "player" :name "player" :value "1" :checked true} "1"]
         [:input {:type "radio" :name "player" :value "2"} "2"]]]
       [:tr
        [:td
         [:label {:for "facing"} "Facing"]]
        [:td
         [:select {:id "facing" :name "facing"}
          (for [[facing-code facing-name]
                [["n" "North"] ["ne" "North-East"] ["se" "South-East"]
                 ["s" "South"] ["sw" "South-West"] ["nw" "North-West"]]]
            [:option {:value facing-code} facing-name])]]]
       [:tr
        [:td
         "Profile"]
        [:td
         [:table
          [:thead
           [:th [:label {:for "M"} "M"]]
           [:th [:label {:for "Ld"} "Ld"]]]
          [:tbody
           [:tr
            [:td [:input {:type "number" :id "M" :name "M" :min 1 :max 10 :step 1 :value 4}]]
            [:td [:input {:type "number" :id "Ld" :name "Ld" :min 1 :max 10 :step 1 :value 7}]]]]]]]]
      [:input {:type "submit" :value "Add Unit"}]]]]))


(defmethod render [:setup :remove-unit]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Remove Unit"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state) [:br] [:br]
     [:form {:action "/setup/remove-unit" :method "post"}
      [:input {:type "submit" :value "Remove Unit"}]]]]))


(defmethod render [:movement :select-hex]
  [state]
  (html
   [:html
    [:head
     [:h1 (str "Player - " (:game/player state))]
     [:h2 "Movement"]
     [:style STYLESHEET]]
    [:body
     (render-battlefield state)]]))


(defn render-movement
  [state movement]
  [:html
   [:head
    [:h1 (str "Player - " (:game/player state))]
    [:h2 (str "Movement - " (str/capitalize (name movement)))]
    [:style STYLESHEET]
    [:body
     (render-battlefield state) [:br] [:br]
     [:form {:action "/movement/skip-movement" :method "post"}
      [:input {:type "submit" :value "Skip Movement"}]
      (when (get-in state [:game/movement :moved?])
        [:input {:type "submit" :value "Finish Movement"
                 :formaction "/movement/finish-movement"}])]
     [:table
      [:tr
       (for [option [:reform :forward :reposition :march]]
         [:td
          (if (= movement option)
            (str/capitalize (name movement))
            [:a {:href (str "/movement/" (name option))}
             (str/capitalize (name option))])])]]]]])


(defmethod render [:movement :reform]
  [state]
  (html (render-movement state :reform)))


(defmethod render [:movement :forward]
  [state]
  (html (render-movement state :forward)))


(defmethod render [:movement :reposition]
  [state]
  (html (render-movement state :reposition)))


(defmethod render [:movement :march]
  [state]
  (html (render-movement state :march)))
