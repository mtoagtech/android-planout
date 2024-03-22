package com.planout.api_calling


class ApiController {

    interface api {

        companion object {

            val server = "https://www.planoutapp.co/"


            val api_url="${server}api/v1/"

            val about_url="${server}about-us"
            val terms_url="${server}terms-conditions"
            val policy_url="${server}privacy-policy"

            //api urls
            val industries="${api_url}industries"
            val register_visitor="${api_url}register-visitor"
            val register_store="${api_url}register-store"
            val login="${api_url}login"
            val forgot_password="${api_url}forgot-password"
            val social_login="${api_url}social-login"
            val logout="${api_url}logout"
            val profile="${api_url}profile"
            val update_visitor="${api_url}update/visitor"
            val deleteaccount="${api_url}deleteaccount"
            val changepassword="${api_url}changepassword"
            val parameters="${api_url}parameters"
            val contactus="${api_url}contactus"
            val searches="${api_url}searches"

            val tags="${api_url}tags"
            val update_store="${api_url}update/store"
            val store_media="${api_url}store/media"

            val store_locations="${api_url}store/locations"

            val cities="${api_url}cities"
            val stores="${api_url}stores"
            val reservations="${api_url}reservations"
            val reservationsCreateByStore="${api_url}reservations/createbystore"
            val notifications="${api_url}notifications"
            val notifications_enable="${api_url}notifications/enable"
            val notifications_disable="${api_url}notifications/disable"
            val reservationStatusUpdate="${api_url}reservations/arrivalstatus/"
            val reservation_enable="${api_url}stores/reservations/enable"
            val reservation_disable="${api_url}stores/reservations/disable"
            val notifications_markallread="${api_url}notifications/markallread"
            val markasread="${api_url}notifications/markasread"
            val favorites="${api_url}favorites"
            val reservations_cancel="${api_url}reservations/cancel"
            val reservations_decline="${api_url}reservations/decline"
            val reservations_updatetableno="${api_url}reservations/updatetableno"
            val events="${api_url}events"
            val events_update="${api_url}events/update"
            val searchfilters="${api_url}searchfilters"
            val subscriptions_payment_history="${api_url}subscriptions/payment/history"
            val reservations_confirm="${api_url}reservations/confirm"
            val subscriptions_packages="${api_url}subscriptions/packages"
            val subscriptions_cancel="${api_url}subscriptions/cancel"
            val subscriptions_applycoupon="${api_url}subscriptions/applycoupon"
            val payments_checkstatus="${api_url}payments/checkstatus"
            val payments_process="${api_url}payments/process/"
            val subscriptions_payment_checkout="${api_url}subscriptions/payment/checkout"
            val subscriptions_payment_response="${api_url}subscriptions/payment/response"
            val stores_updatetiming="${api_url}stores/updatetiming"
            val stores_account="${api_url}store-accounts"
            val change_status="${api_url}store-accounts/change-status"
            val update_language="${api_url}update-language"

        }
    }
}