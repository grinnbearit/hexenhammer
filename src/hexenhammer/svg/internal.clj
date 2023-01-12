(ns hexenhammer.svg.internal
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]))


(def WIDTH 120)
(def FONT-SIZE 12)
(def HEIGHT (Math/round (* (Math/sqrt 3) (/ WIDTH 2))))


(defn size->dim
  "converts a map size into an svg dimension {:width _ :height _}"
  [size]
  {:width (* WIDTH  (+ 1 (* 3/2 size)))
   :height (* HEIGHT (inc (* 2 size)))})


(defn gen-hexpoints
  "Given the centre points of a hexagon
  Returns a vector of vector of hexagon points"
  [x y]
  [[(- x (/ WIDTH 2)) y]
   [(- x (/ WIDTH 4)) (- y (/ HEIGHT 2))]
   [(+ x (/ WIDTH 4)) (- y (/ HEIGHT 2))]
   [(+ x (/ WIDTH 2)) y]
   [(+ x (/ WIDTH 4)) (+ y (/ HEIGHT 2))]
   [(- x (/ WIDTH 4)) (+ y (/ HEIGHT 2))]])


(defn points->str
  "Given a seq of points, converts to an svg points string"
  [hexpoints]
  (->> (for [[x y] hexpoints]
         (str x "," y))
       (str/join " ")))


(defn cube->points
  "converts cube coordinates to x y, where x, y is the centre of the coordinate grid"
  [q r s]
  {:pre [(= (+ q s r) 0)]}
  [(* q WIDTH 3/4)
   (+ (* r (/ HEIGHT 2))
      (- (* s (/ HEIGHT 2))))])


(defn translate-points
  "Given a size, returns the translated centre'd x, y coordinates for an svg sheet of that size"
  [size x y]
  (let [{:keys [width height]} (size->dim size)
        x-t (+ x (/ width 2))
        y-t (+ y (/ height 2))]
    (if (or (< width x-t)
            (< height y-t))
      (throw (ex-info "Out of Bounds" {:x x-t :y y-t}))
      [(+ x (/ width 2))
       (+ y (/ height 2))])))


(defn translate-cube
  "combines cube->points and translate-points"
  [size q r s]
  (->> (cube->points q r s)
       (apply translate-points size)))


(defn svg-hexagon
  "Given the size of the sheet and the (q, r, s) cube coordinates
  returns an svg hexagon"
  [size q r s & {:keys [fill stroke] :or {fill "green" stroke "black"}}]
  [:polygon {:points (->> (translate-cube size q r s)
                          (apply gen-hexpoints)
                          (points->str))
             :fill fill :stroke stroke}])


(defn svg-text
  "Returns an svg text element with the text position
  row is a text offset from the centre of the hex"
  [size q r s row text]
  (let [[x y] (translate-cube size q r s)
        x-offset (* (/ (inc (count text)) 2) 1/2 FONT-SIZE)
        y-offset (+ (* row FONT-SIZE) (/ FONT-SIZE 4))]
    [:text {:x (- x x-offset) :y (+ y y-offset)
            :font-family "monospace" :font-size (str FONT-SIZE)}
     text]))


(defn svg-coordinates
  "Writes the cube coordinates of the cell on the hex"
  [size q r s]
  (svg-text size q r s 0 (format "[%d, %d, %d]" q r s)))


(defn svg-unit
  "Writes unit information on the hex"
  [size q r s unit id models]
  (list
   (svg-text size q r s -1 (format "%s - %d" unit id))
   (svg-text size q r s 1 (format "(%d)" models))))
