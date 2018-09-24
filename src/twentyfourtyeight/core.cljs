(ns twentyfourtyeight.core
  (:require
    [rum.core :as rum]))

(enable-console-print!)

(defn add-tile [model]
  (let [free (for [x (range 0 (:width model))
                   y (range 0 (:height model))
                :when (nil? (get (:tiles model) [x y]))]
               [x y])]
    (update model :tiles assoc (rand-nth free) (rand-nth [2 4]))))

;; {[0 0] => 32}
(defn new-model [w h]
  (-> {}
    (assoc
      :tiles  (into {} (for [x (range 0 w)
                            y (range 0 h)]
                        [[x y] nil]))
      :width  w
      :height h
      :score  0
      :best   0
      :game-over? false)
    (add-tile)))

(def *model (atom (new-model 4 4)))

(defn collapse [vals]
  (loop [i 0
         res []]
    (cond
      (== i (count vals))
      res
      (== i (dec (count vals)))
      (recur (inc i) (conj res (nth vals i)))

      (= (nth vals i) (nth vals (inc i)))
      (recur (+ i 2) (conj res (+ (nth vals i) (nth vals (inc i)))))

      :else
      (recur (inc i) (conj res (nth vals i))))))

(defn shift-left [model]
  (update model :tiles
    (fn [tiles]
      (into {}
        (for [y (range 0 (:height model))
              :let [vals (for [x     (range (dec (:width model)) -1 -1)
                               :let  [val (get tiles [x y])]
                               :when (some? val)]
                           val)
                    vals' (collapse vals)]
              i (range 0 (count vals'))]
          [[(- (count vals') 1 i) y] (nth vals' i)])))))

(defn shift-right [model]
  (update model :tiles
    (fn [tiles]
      (into {}
        (for [y (range 0 (:height model))
              :let [vals (for [x (range 0 (:width model))
                               :let [val (get tiles [x y])]
                               :when (some? val)]
                           val)
                    vals' (collapse vals)]
              i (range 0 (count vals'))]
          [[(+ (:width model) (- (count vals')) i) y] (nth vals' i)])))))

(defn shift-up [model]
  (update model :tiles
    (fn [tiles]
      (into {}
        (for [x (range 0 (:width model))
              :let [vals (for [y     (range (dec (:height model)) -1 -1)
                               :let  [val (get tiles [x y])]
                               :when (some? val)]
                           val)
                    vals' (collapse vals)]
              i (range 0 (count vals'))]
          [[x (- (count vals') 1 i)] (nth vals' i)])))))

(defn shift-down [model]
  (update model :tiles
    (fn [tiles]
      (into {}
        (for [x (range 0 (:width model))
              :let [vals (for [y (range 0 (:height model))
                               :let [val (get tiles [x y])]
                               :when (some? val)]
                           val)
                    vals' (collapse vals)]
              i (range 0 (count vals'))]
          [[x (+ (:height model) (- (count vals')) i)] (nth vals' i)])))))

(defn game-over? [model]
  (empty? (for [x (range 0 (:width model))
                y (range 0 (:height model))
                :when (nil? (get (:tiles model) [x y]))]
            [x y])))

(rum/defc board [model]
  [:.board
    (when (:game-over? model)
      [:.game-over "Game over"])
    (for [y (range 0 (:height model))]
      [:.row
        (for [x (range 0 (:width model))
              :let [val (get (:tiles model) [x y])]]
          [:.cell {:class (when (some? val) (str "cell_" val))} val])])])

(defn new-game [model]
  (cond-> (new-model (:width model) (:height model))
    (> (:score model) (:best model)) (assoc :best (:score model))))

(defn make-turn! [f]
  (when-not (:game-over? @*model)
    (swap! *model
      (fn [model]
        (let [model' (f model)]
          (cond
            (= model model')    model
            (game-over? model') (assoc model' :game-over? true)
            :else               (-> model' add-tile)))))))

(defn handle-key [e]
  (case (.-keyCode e)
    37 (make-turn! shift-left)
    38 (make-turn! shift-up)
    39 (make-turn! shift-right)
    40 (make-turn! shift-down)
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
