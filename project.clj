(defproject nvim-parinfer "0.6.0"
 :description "A neovim parinfer plugin"
 :url "http://github.com/snoe/nvim-parinfer.js"

 :dependencies [[org.clojure/clojure "1.8.0"]
                [org.clojure/clojurescript "1.8.40"]]

 :npm {:dependencies [[parinfer "1.8.1"]
                      [source-map-support "0.3.3"]
                      [ws "1.0.1"]
                      [neovim "3.5.0"]]}

 :plugins [[lein-cljsbuild "1.1.2"]
           [lein-figwheel "0.5.0-6"]
           [lein-npm "0.6.1"]]

 :source-paths ["src" "target/classes"]

 :test-paths ["test"]

 :clean-targets ["rplugin/node/nvim-parinfer" "rplugin/node/nvim-parinfer.js"]

 :figwheel {:server-port 9444}

 :cljsbuild {:builds [{:id "plugin"
                       :source-paths ["src"]
                       :compiler {:main nvim-parinfer.main
                                  :asset-path "rplugin/node/nvim-parinfer"
                                  :hashbang false
                                  :output-to "rplugin/node/nvim-parinfer.js"
                                  :output-dir "rplugin/node/nvim-parinfer"
                                  :optimizations :simple
                                  :target :nodejs
                                  :cache-analysis true
                                  :foreign-libs [{:file "node_modules/parinfer/parinfer.js"
                                                  :provides ["parinfer"]
                                                  :module-type :commonjs}]
                                  :closure-warnings {:const :off}
                                  :externs ["plugin.externs.js"]
                                  :source-map "rplugin/node/nvim-parinfer.js.map"}}
                      {:id "fig-test"
                       :source-paths ["src" "test"]
                       :figwheel {:on-jsload "nvim-parinfer.test-runner/test-it"}
                       :compiler {:main nvim-parinfer.test-runner
                                  :output-to "target/out/tests.js"
                                  :output-dir "target/out"
                                  :target :nodejs
                                  :optimizations :none
                                  :foreign-libs [{:file "node_modules/parinfer/parinfer.js"
                                                  :provides ["parinfer"]
                                                  :module-type :commonjs}]
                                  :source-map true}}]})
