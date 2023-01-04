(ns hexenhammer.svg.internal
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]))


(def WIDTH 120)
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
  (let [{:keys [width height]} (size->dim size)]
    [(+ x (/ width 2))
     (+ y (/ height 2))]))


(defn svg-hexagon
  "Given the size of the sheet and the (q, r, s) cube coordinates
  returns an svg hexagon"
  [size q r s & {:keys [colour stroke] :or {colour "green" stroke "black"}}]
  [:polygon {:points (->> (cube->points q r s)
                          (apply translate-points size)
                          (apply gen-hexpoints)
                          (points->str))
             :fill colour :stroke stroke}])
