(defproject nvim-parinfer "0.1.1-SNAPSHOT"
  :description "A neovim parinfer plugin"
  :url "http://github.com/snoe/nvim-parinfer.js"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [parinfer "0.1.0-SNAPSHOT"]
                 [org.clojure/core.async "0.2.374" :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0"]]

  :source-paths ["src" "target/classes"]

  :test-paths ["test"]

  :clean-targets ["lib/nvim-parinfer" "lib/nvim-parinfer.js" "target"]

  :figwheel {:server-port 9443}

  :cljsbuild {:builds [{:id "plugin"
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
                       {:id "fig-test"
                        :source-paths ["src" "test"]
                        :figwheel {:on-jsload "nvim-parinfer.main-test/test-it"}
                        :compiler {:main nvim-parinfer.main-test
                                   :output-to "target/out/tests.js"
                                   :output-dir "target/out"
                                   :target :nodejs
                                   :optimizations :none
                                   :source-map true}}]})
