# circleci.test.teamcity

Teamcity test reporter for [circleci.test](https://github.com/circleci/circleci.test) runner

## Usage

Add `[circleci/circleci.test "0.4.0"]` to your `:dependencies` under `:dev` profile.
Add `[com.leafclick/circleci.test.teamcity "0.1.0"]` to your `:dependencies` under `:teamcity` profile

Put following contents into `dev-resources/circleci.test/config.cjl`:

    (require '[com.leafclick.circleci.test.teamcity])
    {:reporters [com.leafclick.circleci.test.teamcity/teamcity-reporter]}

It's recommended to use this set of Leiningen aliases:

    :aliases {"test" ["run" "-m" "circleci.test/dir" :project/test-paths]
              "tests" ["run" "-m" "circleci.test"]
              "retest" ["run" "-m" "circleci.test.retest"]}

See more about circleci.test runner at [https://github.com/circleci/circleci.test](https://github.com/circleci/circleci.test)

## License

Copyright © 2018 leafclick s. r. o.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
