package com.planout.activities.payment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.planout.R
import com.planout.constant.Utility
import kotlinx.android.synthetic.main.activity_web_view_paypal.*
import kotlinx.android.synthetic.main.header_normal_view.*

class WebViewPaypal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view_paypal)

        imgBackHeader.setOnClickListener {
            onBackPressed()
        }

        txtHeader.text = intent.getStringExtra("title")
        wb.settings.javaScriptEnabled = true
        wb.settings.loadWithOverviewMode = true
        wb.settings.useWideViewPort = true
        wb.settings.builtInZoomControls = true
        wb.settings.pluginState = WebSettings.PluginState.ON
        // wb.settings.setPluginsEnabled(true)

        wb.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
                Log.d("URL_LOAD_START",url)
                Utility.show_progress(this@WebViewPaypal)
                if (url.contains("success")){
                    val url_split=url.split("orderID=")
                    val orderID=url_split[1]
                    val intent = Intent()
                    intent.putExtra("Payment_status","success")
                    intent.putExtra("token",orderID)
                    setResult(Activity.RESULT_OK,intent)
                    finish()

                }else if (url.contains("fail")){
                    val intent = Intent()
                    intent.putExtra("Payment_status","fail")
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                Utility.hide_progress(this@WebViewPaypal)

            }
        }

        wb.loadUrl(intent.getStringExtra("URL")!!)

    }
    override fun onBackPressed() {
        if (wb.canGoBack()) {
            wb.goBack()
        } else {
            super.onBackPressed()
        }
    }

}

