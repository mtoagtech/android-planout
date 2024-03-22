package com.planout.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import com.planout.constant.Utility.checkStringNullOrNot
import com.planout.constant.Utility.showOrGone
import com.planout.models.IndustryModel
import kotlinx.android.synthetic.main.activity_login.editEmail
import kotlinx.android.synthetic.main.activity_registration.*
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONObject
import javax.security.auth.callback.Callback


class RegistrationActivity : AppCompatActivity(), ApiResponse,
    GoogleApiClient.OnConnectionFailedListener {

    private var isComapny = false
    private var strName: String = ""
    private var strEmail: String = ""
    private var strPasword: String = ""
    private var strCompany: String = ""
    private var strTelephone: String = ""
    private var strExtraNotes: String = ""
    private var strMobile: String = ""
    private lateinit var strIndustryIds: ArrayList<Int>
    private var arrIndustry: ArrayList<IndustryModel> = ArrayList()

    /*Google Login parameter*/
    private var mGoogleApiClient: GoogleApiClient? = null
    private val SIGN_IN = 7

    /*End Google login Parameter*/
    private val TAG = "TAG"

    /*Facebook Login parameter*/
    private lateinit var callbackManager: CallbackManager
    /*End Facebook login Parameter*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        setTabData()
        clickView()
        if (Utility.hasConnection(this)) {
            callIndustryApi()
        } else {
            Utility.customErrorToast(this, getString(R.string.msg_internet_conn))
        }

        /*Google Login ------------------Start*/
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        //btn_sign_in.setSize(SignInButton.SIZE_STANDARD)
        //btn_sign_in.setScopes(gso.scopeArray)
        googleClick.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient!!)
            startActivityForResult(signInIntent, SIGN_IN)
        }
        /*Google Login ------------------End---------*/

        /*Facebook Login------------- Start----------*/

        FacebookSdk.sdkInitialize(this)
        callbackManager = CallbackManager.Factory.create()
        //loginButton.setPermissions(listOf("public_profile", "email", "user_birthday"))
        loginButton.setPermissions("public_profile, email, user_birthday")

        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val request =
                    GraphRequest.newMeRequest(result!!.accessToken) { fbObject, response ->
                        Log.v("Login Success", response.toString())
                        try {
                            Log.d(TAG, "onSuccess: fbObject $fbObject")
                            val name = fbObject!!.getString("name")
                            val email = fbObject.getString("email")
                            val personId = fbObject.getString("id")
                            var fb_image_url = ""
                            try {
                                if (fbObject.has("picture")) {
                                    val picture = fbObject.getJSONObject("picture")
                                    val data = picture.getJSONObject("data")
                                    fb_image_url = data.getString("url")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            Log.d(TAG, "onSuccess: fullName $name")
                            Log.d(TAG, "onSuccess: email $email")
                            Log.d(TAG, "onSuccess: imageURL $fb_image_url")
                            disconnectFromFacebook()
                            FirebaseApp.initializeApp(applicationContext)
                            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
                                this@RegistrationActivity
                            ) { instanceIdResult ->
                                val FirebasenewToken = instanceIdResult.token
                                if (!FirebasenewToken.equals("", ignoreCase = true)) {
                                    Log.d("FirebasenewToken", FirebasenewToken)
                                    // LOGIN API
                                    socialLoginApi(
                                        personId,
                                        "facebook",
                                        name,
                                        email,
                                        FirebasenewToken
                                    )
                                } else {
                                    Utility.customErrorToast(
                                        this@RegistrationActivity,
                                        getString(R.string.something_went_wrong)
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                val bundle = Bundle()
                bundle.putString("fields", "id,name,email,gender,picture.type(large),birthday")
                request.parameters = bundle
                request.executeAsync()
            }

            override fun onCancel() {
                Log.d(TAG, "onCancel: called")
            }


            override fun onError(error: FacebookException) {
                Log.d(TAG, "onError: called")
            }

        })

        facebookClick.setOnClickListener {
            loginButton.performClick()
        }
        /*Facebook Login------------- End----------*/

        //for visitor
        editNameVisitor.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editNameVisitor, nameVisitorErr, false, "")
        }
//        editEmailVisitor.customSelectionActionModeCallback = object : Callback,
//            android.view.ActionMode.Callback {
//
//            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
//                return false
//            }
//
//            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
//                return false
//            }
//
//            override fun onActionItemClicked(
//                mode: android.view.ActionMode?,
//                item: MenuItem?
//            ): Boolean {
//                return false
//            }
//
//            override fun onDestroyActionMode(mode: android.view.ActionMode?) {
//
//            }
//        }

//        editEmailVisitor.isLongClickable = false
//        editEmailVisitor.setTextIsSelectable(false)
        editEmailVisitor.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editEmailVisitor, emailVisitorErr, false, "")
        }
        editPassVisitor.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(linPassVisitor, passVisitorErr, false, "")
        }

        //for company
        editName.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editName, nameErr, false, "")
        }
