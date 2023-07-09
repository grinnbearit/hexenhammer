(ns hexenhammer.view.svg
  (:require [clojure.string :as str]
            [ring.util.codec :refer [form-encode]]))


(def WIDTH 80)
(def HEIGHT (Math/round (* (Math/sqrt 3) (/ WIDTH 2))))


(defn size->dim
  "converts rows columns into an svg dimension {:width _ :height _}"
  [rows columns & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  {:pre [(and (pos? rows) (pos? columns))]}
  {:width (* width (+ 1 (* 3/4 (dec columns))))
   :height (if (= 1 columns)
             (* height rows)
             (* height (+ 1/2 rows)))})


(defn cube->point
  "converts cube coordinates to x y"
  [cube & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  [(+ (/ width 2)
      (* (.q cube) width 3/4))
   (+ (/ height 2)
      (* (.r cube) (/ height 2))
      (* (- (.s cube)) (/ height 2)))])


(defn translate
  "Returns the element with a transform attribute that traslates it
  according to (size, q, r, s)"
  [element cube & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  (let [attrs (element 1)
        [x y] (cube->point cube :width width :height height)
        tran-str (format "translate(%.2f, %.2f)" (float x) (float y))]
    (update-in element [1 :transform] #(if % (str tran-str " " %) tran-str))))


(defn points->str
  "Given a seq of points, converts to an svg points string"
  [points]
  (->> (for [[x y] points]
         (str (float x) "," (float y)))
       (str/join " ")))


(defn gen-hexpoints
  ;; points of a hexagon, centred at 0, 0
  [& {:keys [width height] :or {width WIDTH height HEIGHT}}]
  [[(- (/ width 2)) 0]
   [(- (/ width 4)) (- (/ height 2))]
   [(/ width 4) (- (/ height 2))]
   [(/ width 2) 0]
   [(/ width 4) (/ height 2)]
   [(- (/ width 4)) (/ height 2)]])


(defn hexagon
  "returns an svg hexagon"
  [& {:keys [width height]
      :or {width WIDTH height HEIGHT classes []}}]
  [:polygon {:points (points->str (gen-hexpoints :width width :height height))}])


(defn add-classes
  "adds the passed classes to the element"
  [element classes]
  (let [class-str (str/join " " classes)]
    (update-in element [1 :class] #(if % (str % " " class-str) class-str))))


(defn anchor
  "given an element, wraps it in an anchor tag with the passed href"
  [element href]
  [:a {:href href} element])


(defn selectable
  "given an element and a cube, wraps it in an anchor tag  pointing to /select?[cube]"
  [element cube]
  (anchor element (str "/select?" (form-encode cube))))
