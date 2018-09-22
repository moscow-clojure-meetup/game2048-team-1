(ns twentyfourtyeight.core)

(enable-console-print!)

;; {[0 0] => 2048,
;;  [0 1] => nil, ...}
(def model
  (into {}
    (for [x (range 0 4)
          y (range 0 4)]
      [[x y] nil])))

(defn ^:export refresh []
  (println "Hello"))