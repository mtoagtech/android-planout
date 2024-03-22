package com.planout.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.planout.MainActivity
import com.planout.R
import com.planout.constant.Utility
import java.util.Locale

class SplashScreen : AppCompatActivity() {

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Utility.facebookHashKey(this)
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        /*window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)*/
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }*/

        val rememberLanguage = Utility.getForm(this, Utility.key.language)
        val hashM: HashMap<String, String> = HashMap()
        var english: String = "EN"
        var greek: String = "EL"

        Log.d("jjlasfalsfj :- ",rememberLanguage.toString())


        if(rememberLanguage.isNullOrBlank()){
            val locale = Locale("en") // or "el" for Greek
            Locale.setDefault(locale)

            val resources = this.resources

            val configuration = resources.configuration
            configuration.locale = locale
            configuration.setLayoutDirection(locale)

            resources.updateConfiguration(configuration, resources.displayMetrics)

        }
        else {
            // Set the language based on the value retrieved from shared preferences
            when (rememberLanguage) {
                english -> {
                    val locale = Locale("en") // or "el" for Greek
                    Locale.setDefault(locale)

                    val resources = this.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
                greek -> {
                    val locale = Locale("el") // or "el" for Greek
                    Locale.setDefault(locale)

                    val resources = this.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
                else -> {
                    val locale = Locale("en") // or "el" for Greek
                    Locale.setDefault(locale)

                    val resources = this.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        handler = Handler()
        if (Utility.hasConnection(this)) {
            handler.postDelayed(Runnable {
                if (Utility.is_login(this)) {
                    if (Utility.getForm(this, Utility.key.user_type) == "202") {
                        startActivity(Intent(this, HomeCompanyActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeVisitorActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, WalkthroughActivity::class.java))
                }
                finish()
            }, 2000)
        }
    }

    override fun onStop() {
        super.onStop()
    }
}