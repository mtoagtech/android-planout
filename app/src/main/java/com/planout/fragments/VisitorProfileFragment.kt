package com.planout.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.*
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.isLoginCheck
import com.planout.constant.Utility.is_login
import kotlinx.android.synthetic.main.fragment_visitor_profile.view.*
import kotlinx.android.synthetic.main.fragment_visitor_profile.view.switchNotify
import okhttp3.FormBody
import org.json.JSONObject
import java.lang.Exception
import java.util.Locale

class VisitorProfileFragment(val activityBase: HomeVisitorActivity) : Fragment(), ApiResponse {

    lateinit var rootView: View
    var languageData: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_visitor_profile, container, false)
        activityBase.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        activityBase.window.statusBarColor = Color.TRANSPARENT

        clickView()
        return  rootView
    }

    private fun clickView() {
        //notification status |  1->enable  2->disable
        rootView.switchNotify.setOnToggleChanged {
            if (it){
                //call api for enable notification
                enableNotifyApi()
            }else{
                //call api for disable notification
                disableNotifyApi()
            }
        }
        Utility.animationClick(rootView.constLanguage).setOnClickListener {
            openLanguage()
        }
        Utility.animationClick(rootView.constFavorite).setOnClickListener {
            if (isLoginCheck(activityBase))
            startActivity(Intent(activityBase, FavoritesActivity::class.java))
        }
        Utility.animationClick(rootView.constSaved).setOnClickListener {
            if (isLoginCheck(activityBase))
            startActivity(Intent(activityBase, SavedSearchActivity::class.java))
        }
        Utility.animationClick(rootView.constReservation).setOnClickListener {
            if (isLoginCheck(activityBase))
                activityBase.setReservationBottomView()
        }
        Utility.animationClick(rootView.constChangPass).setOnClickListener {
            if (isLoginCheck(activityBase))
            startActivity(Intent(activityBase, ChangePasswordActivity::class.java))
        }
        Utility.animationClick(rootView.constContact).setOnClickListener {
            if (isLoginCheck(activityBase))
            startActivity(Intent(activityBase, ContactFormActivity::class.java))
        }

        ///////Informative Pages////////////
        Utility.animationClick(rootView.constAbout).setOnClickListener {
            startActivity(Intent(activityBase, InformativeActivity::class.java)
                .putExtra("Title", getString(R.string.about))
                .putExtra("invoice", false)
                .putExtra("URLs", ApiController.api.about_url))
        }
        Utility.animationClick(rootView.constPrivacy).setOnClickListener {
            startActivity(Intent(activityBase, InformativeActivity::class.java)
                .putExtra("Title", getString(R.string.privacy_policy))
                .putExtra("invoice", false)
                .putExtra("URLs", ApiController.api.policy_url))
        }
        Utility.animationClick(rootView.constTerms).setOnClickListener {
            startActivity(Intent(activityBase, InformativeActivity::class.java)
                .putExtra("Title", getString(R.string.terms_and_conditions))
                .putExtra("invoice", false)
                .putExtra("URLs", ApiController.api.terms_url))
        }

        Utility.animationClick(rootView.constLogout).setOnClickListener {
            if (isLoginCheck(activityBase)) {
                //dialog for logout account
                showLogoutPopUp(getString(R.string.confirm_logout), getString(R.string.sure_log_out))
            }
        }

    }


    private fun openLanguage() {

        var btnValue = 0
        val hashM: HashMap<String, String> = HashMap()
        var english: String = "EN"
        var greek: String = "EL"

        val rememberLanguage = Utility.getForm(activityBase, Utility.key.language)

        Log.d("Open language selection :- ",rememberLanguage.toString())
        Log.d("Odgdsgds[gd[gd[godgdg :- ",Utility.getForm(activityBase, Utility.key.language).toString())

        val serviceInfo_dialog = BottomSheetDialog(
            activityBase,
            R.style.AppBottomSheetDialogTheme
        )

        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(activityBase)
            .inflate(R.layout.language_bottom, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        val btnEnglish = dialogView.findViewById<LinearLayout>(R.id.btnEnglish)
        val btnGreek = dialogView.findViewById<LinearLayout>(R.id.btnGreek)
        val btnEnglishCheck = dialogView.findViewById<androidx.appcompat.widget.AppCompatCheckBox>(R.id.radBtnEnglish)
        val btnGreekCheck = dialogView.findViewById<androidx.appcompat.widget.AppCompatCheckBox>(R.id.radBtnGreek)
        val btnDone = dialogView.findViewById<Button>(R.id.apply_language)

        Log.d("aljlajfjalsfjas", rememberLanguage!!)

        if(rememberLanguage.isNullOrBlank()){
            hashM[Utility.key.language] = english
            Utility.saveForm(hashM, activityBase)
            btnEnglishCheck.isChecked = true
            btnGreekCheck.isChecked = false
        }
        else {
            // Set the language based on the value retrieved from shared preferences
            when (rememberLanguage) {
                english -> {
                    hashM[Utility.key.language] = english
                    Utility.saveForm(hashM, activityBase)
                    btnEnglishCheck.isChecked = true
                    btnGreekCheck.isChecked = false
                }
                greek -> {
                    hashM[Utility.key.language] = greek
                    Utility.saveForm(hashM, activityBase)
                    btnGreekCheck.isChecked = true
                    btnEnglishCheck.isChecked = false
                }
                else -> {
                    // Handle unexpected values
                    hashM[Utility.key.language] = english
                    Utility.saveForm(hashM, activityBase)
                    btnEnglishCheck.isChecked = true
                    btnGreekCheck.isChecked = false
                }
            }
        }


        btnDone.setOnClickListener {

            if(is_login(activityBase)){
                Log.d("assafsafasfasf1111","asfasfasfasfasf1111")
                if (btnValue == 0) {
                    languageApiCall("en")

                } else {
                    languageApiCall("gr")
                }
            }
            else{
                val hashM: HashMap<String, String> = HashMap()
                Log.d("Language data :- ","Language data :- "+languageData)
                if(btnValue == 0){
                    hashM[Utility.key.language] = "EN"
                    Utility.saveForm(hashM, activityBase)
                    setLocale("en")
                }
                else {
                    hashM[Utility.key.language] = "EL"
                    Utility.saveForm(hashM, activityBase)
                    setLocale("el")
                }
            }

            serviceInfo_dialog.dismiss()
        }

        btnEnglish.setOnClickListener {
            btnEnglishCheck.isChecked = true
            btnGreekCheck.isChecked = false
            btnValue = 0
        }
        btnGreek.setOnClickListener {
            btnGreekCheck.isChecked = true
            btnEnglishCheck.isChecked = false
            btnValue = 1
        }



        imgTop.setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun languageApiCall(type: String){
        languageData = type
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.language, type)
        CallApi.callAPi(mBuilder, ApiController.api.update_language, activityBase, Utility.update_language, true, Utility.POST, true)

    }


    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = activityBase.resources

        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        val hashM: HashMap<String, String> = HashMap()





        startActivity(Intent(activityBase, HomeVisitorActivity::class.java))
        activityBase.finishAffinity()
    }

    private fun enableNotifyApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.notifications_enable, activityBase, Utility.notifications_enable, true, Utility.POST, true)

    }
    private fun disableNotifyApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.notifications_disable, activityBase, Utility.notifications_disable, true, Utility.POST, true)

    }

    private fun showLogoutPopUp(title: String, subTitle: String) {
        val dialog = Dialog(activityBase)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.text=getString(R.string.logout)
        txtDelete.setOnClickListener { dialog.dismiss()
            logoutApi()
        }
        dialog.show()

    }

    private fun logoutApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.logout, activityBase, Utility.logout, true, Utility.POST, true)

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.logout){
            if (isData){
                val rememberEmail = Utility.getForm(activityBase, Utility.key.rememberEmail)
                val rememberPass = Utility.getForm(activityBase, Utility.key.rememberPass)
                val rememberLanguage = Utility.getForm(activityBase, Utility.key.language)


                Utility.customSuccessToast(activityBase,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
                Utility.clear_detail(activityBase)
                Utility.clearForm(activityBase)

                val hashM: java.util.HashMap<String, String> = java.util.HashMap()
                hashM[Utility.key.rememberEmail] = rememberEmail!!
                hashM[Utility.key.rememberPass] = rememberPass!!
                Utility.saveForm(hashM, activityBase)

                //val hashM2: HashMap<String, String> = HashMap()
                hashM[Utility.key.language] = rememberLanguage!!
                Utility.saveForm(hashM, activityBase)

                Utility.set_login(activityBase, false)
                startActivity(Intent(activityBase, LoginActivity::class.java))
                activityBase.finishAffinity()
            }else{
                Utility.customErrorToast(activityBase,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }else if (type==Utility.profile){
            if (isData){
                val data=result.getJSONObject(Utility.key.data)
                /*val total_unread_notifications=data.getInt(Utility.key.total_unread_notifications)
                activity.setNotificationView(total_unread_notifications>0)*/
                if(Utility.checkStringNullOrNot(data, Utility.key.allow_notification) == "1"){
                    rootView.switchNotify.setToggleOn()
                }else{
                    rootView.switchNotify.setToggleOff()
                }
                val hashM: HashMap<String, String> = HashMap()
                hashM[Utility.key.allow_notification] = Utility.checkStringNullOrNot(data, Utility.key.allow_notification)
                Utility.saveForm(hashM, activityBase)
                setData(data.getString(Utility.key.name),data.getString(Utility.key.email),data.getString(Utility.key.profile_image))
                Utility.animationClick(rootView.txtEdit).setOnClickListener {
                    startActivity(Intent(activityBase,EditProfileActivity::class.java)
                        .putExtra(Utility.key.name,data.getString(Utility.key.name))
                        .putExtra(Utility.key.email,data.getString(Utility.key.email))
                        .putExtra(Utility.key.profile_image,data.getString(Utility.key.profile_image))
                        .putExtra(Utility.key.dob,data.getString(Utility.key.dob))
                    )
                }
            }else{
                Utility.customErrorToast(activityBase,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }else if (type==Utility.notifications_enable){
            if (isData){
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.allow_notification] = "1"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchNotify.setToggleOn()
                Utility.customSuccessToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }else{
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.allow_notification] = "0"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchNotify.setToggleOff()
                Utility.customErrorToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }else if (type==Utility.notifications_disable){
            if (isData){
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.allow_notification] = "0"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchNotify.setToggleOff()
                Utility.customSuccessToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }else{
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.allow_notification] = "1"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchNotify.setToggleOn()
                Utility.customErrorToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }
        else if(type==Utility.update_language){
            val hashM: HashMap<String, String> = HashMap()
            Log.d("Language data :- ","Language data :- "+languageData)
            if(languageData == "gr"){
                hashM[Utility.key.language] = "EL"
                Utility.saveForm(hashM, activityBase)
                setLocale("el")
            }
            else {
                hashM[Utility.key.language] = "EN"
                Utility.saveForm(hashM, activityBase)
                setLocale("en")
            }


        }
    }

    override fun onResume() {
        super.onResume()
        if (Utility.is_login(activityBase)) {
            //get profile detail if user exist
            getDataData()
        }else{
            //show custom detail if user as guest
            rootView.txtProfileName.text= getString(R.string.hi_guest)
            rootView.txtMail.visibility = View.INVISIBLE
            rootView.txtEdit.visibility = View.INVISIBLE
            rootView.viewLst.visibility = View.INVISIBLE
            rootView.constLogout.visibility = View.GONE
            rootView.switchNotify.isEnabled = false
            rootView.profile_image.setImageDrawable(ContextCompat.getDrawable(activityBase, R.drawable.placeholder_img))
        }
    }

    private fun getDataData() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.profile, activityBase, Utility.profile, true, Utility.GET, true)

    }

    private fun setData(name: String, email: String, profileImage: String) {
        rootView.txtProfileName.text=name
        rootView.txtMail.text=email
        Utility.SetImageSimple(activityBase,profileImage,rootView.profile_image)
    }

}