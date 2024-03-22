package com.planout.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_change_password.btnResetPass
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject

class ChangePasswordActivity : AppCompatActivity(), ApiResponse {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        txtHeader.text = getString(R.string.change_password)
        clickView()

        editOldPass.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editOldPass, oldPassErr, false, "")
        }
        editNewPass.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(linNewPass, newPassErr, false, "")
        }
        editCnfmPass.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(linCnfmPass, confmPassErr, false, "")
        }
    }

    private fun clickView() {
        Utility.animationClick(imgBackHeader).setOnClickListener {
            onBackPressed()
        }
        Utility.animationClick(imgEyeOnNewPass).setOnClickListener {
            //show hide password
            Utility.showHidePass(this, imgEyeOnNewPass, editNewPass)
        }
        Utility.animationClick(imgEyeOnCnfmPass).setOnClickListener {
            //show hide password
            Utility.showHidePass(this, imgEyeOnCnfmPass, editCnfmPass)
        }
        Utility.animationClick(btnResetPass).setOnClickListener {
            //validation for change password
            when {
                editOldPass.text.toString().isBlank() -> {
                    //Utility.customErrorToast(this,"Enter your old password")
                    Utility.showGoneErrorView(editOldPass, oldPassErr, true, getString(R.string.enter_old_password))
                }
                editNewPass.text.toString().isBlank() -> {
                    //Utility.customErrorToast(this,"Enter your new password")
                    Utility.showGoneErrorView(linNewPass, newPassErr, true, getString(R.string.enter_new_password))
                }
                !Utility.isValidPassword(editNewPass.text.toString()) -> {
                    //Utility.customErrorToast(this,"Please add minimum 6 characters which include one upper/lower case/ special character and one digit")
                    Utility.showGoneErrorView(linNewPass, newPassErr, true, getString(R.string.password_validation))
                }
                editCnfmPass.text.toString().isBlank() -> {
                    //Utility.customErrorToast(this,"Enter confirm password")
                    Utility.showGoneErrorView(linCnfmPass, confmPassErr, true, getString(R.string.enter_confirm_password))
                }
                editNewPass.text.toString()!=editCnfmPass.text.toString() -> {
                    //Utility.customErrorToast(this,"The password and password confirmation must match")
                    Utility.showGoneErrorView(linCnfmPass, confmPassErr, true, getString(R.string.confirm_password_not_match))
                }
                else -> {
                    Utility.hideSoftKeyboard(this)
                    //call api for change password
                    changePasswordApi()
                }
            }

        }

    }

    private fun changePasswordApi() {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.old_password,editOldPass.text.toString())
        mBuilder.add(Utility.key.password,editNewPass.text.toString())
        mBuilder.add(Utility.key.rpassword,editCnfmPass.text.toString())
        CallApi.callAPi(mBuilder, ApiController.api.changepassword, this, Utility.changepassword, true, Utility.POST, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.changepassword){
            if (isData){
                //clear widgets
                editOldPass.setText("")
                editNewPass.setText("")
                editCnfmPass.setText("")
                Utility.customSuccessToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
                onBackPressed()
            }else {
                Utility.customErrorToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }
    }
}