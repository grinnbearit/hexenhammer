(ns hexenhammer.view.html
  (:require [hiccup.core :refer [html]]
            [hexenhammer.view.css :refer [STYLESHEET]]
            [hexenhammer.model.probability :as mp]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.widget :as vw]
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


(defmethod render [:dangerous :start]
  [state]
  (let [{:keys [unit-destroyed? models-destroyed roll unit]} (get-in state [:game/trigger :event])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Dangerous Terrain"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        (if unit-destroyed?
          [:h3 (str (vw/unit-str unit) " destroyed")]
          [:h3 (format "%d Models Destroyed" models-destroyed)])
        (svg/dice roll 2)
        [:form {:action "/trigger/next" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


(defmethod render [:opportunity-attack :start]
  [state]
  (let [{:keys [unit-destroyed? unit wounds]} (get-in state [:game/trigger :event])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Opportunity Attack"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        (if unit-destroyed?
          [:h3 (str (vw/unit-str unit) " destroyed")]
          [:h3 (format "%d Wounds Taken" wounds)])
        [:form {:action "/trigger/next" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


(defmethod render [:heavy-casualties :passed]
  [state]
  (let [{:keys [roll unit-cube]} (get-in state [:game/trigger :event])
        unit (get-in state [:game/battlefield unit-cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Heavy Casualties"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        [:h3 "Passed!"]
        (svg/dice roll 1)
        [:form {:action "/trigger/next" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


(defmethod render [:heavy-casualties :failed]
  [state]
  (let [{:keys [roll unit-cube]} (get-in state [:game/trigger :event])
        unit (get-in state [:game/battlefield unit-cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Heavy Casualties"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        [:h3 "Failed!"]
        (svg/dice roll 7)
        [:form {:action "/flee" :method "post"}
         [:input {:type "submit" :value "Flee!"}]]]]])))


(defmethod render [:heavy-casualties :flee]
  [state]
  (let [{:keys [edge? unit roll]} (get-in state [:game/trigger :event])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Heavy Casualties - Flee!"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        (when edge?
          [:h3 (str (vw/unit-str unit) " flees the Battlefield")])
        (svg/dice roll)
        [:form {:action "/trigger/next" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


(defmethod render [:panic :passed]
  [state]
  (let [{:keys [roll unit-cube]} (get-in state [:game/trigger :event])
        unit (get-in state [:game/battlefield unit-cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Panic!"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        [:h3 "Passed!"]
        (svg/dice roll 1)
        [:form {:action "/trigger/next" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


(defmethod render [:panic :failed]
  [state]
  (let [{:keys [roll unit-cube]} (get-in state [:game/trigger :event])
        unit (get-in state [:game/battlefield unit-cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Event - Panic!"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        [:h3 "Failed!"]
        (svg/dice roll 7)
        [:form {:action "/flee" :method "post"}
         [:input {:type "submit" :value "Flee!"}]]]]])))


(defmethod render [:panic :flee]
  [state]
  (let [{:keys [edge? unit roll]} (get-in state [:game/trigger :event])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Panic! - Flee!"]
       [:style STYLESHEET]
       [:body
        (vw/render-battlefield state) [:br] [:br]
        (vw/render-profile unit) [:br]
        (vw/render-events (:game/events state)) [:br]
        (when edge?
          [:h3 (str (vw/unit-str unit) " flees the Battlefield")])
        (svg/dice roll)
        [:form {:action "/trigger/next" :method "post"}
         [:input {:type "submit" :value "Next"}]]]]])))


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
       [:h1 (str "Player - " (:game/player state))]
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
        events (get-in state [:game/charge :pointer->events pointer])
        charge-range (get-in state [:game/charge :pointer->range pointer])
        charge-prob (mp/charge (:unit/M unit) charge-range)
        charge-perc (Math/round (float (* 100 charge-prob)))]

    (html
     [:html
      [:head
       [:h1 (str "Player - " (:game/player state))]
       [:h2 "Charge"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state) [:br] [:br]
       (vw/render-profile unit) [:br]
       (vw/render-events events) [:br]
       [:form {:action "/charge/declare" :method "post"}
        [:input {:type "submit" :value (format "Declare Charge (~%d%%)" charge-perc)}]]]])))


(defmethod render [:react :select-hex]
  [state]
  (let [targets (get-in state [:game/react :targets])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Charge - React"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state) [:br] [:br]]

      (if (empty? targets)
        [:form {:action "/react/to-charge" :method "post"}
         [:input {:type "submit" :value "To Charge" :disabled true}]])])))


(defmethod render [:react :hold]
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])]
    (html
     [:html
      [:head
       [:h1 "Hexenhammer"]
       [:h2 "Charge - React"]
       [:style STYLESHEET]]
      [:body
       (vw/render-battlefield state) [:br] [:br]
       (vw/render-profile unit) [:br]
       [:form {:action "/react/hold" :method "post"}
        [:input {:type "submit" :value "Hold!"}]]
       [:table
        [:tr
         [:td "Hold"]
         [:td "Flee"]]]]])))


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
        pointer (get-in state [:game/movement :pointer])
        events (get-in state [:game/movement :pointer->events pointer])
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
        (vw/render-events events) [:br]
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
