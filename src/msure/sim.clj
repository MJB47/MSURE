(ns msure.sim
  (:use [msure.slurp]
        [msure.logic]))


(defn show-times [data]
  (loop [data data]
    (if (first data)
      (do
        (if-not (zero? (gen (first data)))
          (do
            (println (day (first data)))
            (println (gen (first data)))))
      (recur (rest data))))))

(defn sim [data]
  (loop [data data]
    (if (first data)
      (do
        ;(println (date-time (first data)) @results)
        (reset! cdg (day-gen (first data)))
        (run-sim (date-time (first data)) (gen (first data)))
        (recur (rest data))))))

(defn reformat [results]
  (if (first results)
    (do
      (let [result (first results)]
        (println (first result) "," (second result)))
      (reformat (rest (sort results))))))

(defn vary-percent []
  (while (>= 100 @curr-percent)
    (do
      (sim november-export)
      (sim december-export)
      (sim january-export)
      (sim february-export)
      (swap! curr-percent + 5)))
  (reformat (sort @results)))