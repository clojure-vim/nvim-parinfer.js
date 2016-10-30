(ns nvim-parinfer.core
  (:require [clojure.string :as string]
            [parinfer :as parinfer]))

(defn dbg
  [msg & args]
  (if (exists? js/debug)
    (apply js/debug msg (map pr-str args))
    (apply js/console.log msg (map pr-str args)))
  (first args))

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

(defn- wrap-vim-interop
  [f]
  (fn [event]
    (-> event
      vim-dict->map
      f
      clj->js)))

(defn- process-reindent
  [event]
  (let [event-type (get event "event")
        mode (if (= event-type "BufEnter")
               "paren"
               (get event "mode"))
        [_ cursorLine cursorX _] (get event "position")
        lines (get event "lines")
        previewCursorScope? (some-> event
                              (get "preview_cursor_scope")
                              pos?)
        result (reindent
                 mode
                 (string/join "\n" lines)
                 #js {"cursorX" cursorX
                      "cursorLine" cursorLine
                      "previewCursorScope" previewCursorScope?})]
    (-> event
      (assoc "lines" (string/split (aget result "text") #"\n"))
      (assoc-in ["position" 2] (aget result "cursorX")))))

(def process
  (-> process-reindent
    wrap-vim-interop))
