(ns clojure-test-junit-output.core
  (:require [clojure.stacktrace :refer (print-cause-trace)]
            [clojure.string :as str]
            [clojure.test]
            [clojure.xml :as xml]
            [robert.hooke]))

(def ^:private testcase-initial-state {:tag :testcase
                                       :attrs {:name ""
                                               :time 0}})

(def ^:private testsuite-initial-state {:tag :testsuite
                                        :attrs {:name ""
                                                :tests 0
                                                :failures 0
                                                :errors 0
                                                :time 0}})

(def ^:private current-testcase (atom testcase-initial-state))

(def ^:private current-testsuite (atom testsuite-initial-state))

(def ^:private testsuites (atom {:tag :testsuites}))

(def ^:private junit-file (atom nil))

(defn- xml-str
  "Escape some troublesome characters in strings destined for XML output."
  [string]
  (str/replace string
               #"[<>\"']"
               (fn [x] (str "&#" (str (int (first x))) ";"))))

(defn- elapsed-seconds
  "Given the value from a previous call to System/nanoTime, compute the number
   of seconds which have elapsed since then."
  [start-nanotime]
  (/ (- (System/nanoTime) start-nanotime)
     1000000000.0))

(defmulti do-report
  "A mirror of clojure.test/do-report but which gathers information to format
   as JUnit output (and does the output, in the :summary action)."
  :type)

(defmethod do-report :end-test-ns
  [data]
  (swap! testsuites
         update :content conj
         (-> @current-testsuite
             (assoc-in [:attrs :name] (-> data :ns ns-name xml-str))
             (assoc-in [:attrs :time] (->> @current-testsuite
                                           :content
                                           (map (comp :time :attrs))
                                           (reduce +)))))
  (reset! current-testsuite testsuite-initial-state))

(defmethod do-report :begin-test-var
  [data]
  (swap! current-testcase assoc-in [:attrs :time] (System/nanoTime)))

(defmethod do-report :end-test-var
  [data]
  (swap! current-testsuite
         update :content conj
         (-> @current-testcase
             (assoc-in [:attrs :name] (-> data :var meta :name xml-str))
             (update-in [:attrs :time] elapsed-seconds)))
  (reset! current-testcase testcase-initial-state))

(defmethod do-report :summary
  [data]
  (spit @junit-file (with-out-str (xml/emit @testsuites))))

(defmethod do-report :default
  [data]
  (when (contains? #{:pass :fail :error} (:type data))
    (swap! current-testsuite update-in [:attrs :tests] inc)
    (when (= :error (:type data))
      (swap! current-testsuite update-in [:attrs :errors] inc)
      (swap! current-testcase
             update :content conj
             {:tag :error
              :attrs {:type (-> data :actual type str)
                      :message (-> data :actual .getMessage)}
              :content ["<![CDATA["
                        (with-out-str (-> data :actual print-cause-trace))
                        "]]>"]}))
    (when (= :fail (:type data))
      (swap! current-testsuite update-in [:attrs :failures] inc)
      (swap! current-testcase
             update :content conj
             {:tag :failure
              :attrs {:type "assertion failure"
                      :message (format "expected: %s actual: %s"
                                       (-> data :expected xml-str)
                                       (-> data :actual xml-str))}}))))

(defn do-report-hook
  "This is the robert.hooke hook function that is used to tap the input to
   clojure.test/do-report."
  [f & args]
  (do-report (into {} args))
  (apply f args))

(defn with-junit-output
  "Return a clojure.test fixture function which taps clojure.test/do-report in
   order to provide JUnit XML output to a file without affecting the normal
   output of the test runner.."
  [output-file]
  (fn [f]
    (reset! junit-file output-file)
    (robert.hooke/add-hook #'clojure.test/do-report #'do-report-hook)
    (f)))
