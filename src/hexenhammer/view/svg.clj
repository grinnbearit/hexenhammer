(ns hexenhammer.view.svg
  (:require [clojure.string :as str]
            [ring.util.codec :refer [form-encode]]))


(def WIDTH 80)
(def HEIGHT (Math/round (* (Math/sqrt 3) (/ WIDTH 2))))
(def FONT-SIZE (/ WIDTH 10))


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


(defn movable
  "given an element, a cube and a facing, wraps it in an anchro tag pointing to /move?[cube]&[facing]"
  [element cube facing]
  (anchor element (str "/move?" (form-encode (assoc cube :facing (name facing))))))


(defn scale
  "Returns the element with a transform attribute that scales it by a factor `factor`"
  [element factor]
  (let [attrs (element 1)
        tran-str (format "scale(%.2f)" (float factor))]
    (update-in element [1 :transform] #(if % (str tran-str " " %) tran-str))))


(defn rotate
  "Returns the element with a transform attribute that rotates it by angle `angle`'"
  [element angle]
  (let [attrs (element 1)
        tran-str (format "rotate(%.2f)" (float angle))]
    (update-in element [1 :transform] #(if % (str tran-str " " %) tran-str))))


(defn gen-chevpoints
  [& {:keys [width height] :or {width WIDTH height HEIGHT}}]
  [[0 (/ height 2)]
   [(- (* width 1/20)) (* height 45/100)]
   [(+ (* width 1/20)) (* height 45/100)]])


(defn gen-arrpoints
  [& {:keys [width height] :or {width WIDTH height HEIGHT}}]
  [[0 (/ height 2)]
   [(- (* width 1/10)) (* height 35/100)]
   [(+ (* width 1/10)) (* height 35/100)]])


(defn wrap-pointer
  "wraps a sequence of chevron or marker points"
  [facing points]
  (let [facing->angle (zipmap [:s :sw :nw :n :ne :se] (map #(* 60 %) (range)))]
    (rotate
     [:polygon {:points (points->str points) :stroke "white" :fill "white"}]
     (facing->angle facing))))


(defn chevron
  "draws a chevron pointing to a face of the hex"
  [facing & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  (wrap-pointer facing (gen-chevpoints :width width :height height)))


(defn arrow
  "draws a large chevron pointing to a face of the hex"
  [facing & {:keys [width height] :or {width WIDTH height HEIGHT}}]
  (wrap-pointer facing (gen-arrpoints :width width :height height)))


(defn text
  "Returns an svg text element withis a text offset from the centre of the hex"
  [text row & {:keys [font-size] :or {font-size FONT-SIZE}}]
  (let [x-offset (- (* (/ (inc (count text)) 2) 1/2 font-size))
        y-offset (+ (* row font-size) (/ font-size 4))]
    [:text {:x x-offset :y y-offset
            :font-family "monospace" :font-size (str font-size) :fill "white"}
     text]))


(defn dice
  "Given a list of die in a roll, returns each die as a text glyph
  wraps successes (at or above `threshold` in the 'passed' class
  wraps failures (below `threshold` in the 'failed' class"
  [roll threshold]
  (let [die->glyph {1 "⚀" 2 "⚁" 3 "⚂" 4 "⚃" 5 "⚄" 6 "⚅"}]
    (for [n roll]
      (cond-> [:text {:class "dice"} (die->glyph n)]

        (<= threshold n)
        (add-classes ["passed"])

        (< n threshold)
        (add-classes ["failed"])))))
