(ns nvim-parinfer.main
 (:require
  [clojure.string :as string]
  [parinfer :as parinfer]))

(defn dbg
  [msg & args]
  (if (exists? js/debug)
    (apply js/debug msg (map pr-str args))
    (apply js/console.log msg (map pr-str args)))
  (first args))

(defn split-lines [s]
  (when s
    (.split s #"\r?\n")))

(defn new-line? [current-line]
  (re-matches #"\s*\S$" current-line))

(defn run-indent [current-text opts mode current-line]
  (cond
   (= "indent" mode)
   (parinfer/indentMode current-text opts)

   (= "paren" mode)
   (parinfer/parenMode current-text opts)

   (= "hybrid" mode)
   (if (new-line? current-line)
     (parinfer/indentMode current-text opts)
     (let [paren (parinfer/parenMode current-text opts)]
       (parinfer/indentMode (.-text paren) opts)))))

(defn format-lines [current-lines cursor-x cursor-line bufnum mode prev-cursor-scope cursor-dx]
  (try
   (let [opts (clj->js (merge
                        {"cursorX" (dec cursor-x)
                         "cursorLine" (dec cursor-line)
                         "cursorDx" cursor-dx}
                        (when (= 1 prev-cursor-scope)
                            {"previewCursorScope" true})))
         current-text (.join current-lines "\n")
         result (run-indent current-text opts mode (get current-lines (dec cursor-line)))
         new-text (.-text result)]
    (when (not= current-text new-text)
     (split-lines new-text)))

   (catch :default e
     (dbg "EXCEPTION" e e.stack))))

(defn parinfer-indent
  [nvim args [[_ cursor-line cursor-x _] bufnum lines mode prev-cursor-scope normal-cmd reg-minus] nvim-callback]
  (let [start (js/Date.)
        cursor-dx (if (string/includes? "cd" normal-cmd) reg-minus 0)]
    (if-let [new-lines (format-lines lines cursor-x cursor-line bufnum mode prev-cursor-scope cursor-dx)]
      (do
       #_(js/debug "c" (- (.getTime (js/Date.)) (.getTime start)))
       (nvim-callback nil new-lines))
      (do
       #_(js/debug "n" (- (.getTime (js/Date.)) (.getTime start)))
       (nvim-callback nil #js [])))))

(defn shift-command [startline endline shift-amount shift-op]
  (str "let l:oldsw=&shiftwidth"
       "|set shiftwidth=" shift-amount
       "|exe \"" shift-op "\""
       "|let &shiftwidth=l:oldsw"))

(defn parinfer-shift
  [nvim [shift-op startline endline] [lines] nvim-callback]
  (let [result (run-indent (.join lines "\n")
                           #js {"cursorX" 0 "cursorLine" (dec startline)}
                           "indent"
                           nil)
        tab-stops (aget result "tabStops")
        first-line (get lines (dec startline))
        first-char (.search first-line #"\S")
        stops (sort (cons 0 (map (fn [ts] (inc (aget ts "x"))) tab-stops)))]
    (if-let [dynamic-shift-op
             (if (string/includes? shift-op "<")
               (when-let [prev-stop (last (filter (partial > first-char) stops))]
                 (shift-command startline endline (- first-char prev-stop) shift-op))
               (when-let [next-stop (first (filter (partial < first-char) stops))]
                 (shift-command startline endline (- next-stop first-char) shift-op)))]
      (.command nvim dynamic-shift-op
                (fn [& args]
                  (nvim-callback nil true)))
      (.command nvim shift-op
                (fn [& args]
                  (nvim-callback nil true))))))

(defn -main []
  (try
   (when (exists? js/plugin)
     (js/debug "hello parinfer")
     (.functionSync js/plugin "ParinferIndent"
                    #js {:eval "[getpos('.'), bufnr('.'), getline(1,line('$')), g:parinfer_mode, g:parinfer_preview_cursor_scope, v:operator, -strlen(@-)]"}
                    parinfer-indent)
     (.functionSync js/plugin "ParinferShift"
                   #js {:eval "[getline(1,line('$'))]"}
                   parinfer-shift))

   (catch :default e
     (dbg "main exception" e e.stack))))

(set! *main-cli-fn* -main)
