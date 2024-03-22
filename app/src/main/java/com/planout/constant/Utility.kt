package com.planout.constant

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.planout.R
import com.planout.activities.LoginActivity
import com.planout.models.SavedSearchData
import com.tapadoo.alerter.Alerter
import com.thekhaeng.pushdownanim.PushDown
import com.thekhaeng.pushdownanim.PushDownAnim
import org.json.JSONArray
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * Created by Atul Papneja on 4/22/2022.
 */
object Utility {
    //reservation status
    val RES_PENDING_STATUS="901"
    val RES_CONFIRMED_STATUS="902"
    val RES_DECLINED_STATUS="903"
    val RES_CANCELLED_STATUS="904"

    //date time format
    val date_format: String = "dd MMM yyyy"
    val date_format_with_day: String = "EEE, dd MMM"
    val api_date_format: String = "yyyy-MM-dd"
    val api_time: String = "HH:mm"
    val api_full_date_format: String = "yyyy-MM-dd HH:mm"
    val date_format_subscription: String = "dd MMM yyyy, hh:mm a"
    val time_format_hms="HH:mm:ss"
    val time_format="hh:mm a"
    val time_formatres="EEE, dd MMM, hh:mm a"
    val date_time_formatres="DD MMM YYYY hh:mm A"

    //api calling type
    val industries = "industries"
    val login = "login"
    val social_login = "social-login"
    val register_visitor = "register-visitor"
    val register_store = "register-store"
    val forgot_password = "forgot-password"
    val logout = "logout"
    val profile = "profile"
    val update_visitor = "update_visitor"
    val deleteaccount = "deleteaccount"
    val changepassword = "changepassword"
    val parameters = "parameters"
    val contactus = "contactus"
    val searches = "searches"
    val searchesDelete = "searchesDelete"
    val tags = "tags"
    val update_store = "update_store"
    val store_media = "store_media"
    val store_media_delete = "store_media_delete"
    val store_media_add = "store_media_add"
    val profileLocation = "profileLocation"
    val store_locations = "store_locations"
    val store_locationsEdit = "store_locationsEdit"
    val cities = "cities"
    val storeDelete = "storeDelete"
    val stores = "stores"
    val stores_first = "stores_first"
    val stores_second = "stores_second"
    val stores_third = "stores_third"
    val stores_forth = "stores_forth"
    val stores_account = "stores_account"
    val stores_account_create = "stores_account_create"
    val stores_account_edit = "stores_account_edit"
    val stores_account_change_status = "stores_account_change_status"
    val stores_account_delete = "stores_account_delete"
    val createSearches = "createSearches"
    val reservationsUpcoming = "reservationsUpcoming"
    val reservationsPast = "reservationsPast"
    val notifications = "notifications"
    val notifications_enable = "notifications_enable"
    val notifications_disable = "notifications_disable"
    val reservation_enable = "reservation_enable"
    val reservation_disable = "reservation_disable"
    val markasread = "markasread"
    val reservationStatusUpdate = "reservationStatusUpdate"
    val notifications_markallread = "notifications_markallread"
    val storeDetail = "storeDetail"
    val favoritesRemove = "favoritesRemove"
    val favoritesAdd = "favoritesAdd"
    val updatetableno = "updatetableno"
    val favoritesRemoveDetail = "favoritesRemoveDetail"
    val favoritesAddDetail = "favoritesAddDetail"
    val favorites = "favorites"
    val reservations = "reservations"
    val reservationsCreateByStore = "reservationsCreateByStore"
    val reservationsCancel = "reservationsCancel"
    val reservationsPending = "reservationsPending"
    val reservationsConfirmed = "reservationsConfirmed"
    val reservationsDeclined = "reservationsDeclined"
    val reservationsDeclinedAdapter = "reservationsDeclinedAdapter"
    val eventsUpcoming = "eventsUpcoming"
    val eventsPast = "eventsPast"
    val eventsAdd = "eventsAdd"
    val eventsUpdate = "eventsUpdate"
    val eventsDelete = "eventsDelete"
    val searchfilters = "searchfilters"
    val subscriptions_payment_history = "subscriptions_payment_history"
    val subscriptions_packages = "subscriptions_packages"
    val subscriptions_cancel = "subscriptions_cancel"
    val subscriptions_applycoupon = "subscriptions_applycoupon"
    val subscriptions_payment_checkout = "subscriptions_payment_checkout"
    val subscriptions_payment_response = "subscriptions_payment_response"
    val stores_updatetiming = "stores_updatetiming"
    val saferpayPayment = "saferpayPayment"
    val payments_checkstatus = "payments_checkstatus"
    val payments_process = "payments_process"
    val update_language = "update_language"



