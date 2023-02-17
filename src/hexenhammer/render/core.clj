(ns hexenhammer.render.core
  (:require [hexenhammer.render.internal :as int]
            [hexenhammer.render.svg :as svg]
            [hexenhammer.cube :as cube]
            [hiccup.core :refer [html]]
            [garden.core :refer [css]]
            [ring.util.codec :refer [form-encode]]))

;; Using the q, r, s coordinate system from https://www.redblobgames.com/grids/hexagons/


(def STYLESHEET
  (css [:polygon
        [:&.grass {:fill "green" :stroke "black"}
         [:&.selected {:stroke "yellow"}]]
        [:&.unit
         [:&.player-0 {:fill "red" :stroke "black"}]
         [:&.player-1 {:fill "blue" :stroke "black"}]]]
       [:table :th :td {:border "1px solid"}]))


(defn render-state
  "Returns the html content for modifying the state"
  [state]
  (html
   [:html
    [:head
     [:h1 "Play"]
     [:a {:href "/modify"} "modify"] [:br]
     [:style STYLESHEET]
     [:body
      (let [{:keys [map/rows map/columns map/battlefield]} state]
        [:svg (int/size->dim rows columns)
         (for [[cube hex-obj] battlefield]
           (int/svg-translate
            cube
            (case (:hexenhammer/class hex-obj)
              :terrain (svg/svg-terrain cube)
              :unit (svg/svg-unit hex-obj))))])]]]))


(defn render-modify
  "Returns the html content for modifying the state"
  [state]
  (html
   [:html
    [:head
     [:h1 "Modify"]
     [:a {:href "/"} "play"] [:br]
     [:style STYLESHEET]]
    (let [{:keys [map/rows map/columns map/battlefield map/selected]} state]
      [:body

       ;; Battlefield
       [:svg (int/size->dim rows columns)
        (for [[cube hex-obj] battlefield]
          (int/svg-translate
           cube
           [:a {:href (str "/modify?" (form-encode cube))}
            (case (:hexenhammer/class hex-obj)
              :terrain (svg/svg-terrain cube)
              :unit (svg/svg-unit hex-obj))]))
        (when-let [hexobj (and selected (battlefield selected))]
          (int/svg-translate
           selected
           [:a {:href (str "/modify?" (form-encode selected))}
            (case (hexobj :hexenhammer/class)
              :terrain (svg/svg-terrain selected :selected? true)
              :unit (svg/svg-unit hexobj :selected? true))]))]

       ;; Content
       (when-let [hexobj (and selected (battlefield selected))]
         (case (hexobj :hexenhammer/class)
           :terrain
           (list
            [:h2 "Add Unit"]
            [:form {:action (str "/modify/add-unit?" (form-encode selected))
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
                       [["n" "North"] ["ne" "North-East"] ["se" "South-East"] ["s" "South"] ["sw" "South-West"] ["nw" "North-West"]]]
                   [:option {:value facing-code} facing-name])]]]]
             [:input {:type "submit" :value "Add Unit"}]])

           :unit
           (list
            [:h2 "Remove Unit"
             [:form {:action (str "/modify/remove-unit?" (form-encode selected))
                     :method "post"}
              [:input {:type "submit" :value "Remove Unit"}]]])))])]))
