(defproject twentyfourtyeight "0.1.0"
  :dependencies
  [[org.clojure/clojure       "1.9.0"    :scope "provided"]
   [org.clojure/clojurescript "1.10.339" :scope "provided"]
   [rum                       "0.11.2"]]

  :plugins
  [[lein-cljsbuild "1.1.7"]
   [lein-figwheel "0.5.16"]]
  :figwheel {
    :repl false
  }
  :cljsbuild
  { :builds
    [{ :id "advanced"
       :source-paths ["src"]
       :compiler
       { :main           twentyfourtyeight.core
         :output-to      "resources/public/main.js"
         :optimizations  :advanced
         :source-map     "resources/public/main.js.map"
         :pretty-print   false
         :compiler-stats true
         :parallel-build true }}
     
     { :id "none"
       :source-paths ["src"]
       :compiler
       { :main           twentyfourtyeight.core
         :output-to      "resources/public/main.js"
         :output-dir     "resources/public/none"
         :asset-path     "none"
         :optimizations  :none
         :source-map     true
         :compiler-stats true
         :parallel-build true }       
       :figwheel {
         :on-jsload "twentyfourtyeight.core/refresh"
       }}]}
)
