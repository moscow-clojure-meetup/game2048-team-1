(ns twentyfourtyeight.core
  (:require
    [rum.core :as rum]))

(enable-console-print!)

;; {[0 0] => 2048,
;;  [0 1] => nil,
;;  ...,
;;  :score => 1000}

(defn new-model [w h]
  (assoc {}
    :tiles (into {} (for [x (range 0 w)
                          y (range 0 h)]
                      [[x y] (rand-nth [nil 2 4 8 16 32 64 128 256 512 1024 2048])]))
    :width  w
    :height h
    :score  0))

(def *model (atom (new-model 4 4)))

(defn shift-left  [model] model)
(defn shift-right [model] model)
(defn shift-up    [model] model)
(defn shift-down  [model] model)

(rum/defc board [model]
  [:.board
    (for [y (range 0 (:height model))]
      [:.row
        (for [x (range 0 (:width model))
              :let [val (get (:tiles model) [x y])]]
          [:.cell {:class (when (some? val) (str "cell_" val))} val])])])

(rum/defc game []
  [:.game
    (board @*model)])

(defn ^:export refresh []
  (rum/mount (game) (js/document.getElementById "mount")))