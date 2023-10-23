(ns hexenhammer.render.bit
  (:require [clojure.string :as str]
            [ring.util.codec :refer [form-encode]]))


(defn phase->url
  ([prefix phase]
   (->> (map name phase)
        (str/join "/")
        (str prefix)))
  ([prefix phase form]
   (str (phase->url prefix phase) "?" (form-encode form))))


(def int->roman
  ["0" "i" "ii" "iii" "iv" "v" "vi" "vii" "viii" "ix" "x"])


(defn unit-key->str
  [{:keys [unit/player unit/name unit/id]}]
  (format "P%d - %s (%s)" player name (int->roman id)))
