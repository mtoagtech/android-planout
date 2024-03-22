package com.planout.api_calling

import android.app.Activity
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import com.planout.activities.LoginActivity
import com.planout.constant.Utility
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class CallFileApi {
    private var mCallback: ApiResponse? = null
    internal fun doPostRequest(
        url: String,
        mBuilder: MultipartBody.Builder,
        activity: Activity,
        type: String,
        showProgress: Boolean,
        apiMethod: String,
        setHeader: Boolean) {

        var client = OkHttpClient()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        client.retryOnConnectionFailure
        client.connectTimeoutMillis
        client.readTimeoutMillis
        client.writeTimeoutMillis
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(60, TimeUnit.SECONDS)
        builder.readTimeout(60, TimeUnit.SECONDS)
        builder.writeTimeout(60, TimeUnit.SECONDS)
        client = builder.build()
        val formBody = mBuilder.build()

        val request :Request
        if (setHeader){
            request = if (apiMethod== Utility.GET){
                Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization","Bearer "+ Utility.getForm(activity, Utility.key.auth_token)!!)
                    .get()
                    .build()

            }else {
                Request.Builder()
                    .url(url)
                    .addHeader("Accept","application/json")
                    .addHeader("Authorization","Bearer "+ Utility.getForm(activity, Utility.key.auth_token)!!)
                    .post(formBody)
                    .build()
            }

        }else{
            request = if (apiMethod==Utility.GET){
                Request.Builder()
                    .url(url)
                    .get()
                    .build()

            }else {
                Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build()
            }

        }

        Log.d("result>>>url", url)
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    if (showProgress) {
                        Utility.hide_progress(activity)
                    }
                    Log.d("error", e.toString())
                    Utility.customErrorToast(
                            activity, "Slow internet connection"
                    )
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                activity.runOnUiThread {
                    if (showProgress) {
                        Utility.hide_progress(activity)
                    }
                    try {
                        val result = response.body!!.string()
                        Log.d("result>>>$type", result)
                        val objJson = JSONObject(result)
                        mCallback = activity as ApiResponse
                        if (Utility.checkStringNullOrNot(objJson, Utility.key.message).contains("Unauthenticated")){
                            Utility.clear_detail(activity)
                            Utility.set_login(activity, false)
                            activity.startActivity(Intent(activity, LoginActivity::class.java))
                            activity.finishAffinity()
                        }else{
                            mCallback!!.onTaskComplete(objJson, type, objJson.getBoolean(Utility.key.success))
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    companion object {
        // post request code here
        @Throws(IOException::class)
        fun callAPi(
                mBuilder: MultipartBody.Builder,
                url: String,
                activity: Activity,
                type: String,
                showProgress: Boolean,
                apiMethod: String,
                setHeader: Boolean
        ) {
            if (showProgress) {
                Utility.show_progress(activity)
            }
            val example = CallFileApi()
            example.doPostRequest(url, mBuilder, activity, type, showProgress, apiMethod, setHeader)

        }
    }
}