package com.planout.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.planout.R

class WelSecondActivity : AppCompatActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash2)
    }

    override fun onResume() {
        super.onResume()
        handler = Handler()
        handler.postDelayed(Runnable { startActivity(Intent(this, WalkthroughActivity::class.java)) }, 2000)
    }

    override fun onStop() {
        super.onStop()
    }
}