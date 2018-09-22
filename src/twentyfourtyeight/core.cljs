(ns twentyfourtyeight.core
  (:require
    [rum.core :as rum]))

(enable-console-print!)

;; {[0 0] => 2048,
;;  [0 1] => nil,
;;  ...,
;;  :score => 1000}

(defn new-model [w h]
  (assoc
    (into {}
      (for [x (range 0 w)
            y (range 0 h)]
        [[x y] nil]))
    :width  w
    :height h
    :score  0
    [2 1]   2
    [1 3]   2048))

(def *model (atom (new-model 4 4)))

(rum/defc board [model]
  [:.board
    (for [y (range 0 (:height model))]
      [:.row
        (for [x (range 0 (:width model))]
          [:.cell (get model [x y])])])])

(rum/defc game []
  [:.game
    (board @*model)])

(defn ^:export refresh []
  (rum/mount (game) (js/document.getElementById "mount")))