package com.planout.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.VolumeShaper
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.ActivityCompat.recreate
import androidx.core.os.LocaleListCompat
import androidx.core.view.marginTop
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.*
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import kotlinx.android.synthetic.main.activity_company_profile.dataAria
import kotlinx.android.synthetic.main.activity_login.editPass
import kotlinx.android.synthetic.main.activity_login.imgEyeOn
import kotlinx.android.synthetic.main.activity_login.linPass
import kotlinx.android.synthetic.main.activity_login.passErr
import kotlinx.android.synthetic.main.activity_registration.editEmailVisitor
import kotlinx.android.synthetic.main.activity_registration.editNameVisitor
import kotlinx.android.synthetic.main.activity_registration.editPassVisitor
import kotlinx.android.synthetic.main.activity_registration.nameVisitorErr
import kotlinx.android.synthetic.main.fragment_company_more.menuIc
import kotlinx.android.synthetic.main.fragment_company_more.switchSubAccount
import kotlinx.android.synthetic.main.fragment_company_more.view.*
import kotlinx.android.synthetic.main.language_bottom.imgTop
import kotlinx.android.synthetic.main.sub_account_popup.btnCreateSub
import kotlinx.android.synthetic.main.sub_account_popup.close_ic
import kotlinx.android.synthetic.main.sub_account_popup.emailText
import kotlinx.android.synthetic.main.sub_account_popup.nameErr
import kotlinx.android.synthetic.main.sub_account_popup.nameText
import kotlinx.android.synthetic.main.sub_account_popup.passText
import kotlinx.android.synthetic.main.sub_account_popup.view.btnCreateSub
import kotlinx.android.synthetic.main.sub_account_popup.view.close_ic
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.util.Locale

class CompanyMoreFragment(val activityBase: HomeCompanyActivity) : Fragment(), ApiResponse {

