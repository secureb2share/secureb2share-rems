(ns rems.landing-page
  (:require [compojure.core :refer [GET defroutes]]
            [rems.context :as context]
            [rems.db.users :as users]
            [rems.role-switcher :refer [has-roles?]]
            [rems.util :refer [get-user-id]]
            [ring.util.response :refer [redirect]]))

(defn- landing-page [req]
  (users/add-user! (get-user-id) context/*user*)
  (cond
    (has-roles? :approver) (redirect "/approvals")
    (has-roles? :reviewer) (redirect "/reviews")
    :else (redirect "/catalogue")))

(defroutes landing-page-routes
  (GET "/landing_page" req (landing-page req)))