//        editEmail.customSelectionActionModeCallback = object : Callback,
//            android.view.ActionMode.Callback {
//
//            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
//                return false
//            }
//
//            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
//                return false
//            }
//
//            override fun onActionItemClicked(
//                mode: android.view.ActionMode?,
//                item: MenuItem?
//            ): Boolean {
//                return false
//            }
//
//            override fun onDestroyActionMode(mode: android.view.ActionMode?) {
//
//            }
//        }
//
//        editEmail.isLongClickable = false
//        editEmail.setTextIsSelectable(false)
        editEmail.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editEmail, emailErr, false, "")
        }
        editPass.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(linPass, passErr, false, "")
        }
        editCompany.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editCompany, companyErr, false, "")
        }
        editTelephone.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editTelephone, telephoneErr, false, "")
        }
        editMobile.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editMobile, mobileErr, false, "")
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        // for facebook Login add this line
        /*callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)*/

        if (requestCode == SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            handleSignInResult(result)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult?) {
        Log.d("TAG", "handleSignInResult:" + result!!.isSuccess)
        if (result.isSuccess) {
            // Signed in successfully
            val acct = result.signInAccount
            Log.e("TAG", "display name: " + acct!!.displayName)
            val personName = acct.displayName
            val personPhotoUrl = acct.photoUrl.toString()
            val email = acct.email
            val personId = acct.id
            Log.e(
                "TAG", "Name: $personName, email: $email, Image: $personPhotoUrl"
            )
            FirebaseApp.initializeApp(applicationContext)
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
                this@RegistrationActivity
            ) { instanceIdResult ->
                val FirebasenewToken = instanceIdResult.token
                if (!FirebasenewToken.equals("", ignoreCase = true)) {
                    Log.d("FirebasenewToken", FirebasenewToken)
                    // LOGIN API
                    socialLoginApi(personId!!, "google", personName!!, email!!, FirebasenewToken)
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient!!)
                } else {
                    Utility.customErrorToast(this, getString(R.string.something_went_wrong))
                }
            }

        } else {

            // Signed in failed
            Log.e(
                "TAG", "Signed in failed"
            )

        }
    }

    private fun socialLoginApi(
        personId: String,
        socialType: String,
        personName: String,
        email: String,
        FirebasenewToken: String
    ) {
        Utility.getCurrentAppVersion(this)
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.social_id, personId)
        mBuilder.add(Utility.key.social_type, socialType)
        mBuilder.add(Utility.key.device_id, Utility.device_id)
        mBuilder.add(Utility.key.device_token, FirebasenewToken)
        mBuilder.add(Utility.key.name, personName)
        mBuilder.add(Utility.key.email, email)
        mBuilder.add(Utility.key.login_from, "0")
        //Log.d("TAG", "callLoginApi: "+strEmail+"..."+strPass+"..."+Utility.device_id+"..."+Utility.fcm_tocken)
        CallApi.callAPi(
            mBuilder,
            ApiController.api.social_login,
            this,
            Utility.social_login,
            true,
            Utility.POST,
            false
        )

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("TAG", "onConnectionFailed:$connectionResult");
    }

    private fun clickView() {
        Utility.animationClick(imgBack).setOnClickListener { onBackPressed() }
        Utility.animationClick(txtSignUp).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        Utility.animationClick(txtSignUp2).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        linVisitor.setOnClickListener {
            isComapny = false
            setTabData()
            constVisitorForm.visibility = View.VISIBLE
            constComapnyForm.visibility = View.GONE
        }
        linCompany.setOnClickListener {
            isComapny = true
            setTabData()
            constVisitorForm.visibility = View.GONE
            constComapnyForm.visibility = View.VISIBLE
        }
        btnSignUpVisitor.setOnClickListener {
            checkValidationVisitor()
        }
        btnSignUpComapny.setOnClickListener {
            checkValidationCompany()
        }
        Utility.animationClick(imgEyeVisitor).setOnClickListener(View.OnClickListener {
            Utility.showHidePass(this, imgEyeVisitor, editPassVisitor)
        })
        Utility.animationClick(imgEye).setOnClickListener(View.OnClickListener {
            Utility.showHidePass(this, imgEye, editPass)
        })
    }

    private fun setTabData() {
        if (!isComapny) {
            txtVisitor.setTextColor(ContextCompat.getColor(this, R.color.app_green))
            viewVisitor.setBackgroundColor(ContextCompat.getColor(this, R.color.app_green))
            txtCompany.setTextColor(ContextCompat.getColor(this, R.color.gray_84_5B))
            viewCompany.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_F8_48))
        } else {
            txtVisitor.setTextColor(ContextCompat.getColor(this, R.color.gray_84_5B))
            viewVisitor.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_F8_48))
            txtCompany.setTextColor(ContextCompat.getColor(this, R.color.app_green))
            viewCompany.setBackgroundColor(ContextCompat.getColor(this, R.color.app_green))
        }
    }

    fun disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return  // already logged out
        }
        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/me/permissions/",
            null,
            HttpMethod.DELETE,
            { LoginManager.getInstance().logOut() }).executeAsync()
    }

    private fun callIndustryApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            ApiController.api.industries,
            this,
            Utility.industries,
            true,
            Utility.GET,
            false
        )
    }

    private fun checkValidationVisitor() {
        strName = editNameVisitor.text.toString()
        strEmail = editEmailVisitor.text.toString()
        strPasword = editPassVisitor.text.toString()

        if (TextUtils.isEmpty(strName)) {
            editNameVisitor.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_name_valid)
            Utility.showGoneErrorView(editNameVisitor, nameVisitorErr, true, getString(R.string.msg_name_valid))
        } else if (TextUtils.isEmpty(strEmail)) {
            editEmailVisitor.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_email)
            Utility.showGoneErrorView(editEmailVisitor, emailVisitorErr, true, getString(R.string.msg_email))
        } else if (!Utility.isValidMail(strEmail)) {
            editEmailVisitor.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_email_valid)
            Utility.showGoneErrorView(
                editEmailVisitor,
                emailVisitorErr,
                true,
                getString(R.string.msg_email_valid)
            )
        } else if (TextUtils.isEmpty(strPasword)) {
            editPassVisitor.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_pass_valid)
            Utility.showGoneErrorView(linPassVisitor, passVisitorErr, true, getString(R.string.msg_email_valid))
        } else if (!Utility.isValidPassword(strPasword)) {
            //Utility.customErrorToast(this,"Please add minimum 6 characters which include one upper/lower case/ special character and one digit")
            Utility.showGoneErrorView(
                linPassVisitor,
                passVisitorErr,
                true,
                getString(R.string.msg_pass_valid_char)
            )
        } else {
            Utility.hideSoftKeyboard(this)
            if (Utility.hasConnection(this)) {
                FirebaseApp.initializeApp(applicationContext)
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
                    this
                ) { instanceIdResult ->
                    val FirebasenewToken = instanceIdResult.token
                    if (!FirebasenewToken.equals("", ignoreCase = true)) {
                        Log.d("FirebasenewToken", FirebasenewToken)
                        // LOGIN API
                        callVisitorRegisterApi(FirebasenewToken)
                    } else {
                        Utility.customErrorToast(this, getString(R.string.something_went_wrong))
                    }
                }
            } else {
                Utility.customErrorToast(this, getString(R.string.msg_internet_conn))
            }
        }
    }

    private fun callVisitorRegisterApi(FirebasenewToken: String) {
        Utility.getCurrentAppVersion(this)
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.name, strName)
        mBuilder.add(Utility.key.email, strEmail)
        mBuilder.add(Utility.key.password, strPasword)
        mBuilder.add(Utility.key.confirm_password, strPasword)
        mBuilder.add(Utility.key.device_id, Utility.device_id)
        mBuilder.add(Utility.key.device_token, FirebasenewToken)
        mBuilder.add(Utility.key.login_from, "0")
        CallApi.callAPi(
            mBuilder,
            ApiController.api.register_visitor,
            this,
            Utility.register_visitor,
            true,
            Utility.POST,
            false
        )
    }

    private fun checkValidationCompany() {
        strName = editName.text.toString()
        strEmail = editEmail.text.toString()
        strPasword = editPass.text.toString()
        strCompany = editCompany.text.toString()
        strIndustryIds = getSelectedIndustries()
        strTelephone = editTelephone.text.toString()
        strMobile = editMobile.text.toString()
        strExtraNotes = editMessage.text.toString()
        //Log.d("TAG", "checkValidationCompany: "+strIndustryIds.toString())
        if (TextUtils.isEmpty(strName)) {
            editName.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_name_valid)
            Utility.showGoneErrorView(editName, nameErr, true, getString(R.string.msg_name_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
        } else if (TextUtils.isEmpty(strEmail)) {
            editEmail.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_email)
            Utility.showGoneErrorView(editEmail, emailErr, true, getString(R.string.msg_email))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
        } else if (!Utility.isValidMail(strEmail)) {
            editEmail.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_email_valid)
            Utility.showGoneErrorView(editEmail, emailErr, true, getString(R.string.msg_email_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
        } else if (TextUtils.isEmpty(strPasword)) {
            editPass.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_pass_valid)
            Utility.showGoneErrorView(linPass, passErr, true, getString(R.string.msg_pass_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
        } else if (!Utility.isValidPassword(strPasword)) {
            editPass.requestFocus()
            //Utility.customErrorToast(this,"Please add minimum 6 characters which include one upper/lower case/ special character and one digit")
            Utility.showGoneErrorView(linPass, passErr, true, getString(R.string.msg_pass_valid_char))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
        } else if (TextUtils.isEmpty(strCompany)) {
            editCompany.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_company_valid)
            Utility.showGoneErrorView(editCompany, companyErr, true, getString(R.string.msg_company_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
        } else if (strIndustryIds.size <= 0) {
            //Utility.customErrorToast(this, Utility.msg_industry)
            industryErr.showOrGone(true)
            industryErr.text = getString(R.string.msg_industry)
        } else if (TextUtils.isEmpty(strTelephone)) {
            editTelephone.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_telephone_valid)
            Utility.showGoneErrorView(
                editTelephone,
                telephoneErr,
                true,
                getString(R.string.msg_telephone_valid)
            )
        } else if (strTelephone.length < 8) {
            editTelephone.requestFocus()
            //Utility.customErrorToast(this, getString(R.string.telephone_mus_be_8_char))
            Utility.showGoneErrorView(
                editTelephone,
                telephoneErr,
                true,
                getString(R.string.telephone_mus_be_8_char)
            )
        } else if (TextUtils.isEmpty(strMobile)) {
            editMobile.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_mobile_valid)
            Utility.showGoneErrorView(editMobile, mobileErr, true, getString(R.string.msg_mobile_valid))
        } else if (strMobile.length < 8) {
            editMobile.requestFocus()
            //Utility.customErrorToast(this, getString(R.string.mobile_must_be_8_char))
            Utility.showGoneErrorView(
                editMobile,
                mobileErr,
                true,
                getString(R.string.mobile_must_be_8_char)
            )
        } else {
            Utility.hideSoftKeyboard(this)
            if (Utility.hasConnection(this)) {
                FirebaseApp.initializeApp(applicationContext)
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(
                    this
                ) { instanceIdResult ->
                    val FirebasenewToken = instanceIdResult.token
                    if (!FirebasenewToken.equals("", ignoreCase = true)) {
                        Log.d("FirebasenewToken", FirebasenewToken)
                        // LOGIN API
                        callCompanyRegisterApi(FirebasenewToken)
                    } else {
                        Utility.customErrorToast(this, getString(R.string.something_went_wrong))
                    }
                }
            } else {
                Utility.customErrorToast(this, getString(R.string.msg_internet_conn))
            }
        }
    }

    private fun callCompanyRegisterApi(FirebasenewToken: String) {
        Utility.getCurrentAppVersion(this)
        val mBuilder = JSONObject()
        mBuilder.put(Utility.key.name, strName)
        mBuilder.put(Utility.key.email, strEmail)
        mBuilder.put(Utility.key.password, strPasword)
        mBuilder.put(Utility.key.confirm_password, strPasword)
        mBuilder.put(Utility.key.mobile, strMobile)
        mBuilder.put(Utility.key.telephone, strTelephone)
        mBuilder.put(Utility.key.extra_notes, strExtraNotes)
        mBuilder.put(Utility.key.store_name, strCompany)
        mBuilder.put(Utility.key.industries, JSONArray(strIndustryIds))
        mBuilder.put(Utility.key.device_id, Utility.device_id)
        mBuilder.put(Utility.key.device_token, FirebasenewToken)
        mBuilder.put(Utility.key.login_from, "0")
//        Log.d("JSON_DATA",mBuilder.toString())
        CallApi.callAPiJson(
            mBuilder,
            ApiController.api.register_store,
            this,
            Utility.register_store,
            true,
            Utility.POST,
            false
        )
    }

    private fun getSelectedIndustries(): ArrayList<Int> {
        val list: ArrayList<Int> = ArrayList()
        for (i in 0 until arrIndustry.size) {
            if (arrIndustry[i].isSelected == true) {
                list.add(arrIndustry[i].id!!.toInt())
            }
        }
        return list
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.register_visitor) {
            if (isData) {
                if (result.getBoolean(Utility.key.success)) {
                    val data = result.getJSONObject(Utility.key.data)
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.auth_token] = data.getString(Utility.key.token)
                    hashM[Utility.key.name] = data.getString(Utility.key.name)
                    hashM[Utility.key.is_owner] = Utility.checkStringNullOrNot(data, Utility.key.is_owner)
                    hashM[Utility.key.allow_notification] =
                        Utility.checkStringNullOrNot(data, Utility.key.allow_notification)
                    hashM[Utility.key.user_type] = "201"
                    Utility.saveForm(hashM, this)
                    Utility.customSuccessToast(
                        this,
                        checkStringNullOrNot(result, Utility.key.message)
                    )
                    Utility.set_login(this, true)
                    startActivity(Intent(this, HomeVisitorActivity::class.java))
                    finishAffinity()

                } else {
                    Utility.customErrorToast(
                        this,
                        checkStringNullOrNot(result, Utility.key.message)
                    )
                }
            } else {
                Utility.showApiMessageError(this, result, "data")
            }
        } else if (type == Utility.register_store) {
            if (isData) {
                if (result.getBoolean(Utility.key.success)) {
                    val data = result.getJSONObject(Utility.key.data)
//                    val hashM: HashMap<String, String> = HashMap()
//                    hashM[Utility.key.auth_token] = data.getString(Utility.key.token)
//                    hashM[Utility.key.name] = data.getString(Utility.key.name)
//                    hashM[Utility.key.store_id] =
//                        Utility.checkStringNullOrNot(data, Utility.key.store_id)
//                    hashM[Utility.key.allow_notification] =
//                        Utility.checkStringNullOrNot(data, Utility.key.allow_notification)
//                    hashM[Utility.key.reservation_status] =
//                        Utility.checkStringNullOrNot(data, Utility.key.reservation_status)
//                    hashM[Utility.key.is_owner] =
//                        Utility.checkStringNullOrNot(data, Utility.key.is_owner)
//                    hashM[Utility.key.user_type] = "202"
//                    Utility.saveForm(hashM, this)
                    //Utility.set_login(this, true)
                    //val message = result.getString("message")

                    Utility.customSuccessToast(this, Utility.checkStringNullOrNot(result, Utility.key.message))

                    //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    val delayMillis: Long = 1000

                    val handler = Handler()

                    handler.postDelayed({
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()

                    }, delayMillis)

                } else {
                    Utility.customErrorToast(
                        this,
                        checkStringNullOrNot(result, Utility.key.message)
                    )
                }
            } else {
                Utility.showApiMessageError(this, result, "data")
            }
        } else if (type == Utility.industries) {
            if (isData) {
                if (result.getBoolean(Utility.key.success)) {
                    val data = result.getJSONArray(Utility.key.data)
                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        arrIndustry.add(
                            IndustryModel(
                                checkStringNullOrNot(
                                    obj,
                                    Utility.key.industry_name
                                ),
                                checkStringNullOrNot(obj, Utility.key.industry_image),
                                checkStringNullOrNot(obj, Utility.key.id),
                                false
                            )
                        )
                        addChipToGroup(
                            checkStringNullOrNot(obj, Utility.key.industry_name),
                            chipGrpIndustry
                        )
                    }
                } else {
                    Utility.customErrorToast(
                        this,
                        checkStringNullOrNot(result, Utility.key.message)
                    )
                }
            } else {
                Utility.customErrorToast(
                    this,
                    checkStringNullOrNot(result, Utility.key.message)
                )

            }
        } else if (type == Utility.social_login) {
            if (isData) {
                if (result.getBoolean(Utility.key.success)) {
                    val data = result.getJSONObject(Utility.key.data)
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.auth_token] = checkStringNullOrNot(data, Utility.key.token)
                    hashM[Utility.key.name] = checkStringNullOrNot(data, Utility.key.name)
                    hashM[Utility.key.email] = checkStringNullOrNot(data, Utility.key.email)
                    hashM[Utility.key.mobile] = checkStringNullOrNot(data, Utility.key.mobile)
                    hashM[Utility.key.user_type] = checkStringNullOrNot(data, Utility.key.user_type)
                    hashM[Utility.key.id] = checkStringNullOrNot(data, Utility.key.id)
                    Utility.saveForm(hashM, this)
                    Utility.customSuccessToast(
                        this,
                        checkStringNullOrNot(result, Utility.key.message)
                    )
                    Utility.set_login(this, true)
                    if (checkStringNullOrNot(data, Utility.key.user_type) == "202") {
                        hashM[Utility.key.reservation_status] =
                            Utility.checkStringNullOrNot(data, Utility.key.reservation_status)

                        startActivity(Intent(this, HomeCompanyActivity::class.java))
                        finishAffinity()
                    } else {
                        startActivity(Intent(this, HomeVisitorActivity::class.java))
                        finishAffinity()
                    }
                } else {
                    Utility.customErrorToast(
                        this,
                        checkStringNullOrNot(result, Utility.key.message)
                    )
                }
            } else {
                /*Utility.customErrorToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )*/
                Utility.showApiMessageError(this, result, "data")
            }
        }
    }

    private fun addChipToGroup(str: String, chipGrp: ChipGroup) {
        val chip = Chip(this)

        chip.text = str
        chip.setTextAppearance(R.style.chipText)
        //chip.chipIcon = ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)
        chip.setTextAppearance(R.style.chipText_bold)
        chip.setTextColor(ContextCompat.getColor(this, R.color.gray_5B_48))
        chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8))
        chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.transparent))
        chip.chipStrokeWidth = 2f
        chip.chipCornerRadius = 10f
        //chip.isChipIconVisible = false


        chip.setPadding(50, 50, 50, 50)
        chipGrp.chipSpacingVertical = 5
        chipGrp.chipSpacingHorizontal = 20
        //chip.isCheckable = true
        chipGrp.addView(chip as View)
        //chip.setOnCloseIconClickListener { chipGrp.removeView(chip as View) }
        chip.setOnClickListener {
            industryErr.showOrGone(false)
            industryErr.text = ""
            val txtName = chip.text.toString()
            if (isZeroSelected()) {
                setExistChip(txtName, chip)
            } else {
                when {
                    isSelIndus_1_3() -> {
                        setExistChip(txtName, chip)
                    }

                    isSelected(txtName) -> {
                        setExistChip(txtName, chip)
                    }

                    else -> {
                        Utility.customErrorToast(this, getString(R.string.msg_industry_valid))
                    }
                }
            }
        }
    }

    private fun isZeroSelected(): Boolean {
        var count = 0
        for (i in 0 until arrIndustry.size) {
            if (arrIndustry[i].isSelected == true) {
                count++
            }
        }
        return count == 0
    }

    private fun isSelected(name: String): Boolean {
        for (i in 0 until arrIndustry.size) {
            if (name == arrIndustry[i].industryName) {
                if (arrIndustry[i].isSelected == true) {
                    return true
                }
            }
        }
        return false
    }

    private fun isSelIndus_1_3(): Boolean {
        var count = 0
        for (i in 0 until arrIndustry.size) {
            if (arrIndustry[i].isSelected == true) {
                count++
            }
        }
        return count < 3
    }

    private fun setExistChip(name: String, chip: Chip) {
        for (i in 0 until arrIndustry.size) {
            if (name == arrIndustry[i].industryName) {
                if (arrIndustry[i].isSelected == true) {
                    chip.setPadding(50, 50, 50, 50)
                    chip.setTextColor(ContextCompat.getColor(this, R.color.gray_5B_48))
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8))
                    chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.transparent))
                    chip.chipStrokeWidth = 2f
                    arrIndustry.set(
                        i,
                        IndustryModel(
                            arrIndustry[i].industryName,
                            arrIndustry[i].industryImage,
                            arrIndustry[i].id,
                            false
                        )
                    )
                } else {
                    chip.setPadding(50, 50, 50, 50)
                    chip.setTextColor(ContextCompat.getColor(this, R.color.app_green))
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8_0D))
                    chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.app_green))
                    chip.chipStrokeWidth = 2f
                    arrIndustry.set(
                        i,
                        IndustryModel(
                            arrIndustry[i].industryName,
                            arrIndustry[i].industryImage,
                            arrIndustry[i].id,
                            true
                        )
                    )
                }
            }
        }
    }
}