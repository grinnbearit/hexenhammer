(ns hexenhammer.view.setup
  (:require [hiccup.core :refer [html]]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.web.css :refer [STYLESHEET]]
            [hexenhammer.render.core :as r]))


(defn select-hex
  [state]
  (html
   [:html
    [:head
     [:h1 "Hexenhammer"]
     [:h2 "Setup - Select Hex"]
     [:style STYLESHEET]
     [:body
      (r/render-battlefield state) [:br] [:br]
      [:form {:action "/to-start" :method "post"}
       [:input {:type "submit" :value "Start"}]]]]]))


(defn add-unit
  [state]
  (let [cube (:game/cube state)
        terrain (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Setup - Add Unit"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state) [:br] [:br]

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
             [:th [:label {:for "Ld"} "Ld"]]
             [:th [:label {:for "R"} "R"]]]
            [:tbody
             [:tr
              [:td [:input {:type "number" :id "M" :name "M" :min 1 :max 10 :step 1 :value 4}]]
              [:td [:input {:type "number" :id "Ld" :name "Ld" :min 1 :max 10 :step 1 :value 7}]]
              [:td [:input {:type "number" :id "R" :name "R" :min 1 :max 4 :step 1 :value 4}]]]]]]]]

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


(defn remove-unit
  [state]
  (let [cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        terrain (let/clear unit)]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Setup - Remove Unit"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]

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
