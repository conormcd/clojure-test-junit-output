(ns clojure-test-junit-output.test-core
  (:require [clojure.test :refer :all]
            [clojure-test-junit-output.core :refer (with-junit-output)]))

(use-fixtures :once (with-junit-output "test/junit.xml"))

(deftest some-sort-of-a-test
  (testing "Success works"
    (is (= 1 1)))
  (testing "Failure works"
    (is (= 1 0)))
  (testing "Errors work."
    (throw (Exception. "This shouldn't really kill things."))))
