package com.planout.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.FormBody
import org.json.JSONObject
import java.util.*
import javax.security.auth.callback.Callback

class LoginActivity : AppCompatActivity(), ApiResponse, GoogleApiClient.OnConnectionFailedListener {

    var strEmail: String = ""
    var strPass: String = ""
    var isRemember: Boolean = false

    /*Google Login parameter*/
    private var mGoogleApiClient: GoogleApiClient? = null
    private val SIGN_IN = 7

    private val TAG = "TAG"
    /*Facebook Login parameter*/
    private lateinit var callbackManager: CallbackManager
    /*End Facebook login Parameter*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        askNotificationPermission();

        //Utility.setStatusBarColor(this)
        val rememberEmail = Utility.getForm(this, Utility.key.rememberEmail)
        val rememberPass = Utility.getForm(this, Utility.key.rememberPass)
        Log.d("rememberEmail",rememberEmail!!)
        Log.d("rememberPass",rememberPass!!)
        editEmail.setText(rememberEmail)
        editPass.setText(rememberPass)
        rememberMeCb.isChecked = rememberEmail!=""
        clickView()
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
        val permissions: List<String> = listOf("public_profile", "email", "user_birthday")
        loginButton.setPermissions("public_profile, email, user_birthday")

        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val request = GraphRequest.newMeRequest(result!!.accessToken){
                        fbObject, response ->
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
                            this@LoginActivity
                        ) { instanceIdResult ->
                            val FirebasenewToken = instanceIdResult.token
                            if (!FirebasenewToken.equals("", ignoreCase = true)) {
                                Log.d("FirebasenewToken", FirebasenewToken)
                                // LOGIN API
                                socialLoginApi(personId,"facebook",name,email,FirebasenewToken)
                            } else {
                                Utility.customErrorToast(this@LoginActivity, getString(R.string.something_went_wrong))
                            }
                        }
                    }
                    catch (e: Exception) {
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

        fbClick.setOnClickListener {
            loginButton.performClick()
        }
        /*Facebook Login------------- End----------*/



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

//        editEmail.isLongClickable = false
//        editEmail.setTextIsSelectable(false)

