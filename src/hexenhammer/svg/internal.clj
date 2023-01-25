(ns hexenhammer.svg.internal
  (:require [clojure.string :as str]
            [hiccup.core :refer [html]]))


(def WIDTH 80)
(def FONT-SIZE (/ WIDTH 10))
(def HEIGHT (Math/round (* (Math/sqrt 3) (/ WIDTH 2))))


(defn size->dim
  "converts a map size into an svg dimension {:width _ :height _}"
  [size & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  {:width (* width  (+ 1 (* 3/2 size)))
   :height (* height (inc (* 2 size)))})


(defn cube->point
  "converts cube coordinates to x y, where x, y is the centre of the coordinate grid"
  [cube & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  [(* (.q cube) width 3/4)
   (+ (* (.r cube) (/ height 2))
      (- (* (.s cube) (/ height 2))))])


(defn translate-point
  "Given a size, returns the translated centre'd x, y coordinates for an svg sheet of that size"
  [size [x y] & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  (let [dim (size->dim size :width width :height height)
        x-t (+ x (/ (dim :width) 2))
        y-t (+ y (/ (dim :height) 2))]
    (if (or (< (dim :width) x-t)
            (< (dim :height) y-t))
      (throw (ex-info "Out of Bounds" {:x x-t :y y-t}))
      [(+ x (/ (dim :width) 2))
       (+ y (/ (dim :height) 2))])))


(defn translate-cube
  "composes cube->points and translate-point"
  [size cube & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  (let [point (cube->point cube :width width :height height)]
    (translate-point size point :width width :height height)))


(defn svg-translate
  "Returns the element with a transform attribute that traslates it
  according to (size, q, r, s)"
  [size cube element & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  (let [attrs (element 1)
        [x y] (translate-cube size cube :width width :height height)
        tran-str (format "translate(%.2f, %.2f)" (float x) (float y))]
    (update-in element [1 :transform] #(if % (str tran-str " " %) tran-str))))


(defn svg-rotate
  "Returns the element with a transform attribute that rotates it by angle `angle`'"
  [angle element]
  (let [attrs (element 1)
        tran-str (format "rotate(%.2f)" (float angle))]
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


(defn svg-hexagon
  "returns an svg hexagon"
  [& {:keys [fill stroke width height]
      :or {fill "green" stroke "black" width WIDTH height HEIGHT}}]
  [:polygon {:points (points->str (gen-hexpoints :width width :height height))
             :fill fill :stroke stroke}])


(defn svg-text
  "Returns an svg text element withis a text offset from the centre of the hex"
  [row text & {:keys [font-size] :or {font-size FONT-SIZE}}]
  (let [x-offset (- (* (/ (inc (count text)) 2) 1/2 font-size))
        y-offset (+ (* row font-size) (/ font-size 4))]
    [:text {:x x-offset :y y-offset
            :font-family "monospace" :font-size (str font-size)}
     text]))


(defn svg-coordinates
  "Writes the cube coordinates of the cell on the hex"
  [cube]
  (svg-text 0 (format "[%d, %d, %d]" (.q cube) (.r cube) (.s cube))))


(defn gen-chevpoints
  [& {:keys [width height] :or {width WIDTH height HEIGHT}}]
  [[0 (/ height 2)]
   [(- (* width 1/20)) (* height 45/100)]
   [(+ (* width 1/20)) (* height 45/100)]])


(defn svg-chevron
  "draws a tiny chevron pointing to a face of the hex"
  [facing & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  {:pre [(#{:nw :n :ne :se :s :sw} facing)]}
  (let [facing->angle (zipmap [:s :sw :nw :n :ne :se]
                              (map #(* 60 %) (range)))]
    (svg-rotate
     (facing->angle facing)
     [:polyline {:points (points->str (gen-chevpoints :width width :height height))
                 :stroke "black"}])))
