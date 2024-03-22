package com.planout.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.planout.R
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_create_new_password.*

class CreateNewPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_password)

        clickView()
    }

    private fun clickView() {
        Utility.animationClick(imgEyeOn).setOnClickListener(View.OnClickListener {
            //show hide password
            Utility.showHidePass(this, imgEyeOn, editPass)
        })
    }
}