    lateinit var rootView: View
    lateinit var subAccountData: JSONArray
    var strName: String = ""
    var strEmail: String = ""
    var strPasword: String = ""
    var strNameEdit: String = ""
    var strEmailEdit: String = ""
    var subAccountId: String = ""
    var languageData: String = ""
    var subAccountStatus: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_company_more, container, false)

        clickView()

        if(Utility.getForm(activityBase, Utility.key.is_owner) != "1"){
            companyDetailApi()

        }
        else{
            subAccountToogleApi()

        }




        return rootView
    }

    override fun onResume() {
        super.onResume()
        viewData()
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
            if (btnValue == 0) {
                languageApiCall("en")

            } else {
                languageApiCall("gr")
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

        startActivity(Intent(activityBase, HomeCompanyActivity::class.java))
        activityBase.finishAffinity()
    }



    private fun viewData() {
        //notification status |  1->enable  2->disable



        if(Utility.getForm(activityBase, Utility.key.is_owner) == "1"){
            rootView.topAria3.visibility = View.VISIBLE

            if(Utility.getForm(activityBase, Utility.key.allow_notification) == "1"){
                rootView.switchNotify.setToggleOn()
            }else{
                rootView.switchNotify.setToggleOff()
            }

            if(Utility.getForm(activityBase, Utility.key.reservation_status) == "1"){
                rootView.switchReservation.setToggleOn()
            }else{
                rootView.switchReservation.setToggleOff()
            }
        }else{
            rootView.topAria3.visibility = View.GONE
            val layoutParams =  rootView.topAria2.layoutParams
            layoutParams.height = 150
            rootView.imageReservition.visibility = View.GONE
            rootView.textReservition.visibility = View.GONE
            rootView.switchReservation.visibility = View.GONE
            rootView.topAria.visibility = View.GONE
            rootView.switchReservation.isEnabled = false
            rootView.switchNotify.isEnabled = false
            rootView.changePasswordTxt.visibility = View.GONE
            rootView.lineChangePassword.visibility = View.GONE
            //rootView.contactTxt.isEnabled = false

            if(Utility.getForm(activityBase, Utility.key.reservation_status) == "1"){
                rootView.switchReservation.setToggleOn()
            }else{
                rootView.switchReservation.setToggleOff()
            }

        }

    }

    private fun clickView() {
        rootView.switchNotify.setOnToggleChanged {
            //notification status |  1->enable  2->disable
            if (it){
                //call api for enable notification
                enableNotifyApi()
            }else{
                //call api for disable notification
                disableNotifyApi()
            }
        }

        rootView.txtBtnLanguage.setOnClickListener {
            openLanguage()
        }

        rootView.switchReservation.setOnToggleChanged {
            //reservation status |  1->enable  2->disable
            if (it){
                //call api for enable notification
                enableReservationApi()
            }else{
                //call api for disable notification
                disableReservationApi()
            }
        }


        Utility.animationClick(rootView.logoutTxt).setOnClickListener {
            //dialog for logout account
            showLogoutPopUp(getString(R.string.confirm_logout), getString(R.string.sure_log_out))
        }

        Utility.animationClick(rootView.changePasswordTxt).setOnClickListener {
            startActivity(Intent(activityBase, ChangePasswordActivity::class.java))
        }



        Utility.animationClick(rootView.companyTxt).setOnClickListener {
            startActivity(Intent(activityBase, CompanyProfileActivity::class.java))
        }

        if(Utility.getForm(activityBase, Utility.key.is_owner) == "1") {
            Utility.animationClick(rootView.locationTxt).setOnClickListener {
                startActivity(Intent(activityBase,AddLocationActivity::class.java)
                    .putExtra(Utility.key.store_id,Utility.getForm(activityBase,Utility.key.store_id)))
            }
            Utility.animationClick(rootView.galleryTxt).setOnClickListener {
                startActivity(Intent(activityBase,PhotoGalleryActivity::class.java)
                    .putExtra(Utility.key.store_id,Utility.getForm(activityBase,Utility.key.store_id)))
            }
            Utility.animationClick(rootView.workingDaysHour).setOnClickListener {
                startActivity(Intent(activityBase, CompanyWorkDayHourActivity::class.java))
            }
            Utility.animationClick(rootView.contactTxt).setOnClickListener {
                startActivity(Intent(activityBase, ContactFormActivity::class.java))
            }
            Utility.animationClick(rootView.subscriptionTxt).setOnClickListener {
                activityBase.setSubscriptionBottomView()
            }
        }
        else{
            rootView.locationTxt.visibility = View.GONE
            rootView.galleryTxt.visibility = View.GONE
            rootView.workingDaysHour.visibility = View.GONE
            rootView.contactTxt.visibility = View.GONE
            rootView.lineGallery.visibility = View.GONE
            rootView.lineLoaction.visibility = View.GONE
            rootView.lineWorkingHour.visibility = View.GONE
            rootView.lineContact.visibility = View.GONE
            rootView.subscriptionTxt.visibility = View.GONE
            rootView.lineSubscription.visibility = View.GONE

        }

         Utility.animationClick(rootView.txtBtnPayHistory).setOnClickListener {
             startActivity(Intent(activityBase, PaymentHistoryActivity::class.java))
        }


        /////////Informative Pages//////////////////
        Utility.animationClick(rootView.txtBtnAbout).setOnClickListener {
            startActivity(Intent(activityBase, InformativeActivity::class.java)
                .putExtra("Title", getString(R.string.about))
                .putExtra("invoice", false)
                .putExtra("URLs", ApiController.api.about_url))
        }
        Utility.animationClick(rootView.txtBtnPrivacy).setOnClickListener {
            startActivity(Intent(activityBase, InformativeActivity::class.java)
                .putExtra("Title", getString(R.string.privacy_policy))
                .putExtra("invoice", false)
                .putExtra("URLs", ApiController.api.policy_url))
        }
        Utility.animationClick(rootView.txtBtnTerms).setOnClickListener {
            startActivity(Intent(activityBase, InformativeActivity::class.java)
                .putExtra("Title", getString(R.string.terms_and_conditions))
                .putExtra("invoice", false)
                .putExtra("URLs", ApiController.api.terms_url))
        }


        Utility.animationClick(rootView.eventsTxt).setOnClickListener {
            activityBase.setEventBottomView()
        }
        Utility.animationClick(rootView.txtReservation).setOnClickListener {
            activityBase.setHomeBottomView()
        }


        rootView.menuIc.setOnClickListener {
            showPopup(menuIc)
        }
        rootView.switchSubAccount.setOnClickListener{
            if(TextUtils.isEmpty(subAccountId)){
                createSubPopup()
            }
            else{
                subAccEnableDisable()
            }

        }

    }

    private fun companyDetailApi() {
       // dataAria.showOrGone(false)
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.profile, activityBase, Utility.profile, true, Utility.GET, true)
    }


    private fun createSubPopup() {
        val dialog = Dialog(activityBase)
        dialog.setContentView(R.layout.sub_account_popup)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val close_ic = dialog.findViewById(R.id.close_ic) as CardView
        val nameText = dialog.findViewById(R.id.nameText) as EditText
        val emailText = dialog.findViewById(R.id.emailText) as EditText
        val passText = dialog.findViewById(R.id.passText) as EditText
        val btnCreateSub = dialog.findViewById(R.id.btnCreateSub) as Button
        val nameErr = dialog.findViewById(R.id.nameErr) as TextView
        val emailErr = dialog.findViewById(R.id.emailErr) as TextView
        val passErr = dialog.findViewById(R.id.passErr) as TextView
        val imgEyeOn = dialog.findViewById(R.id.imgEyeOn) as ImageView
        val topText = dialog.findViewById(R.id.topText) as TextView

        nameText.setText(strNameEdit)
        emailText.setText(strEmailEdit)

        if(TextUtils.isEmpty(subAccountId)){
            topText.text = getString(R.string.create)
            btnCreateSub.text = getString(R.string.create)
            passText.hint = getString(R.string.password)
        }
        else{
            topText.text = getString(R.string.edit)
            btnCreateSub.text = getString(R.string.update)
            passText.hint = getString(R.string.password)
        }

        Utility.animationClick(imgEyeOn).setOnClickListener(View.OnClickListener {
            Utility.showHidePass(activityBase, imgEyeOn, passText)
        })

        nameText.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(nameText, nameErr, false, getString(R.string.msg_name_valid_sub))
        }
        emailText.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(emailText, emailErr, false, getString(R.string.msg_email_sub))
        }
        passText.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(passText, passErr, false, getString(R.string.msg_pass_sub))
        }

        btnCreateSub.setOnClickListener {

            strName = nameText.text.toString()
            strEmail = emailText.text.toString()
            strPasword = passText.text.toString()

            if(TextUtils.isEmpty(strName)){
                //nameText.requestFocus()
                Utility.showGoneErrorView(nameText, nameErr, true, getString(R.string.msg_name_valid_sub))
            }
            else if(TextUtils.isEmpty(strEmail)){
                Utility.showGoneErrorView(emailText, emailErr, true, getString(R.string.msg_email_sub))
            }
            else if(!Utility.isValidMail(strEmail)){
                Utility.showGoneErrorView(emailText, emailErr, true, getString(R.string.msg_email_valid))
            }
            else if(TextUtils.isEmpty(strPasword) && TextUtils.isEmpty(subAccountId)){
                Utility.showGoneErrorView(passText, passErr, true, getString(R.string.msg_pass_sub))
            }
            else if (strPasword.length<6 && TextUtils.isEmpty(subAccountId)){
                Utility.showGoneErrorView(passText, passErr, true, getString(R.string.msg_pass_valid_char))
            }
            else{
                Utility.hideSoftKeyboard(activityBase)
                if(TextUtils.isEmpty(subAccountId)){
                    createSubAccountApi()
                }
                else{
                    editSubAccountApi()
                }

                dialog.dismiss()
            }
        }

        close_ic.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun editSubAccountApi(){
        Utility.getCurrentAppVersion(activityBase)
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.name, strName)
        mBuilder.add(Utility.key.email, strEmail)
        mBuilder.add(Utility.key.password, strPasword)
        mBuilder.add(Utility.key.confirm_password, strPasword)
        mBuilder.add(Utility.key.login_from,"0")
        CallApi.callAPi(mBuilder, ApiController.api.stores_account+"/"+subAccountId, activityBase, Utility.stores_account_edit, true, Utility.PUT, true)

    }


    private fun createSubAccountApi(){
        Utility.getCurrentAppVersion(activityBase)
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.name, strName)
        mBuilder.add(Utility.key.email, strEmail)
        mBuilder.add(Utility.key.password, strPasword)
        mBuilder.add(Utility.key.confirm_password, strPasword)
        mBuilder.add(Utility.key.login_from,"0")
        //Log.d("TAG", "callLoginApi: "+strEmail+"..."+strPass+"..."+Utility.device_id+"..."+Utility.fcm_tocken)
        CallApi.callAPi(mBuilder, ApiController.api.stores_account, activityBase, Utility.stores_account_create, true, Utility.POST, true)

    }


    private fun subAccEnableDisable(){
        val mBuilder = FormBody.Builder()
        if(subAccountStatus == 501){
            mBuilder.add(Utility.key.status, "502")
        }
        else{
            mBuilder.add(Utility.key.status, "501")
        }

        //Log.d("TAG", "callLoginApi: "+strEmail+"..."+strPass+"..."+Utility.device_id+"..."+Utility.fcm_tocken)
        CallApi.callAPi(mBuilder, ApiController.api.change_status+"/"+subAccountId, activityBase, Utility.stores_account_change_status, true, Utility.POST, true)
    }





    private fun showPopup(view: View) {
        val popup = PopupMenu(activityBase, view)
        popup.inflate(R.menu.company_more_menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.editBtn -> {
                    createSubPopup()
                }
                R.id.deleteBtn -> {
                    //Toast.makeText(activityBase, item.title, Toast.LENGTH_SHORT).show()
                    deleteSubAccountPopup()
                }
            }

            true
        })

        popup.show()
    }

    private fun deleteSubAccountPopup(){
//        deleteSubAccount()
        val dialog = Dialog(activityBase)
        dialog.setContentView(R.layout.delete_subaccount_popup)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView

        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.setOnClickListener { dialog.dismiss()
            deleteSubAccount()
        }

        dialog.show()
    }

    private fun deleteSubAccount(){
        //Utility.getCurrentAppVersion(activityBase)
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.stores_account+"/"+subAccountId, activityBase, Utility.stores_account_delete, true, Utility.DELETE, true)

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

        /*val inflater = activityBase.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.logout_popup_view, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

        val txtTitle = popupView.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = popupView.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = popupView.findViewById(R.id.txtCancel) as TextView
        val txtDelete = popupView.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.setOnClickListener { popupWindow.dismiss() }
        txtDelete.text="Logout"
        txtDelete.setOnClickListener { popupWindow.dismiss()
            logoutApi()
        }*/

    }
    private fun logoutApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.logout, activityBase, Utility.logout, true, Utility.POST, true)

    }

    private fun enableNotifyApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.notifications_enable, activityBase, Utility.notifications_enable, true, Utility.POST, true)

    }
    private fun disableNotifyApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.notifications_disable, activityBase, Utility.notifications_disable, true, Utility.POST, true)

    }
    private fun enableReservationApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.reservation_enable, activityBase, Utility.reservation_enable, true, Utility.POST, true)
    }

    private fun disableReservationApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.reservation_disable, activityBase, Utility.reservation_disable, true, Utility.POST, true)
    }

    private fun subAccountToogleApi() {

        Log.d("ajkaksjfkasj","paipiapipip")
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            ApiController.api.stores_account,
            activityBase,
            Utility.stores_account,
            true,
            Utility.GET,
            true
        )

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {

        Log.d("Result type :- ","slklaskflkaflkaslf")
        if (type==Utility.logout){
            if (isData){

                val rememberEmail = Utility.getForm(activityBase, Utility.key.rememberEmail)
                val rememberPass = Utility.getForm(activityBase, Utility.key.rememberPass)
                val rememberLanguage = Utility.getForm(activityBase, Utility.key.language)


                Utility.customSuccessToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
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
        }
        else if (type==Utility.notifications_enable){
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
        }
        else if (type==Utility.notifications_disable){
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
        else if (type==Utility.reservation_enable){
            Log.d("'---------------------'",result.toString())
            if (isData){
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.reservation_status] = "1"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchReservation.setToggleOn()
                Utility.customSuccessToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }else{
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.reservation_status] = "0"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchReservation.setToggleOff()
                Utility.customErrorToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }
        else if (type==Utility.reservation_disable){
            Log.d("'---------------------'",result.toString())

            if (isData){
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.reservation_status] = "0"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchReservation.setToggleOff()
                Utility.customSuccessToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }else{
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.reservation_status] = "1"
                    Utility.saveForm(hashM, activityBase)
                }catch (e: Exception){e.printStackTrace()}
                rootView.switchReservation.setToggleOn()
                Utility.customErrorToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }
        else if(type==Utility.stores_account){
           Log.d("1111111111111111",result.toString())
            val data = result.getJSONArray("data")
            strNameEdit = data.getJSONObject(0).getString("name")
            strEmailEdit = data.getJSONObject(0).getString("email")
            strPasword = data.getJSONObject(0).getString("store_name")
            subAccountId = data.getJSONObject(0).getString("id")
            subAccountStatus = data.getJSONObject(0).getInt("status")
            var status = data.getJSONObject(0).getInt("status")

            Log.d("email value data :-","Edit Name Data :- "+ data.length())

           subAccountData = result.getJSONArray("data")
            if(data.length() > 0){
                if(status == 501){
                    rootView.switchSubAccount.setToggleOn()
                    rootView.menuIc.visibility = View.VISIBLE
                }
                else{
                    rootView.switchSubAccount.setToggleOff()
                    rootView.menuIc.visibility = View.GONE
                }

            }
            else{
                rootView.switchSubAccount.setToggleOff()
                rootView.menuIc.visibility = View.GONE
            }
        }
        else if(type==Utility.stores_account_edit){
            if (result.getBoolean(Utility.key.success)) {
                subAccountToogleApi()
                val data = result.getJSONArray("data")
                strNameEdit = data.getJSONObject(0).getString("name")
                strEmailEdit = data.getJSONObject(0).getString("email")
                strPasword = data.getJSONObject(0).getString("store_name")
                subAccountId = data.getJSONObject(0).getString("id")
                subAccountStatus = data.getJSONObject(0).getInt("status")

                val message = result.getString("message")
                Toast.makeText(activityBase, message, Toast.LENGTH_SHORT).show()
            }else{
                Utility.showApiMessageError(activityBase, result, "data")

            }

        }
        else if(type==Utility.stores_account_delete){
            strNameEdit = ""
            strEmailEdit = ""
            strPasword = ""
            subAccountId = ""
            rootView.switchSubAccount.setToggleOff()
            rootView.menuIc.visibility = View.GONE
            //subAccountToogleApi()
            val message = result.getString("message")
            Toast.makeText(activityBase, message, Toast.LENGTH_SHORT).show()
        }
        else if(type==Utility.stores_account_create){
            Log.d("Result type :- ",result.toString())
            if (result.getBoolean(Utility.key.success)) {
                val data = result.getJSONObject("data")
                Log.d("ajfljasjlasjf","All create data :- "+data)
                strNameEdit = data.getString("name")
                strEmailEdit = data.getString("email")
                //strPasword = data.getJSONObject(0).getString("store_name")
                subAccountId = data.getString("id")
                subAccountStatus = data.getInt("status")
                rootView.switchSubAccount.setToggleOn()
                rootView.menuIc.visibility = View.VISIBLE
                val message = result.getString("message")
                Toast.makeText(activityBase, message, Toast.LENGTH_SHORT).show()
            }else{

                Utility.showApiMessageError(activityBase, result, "data")
            }

        }
        else if(type==Utility.stores_account_change_status){
            //val data = result.getJSONArray("data")
            if(subAccountStatus == 501){
                subAccountStatus = 502
                rootView.switchSubAccount.setToggleOff()
                rootView.menuIc.visibility = View.GONE
            }
            else{
                subAccountStatus = 501
                rootView.switchSubAccount.setToggleOn()
                rootView.menuIc.visibility = View.VISIBLE
            }

            var message = result.getString("message")
            Toast.makeText(activityBase, message, Toast.LENGTH_SHORT).show()
        }
        else if(type==Utility.profile){

            var reservation = result.getJSONObject("data").getJSONObject("store").getInt("reservation_status")
            Log.d("dslgdsjgl;dsj;g ","kslakfkasflkas :- "+reservation)
            if(reservation == 1){
                rootView.switchReservation.setToggleOn()
            }
            else{
                rootView.switchReservation.setToggleOff()
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
}