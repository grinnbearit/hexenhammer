(ns hexenhammer.view.html
  (:require [hiccup.core :refer [html]]
            [hexenhammer.view.css :refer [STYLESHEET]]
            [hexenhammer.model.probability :as mp]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.view.widget :as vw]
            [hexenhammer.view.svg :as svg]
            [clojure.string :as str]))


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
     (vw/render-battlefield state) [:br] [:br]
     [:form {:action "/setup/to-charge" :method "post"}
      [:input {:type "submit" :value "To Charge"}]]]]))


(defmethod render [:setup :add-unit]
  [state]
  (let [cube (:game/selected state)
        terrain (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Setup - Add Unit"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state) [:br] [:br]
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

        (cond-> [:input {:type "submit" :value "Add Unit"}]

          (= :impassable (:terrain/type terrain))
          (assoc-in [1 :disabled] true))]

       [:form {:action "/setup/swap-terrain" :method "post"}
        [:table
         [:tr
          [:td
           [:label {:for "terrain"} "Terrain"]]

          (case (:terrain/type terrain)

            :open
            [:td
             [:input {:type "radio" :id "terrain" :name "terrain" :value "impassable" :checked true} "impassable"]
             [:input {:type "radio" :id "terrain" :name "terrain" :value "dangerous"} "dangerous"]]

            :dangerous
            [:td
             [:input {:type "radio" :id "terrain" :name "terrain" :value "open" :checked true} "open"]
             [:input {:type "radio" :id "terrain" :name "terrain" :value "impassable"} "impassable"]]

            :impassable
            [:td
             [:input {:type "radio" :id "terrain" :name "terrain" :value "open" :checked true} "open"]
             [:input {:type "radio" :id "terrain" :name "terrain" :value "dangerous"} "dangerous"]])]]

        [:input {:type "submit" :value "Swap Terrain"}]]]])))


(defmethod render [:setup :remove-unit]
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        terrain (lt/pickup unit)]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Setup - Remove Unit"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state)
       (vw/render-profile unit) [:br]
       [:form {:action "/setup/remove-unit" :method "post"}
        [:input {:type "submit" :value "Remove Unit"}]]
       [:form {:action "/setup/swap-terrain" :method "post"}
        [:table
         [:tr
          [:td
           [:label {:for "terrain"} "Terrain"]]
          [:td
           (case (:terrain/type terrain)

             :open
             [:input {:type "radio" :id "terrain" :name "terrain" :value "dangerous" :checked true} "dangerous"]

             :dangerous
             [:input {:type "radio" :id "terrain" :name "terrain" :value "open" :checked true} "open"])]]]

        [:input {:type "submit" :value "Swap Terrain"}]]]])))


(defmethod render [:charge :select-hex]
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Charge"]
     [:style STYLESHEET]]
    [:body
     (vw/render-battlefield state) [:br] [:br]
     [:form {:action "/charge/to-movement" :method "post"}
      [:input {:type "submit" :value "To Movement"}]]]]))


(defmethod render [:charge :select-target]
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        charge-prob (sort (mp/charge (:unit/M unit)))]

    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Charge"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state) [:br] [:br]
       (vw/render-profile unit) [:br]
       [:table
        [:thead
         [:th "Charge Range"]
         [:th "Success %"]]
        [:tbody
         (for [[hexes prob] charge-prob
               :let [perc (Math/round (float (* 100 prob)))]]
           [:tr
            [:td hexes]
            [:td (format "~%d%%" perc)]])]]]])))


(defmethod render [:charge :declare]
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        pointer (get-in state [:game/charge :pointer])
        charge-range (get-in state [:game/charge :ranges pointer])
        charge-prob (mp/charge (:unit/M unit) charge-range)
        charge-perc (Math/round (float (* 100 charge-prob)))]

    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Charge"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state) [:br] [:br]
       (vw/render-profile unit) [:br]
       [:input {:type "button" :disabled true
                :value (format "Charge! (~%d%%)" charge-perc)}]]])))


(defmethod render [:movement :select-hex]
  [state]
  (let [player (:game/player state)]

    (html
     [:html
      [:head
       [:h1 (str "Player - " (:game/player state))]
       [:h2 "Movement"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state)]])))


(defmethod render [:movement :reform]
  [state]
  (html (vw/render-movement state :reform)))


(defmethod render [:movement :forward]
  [state]
  (html (vw/render-movement state :forward)))


(defmethod render [:movement :reposition]
  [state]
  (html (vw/render-movement state :reposition)))


(defmethod render [:movement :march]
  [state]
  (let [player (:game/player state)
        cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        status (get-in state [:game/movement :march])
        moved? (get-in state [:game/movement :moved?])
        Ld (get-in state [:game/battlefield cube :unit/Ld])
        prob-Ld (Math/round (float (* 100 (mp/march Ld))))
        roll (get-in unit [:unit/movement :roll])]

    (html
     [:html
      [:head
       [:h1 (str "Player - " player)]
       [:h2 "Movement - March"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state)
        (vw/render-profile unit) [:br]
        [:form {:action "/movement/skip-movement" :method "post"}
         [:input {:type "submit" :value "Skip Movement"}]

         (when moved?
           (cond-> [:input {:type "submit" :value "Finish Movement"
                            :formaction "/movement/finish-movement"}]

             (#{:failed :required} status)
             (assoc-in [1 :disabled] true)))]

        [:table
         [:tr
          (for [option [:reform :forward :reposition]]
            [:td [:a {:href (str "/movement/" (name option))}
                  (str/capitalize (name option))]])
          [:td "March"]]]

        (case status

          :required
          (list
           [:br]
           [:form {:action "/movement/test-leadership" :method "post"}
            [:input {:type "submit"
                     :value (format "Test Leadership (~%d%%)" prob-Ld)}]])

          :passed
          (svg/dice roll 1)

          :failed
          (svg/dice roll 7)

          nil)]]])))
