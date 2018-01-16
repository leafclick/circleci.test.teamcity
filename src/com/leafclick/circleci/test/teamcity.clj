(ns com.leafclick.circleci.test.teamcity
  (:require
    [clojure.stacktrace :as stack]
    [clojure.string :as str]
    [clojure.test :as test]
    [circleci.test.report :as circleci]))

(defn escape
  [s]
  (when s
    (str/replace s #"['|\n\r\[\]]"
                 (fn [x]
                   (cond (= x "\n") "|n"
                         (= x "\r") "|r"
                         :else (str "|" x))))))

(defprotocol TestDotCheckTrialReporter
  (trial [this m]))

(deftype ClojureDotTestTeamcityReporter []
  circleci/TestReporter
  (default [this m]
    (test/with-test-out (prn m)))

  (pass [this m]
    (test/with-test-out (test/inc-report-counter :pass)))

  (fail [this m]
    (test/with-test-out
      (test/inc-report-counter :fail)
      (println (str "##teamcity[testFailed name='" (circleci/testing-vars-str m) "'"
                    " message='" (escape (:message m)) "'"
                    (when (seq test/*testing-contexts*)
                      (str " details='" (escape (str (:message m) "\n" (circleci/testing-contexts-str))) "'"))
                    " type='comparisonFailure'"
                    " expected='" (escape (pr-str (:expected m))) "'"
                    " actual='" (escape (pr-str (:actual m))) "'"
                    "]"))))

  (error [this m]
    (test/with-test-out
      (test/inc-report-counter :error)
      (println (str "##teamcity[testFailed name='" (circleci/testing-vars-str m) "'"
                    " message='" (escape (:message m)) "'"
                    " details='" (escape (str (:message m) "\n"
                                              (when (seq test/*testing-contexts*)
                                                (str (circleci/testing-contexts-str) "\n"))
                                              "expected='" (pr-str (:expected m)) "'\n"
                                              "actual='"
                                              (let [actual (:actual m)]
                                                (if (instance? Throwable actual)
                                                  (with-out-str
                                                    (stack/print-cause-trace actual test/*stack-trace-depth*))
                                                  (pr-str actual)))
                                              "'"))
                    "]"))))

  (summary [this m]
    (test/with-test-out
      (println "\nRan" (:test m) "tests containing"
               (+ (:pass m) (:fail m) (:error m)) "assertions.")
      (println (:fail m) "failures," (:error m) "errors.")))

  (begin-test-ns [this m]
    (test/with-test-out
      (println (str "##teamcity[testSuiteStarted name='" (ns-name (:ns m)) "']"))))

  (end-test-ns [this m]
    (println (str "##teamcity[testSuiteFinished name='" (ns-name (:ns m)) "']")))

  (begin-test-var [this m]
    (test/with-test-out
      (test/inc-report-counter :test)
      (println (str "##teamcity[testStarted name='" (circleci/testing-vars-str m) "' captureStandardOutput='true']"))))

  (end-test-var [this m]
    (println (str "##teamcity[testFinished name='" (circleci/testing-vars-str m) "']")))

  TestDotCheckTrialReporter
  (trial [this m]
    (let [progress (:clojure.test.check.clojure-test/trial m)]
      (println (str "##teamcity[progressMessage 'trial " (first progress) "/" (second progress) "']")))))

(defn teamcity-reporter [_config]
  (->ClojureDotTestTeamcityReporter))

(defmethod circleci/report :clojure.test.check.clojure-test/trial [m]
  (doseq [reporter circleci/*reporters*]
    (if (satisfies? TestDotCheckTrialReporter reporter)
      (trial reporter m)
      (circleci/default reporter m))))
