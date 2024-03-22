package com.planout.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.planout.R
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_event_details.*

class EventDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_details)

        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT

        imgBack.setOnClickListener { onBackPressed() }

        //set event details
        if (intent.hasExtra("eventName")){
            Utility.SetImageSimple(this,intent.getStringExtra("eventImg").toString(),imgCoverPhoto)
            Utility.SetImageSimple(this,intent.getStringExtra("storeImg").toString(),profile_image)
            txtTitle.text = intent.getStringExtra("eventName").toString()
            txtSubTitle.text = intent.getStringExtra("eventDateTime").toString()
            txtAddress.text = intent.getStringExtra("eventAddress").toString()
            if (intent.getStringExtra("eventDesc").toString().isNotEmpty()) {
                txtDesc.visibility = View.VISIBLE
                txtDesc.text = intent.getStringExtra("eventDesc").toString()
            }
        }
    }
}