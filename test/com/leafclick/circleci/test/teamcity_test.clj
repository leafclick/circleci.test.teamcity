(ns com.leafclick.circleci.test.teamcity-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [circleci.test]
            [com.leafclick.circleci.test.teamcity :refer :all]
            [clojure.string :as str])
  (:import (java.io File)))

(defn teamcity-test-messages
  [s]
  (re-seq #"##teamcity.*" s))

(defn lein-test
  [test-project-root]
  (spit (str test-project-root "/.lein-classpath") (.getCanonicalPath (File. "src")))
  (sh "lein" "test" :dir test-project-root))

(deftest report-all-passing
  (let [{:keys [exit out]} (lein-test "./test-projects/all-pass")]
    (is (= 0 exit))
    (is (= ["##teamcity[testSuiteStarted name='all-pass.core-test']"
            "##teamcity[testStarted name='all-pass.core-test/ (a-test) (:)' captureStandardOutput='true']"
            "##teamcity[testFinished name='all-pass.core-test/ (a-test) (:)']"
            "##teamcity[testSuiteFinished name='all-pass.core-test']"]
          (teamcity-test-messages out)))))

(deftest report-failure
  (let [{:keys [exit out]} (lein-test "./test-projects/one-failure")]
    (is (= 1 exit))
    (is (= ["##teamcity[testSuiteStarted name='one-failure.core-test']"
            "##teamcity[testStarted name='one-failure.core-test/ (a-failing-test) (:)' captureStandardOutput='true']"
            "##teamcity[testFailed name='one-failure.core-test/ (a-failing-test) (core_test.clj:7)' message='' details='|nI fail.' type='comparisonFailure' expected='(= 0 1)' actual='(not (= 0 1))']"
            "##teamcity[testFinished name='one-failure.core-test/ (a-failing-test) (:)']"
            "##teamcity[testSuiteFinished name='one-failure.core-test']"]
           (teamcity-test-messages out)))))

(deftest report-error
  (let [{:keys [exit out]} (lein-test "./test-projects/one-error")
        messages (teamcity-test-messages out)]
    (is (= 1 exit))
    (is (= 5 (count messages)))
    (is (= ["##teamcity[testSuiteStarted name='one-error.core-test']"
            "##teamcity[testStarted name='one-error.core-test/ (an-erroring-test) (:)' captureStandardOutput='true']"]
           (take 2 messages)))
    (is (str/starts-with? (nth messages 2) "##teamcity[testFailed name='one-error.core-test/ (an-erroring-test) (core_test.clj:6)' message='Uncaught exception, not in assertion.' details='Uncaught exception, not in assertion.|nexpected=|'nil|'|nactual=|'java.lang.Exception: ERROR|n at one_error.core_test/fn (core_test.clj:6)|n"))
    (is (str/ends-with? (nth messages 2) "clojure.main.main (main.java:37)|n|'']"))
    (is (= "##teamcity[testSuiteFinished name='one-error.core-test']" (last messages)))))

(deftest report-test-check-all-passing
  (let [{:keys [exit out]} (lein-test "./test-projects/test-check-all-pass")]
    (is (= 0 exit))
    (is (= ["##teamcity[testSuiteStarted name='test-check-all-pass.core-test']"
            "##teamcity[testStarted name='test-check-all-pass.core-test/ (crypto-should-roundtrip) (:)' captureStandardOutput='true']"
            "##teamcity[progressMessage 'trial 0/10']"
            "##teamcity[progressMessage 'trial 1/10']"
            "##teamcity[progressMessage 'trial 2/10']"
            "##teamcity[progressMessage 'trial 3/10']"
            "##teamcity[progressMessage 'trial 4/10']"
            "##teamcity[progressMessage 'trial 5/10']"
            "##teamcity[progressMessage 'trial 6/10']"
            "##teamcity[progressMessage 'trial 7/10']"
            "##teamcity[progressMessage 'trial 8/10']"
            "##teamcity[progressMessage 'trial 9/10']"
            "##teamcity[progressMessage 'trial 10/10']"
            "##teamcity[testFinished name='test-check-all-pass.core-test/ (crypto-should-roundtrip) (:)']"
            "##teamcity[testSuiteFinished name='test-check-all-pass.core-test']"]
           (teamcity-test-messages out)))))

(deftest report-test-check-failure
  (let [{:keys [exit out]} (lein-test "./test-projects/test-check-failure")]
    (is (= 1 exit))
    (is (= ["##teamcity[testSuiteStarted name='test-check-failure.core-test']"
            "##teamcity[testStarted name='test-check-failure.core-test/ (crypto-should-roundtrip) (:)' captureStandardOutput='true']"
            "##teamcity[testFailed name='test-check-failure.core-test/ (crypto-should-roundtrip) (core_test.clj:18)' message='' details='|nm != e(m)' type='comparisonFailure' expected='(:result (tc/quick-check 10 (prop/for-all |[message (gen/string) user-key (gen/string)|] (not= message (encrypt message user-key) user-key))))' actual='false']"
            "##teamcity[testFinished name='test-check-failure.core-test/ (crypto-should-roundtrip) (:)']"
            "##teamcity[testSuiteFinished name='test-check-failure.core-test']"]
           (teamcity-test-messages out)))))

(deftest do-not-report-syntax-error
  (let [{:keys [exit out]} (lein-test "./test-projects/syntax-error")]
    (is (= 1 exit))
    (is (= (teamcity-test-messages out) nil))))

(deftest do-not-report-syntax-error-in-test
  (let [{:keys [exit out]} (lein-test "./test-projects/syntax-error-test")]
    (is (= 1 exit))
    (is (= (teamcity-test-messages out) nil))))
