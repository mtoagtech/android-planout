package com.planout.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import androidx.core.widget.doOnTextChanged
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_forgot_password.*
import okhttp3.FormBody
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity(), ApiResponse {

    var strEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        onClick()
        editEmail.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editEmail, emailErr, false, "")
        }
    }



    private fun onClick() {
        Utility.animationClick(imgBack).setOnClickListener { onBackPressed() }
        btnSendLink.setOnClickListener {
            //validation for forgot password
            checkValidation()
        }
    }

    private fun checkValidation() {
        strEmail = editEmail.text.toString()
        if (TextUtils.isEmpty(strEmail)){
            //Utility.customErrorToast(this, Utility.msg_email)
            Utility.showGoneErrorView(editEmail, emailErr, true, getString(R.string.msg_email))
        }else if (!Utility.isValidMail(strEmail)){
            //Utility.customErrorToast(this, Utility.msg_email_valid)
                    Utility.showGoneErrorView(editEmail, emailErr, true, getString(R.string.msg_email_valid))
        }else{
            Utility.hideSoftKeyboard(this)
            if (Utility.hasConnection(this)) {
                //call api for forgot password
                callForgotApi()
            }else{
                Utility.customErrorToast(this, getString(R.string.msg_internet_conn))
            }
        }
    }

    private fun callForgotApi() {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.email, strEmail)
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
        mBuilder.add(Utility.key.language,language)

        CallApi.callAPi(mBuilder, ApiController.api.forgot_password, this, Utility.forgot_password, true, Utility.POST, false)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.forgot_password){
            if (isData){
                if (result.getBoolean(Utility.key.success)){
                    startActivity(Intent(this, MailSentActivity::class.java))
                    Utility.customSuccessToast(this,
                        Utility.checkStringNullOrNot(result, Utility.key.message)
                    )
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
        }
    }
}