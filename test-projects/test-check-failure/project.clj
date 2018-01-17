(defproject all-pass "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.leafclick/circleci.test.teamcity "0.2.0-SNAPSHOT"]]
  :monkeypatch-clojure-test false
  :aliases {"test" ["run" "-m" "circleci.test/dir" :project/test-paths]
            "tests" ["run" "-m" "circleci.test"]
            "retest" ["run" "-m" "circleci.test.retest"]}

  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}})
