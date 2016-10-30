(ns nvim-parinfer.core
  (:require [clojure.string :as string]
            [parinfer :as parinfer]))

(def ^:private parinfer-mode-fn
  {"indent" parinfer/indentMode
   "paren" parinfer/parenMode})

(defn- reindent
  "Wrapper for *Mode, translating to/from JS structures."
  [mode text options]
  ((parinfer-mode-fn mode) text options))

(defn- vim-dict->map
  "js->clj doesn't want to translate Vim dictionaries for us, so we force
  the issue."
  [x]
  (into {} (for [k (.keys js/Object x)]
             [k (js->clj (aget x k))])))

(defn process
  [event]
  (let [event (vim-dict->map event)
        [_ cursorLine cursorX _] (get event "position")
        lines (get event "lines")
        previewCursorScope? (some-> event
                              (get "preview_cursor_scope")
                              pos?)
        result (reindent
                (get event "mode")
                (string/join "\n" lines)
                #js {"cursorX" cursorX
                     "cursorLine" cursorLine
                     "previewCursorScope" previewCursorScope?})]
    (-> event
      (assoc "lines" (string/split (aget result "text") #"\n"))
      (assoc-in ["position" 2] (aget result "cursorX"))
      clj->js)))
