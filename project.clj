(defproject nvim-parinfer "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [parinfer "0.1.0-SNAPSHOT"]]

  :npm
  {:dependencies [[source-map-support "0.3.3"]
                  [neovim-client "1.0.5"]
                  [diff "2.2.0"]]}

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-npm "0.6.1"]]

  :source-paths ["src" "target/classes"]

  :clean-targets ["lib/nvim-parinfer" "lib/nvim-parinfer.js"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:main nvim-parinfer.main
                                   :asset-path "lib/nvim-parinfer"
                                   :hashbang false
                                   :output-to "lib/nvim-parinfer.js"
                                   :output-dir "lib/nvim-parinfer"
                                   :optimizations :simple
                                   :target :nodejs
                                   :cache-analysis true
                                   :source-map "lib/nvim-parinfer.js.map"}}
                       #_
                       {:id "lib"
                        :source-paths ["src"]
                        :compiler {:main nvim-parinfer.main
                                   :output-to "lib/nvim-parinfer.js"
                                   :output-dir "lib/nvim-parinfer"
                                   :optimizations :simple
                                   :target :nodejs
                                   :cache-analysis true
                                   :source-map "lib/nvim-parinfer.js.map"}}]})
