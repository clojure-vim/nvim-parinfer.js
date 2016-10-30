(ns nvim-parinfer.test-runner
  (:require [cljs.test :as test]
            [doo.runner :refer-macros [doo-tests]]
            [nvim-parinfer.change-test]))

(doo-tests 'nvim-parinfer.change-test)
