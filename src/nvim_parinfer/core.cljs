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

(defn text-changed
  [event]
  (let [[cursorLine cursorX] (get event "position")
        lines (get event "lines")
        previewCursorScope? (some-> event
                              (get "parinfer_preview_cursor_scope")
                              pos?)
        result (reindent
                (get event "parinfer_mode")
                (string/join "\n" lines)
                #js {"cursorX" cursorX
                     "cursorLine" cursorLine
                     "previewCursorScope" previewCursorScope?})]
    (-> event
      (assoc "lines" (string/split (aget result "text") #"\n"))
      (assoc-in ["position" 1] (aget result "cursorX")))))
