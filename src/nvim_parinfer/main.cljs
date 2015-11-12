(ns nvim-parinfer.main
  (:require [parinfer.indent-mode :as indent-mode]
            [clojure.string :as string]))

(defn dbg
  ([v]
   (dbg "DEBUG" v))
  ([msg v]
   (if (exists? js/debug)
     (js/debug msg (pr-str v))
     (js/console.log msg (pr-str v)))
   v))

(def buffer-results (atom {}))

(defn diff [old-text new-text]
  (try
   (let [differ (if (exists? js/JsDiff) js/JsDiff (js/require "diff"))
         diff (.structuredPatch differ "old" "old" old-text new-text "old" "new" #js {"context" 0})
         hunk (first (aget diff "hunks"))
         start (dec (aget hunk "oldStart"))
         num-lines (aget hunk "oldLines")
         lines (js->clj (aget hunk "lines"))
         change {:line-no [start (+ start num-lines)]
                 :new-line (map (fn [line] (subs line 1)) (filter (fn [line] (= \+ (first line))) lines))}]

     ;; TODO deal with multiple hunks?
     (when (> (count (aget diff "hunks")) 1)
       (dbg "EXTRA HUNKS"))

     (comment
      (dbg jdiff)
      (dbg diff)
      (dbg (= jdiff diff))
      (js/debug diff)
      (js/debug hunk)
      (js/debug start)
      (js/debug num-lines)
      (js/debug lines)
      (dbg "change" change))
     change)
   (catch js/Error e
     (dbg "DIFF EXCEPTION" e))))

(defn format-text [new-lines [_ cursor-line cursor-x _] buffer-results bufnum]
  (try
   (let [new-text (string/join "\n" new-lines)
         opts {:cursor-x (dec cursor-x) :cursor-line (dec cursor-line)}
         res (get @buffer-results bufnum)
         old-text (:text res)]
     (when (not= old-text new-text)
       (if res
         (swap! buffer-results assoc bufnum (indent-mode/format-text-change new-text (:state res) (diff old-text new-text) opts))
         (swap! buffer-results assoc bufnum (indent-mode/format-text new-text opts)))
       (when (not= (:text res) (:text (get @buffer-results bufnum)))
         (get @buffer-results bufnum))))
   (catch js/Error e
     (dbg "EXCEPTION" e))))

(defn format-buffer [nvim cursor]
  (.getCurrentBuffer nvim
                     (fn [err buf]
                       (.getNumber buf (fn [err bufnum]
                                         (.getLineSlice buf 0 -1 true true
                                                        (fn [err lines]
                                                          (when-let [new-result (format-text lines cursor buffer-results bufnum)]
                                                            (->> new-result
                                                                 (:text)
                                                                 (string/split-lines)
                                                                 (clj->js)
                                                                 (.setLineSlice buf 0 -1 true true))))))))))


(when (exists? js/plugin)
  (js/debug "hello")
  (.autocmdSync js/plugin "BufEnter" #js {:pattern "*.cljs,*.clj,*.edn" :eval "getpos('.')"} format-buffer)
  (.autocmdSync js/plugin "TextChanged,TextChangedI" #js {:pattern "*.cljs,*.clj,*.edn" :eval "getpos('.')"} format-buffer))

(defn -main []
  #_
  (do
   (dbg (format-text ["hello" "yo" "xworld"] [0 0]))
   (dbg (format-text ["(hello" "yo" "yworld"] [0 0]))))

(set! *main-cli-fn* -main)
