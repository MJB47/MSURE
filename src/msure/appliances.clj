(ns msure.appliances)

;;; power in watts
;;; duration in minutes

;; 0 = off, 1 = running 2 = finished

;; default started is an impossible date, 50th of the 0th month of the 0th year

;; power draw would likely vary throughout each appliances cycles, cant find solid information about this however

(def dish-washer 
  {:power 1300
   :duration 120
   :run 0
   :started {:day 50
             :month 0
             :year 0
             :hour 0
             :minute 0
             :second 0}})

(def washing-machine
  {:power 400
   :duration 120
   :run 0
   :started {:day 50
             :month 0
             :year 0
             :hour 0
             :minute 0
             :second 0}})

(def dryer
  {:power 3000
   :duration 120
   :run 0
   :started {:day 50
             :month 0
             :year 0
             :hour 0
             :minute 0
             :second 0}})

(def appliances
  (atom {:dishwasher dish-washer
         :washingmachine washing-machine
         :dryer dryer
         ; :dw dish-washer
         ; :wm washing-machine
         ; :dry dryer
         ; :dw2 dish-washer
         }))