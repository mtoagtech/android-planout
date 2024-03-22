package com.planout.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.planout.R
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_mail_sent.*

class MailSentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail_sent)

        //Utility.setStatusBarColor(this)
        clickView()
    }

    private fun clickView() {
        Utility.animationClick(imgBack).setOnClickListener { onBackPressed() }
        Utility.animationClick(txtBottom).setOnClickListener { onBackPressed() }
        Utility.animationClick(btnDone).setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            finishAffinity()
        }
    }
}