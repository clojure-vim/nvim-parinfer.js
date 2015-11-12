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

(def result (atom nil))

(defn diff [old-text new-text]
  (try
   ;; TODO deal with multiple hunks?)
   (let [differ (if (exists? js/JsDiff) js/JsDiff (js/require "diff"))
         diff (.structuredPatch differ "old" "old" old-text new-text "old" "new" #js {"context" 0})
         hunk (first (aget diff "hunks"))
         start (dec (aget hunk "oldStart"))
         num-lines (aget hunk "oldLines")
         lines (js->clj (aget hunk "lines"))
         change {:line-no [start (+ start num-lines)]
                 :new-line (map (fn [line] (subs line 1)) (filter (fn [line] (= \+ (first line))) lines))}]
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
      (js/debug lines))
     (dbg "change" change)
     change)
   (catch js/Error e
     (dbg "DIFF EXCEPTION" e))))

(defn format-text [new-lines [_ cursor-line cursor-x _]]
  (try
   (let [new-text (string/join "\n" new-lines)
         opts (dbg "cursor" {:cursor-x (dec cursor-x) :cursor-line (dec cursor-line)})
         res @result
         old-text (:text res)]
     (when (not= old-text new-text)
       (if res
         (reset! result (indent-mode/format-text-change new-text (:state res) (diff old-text new-text) opts))
         (reset! result (indent-mode/format-text new-text opts)))
       (when (not= (:text res) (:text @result))
         @result)))
   (catch js/Error e
     (dbg "EXCEPTION" e))))

(defn format-buffer [nvim cursor]
  (.getCurrentBuffer nvim
                     (fn [err buf]
                       (.getLineSlice buf 0 -1 true true
                                      (fn [err lines]

                                        (when-let [new-result (format-text lines cursor)]
                                          (->> new-result
                                               (:text)
                                               (string/split-lines)
                                               (clj->js)
                                               (.setLineSlice buf 0 -1 true true))))))))

(when (exists? js/plugin)
  (js/debug "hello")
  (.autocmdSync js/plugin "BufEnter" #js {:pattern "*.cljs" :eval "getpos('.')"} format-buffer)
  (.autocmdSync js/plugin "TextChanged,TextChangedI" #js {:pattern "*.cljs"  :eval "getpos('.')"} format-buffer))

(defn -main []
  #_
  (do
   (dbg (format-text ["hello" "yo" "xworld"] [0 0]))
   (dbg (format-text ["(hello" "yo" "yworld"] [0 0]))))

(set! *main-cli-fn* -main)
