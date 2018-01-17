(defproject com.leafclick/circleci.test.teamcity "0.1.0"
  :description "A Teamcity reporter for circleci.test runner"
  :url "http://github.com/leafclick/circleci.test.teamcity"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [circleci/circleci.test "0.4.0"]]
  :profiles {:dev {:resource-paths ["dev-resources"]}})