(ns rems.test.themes
  (:require [clojure.test :refer :all]
            [rems.config :as config]
            [rems.util :as util])
  (:import (java.io FileNotFoundException)))

(deftest load-external-theme-test
  (testing "no theme gives default theme"
    (let [config {:theme-path nil
                  :theme {:default "foo"}}]
      (is (= config (config/load-external-theme config)))))

  (testing "non-existing theme file produces an error"
    (let [config {:theme-path "no-such-file.edn"
                  :theme {:default "foo"}}]
      (is (thrown-with-msg? FileNotFoundException #"^\Qthe file specified in :theme-path does not exist: no-such-file.edn\E$"
                            (config/load-external-theme config)))))

  (testing "custom theme overrides values from the default theme"
    (let [config {:theme-path "theme-lbr/lbr.edn"
                  :theme {:color1 "should be overridden"
                          :color42 "not overridden"}}]
      (is (= {:color1 "#CAD2E6"
              :color42 "not overridden"}
             (-> (config/load-external-theme config)
                 :theme
                 (select-keys [:color1 :color42]))))))

  (testing "static resources can be placed in a 'public' directory next to the theme file"
    (let [config {:theme-path "theme-lbr/lbr.edn"
                  :theme {:default "foo"}}]
      (is (= "theme-lbr/public"
             (:theme-static-resources (config/load-external-theme config)))))))

(deftest get-theme-attribute-test
  (with-redefs [config/env {:theme {:test "success"
                                    :test-color 2}}]
    (is (= 2 (util/get-theme-attribute :test-color)))
    (is (= "success" (util/get-theme-attribute :test)))
    (is (nil? (util/get-theme-attribute :no-such-attribute)))))
