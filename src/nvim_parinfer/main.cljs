(ns nvim-parinfer.main
 (:require
  [clojure.string :as string]
  [nvim-parinfer.core :as core]
  [parinfer :as parinfer]))

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
     (doto js/plugin
      (.functionSync "ParinferShift"
                    #js {:eval "[getline(1,line('$'))]"}
                    parinfer-shift)
      (.functionSync "ParinferProcessEvent"
                     (fn [nvim [event] nvim-callback]
                       (nvim-callback nil (core/process event))))))
   (catch :default e
     (core/dbg "main exception" e e.stack))))

(set! *main-cli-fn* -main)
