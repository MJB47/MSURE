(ns msure.slurp
  (:use clojure-csv.core))

(def november-export
  (parse-csv (slurp "resources/November_Export.csv")))

(def december-export
  (parse-csv (slurp "resources/December_Export.csv")))

(def january-export
  (parse-csv (slurp "resources/January_Export.csv")))

(def february-export
  (parse-csv (slurp "resources/February_Export.csv")))

;; take in a row of data and return date
;; make sure to not send any row that doesnt start with a valid date

(defn day [row]
  (Integer. (subs (first row) 8 10)))

(defn month [row]
  (Integer. (subs (first row) 5 7)))

(defn year [row]
  (Integer. (subs (first row) 0 4)))

(defn hour [row]
  (Integer. (subs (first row) 11 13)))

(defn minute [row]
  (Integer. (subs (first row) 14 16)))

(defn secs [row]
  (Integer. (subs (first row) 17 19)))

(defn gen [row]
  (Integer. (nth row 5)))

(defn day-gen [row]
  (Integer. (nth row 7)))

(defn date-time [row]
  {:day (day row)
   :month (month row)
   :year (year row)
   :hour (hour row)
   :minute (minute row)
   :second (secs row)})

