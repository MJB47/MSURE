(ns msure.logic
  (:use [msure.appliances]
        [msure.slurp]))

; current day gen
(def cdg (atom 0))

; current percent of power generated required to turn appliance on
(def curr-percent (atom 0))

; check if new day
; check if anything has finished running and update accordingly
; check if start time (8am)
; check what is running/been run today
; check if the required percentage has been met including with running appliances - check all appliances
; check if there isnt enough time to finish all appliances and run any remaining accordingly
; update start time and :run if new appliance has started
; update map on:
;   how much elec has been produced while the appliances have been running - record start and end day-gen

; keep a mapping of percentages and total elec produced while app's are running
; {0 100, 5 670, etc}
(def results (atom {}))

; map of appliance as key with a map on start and end day-gen as value
; take the start day-gen from the end and add that value to results to measure how much was used by appliances
; make sure not to add more day-gen than is actually used (e.g. 500 watt appliance being used while 800 being generated)
; clear every day
(def pu (atom {}))
(def remaining (atom 0))

;(def start-times (atom []))

(defn run [app dt]
  (swap! appliances assoc-in [app :run] 1)
  (swap! appliances assoc-in [app :started] dt)
  (swap! pu assoc-in [app :start] @cdg)
  ;(swap! start-times conj dt)
  )

(defn result-exists []
  (if (get @results @curr-percent)
    (get @results @curr-percent)
    0))

(defn dont-double-count [start]
  (if (> @remaining start)
    @remaining
    start))

(defn add-to-results []
  ;(println @results)
  (loop [apps (sort-by (comp :start second) @pu)]
    (if (first apps)
      (do
        (let [app (ffirst apps)
              diff (- (:end (app @pu)) (dont-double-count (:start (app @pu))))
              pd (* (:power (app @appliances)) 2)] 
              ; assuming everything lasts 2 hours, this would give watt hours for each appliance
          (if (> diff pd)
            (swap! results assoc @curr-percent (+ (result-exists) pd))
            (swap! results assoc @curr-percent (+ (result-exists) diff))))
        ;(println @pu @remaining)
        (reset! remaining (:end ((ffirst apps) @pu)))
        (recur (rest (sort-by (comp :start second) apps))))
      (do
        (reset! pu {})
        (reset! remaining 0)))))

;; assuming all appliances take 2 hours
(defn finish-up [dt]
  (loop [apps @appliances]
    (if (first apps)
      (do 
        (let [app (first (first apps))]
          (if (zero? (:run (app @appliances)))
            (run app dt)))
        (recur (rest apps))))))

;; assumes you start the system at 8am
(defn start-time? [dt]
  (and (<= 8 (:hour dt)) (> 18 (:hour dt))))

;; assumed you come home at 6pm
(defn out-of-time [dt]
  (if (= (:hour dt) 16)
    (finish-up dt)))

;; assuming all appliances last 2 hours, which is pretty bad to hardcode
;; honestly this function is horrible
(defn check-finished [dt]
  (loop [apps @appliances]
    (if (first apps)
      (do
        (let [app (first (first apps))]
          (if (= 1 (:run (app @appliances)))
            (let [started (:started (app @appliances))]
              (if (<= 2 (- (:hour dt) (:hour started)))
                (if (<= 0 (- (:minute dt) (:minute started)))
                  (do
                    (swap! appliances assoc-in [app :run] 2)
                    (swap! pu assoc-in [app :end] @cdg)))))))
        (recur (rest apps))))))

(defn power-being-used []
  (loop [apps @appliances
         total 0]
    (if (first apps)
      (let [app (first (first apps))]
        (if (= 1 (:run (app @appliances)))
          (recur (rest apps)
                 (+ total (:power (app @appliances))))
          (recur (rest apps)
                 total)))
      total)))

(defn power-remaining [g]
  (let [r (- g (power-being-used))]
    (if (>= 0 r)
      0
      r)))

(defn new-day []
  (add-to-results)
  (loop [apps @appliances]
    (if (first apps)
      (do 
        (let [app (first (first apps))]
          (swap! appliances assoc-in [app :run] 0))
        (recur (rest apps))))))

(defn check-new-day [dt]
  ;(if-not (= (:day (:started ((first (first @appliances)) @appliances))) (:day dt))
  (if (and (<= 19 (:hour dt)) (= 2 (:run ((ffirst @appliances) @appliances))))
    (new-day)))

(defn naive-alg [dt g]
  (loop [apps @appliances]
    (if (first apps)
      (do
        (let [app (first (first apps))]
          (if (zero? (:run (app @appliances)))
            (if (>= (power-remaining g) (* (:power (app @appliances)) (/ @curr-percent 100)))
              (run app dt))))
        (recur (rest apps))))))

; date time, generated
(defn run-sim [dt g]
  (check-finished dt)
  (check-new-day dt)
  (out-of-time dt)
  (if (start-time? dt)
    (naive-alg dt g)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; for finding total energy produced
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ep (atom 0))

(defn elec-produced [data]
  (loop [data data
         prev-prod 0]
    (if (first data)
      (do 
        (let [prod (day-gen (first data))]
          (if (> prev-prod prod)
            (swap! ep + prev-prod)))
        (recur (rest data)
               (day-gen (first data))))
      (do
        (swap! ep + prev-prod)
        (println (str "Electricity produced in watt hours: " @ep))))))
; => 40568 for novemeber