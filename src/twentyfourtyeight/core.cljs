(ns twentyfourtyeight.core)

(enable-console-print!)

;; {[0 0] => 2048,
;;  [0 1] => nil,
;;  ...,
;;  :score => 1000}
(def model
  (assoc
    (into {}
      (for [x (range 0 4)
            y (range 0 4)]
        [[x y] nil]))
    :score 0))

(defn ^:export refresh []
  (println "Hello"))