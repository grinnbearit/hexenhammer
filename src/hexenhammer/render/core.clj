(ns hexenhammer.render.core
  (:require [hexenhammer.render.internal :as int]
            [hexenhammer.render.svg :as svg]
            [hexenhammer.cube :as cube]
            [hiccup.core :refer [html]]
            [garden.core :refer [css]]
            [ring.util.codec :refer [form-encode]]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


;; https://spreadsheet.dev/how-to-get-the-hexadecimal-codes-of-colors-in-google-sheets
(def STYLESHEET
  (css [:polygon
        [:&.grass {:fill "#6aa84f" :stroke "black"} ;
         [:&.selected {:stroke "yellow"}]]
        [:&.unit
         [:&.player-0 {:fill "#990000" :stroke "black"}]
         [:&.player-1 {:fill "#1155cc" :stroke "black"}]]]
       [:table :th :td {:border "1px solid"}]))


(defn render-setup
  "Returns the html content for setting up the gameboard"
  [state]
  [:html
   [:head
    [:h1 "Hexenhammer"]
    [:h2 "Setup"]
    [:style STYLESHEET]]
   (let [{:keys [map/rows map/columns map/battlefield map/selected]} state]
     [:body

      ;; Battlefield
      [:svg (int/size->dim rows columns)
       (for [[cube component] battlefield]
         (int/svg-translate
          cube
          [:a {:href (str "/select?" (form-encode cube))}
           (case (:hexenhammer/class component)
             :terrain (svg/svg-terrain cube)
             :unit (svg/svg-unit component))]))

       (when-let [component (and selected (battlefield selected))]
         (int/svg-translate
          selected
          [:a {:href (str "/select?" (form-encode selected))}
           (case (component :hexenhammer/class)
             :terrain (svg/svg-terrain selected :selected? true)
             :unit (svg/svg-unit component :selected? true))]))]

      ;; Content
      (if-let [component (and selected (battlefield selected))]
        (case (component :hexenhammer/class)
          :terrain
          (list
           [:h2 "Add Unit"]
           [:form {:action (str "/setup/add-unit?" (form-encode selected))
                   :method "post"}
            [:h3 "Infantry"]
            [:table
             [:tr
              [:td
               [:label {:for "player"} "Player"]]
              [:td
               [:input {:type "radio" :id "player" :name "player" :value "0" :checked true} "1"]
               [:input {:type "radio" :name "player" :value "1"} "2"]]]
             [:tr
              [:td
               [:label {:for "facing"} "Facing"]]
              [:td
               [:select {:id "facing" :name "facing"}
                (for [[facing-code facing-name]
                      [["n" "North"] ["ne" "North-East"] ["se" "South-East"]
                       ["s" "South"] ["sw" "South-West"] ["nw" "North-West"]]]
                  [:option {:value facing-code} facing-name])]]]]
            [:input {:type "submit" :value "Add Unit"}]])

          :unit
          (list
           [:h2 "Remove Unit"
            [:form {:action (str "/setup/remove-unit?" (form-encode selected))
                    :method "post"}
             [:input {:type "submit" :value "Remove Unit"}]]]))

        [:form {:action (str "/setup/to-movement" (form-encode selected))
                :method "post"}
         [:input {:type "submit" :value "Movement"}]])])])


(defn render-movement
  "Returns the html content for displaying the state"
  [state]
  [:html
   [:head
    [:h1 "Hexenhammer"]
    [:h2 "Movement"]
    [:style STYLESHEET]
    (let [{:keys [map/rows map/columns map/battlefield map/selected]} state]
      [:body

       ;; Battlefield
       [:svg (int/size->dim rows columns)
        (for [[cube component] battlefield]
          (int/svg-translate
           cube
           (case (:hexenhammer/class component)
             :terrain (svg/svg-terrain cube)
             :unit [:a {:href (str "/select?" (form-encode cube))}
                    (svg/svg-unit component)])))
        (when-let [component (and selected (battlefield selected))]
          (int/svg-translate
           selected
           [:a {:href "/select"}
            (case (component :hexenhammer/class)
              :terrain (svg/svg-terrain selected :selected? true)
              :unit (svg/svg-unit component :selected? true))]))]])]])


(defn render-state
  [state]
  (html
   (case (:game/phase state)
     :setup (render-setup state)
     :movement (render-movement state))))
