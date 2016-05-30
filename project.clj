(defproject nvim-parinfer "0.5.1"
  :description "A neovim parinfer plugin"
  :url "http://github.com/snoe/nvim-parinfer.js"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [org.clojure/core.async "0.2.374" :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-npm "0.6.1"]]

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
                                   :source-map "rplugin/node/nvim-parinfer.js.map"}}]})
