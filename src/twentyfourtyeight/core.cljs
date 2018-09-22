(ns twentyfourtyeight.core
  (:require
    [rum.core :as rum]))

(enable-console-print!)

(defn add-tile [tiles]
  (let [free (for [[coord val] tiles
                :when (nil? val)]
                coord)]
    (if (empty? free)
      tiles
      (assoc tiles (rand-nth free) (rand-nth [2 4])))))

(defn new-model [w h]
  (assoc {}
    :tiles  (-> (into {} (for [x (range 0 w)
                               y (range 0 h)]
                           [[x y] nil #_(rand-nth [nil 2 4 8 16 32 64 128 256 512 1024 2048])]))
                (add-tile))
    :width  w
    :height h
    :score  0
    :best   0))

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

(defn new-game [model]
  (cond-> (new-model (:width model) (:height model))
    (> (:score model) (:best model)) (assoc :best (:score model))))

(defn handle-key [e]
  (case (.-keyCode e)
    37 (swap! *model shift-left)
    38 (swap! *model shift-up)
    39 (swap! *model shift-right)
    40 (swap! *model shift-down)
    nil))

(rum/defc game
  < rum/reactive
  []
  (let [model (rum/react *model)]
    [:.game
      [:.header
        [:button.newgame {:on-click (fn [_] (reset! *model (new-game model)))} "New game"]
        [:.score [:.score_title "Score"] [:.score_value (:score model)]]
        [:.score [:.score_title "Best"] [:.score_value (:best model)]]]
      (board model)]))

(rum/defc main
  < {:before-render
     (fn [state]
       (when-some [handler (.-keydown js/document)]
         (js/document.removeEventListener "keydown" handler))
       (js/document.addEventListener "keydown" handle-key)
       (set! js/document.keydown handle-key)
       state)}
  []
  (game))

(defn ^:export refresh []
  (rum/mount (main) (js/document.getElementById "mount")))