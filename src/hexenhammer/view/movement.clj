(ns hexenhammer.view.movement
  (:require [hiccup.core :refer [html]]
            [hexenhammer.logic.probability :as lp]
            [hexenhammer.web.css :refer [STYLESHEET]]
            [hexenhammer.render.core :as r]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.render.svg :as rs]
            [clojure.string :as str]))


(defn select-hex
  [state]
  (let [player (:game/player state)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Movement"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)]])))


(defn render-movement
  [state movement]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        moved? (get-in state [:game/movement :moved?])
        title (str/capitalize (name movement))]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 (str "Movement - " title)]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state)
       (r/render-profile unit) [:br]

       [:form {:action "/movement/skip-movement" :method "post"}
        [:input {:type "submit" :value "Skip Movement"}]

        (when moved?
          [:input {:type "submit" :value "Finish Movement"
                   :formaction "/movement/finish-movement"}])]

       [:table
        [:tr
         (for [option [:reform :forward :reposition :march]
               :when (not= option movement)
               :let [option-url (str "/movement/switch-movement/" (name option))
                     option-title (str/capitalize (name option))]]
           [:td [:a {:href option-url} option-title]])]]]])))


(defn reform
  [state]
  (render-movement state :reform))


(defn forward
  [state]
  (render-movement state :forward))


(defn reposition
  [state]
  (render-movement state :reposition))


(defn march
  [state]
  (let [player (:game/player state)
        cube (:game/cube state)
        unit (get-in state [:game/battlefield cube])
        moved? (get-in state [:game/movement :moved?])
        events (get-in state [:game/movement :events])
        status (get-in state [:game/movement :march])
        roll (get-in state [:game/movement :roll])
        Ld (:unit/Ld unit)
        prob-Ld (Math/round (float (* 100 (lp/march Ld))))]

    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Movement - March"]
       [:style STYLESHEET]
       [:body
        (r/render-battlefield state)
        (r/render-profile unit) [:br]
        (r/render-events events) [:br]

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
            [:td [:a {:href (str "/movement/switch-movement/" (name option))}
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
          (rs/dice roll 1)

          :failed
          (rs/dice roll 7)

          :unnecessary
          nil)]]])))


(defn to-close-combat
  [state]
  (let [player (:game/player state)]
    (html
     [:html
      [:head
       [:h1 (rb/player->str player)]
       [:h2 "Movement"]
       [:style STYLESHEET]]
      [:body
       (r/render-battlefield state) [:br] [:br]
       [:form {:action "/to-close-combat" :method "post"}
        [:input {:type "submit" :value "To Close Combat"}]]]])))
