(ns rems.routes.home
  (:require [rems.layout :as layout]
            [rems.context :as context]
            [rems.catalogue :as catalogue]
            [rems.contents :as contents]
            [rems.applications :as applications]
            [rems.cart :as cart]
            [rems.form :as form]
            [rems.language-switcher :as language-switcher]
            [compojure.core :refer [defroutes GET]]))

(defn home-page []
  (layout/render
    "home" (contents/login context/*root-path*)))

(defn about-page []
  (layout/render
    "about" (contents/about)))

(defn catalogue-page []
  (layout/render
    "catalogue" (catalogue/catalogue)))

(defn form-page [id application]
  (layout/render
   "form"
   (form/form (form/get-form-for
               id
               (name context/*lang*)
               application))))

(defn applications-page []
  (layout/render
   "applications"
   (applications/applications)))

(defroutes public-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  language-switcher/switcher-routes)

(defroutes secured-routes
  (GET "/applications" [] (applications-page))
  (GET "/catalogue" [] (catalogue-page))
  (GET "/form/:id/:application" [id application]
       (form-page (Long/parseLong id) (Long/parseLong application)))
  (GET "/form/:id" [id]
       (form-page (Long/parseLong id) nil))
  cart/cart-routes
  form/form-routes)
