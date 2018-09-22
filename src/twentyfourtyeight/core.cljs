(ns twentyfourtyeight.core
  (:require
    [rum.core :as rum]))

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

(rum/defc game []
  [:div "Hello world"])

(defn ^:export refresh []
  (rum/mount (game) (js/document.getElementById "mount")))