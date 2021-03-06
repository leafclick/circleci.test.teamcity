(defproject com.leafclick/circleci.test.teamcity "0.3.0-SNAPSHOT"
  :description "A Teamcity reporter for circleci.test runner"
  :url "http://github.com/leafclick/circleci.test.teamcity"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [circleci/circleci.test "0.4.1"]]
  :profiles {:dev {:resource-paths ["dev-resources"]}})