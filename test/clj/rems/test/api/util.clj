(ns rems.test.api.util
  (:require [clojure.test :refer :all]
            [compojure.api.sweet :refer :all]
            [rems.api.util :refer :all]
            [rems.auth.ForbiddenException]
            [rems.auth.NotAuthorizedException]
            [rems.context :as context]
            [ring.util.http-response :refer :all])
  (:import (rems.auth ForbiddenException NotAuthorizedException)))

(deftest longify-keys-test
  (is (= {} (longify-keys nil)))
  (is (= {42 42} (longify-keys {:42 42})) "converts keywords to numbers")
  (is (= {42 42} (longify-keys {42 42})) "keeps numbers as numbers"))

(deftest route-role-check-test
  (testing "no roles required"
    (let [route (GET "/foo" []
                  :summary "Summary text"
                  (ok {:success true}))]
      (is (= {:status 200
              :headers {}
              :body {:success true}}
             (route {:request-method :get
                     :uri "/foo"})))))

  (testing "role required"
    (let [route (GET "/foo" []
                  :summary "Summary text"
                  :roles #{:approver}
                  (ok {:success true}))]

      (testing "and user has it"
        (binding [context/*roles* #{:approver}
                  context/*user* {"eppn" "user1"}]
          (is (= {:status 200
                  :headers {}
                  :body {:success true}}
                 (route {:request-method :get
                         :uri "/foo"})))))

      (testing "but user doesn't have it"
        (binding [context/*roles* #{}
                  context/*user* {"eppn" "user1"}]
          (is (thrown? ForbiddenException
                       (route {:request-method :get
                               :uri "/foo"})))))

      (testing "but user is not logged in"
        (binding [context/*roles* #{:approver}
                  context/*user* nil]
          (is (thrown? NotAuthorizedException
                       (route {:request-method :get
                               :uri "/foo"})))))))

  (testing "required roles are added to summary documentation"
    (let [route (GET "/foo" []
                  :summary "Summary text"
                  :roles #{:approver :reviewer}
                  (ok {:success true}))]
      (is (= "Summary text (roles: approver, reviewer)"
             (get-in route [:info :public :summary])))))

  (testing "summary documentation is required"
    ; TODO: is there a way to test exceptions thrown from macros, so that we don't need to expose this private helper function?
    (is (thrown-with-msg? IllegalArgumentException #"^\QRoute must have a :summary when using :roles and it must be specified before :roles\E$"
                          (add-roles-documentation nil #{:some-role})))))
