(ns rems.auth.ldap
  (:require [rems.text :refer [text]]))

(defn login-component []
  [:div.m-auto.jumbotron
   [:h2 (text :t.ldap/title)]
   [:form
    {:action "/ldap/login" :method "post"}
    [:input.form-control {:type "text" :placeholder (text :t.ldap/username) :name "username" :required true}]
    [:input.form-control {:type "password" :placeholder (text :t.ldap/password) :name "password" :required true}]
    #_(anti-forgery-field)
    [:button.btn.btn-lg.btn-primary.btn-block {:type "submit"} (text :t.ldap/login)]]])
