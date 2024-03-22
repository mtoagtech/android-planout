package com.planout.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.planout.R
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_reset_failed.*

class ResetFailedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_failed)
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out)
        txtSubTitle.text=intent.getStringExtra(Utility.key.message)
        doneButton.setOnClickListener {
            overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out)
            finish()
        }
    }
    override fun onBackPressed() {
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out)
        finish()
    }
}