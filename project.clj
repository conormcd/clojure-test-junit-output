(defproject clojure-test-junit-output "0.1.0-SNAPSHOT"
  :description "Make it easier to output your test results in JUnit format."
  :url "https://github.com/conormcd/clojure-test-junit-output"
  :license {:name "BSD"
            :url "https://github.com/conormcd/clojure-test-junit-output/blob/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [robert/hooke "1.3.0"]]
  :pedantic? :abort
  :deploy-repositories ^:replace [["clojars" {:url "https://clojars.org/repo"
                                              :username [:gpg :env/clojars_username]
                                              :password [:gpg :env/clojars_password]
                                              :sign-releases false}]])