        editEmail.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editEmail, emailErr, false, "")
        }
        editPass.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(linPass, passErr, false,"")
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
                this@LoginActivity
            ) { instanceIdResult ->
                val FirebasenewToken = instanceIdResult.token
                if (!FirebasenewToken.equals("", ignoreCase = true)) {
                    Log.d("FirebasenewToken", FirebasenewToken)
                    // LOGIN API
                    socialLoginApi(personId!!,"google",personName!!,email!!,FirebasenewToken)
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
        mBuilder.add(Utility.key.social_id,personId)
        mBuilder.add(Utility.key.social_type, socialType)
        mBuilder.add(Utility.key.device_id, Utility.device_id)
        mBuilder.add(Utility.key.device_token, FirebasenewToken)
        mBuilder.add(Utility.key.name, personName)
        mBuilder.add(Utility.key.email,email)
        mBuilder.add(Utility.key.login_from,"0")
        //Log.d("TAG", "callLoginApi: "+strEmail+"..."+strPass+"..."+Utility.device_id+"..."+Utility.fcm_tocken)
        CallApi.callAPi(mBuilder, ApiController.api.social_login, this, Utility.social_login, true, Utility.POST, false)

    }
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("TAG", "onConnectionFailed:$connectionResult");
    }

    private fun clickView() {



        Utility.animationClick(imgBack).setOnClickListener { onBackPressed() }
        Utility.animationClick(txtSignUp).setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
        Utility.animationClick(txtForgotPass).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        Utility.animationClick(imgEyeOn).setOnClickListener(View.OnClickListener {
            Utility.showHidePass(this, imgEyeOn, editPass)
        })

        rememberMeCb.setOnCheckedChangeListener { compoundButton, b ->
            isRemember = b
        }

        btnLogin.setOnClickListener {
            checkValidation()
        }
    }

    private fun checkValidation() {
        strEmail = editEmail.text.toString()
        strPass = editPass.text.toString()
        if (TextUtils.isEmpty(strEmail)){
            //Utility.customErrorToast(this, Utility.msg_email)
            editEmail.requestFocus()
            Utility.showGoneErrorView(editEmail, emailErr, true, getString(R.string.msg_email))
        }else if (!Utility.isValidMail(strEmail)){
            //Utility.customErrorToast(this, Utility.msg_email_valid)
            editEmail.requestFocus()
            Utility.showGoneErrorView(editEmail, emailErr, true, getString(R.string.msg_email_valid))
        }else if (TextUtils.isEmpty(strPass)){
            //Utility.customErrorToast(this, Utility.msg_pass)
            editPass.requestFocus()
            Utility.showGoneErrorView(linPass, passErr, true, getString(R.string.msg_pass))
        }else if (strPass.length<6){
            //Utility.customErrorToast(this, Utility.msg_pass_valid)
            editPass.requestFocus()
            Utility.showGoneErrorView(linPass, passErr, true, getString(R.string.msg_pass_valid))
        }else{
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
                        callLoginApi(FirebasenewToken)
                    } else {
                        Utility.customErrorToast(this, getString(R.string.something_went_wrong))
                    }
                }
            }else{
                Utility.customInternetToast(this, "Internet Connection", getString(R.string.msg_internet_conn))
            }
        }
    }

    private fun callLoginApi(FirebasenewToken: String) {
        val rememberLanguage = Utility.getForm(this, Utility.key.language)
        var language = ""
        if(rememberLanguage.isNullOrBlank()){
            language = "en"
        }
        else if(rememberLanguage == "EL"){
            language = "gr"
        }
        else if(rememberLanguage == "EN"){
            language = "en"
        }
        Utility.getCurrentAppVersion(this)
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.email, strEmail)
        mBuilder.add(Utility.key.password, strPass)
        mBuilder.add(Utility.key.device_id, Utility.device_id)
        mBuilder.add(Utility.key.device_token, FirebasenewToken)
        mBuilder.add(Utility.key.login_from,"0")
        mBuilder.add(Utility.key.language,language)
        //Log.d("TAG", "callLoginApi: "+strEmail+"..."+strPass+"..."+Utility.device_id+"..."+Utility.fcm_tocken)
        CallApi.callAPi(mBuilder, ApiController.api.login, this, Utility.login, true, Utility.POST, false)

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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Can post notifications.
        } else {
            // Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.login){
            if (isData){
                if (result.getBoolean(Utility.key.success)){
                    val data = result.getJSONObject(Utility.key.data)
                    Log.d("All login data :- ",data.toString())
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.auth_token] = Utility.checkStringNullOrNot(data, Utility.key.token)
                    hashM[Utility.key.name] = Utility.checkStringNullOrNot(data, Utility.key.name)
                    hashM[Utility.key.email] = Utility.checkStringNullOrNot(data, Utility.key.email)
                    hashM[Utility.key.mobile] = Utility.checkStringNullOrNot(data, Utility.key.mobile)
                    hashM[Utility.key.user_type] = Utility.checkStringNullOrNot(data, Utility.key.user_type)
                    hashM[Utility.key.allow_notification] = Utility.checkStringNullOrNot(data, Utility.key.allow_notification)
                    hashM[Utility.key.is_owner] = Utility.checkStringNullOrNot(data, Utility.key.is_owner)
                    if(data.getString("language") == "gr"){
                        hashM[Utility.key.language] = "EL"
                        Utility.saveForm(hashM, this)
                        val locale = Locale("el") // or "el" for Greek
                        Locale.setDefault(locale)

                        val resources = this.resources

                        val configuration = resources.configuration
                        configuration.locale = locale
                        configuration.setLayoutDirection(locale)

                        resources.updateConfiguration(configuration, resources.displayMetrics)
                    }
                    else{
                        hashM[Utility.key.language] = "EN"
                        Utility.saveForm(hashM, this)
                        val locale = Locale("en") // or "el" for Greek
                        Locale.setDefault(locale)

                        val resources = this.resources

                        val configuration = resources.configuration
                        configuration.locale = locale
                        configuration.setLayoutDirection(locale)

                        resources.updateConfiguration(configuration, resources.displayMetrics)
                    }


                    if(isRemember){
                        hashM[Utility.key.rememberPass] = strPass
                        hashM[Utility.key.rememberEmail] = strEmail
                    }else{
                        hashM[Utility.key.rememberPass] = ""
                        hashM[Utility.key.rememberEmail] = ""
                    }
                    if (Utility.checkStringNullOrNot(data, Utility.key.user_type) == "202"){
                        hashM[Utility.key.reservation_status] = Utility.checkStringNullOrNot(data, Utility.key.reservation_status)

                        hashM[Utility.key.store_id] = Utility.checkStringNullOrNot(data, Utility.key.store_id)
                        Utility.saveForm(hashM, this)
                        Utility.set_login(this, true)
                        Utility.customSuccessToast(this,
                            Utility.checkStringNullOrNot(result, Utility.key.message)
                        )
                        startActivity(Intent(this, HomeCompanyActivity::class.java))
                        finishAffinity()
                    }else{
                        Utility.saveForm(hashM, this)
                        Utility.set_login(this, true)
                        Utility.customSuccessToast(this,
                            Utility.checkStringNullOrNot(result, Utility.key.message)
                        )
                        startActivity(Intent(this, HomeVisitorActivity::class.java))
                        finishAffinity()
                    }

                }else{
                    Utility.customErrorToast(this,
                        Utility.checkStringNullOrNot(result, Utility.key.message)
                    )
                }
            }else{
                Utility.customErrorToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }else if (type==Utility.social_login){
            if (isData){
                if (result.getBoolean(Utility.key.success)){
                    val data = result.getJSONObject(Utility.key.data)
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.auth_token] =
                        Utility.checkStringNullOrNot(data, Utility.key.token)
                    hashM[Utility.key.name] = Utility.checkStringNullOrNot(data, Utility.key.name)
                    hashM[Utility.key.email] = Utility.checkStringNullOrNot(data, Utility.key.email)
                    hashM[Utility.key.mobile] =
                        Utility.checkStringNullOrNot(data, Utility.key.mobile)
                    hashM[Utility.key.user_type] =
                        Utility.checkStringNullOrNot(data, Utility.key.user_type)
                    hashM[Utility.key.id] = Utility.checkStringNullOrNot(data, Utility.key.id)
                    hashM[Utility.key.allow_notification] = Utility.checkStringNullOrNot(data, Utility.key.allow_notification)
                    Utility.saveForm(hashM, this)
                    Utility.set_login(this, true)
                    if (Utility.checkStringNullOrNot(data, Utility.key.user_type) == "202"){
                        hashM[Utility.key.reservation_status] = Utility.checkStringNullOrNot(data, Utility.key.reservation_status)

                        startActivity(Intent(this, HomeCompanyActivity::class.java))
                        finishAffinity()
                    }else{
                        startActivity(Intent(this, HomeVisitorActivity::class.java))
                        finishAffinity()
                    }
                    Utility.customSuccessToast(this,
                        Utility.checkStringNullOrNot(result, Utility.key.message)
                    )
                }else{
                    Utility.customErrorToast(this,
                        Utility.checkStringNullOrNot(result, Utility.key.message)
                    )
                }
            }else{
                /*Utility.customErrorToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )*/
                Utility.showApiMessageError(this, result, "data")
            }
        }
    }
}