    //local error messages
    val msg_internet_conn = "Please check your internet connection"
    val msg_name = "Enter your full name"
    val msg_name_valid = "Enter your full name"
    val msg_name_valid_sub = "Enter your name."
    val msg_company = "Enter your business name"
    val msg_company_valid = "Enter your business name"
    val msg_telephone = "Enter your telephone"
    val msg_telephone_valid = "Enter your valid telephone"
    val msg_mobile = "Enter your mobile"
    val msg_mobile_valid = "Enter your valid mobile"
    val msg_email = "Enter your email"
    val msg_email_sub = "Please enter your email address."
    val msg_email_valid = "Enter your valid email."
    val msg_fax = "Enter your fax"
    val msg_fax_valid = "The fax must be at least 8 characters."
    val msg_pass = "Enter your password"
    val msg_pass_sub = "Please enter your password."
    val msg_pass_valid = "Enter your valid password with 6 minimum character length"
    val msg_pass_valid_char = "Please add minimum 6 characters which include one upper/lower case/ special character and one digit."
    val msg_industry = "One category must be selected"
    val msg_tags = "One tag must be selected"//Select your tag at least one
    val msg_industry_valid = "Select your category Min 1 and Max 3 selection"
    val msg_tag_valid = "Select your tag Min 1 and Max 5 selection"

    lateinit var device_id: String
    lateinit var currentVersion: String
    lateinit var device_name: String
    lateinit var system_version: String
    lateinit var fcm_tocken: String
    private var progress: AlertDialog? = null
    val GET: String = "GET"
    val POST: String = "POST"
    val DELETE: String = "DELETE"
    val PUT: String = "PUT"

