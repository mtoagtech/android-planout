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
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.header_normal_view.*

class WebView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
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
                Log.d("URL_LOAD_START", url)
                Utility.show_progress(this@WebView)
                if (url.contains("success")) {
                    val order_id = intent.getStringExtra("order_id")
                    val intent = Intent()
                    intent.putExtra("Payment_status", "success")
                    intent.putExtra("order_id", order_id)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else if (url.contains("fail")) {
                    val intent = Intent()
                    intent.putExtra("Payment_status", "fail")
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Utility.hide_progress(this@WebView)
            }
        }
        wb.loadUrl(intent.getStringExtra("URL")!!)
    }
    override fun onBackPressed() {
        if (wb.canGoBack()) {
            wb.goBack()
        } else {
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

}