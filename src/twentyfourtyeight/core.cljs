(ns twentyfourtyeight.core
  (:require
    [rum.core :as rum]))

(enable-console-print!)

(defn add-tile [tiles]
  (let [free (for [[coord val] tiles
                :when (nil? val)]
                coord)]
    (assoc tiles (rand-nth free) (rand-nth [2 4]))))

(defn new-model [w h]
  (assoc {}
    :tiles  (-> (into {} (for [x (range 0 w)
                               y (range 0 h)]
                           [[x y] nil #_(rand-nth [nil 2 4 8 16 32 64 128 256 512 1024 2048])]))
                (add-tile))
    :width  w
    :height h
    :score  0
    :best   0
    :game-over? false))

(def *model (atom (new-model 4 4)))

(defn modinc [x m] (mod (inc x) m))
(defn moddec [x m] (mod (+ x (dec m)) m))

(defn shift-left  [model]
  (let [{:keys [tiles width height]} model]
    (assoc model :tiles
      (into {}
        (for [[[x y] v] tiles]
          [[(moddec x width) y] v])))))

(defn shift-right [model]
  (let [{:keys [tiles width height]} model]
    (assoc model :tiles
      (into {}
        (for [[[x y] v] tiles]
          [[(modinc x width) y] v])))))

(defn shift-up* [model]
  (assoc model
         :tiles (reduce-kv (fn [acc [x y] v]
                             (let [new-x x
                                   new-y (dec y)
                                   new-value (get acc [x y])
                                   old-value (get acc [x new-y])]
                               (merge (dissoc acc [x y])
                                      (if (and (= old-value
                                                  new-value)
                                               (> y 0))
                                        (if old-value
                                          {[x new-y] (* v 2)}
                                          {[x new-y] v})
                                        {[x y] v}))))
                           {}
                           (:tiles model))))

(defn shift-down* [model]
  (assoc model
         :tiles (reduce-kv (fn [acc [x y] v]
                             (let [new-x x
                                   new-y (inc y)
                                   new-value (get acc [x y])
                                   old-value (get acc [x new-y])]
                               (merge (dissoc acc [x y])
                                      (if (and (= old-value
                                                  new-value)
                                               (< y (dec (:height model))))
                                        (if old-value
                                          {[x new-y] (* v 2)}
                                          {[x new-y] v})
                                        {[x y] v}))))
                           {}
                           (into {} (reverse (:tiles model))))))

(defn shift* [shift-function model]
  (loop [current-model model]
    (let [new-model (shift-function current-model)]
      (if (= (:tiles new-model)
             (:tiles current-model))
        new-model
        (recur new-model)))))

(def shift-up (partial shift* shift-up*))
(def shift-down (partial shift* shift-down*))

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
          (if (game-over? model')
            (assoc model' :game-over? true)
            (update model' :tiles add-tile)))))))

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