    // Manage Api and response keys
    interface key {
        companion object {
            val recentSize: String = "recentSize"
            val recentTitle: String = "recentTitle"
            val recentJson: String = "recentJson"

            val form: String = "form"
            val auth_token: String = "auth_token"
            val language: String = "language"
            val success: String = "success"
            val message: String = "message"
            val details: String = "details"
            val data: String = "data"
            val edit: String = "edit"
            val add: String = "add"

            val login_from: String = "login_from"
            val user_type: String = "user_type"
            val name: String = "name"
            val email: String = "email"
            val password: String = "password"
            val confirm_password: String = "confirm_password"
            val mobile: String = "mobile"
            val telephone: String = "telephone"
            val store_name: String = "store_name"
            val industries: String = "industries"
            val social_id: String = "social_id"
            val social_type: String = "social_type"
            val device_id: String = "device_id"
            val device_token: String = "device_token"
            val token: String = "token"
            val filename: String = "filename[]"

            val id: String = "id"
            val industry_name: String = "industry_name"
            val industry_image: String = "industry_image"
            val profile_image: String = "profile_image"
            val dob: String = "dob"
            val old_password: String = "old_password"
            val rpassword: String = "rpassword"
            val contact_topics: String = "contact_topics"
            val reservation_status: String = "reservation_status"
            val topic: String = "topic"
            val store: String = "store"
            val city_id:String="city_id"
            val city_name:String="city_name"
            val area:String="area"
            val address:String="address"
            val address1:String="address1"
            val postal_code:String="postal_code"
            val address_type:String="address_type"
            val latitude:String="latitude"
            val longitude:String="longitude"
            val table_indoor:String="table_indoor"
            val table_outdoor:String="table_outdoor"
            val allow_notification:String="allow_notification"
            //val reservation_status:String="reservation_status"
            val is_owner:String="is_owner"
            val total_unread_notifications:String="total_unread_notifications"

            val url: String = "url"
            val order_id: String = "order_id"
            val store_image: String = "store_image"
            val cover_image: String = "cover_image"
            val is_open: String = "is_open"
            val starttime: String = "starttime"
            val endtime: String = "endtime"
            val starttime1: String = "starttime1"
            val endtime1: String = "endtime1"
            val fax: String = "fax"
            val tags: String = "tags"
            val locations: String = "locations"
            val media: String = "media"
            val tag_name: String = "tag_name"
            val storeData: String = "storeData"
            val store_id: String = "store_id"
            val media_url: String = "media_url"
            val isFrom: String = "isFrom"
            val popular_industries: String = "popular_industries"
            val stores: String = "stores"
            val search_title: String = "search_title"
            val search_data: String = "search_data"
            val title: String = "title"
            val page: String = "page"
            val cities: String = "cities"
            val type: String = "type"
            val status: String = "status"
            val total: String = "total"
            val current_page: String = "current_page"
            val records: String = "records"
            val res_id: String = "res_id"
            val contact_name: String = "contact_name"
            val contact_mobile: String = "contact_mobile"
            val store_mobile: String = "store_mobile"
            val total_people: String = "total_people"
            val resdate: String = "resdate"
            val restime: String = "restime"
            val location_id: String = "location_id"
            val preferred_table: String = "preferred_table"
            val extra_notes: String = "extra_notes"
            val remark: String = "remark"
            val is_arrived: String = "is_arrived"
            val res_date_formated: String = "res_date_formated"
            val location: String = "location"
            val total_unread: String = "total_unread"
            val action_type: String = "action_type"
            val reservation_id: String = "reservation_id"
            val event_id: String = "event_id"
            val is_read: String = "is_read"
            val noti_date: String = "noti_date"
            val notification_ids: String = "notification_ids"
            val default_location_id: String = "default_location_id"
            val is_favorite: String = "is_favorite"
            val timings: String = "timings"
            val events: String = "events"
            val default_location: String = "default_location"
            val itemposition: String = "itemposition"
            val res_date: String = "res_date"
            val startdate: String = "startdate"
            val enddate: String = "enddate"
            val event_image: String = "event_image"
            val event_date: String = "event_date"
            val description: String = "description"
            val event_title: String = "event_title"
            val searchkey: String = "searchkey"
            val final_price: String = "final_price"
            val user_id: String = "user_id"
            val item_name: String = "item_name"
            val subtotal: String = "subtotal"
            val tax: String = "tax"
            val discount: String = "discount"
            val transaction_id: String = "transaction_id"
            val order_unique_id: String = "order_unique_id"
            val order_date: String = "order_date"
            val similar_stores: String = "similar_stores"
            val payment_method: String = "payment_method"
            val discount_percent: String = "discount_percent"
            val download_link: String = "download_link"
            val view_link: String = "view_link"
            val active_package: String = "active_package"
            val package_name: String = "package_name"
            val duration: String = "duration"
            val package_price: String = "package_price"
            val package_end_date: String = "package_end_date"
            val packages: String = "packages"
            val price: String = "price"
            val tax_percent: String = "tax_percent"
            val available_vouchers: String = "available_vouchers"
            val voucher_name: String = "voucher_name"
            val voucher_code: String = "voucher_code"
            val expired_at: String = "expired_at"
            val discount_amount: String = "discount_amount"
            val package_id: String = "package_id"
            val coupon_code: String = "coupon_code"
            val paid_by: String = "paid_by"
            val updated_at: String = "updated_at"
            val created_at: String = "created_at"
            val amount: String = "amount"
            val payment_response: String = "payment_response"
            val payment_response_text: String = "payment_response_text"
            val is_default: String = "is_default"
            val rememberPass: String = "rememberPass"
            val rememberEmail: String = "rememberEmail"
            val table_no: String = "table_no"
            val age_group: String = "age_group"
        }
    }
    
