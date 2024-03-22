package com.planout.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.planout.R
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_no_internet.*

class NoInternetActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out)
        btnTry.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        if (p0!!.id == R.id.btnTry) {
            if (Utility.hasConnection(this@NoInternetActivity)) {
                /*val intent = Intent()
                setResult(2, intent)
                overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out)*/
                finish()
            } else {
                Utility.customWarningToast(this, getString(R.string.no_internet_connection))
            }
        }
    }

    override fun onBackPressed() {
        if (Utility.hasConnection(this@NoInternetActivity)) {
            /*val intent = Intent()
            setResult(2, intent)
            overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out)*/
            finish()
            //finishing activity
        } else {
            Utility.normal_toast(this, getString(R.string.no_internet_connection))
        }
    }
}