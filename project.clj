(defproject nvim-parinfer "0.5.3"
 :description "A neovim parinfer plugin"
 :url "http://github.com/snoe/nvim-parinfer.js"

 :dependencies [[org.clojure/clojure "1.8.0"]
                [org.clojure/clojurescript "1.8.40"]]

 :plugins [[lein-cljsbuild "1.1.2"]
           [lein-npm "0.6.1"]
           [lein-doo "0.1.7"]]

 :npm {:dependencies [[parinfer "1.8.1"]]}

 :clean-targets ["rplugin/node/nvim-parinfer" "rplugin/node/nvim-parinfer.js"]

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
                      {:id "test"
                       :source-paths ["src" "test"]
                       :compiler {:main nvim-parinfer.test-runner
                                  :asset-path "target/doo/test"
                                  :hashbang false
                                  :output-to "target/doo/test.js"
                                  :output-dir "target/doo/test"
                                  :optimizations :simple
                                  :target :nodejs
                                  :cache-analysis true
                                  :foreign-libs [{:file "node_modules/parinfer/parinfer.js"
                                                  :provides ["parinfer"]
                                                  :module-type :commonjs}]
                                  :closure-warnings {:const :off}
                                  :source-map "target/doo/test.js.map"}}]})
