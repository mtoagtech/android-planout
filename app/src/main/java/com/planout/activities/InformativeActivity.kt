package com.planout.activities

import android.graphics.Bitmap
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.planout.R
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_informative.*
import kotlinx.android.synthetic.main.header_normal_view.*
import java.util.Locale


class InformativeActivity : AppCompatActivity() {
    val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informative)

        txtHeader.text = intent.getStringExtra("Title").toString()
        viewClick()
        //set webView
        webViewSet()

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

    private fun viewClick() {
        imgBackHeader.setOnClickListener { onBackPressed() }
    }

    private fun webViewSet() {
        runOnUiThread {
            webView.settings.javaScriptEnabled = true
            if (!intent.getBooleanExtra("invoice", false)) {
                webView.settings.loadWithOverviewMode = true
                webView.settings.builtInZoomControls = false
                webView.settings.useWideViewPort = true
                webView.settings.pluginState = WebSettings.PluginState.ON
                webView.setBackgroundColor(Color.TRANSPARENT)

                //webView.settings.defaultFontSize = 15
//                webView.settings.setAppCacheEnabled(false)
                webView.settings.blockNetworkImage = true
                webView.settings.loadsImagesAutomatically = true
                webView.settings.setGeolocationEnabled(false)
                webView.settings.setNeedInitialFocus(false)
                webView.settings.saveFormData = false
            }
            // wb.settings.setPluginsEnabled(true)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
                    Utility.show_progress(this@InformativeActivity)
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    Utility.hide_progress(this@InformativeActivity)
                }
            }
            webView.loadUrl(intent.getStringExtra("URLs")!!)
            Log.d("WEB_URL",intent.getStringExtra("URLs")!!)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }
}