    /*fun isLoginCheck(requireActivity: Activity): Boolean {
        var status = false
        if (is_login(requireActivity)) {
            status = true
        }else{
            status = false
            customWarningToast(requireActivity, "You are login as guest user. please login or register yourself at first.")
        }
        return status
    }*/

    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!~'*()-_{}|/:;<>?.])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher = pattern.matcher(password!!)
        return matcher.matches()
    }

    @SuppressLint("Recycle")
    fun getPath(uri: Uri?, activity: Activity): String? {
        val projection =
            arrayOf(MediaStore.Video.Media.DATA)
        val cursor =
            activity.contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }
    fun bitmapToFile(
        bitmap: Bitmap,
        activity: Activity
    ): File {
        var newfile: File? = null

        // Get the context wrapper
        val wrapper = ContextWrapper(activity)

        // Initialize a new file instance to save bitmap object
        newfile = wrapper.getDir("Images", Context.MODE_PRIVATE)
        newfile = File(newfile, "${UUID.randomUUID()}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(newfile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Return the saved bitmap uri
        return newfile
    }

    fun facebookHashKey(activity: Activity) {
        try {
            val info = activity.packageManager.getPackageInfo(
                "com.planout",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("Before-----","Abcd")
                Log.d("facebookKeyHash: ", Base64.encodeToString(md.digest(), Base64.DEFAULT).toString())
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
    }
    @Throws(ParseException::class)
    fun timeCoversion12to24(twelveHoursTime: String?): String? {

        //Date/time pattern of input date (12 Hours format - hh used for 12 hours)
        val df: DateFormat = SimpleDateFormat("hh:mm aa",Locale.ENGLISH)

        //Date/time pattern of desired output date (24 Hours format HH - Used for 24 hours)
        val outputformat: DateFormat = SimpleDateFormat(Utility.api_time,Locale.ENGLISH)
        var date: Date? = null
        var output: String? = null

        //Returns Date object
        date = df.parse(twelveHoursTime!!)

        //old date format to new date format
        output = outputformat.format(date!!)
        println(output)
        return output
    }
    /*TO get first date of month*/
    fun first_date_month(pattern: String): String {
        val calendar_range = Calendar.getInstance()
        calendar_range.add(Calendar.MONTH, 0)
        calendar_range[Calendar.DATE] = calendar_range.getActualMinimum(Calendar.DAY_OF_MONTH)
        val monthFirstDay = calendar_range.time
        calendar_range[Calendar.DATE] = calendar_range.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthLastDay = calendar_range.time

        val df = SimpleDateFormat(pattern,Locale.ENGLISH)
        return df.format(monthFirstDay)
    }/*TO get current date*/
    fun current_date(pattern: String): String {
        val c: Date = Calendar.getInstance().time
        println("Current time => $c")
        val df = SimpleDateFormat(pattern ,Locale.ENGLISH)
        return df.format(c)
    }
    //change date format
    fun change_date_format(date: String, format: String): String {
        val date_selected = Date(date)
        val formatter5 = SimpleDateFormat(format,Locale.ENGLISH)
        val formats1 = formatter5.format(date_selected)
        return formats1
    }
    fun formatdate(fdate: String?): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat(api_date_format,Locale.ENGLISH)
        val d = SimpleDateFormat(date_format,Locale.ENGLISH)
        try {
            val convertedDate = inputFormat.parse(fdate!!)
            datetime = d.format(convertedDate!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return datetime
    }
    fun formatdateres(fdate: String?): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat(api_full_date_format,Locale.ENGLISH)
        val d = SimpleDateFormat(time_formatres,Locale.ENGLISH)
        try {
            val convertedDate = inputFormat.parse(fdate!!)
            datetime = d.format(convertedDate!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return datetime!!.replace("am", "AM").replace("pm", "PM")
    }

    fun formatdateevent(fdate: String?): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat(api_date_format,Locale.ENGLISH)
        val d = SimpleDateFormat(date_format_with_day,Locale.ENGLISH)
        try {
            val convertedDate = inputFormat.parse(fdate!!)
            datetime = d.format(convertedDate!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return datetime!!.replace("am", "AM").replace("pm", "PM")
    }
    fun formattimeevent(fdate: String?): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat(api_time,Locale.ENGLISH)
        val d = SimpleDateFormat(time_format,Locale.ENGLISH)
        try {
            val convertedDate = inputFormat.parse(fdate!!)
            datetime = d.format(convertedDate!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return datetime!!.replace("am", "AM").replace("pm", "PM")
    }

    fun formatdatetime(fdate: String?, input: String, output: String): String? {
        var datetime: String? = null
        val inputFormat: DateFormat = SimpleDateFormat(input,Locale.ENGLISH)
        val d = SimpleDateFormat(output,Locale.ENGLISH)
        try {
            val convertedDate = inputFormat.parse(fdate!!)
            datetime = d.format(convertedDate!!)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return datetime!!.replace("am", "AM").replace("pm", "PM")
    }

    fun showGoneErrorView(view: View, textView: TextView, errorShow: Boolean, msg: String){
        if (errorShow){
            textView.showOrGone(true)
            textView.text = msg
            view.setBackgroundResource(R.drawable.error_bg_round_edit_drawable)
        }else{
            textView.showOrGone(false)
            view.setBackgroundResource(R.drawable.bg_round_edit_drawable)
        }
    }


    //save report form
    fun saveForm(data: HashMap<String, String>, activity: Context) {
        val sharedpreferences = activity.getSharedPreferences(key.form, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        for (key in data.keys) {
            val value = data[key]
            Log.d("Key Values", "$key-----------------$value")
            editor.putString(key, value)
        }
        editor.apply()
    }

    //Visibility show gone
    fun View.showOrGone(show: Boolean) {
        visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    //Visibility show invisible
    fun View.showOrInvisible(show: Boolean) {
        visibility = if (show) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    fun getBitmap(context: Context, drawableId: Int): Bitmap? {
        Log.e("TAG", "getBitmap: 2")
        val drawable = ContextCompat.getDrawable(context, drawableId)
        return if (drawable is BitmapDrawable) {
            BitmapFactory.decodeResource(context.resources, drawableId)
        } else if (drawable is VectorDrawable) {
            getBitmap(drawable)
        } else {
            throw java.lang.IllegalArgumentException("unsupported drawable type")
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap? {
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        vectorDrawable.draw(canvas)
        Log.e("TAG", "getBitmap: 1")
        return bitmap
    }

    fun toTitleCase(string: String?): String? {

        // Check if String is null
        if (string == null) {
            return null
        }
        var whiteSpace = true
        val builder = StringBuilder(string) // String builder to store string
        val builderLength = builder.length

        // Loop through builder
        for (i in 0 until builderLength) {
            val c = builder[i] // Get character at builders position
            if (whiteSpace) {

                // Check if character is not white space
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and leave whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c))
                    whiteSpace = false
                }
            } else if (Character.isWhitespace(c)) {
                whiteSpace = true // Set character is white space
            } else {
                builder.setCharAt(i, Character.toLowerCase(c)) // Set character to lowercase
            }
        }
        return builder.toString() // Return builders text
    }

    //to get save form detail
    fun getForm(activity: Context, key: String): String? {
        val sharedpreferences = activity.getSharedPreferences(
            Utility.key.form,
            Context.MODE_PRIVATE
        )
        if (sharedpreferences.getString(key, "")!! == "null") {
            return ""
        } else {
            return sharedpreferences.getString(key, "")

        }
    }

    //cler user  from detail
    fun clearForm(activity: Context) {
        val sharedpreferences = activity.getSharedPreferences(key.form, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        editor.clear()
        editor.apply()
//        is_login(activity, false)
    }

    //cler user  detail
    fun clear_detail(activity: Context) {
        val sharedpreferences = activity.getSharedPreferences(key.details, Context.MODE_PRIVATE)
        val editor = sharedpreferences.edit()
        editor.clear()
        editor.apply()
    }

    //user session management
    fun set_login(activity: Context, value: Boolean) {
        val prefs = activity.getSharedPreferences("session", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("is_login", value)
        editor.apply()
    }

    //get  user  login  or  not
    fun is_login(activity: Activity): Boolean {
        val prefs = activity.getSharedPreferences("session", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_login", false)
    }
    fun SetImageSimple(
        activity: Activity,
        imagepath: String,
        imageview: ImageView
    ) {
        Glide.with(activity)
            .load(imagepath)
            .error(R.drawable.placeholder_img)
            .placeholder(R.drawable.placeholder_img)
            .disallowHardwareConfig()
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }
            })
            .into(imageview)
    }

    fun SetImage(
        activity: Context,
        imagepath: String,
        set: ImageView,
        progressBar: ProgressBar
    ) {
        try {  // Initialize placeholder drawable once
            if (!imagepath.isEmpty()) {

//                Picasso.get().load(imagepath).placeholder(activity.getResources().getDrawable(R.drawable.ic_basket)).into(set);
//                progressBar.setVisibility(View.GONE);
                Glide.with(activity)
                    .load(imagepath)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any,
                            target: Target<Drawable?>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }
                    }).error(
                        Glide.with(set.context)
                            .load(R.drawable.ic_launcher_foreground)
                    )
                    .into(set)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun isLoginCheck(activity: Activity): Boolean {
        var status = false
        if (is_login(activity)) {
            status = true
        }else{
            status = false
            showLoginAlert(activity)
        }
        return status
    }

    fun showLoginAlert(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        /*if (title.isNotEmpty())*/
            txtTitle.text = context.getString(R.string.login_confirmation)
        /*if (subTitle.isNotEmpty())*/
            txtSubTitle.text = context.getString(R.string.login_confirmation_text)
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.text=context.getString(R.string.login)
        txtDelete.setOnClickListener { dialog.dismiss()
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
        dialog.show()

    }

    fun showUpdateAlert(context: Context, appVersion: String, forceUpdate: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        txtTitle.text = "Update confirmation"
        txtSubTitle.text = "Update application to\naccess new features."
        if (forceUpdate == "1") {
            txtCancel.showOrGone(false)
        }
        txtDelete.text="Not now"
        txtDelete.text="Update"
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun showHidePass(context: Context, imageView: ImageView, editText: EditText) {
        if (editText.transformationMethod.equals(
                PasswordTransformationMethod.getInstance()
            )
        ) {
            editText.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_eye_on
                )
            )
            //editText.inputType = InputType.TYPE_CLASS_TEXT
            editText.setSelection(editText.length())
        } else {
            editText.transformationMethod =
                PasswordTransformationMethod.getInstance()
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_eye_off
                )
            )
            //editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setSelection(editText.length())
        }
    }


    //to check  internet  is  connect or not
    fun hasConnection(ct: Context): Boolean {
        val cm = (ct.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        @SuppressLint("MissingPermission") val wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiNetwork != null && wifiNetwork.isConnected) {
            return true
        }
        val mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (mobileNetwork != null && mobileNetwork.isConnected) {
            return true
        }
        val activeNetwork = cm.activeNetworkInfo
        return !(activeNetwork == null || !activeNetwork.isConnected)
    }

    //to check  internet  is  connect or not
    fun hasbackfragment(activity: AppCompatActivity): Boolean {
        return 0 != activity.supportFragmentManager.backStackEntryCount
    }

    //to get device  detail
    @SuppressLint("HardwareIds")
    fun getCurrentAppVersion(activity: AppCompatActivity) {
        try {
            val pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
            val tm = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            //            device_id = tm.getDeviceId();
            device_id = Settings.Secure.getString(
                activity.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            Log.d("device_id", device_id)
            currentVersion = pInfo.versionName
            Log.e("version", "code$currentVersion")
            device_name = Build.MANUFACTURER + " " + Build.MODEL
            Log.e("device_name", device_name!!)
            system_version = Build.VERSION.RELEASE
            Log.e("system_version", system_version!!)
            if (hasConnection(activity)) {
                FirebaseApp.initializeApp(activity);
                FirebaseInstanceId.getInstance().instanceId
                    .addOnSuccessListener(object : OnSuccessListener<InstanceIdResult> {
                        override fun onSuccess(instanceIdResult: InstanceIdResult) {
                            fcm_tocken = instanceIdResult.token //Token
                            Log.d("Fcm_tocken", fcm_tocken);
                        }
                    })
            } else {
                fcm_tocken = ""
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun hideSoftKeyboard(activity: Activity) {
        try {
            if (activity != null) {
                val inputMethodManager = (activity.getSystemService(
                    AppCompatActivity.INPUT_METHOD_SERVICE
                ) as InputMethodManager)
                inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus?.getWindowToken(), 0
                )
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun show_progress(activity: Activity) {
        try {
            val dialogBuilder = AlertDialog.Builder(activity, R.style.NewDialog)
            val dialogView = LayoutInflater.from(activity).inflate(R.layout.progress_load, null)

            val progressBar: ProgressBar
            progressBar = dialogView.findViewById(R.id.progressBar);

            progressBar.setBackgroundColor(activity.resources.getColor(R.color.transparent))
            dialogBuilder.setView(dialogView)
            dialogBuilder.setCancelable(false)
            progress = dialogBuilder.create()
            progress!!.window!!.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            progress!!.show()
        } catch (e: WindowManager.BadTokenException) {
            e.printStackTrace()
        }
    }

    fun hide_progress(activity: Activity) {
        try {
            if (!activity.isFinishing) {
                progress!!.dismiss()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun show_progress_doanload(activity: Activity) {
        try {
            val dialogBuilder = AlertDialog.Builder(activity, R.style.NewDialog)
            val dialogView = LayoutInflater.from(activity).inflate(R.layout.show_doanload_progress_view, null)

            dialogBuilder.setView(dialogView)
            dialogBuilder.setCancelable(false)
            progress = dialogBuilder.create()
            progress!!.window!!.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            progress!!.show()
        } catch (e: WindowManager.BadTokenException) {
            e.printStackTrace()
        }
    }

    fun hide_progress_doanload(activity: Activity) {
        try {
            if (!activity.isFinishing) {
                progress!!.dismiss()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun hideSoftInput(activity: Activity){
        activity.currentFocus?.let { view ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun checkStringNullOrNot(json: JSONObject, str: String) : String{
        return if(json.has(str)){
            json.getString(str)
        }else{
            ""
        }
    }

    fun showApiMessageError(activity: Activity, json: JSONObject, s: String){
        val jObj = json.getJSONObject(s)
        val iter: Iterator<String> = jObj.keys()
        val key: String = iter.next()
        val arrObj: JSONArray = jObj.getJSONArray(key)
        val strMsg = arrObj[0].toString()
        customErrorToast(activity,
            key+": "+strMsg
        )
    }

    fun animationClick(click_view: View?): PushDown {
        return PushDownAnim.setPushDownAnimTo(click_view)
            .setScale(PushDownAnim.MODE_STATIC_DP, 4f)
    }

    fun normal_toast(activity: Activity, msg: String) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
    }

    fun isValidMail(email: String): Boolean {
        val EMAIL_STRING = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        return Pattern.compile(EMAIL_STRING).matcher(email).matches()
    }

    fun checkStringNullOrEmpty(str: String): String? {
        if (str=="null"){
            return ""
        }else{
           return str
        }

    }

    fun customSuccessToast(activity: Activity, title: String, message: String){
        MotionToast.darkColorToast(activity,
            title,
            message,
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular))
    }

    fun customErrorToast(activity: Activity, title: String, message: String){
        MotionToast.darkColorToast(activity,
            title,
            message,
            MotionToastStyle.ERROR,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular))
    }

    fun customWarningToast(activity: Activity, title: String, message: String){
        MotionToast.darkColorToast(activity,
            title,
            message,
            MotionToastStyle.WARNING,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular))
    }

    fun customInfoToast(activity: Activity, title: String, message: String){
        MotionToast.darkColorToast(activity,
            title,
            message,
            MotionToastStyle.INFO,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular))
    }

    fun customInternetToast(activity: Activity, title: String, message: String){
        MotionToast.darkColorToast(activity,
            title,
            message,
            MotionToastStyle.NO_INTERNET,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular))
    }

    fun customErrorToast(activity: Activity,message: String){
        Alerter.create(activity, R.layout.custom_error_layout)
            .setBackgroundColorRes(R.color.transparent)
            .also { alerter ->
                val tvCustomView = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.content)
                tvCustomView?.text = message
            }
            .setDuration(5000)
            .show()
    }

    fun customSuccessToast(activity: Activity,message: String){
        Alerter.create(activity, R.layout.custom_success_layout)
            .setBackgroundColorRes(R.color.transparent)
            .also { alerter ->
                val tvCustomView = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.content)
                tvCustomView?.text = message
            }
            .show()
    }
    fun customWarningToast(activity: Activity,message: String){
        Alerter.create(activity, R.layout.custom_warning_layout)
            .setBackgroundColorRes(R.color.transparent)
            .also { alerter ->
                val tvCustomView = alerter.getLayoutContainer()?.findViewById<TextView>(R.id.content)
                tvCustomView?.text = message
            }
            .show()
    }

    fun setStatusBarColor(activity: Activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.black_white);
        }
    }

    fun getFileNameFromURL(url: String?): String? {
        if (url == null) {
            return ""
        }
        try {
            val resource = URL(url)
            val host: String = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                // handle ...example.com
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')

        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // calculate the end index
        val endIndex = Math.min(lastQMPos, lastHashPos)
        return url.substring(startIndex, endIndex)
    }

}