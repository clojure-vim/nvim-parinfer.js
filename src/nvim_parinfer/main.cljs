(ns nvim-parinfer.main
  (:require [parinfer.indent-mode :as indent-mode]
            [clojure.string :as string]))

(defn dbg [v]
  (if (exists? js/debug)
    (js/debug (pr-str v))
    (js/console.log (pr-str v)))
  v)


(def result (atom nil))

(defn diff [old-text new-text]
  ;; TODO deal with multiple hunks?
  (let [differ (if (exists? js/JsDiff) js/JsDiff (js/require "diff"))
        diff (dbg (js->clj (.structuredPatch differ "old" "old" old-text new-text "old" "new" #js {"context" 0})))
        hunk (first (get diff "hunks"))
        start (dec (get hunk "oldStart"))
        num-lines (get hunk "oldLines")
        lines (get hunk "lines")]
    (dbg {:line-no [start (+ start num-lines)]
          :new-line (map (fn [line] (subs line 1)) (filter (fn [line] (= \+ (first line))) lines))})))

(defn format-text [new-lines]
  (let [new-text (string/join "\n" new-lines)
        res @result]
    ;; TODO pass in cursor opts
    (if res
      (reset! result (indent-mode/format-text-change new-text (:state res) (diff (:text res) new-text)))
      (reset! result (indent-mode/format-text new-text)))
    (when (not= (:text res) (:text @result))
      @result)))

(defn format-buffer [nvim]
  (.getCurrentBuffer nvim
                     (fn [err buf]
                       (.getLineSlice buf 0 -1 true true
                                      (fn [err lines]
                                        (when-let [new-result (format-text lines)]
                                          (->> new-result
                                               (:text)
                                               (string/split-lines)
                                               (clj->js)
                                               (.setLineSlice buf 0 -1 true true))))))))

(when (exists? js/plugin)
  (js/debug "hello")
  (.autocmdSync js/plugin "BufEnter" #js {:pattern "*.xcljs" :eval "expand(\"<afile>\")"} format-buffer)
  (.autocmdSync js/plugin "TextChanged,TextChangedI" #js {:pattern "*.xcljs"} format-buffer))

(defn -main []
  (js/console.log (diff "hello\nyo\nworld" "hell\nyo")))

(set! *main-cli-fn* -main)

