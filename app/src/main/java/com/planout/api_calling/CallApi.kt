package com.planout.api_calling

import android.app.Activity
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import com.planout.activities.LoginActivity
import com.planout.constant.Utility
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class CallApi {
    private var mCallback: ApiResponse? = null

    private fun doPostRequestJson(
        url: String,
        mBuilder: JSONObject,
        activity: Activity,
        type: String,
        showProgress: Boolean,
        apiMethod: String,
        sendHeader: Boolean
    ) {
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
        val mediaType = "application/json".toMediaTypeOrNull()
        val body: RequestBody = mBuilder.toString().toRequestBody(mediaType)
        val request: Request

        if (sendHeader){
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
                    .post(body)
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
                    .post(body)
                    .build()
            }
        }

        Log.d("result>>>url", url)
        Log.d("result>>>body", mBuilder.toString())

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


    internal fun doPostRequest(
        url: String,
        mBuilder: FormBody.Builder,
        activity: Activity,
        type: String,
        showProgress: Boolean,
        apiMethod: String,
        sendHeader: Boolean) {

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
        val request: Request
        Log.d("Auth Token", Utility.getForm(activity, Utility.key.auth_token)!!);
        if (sendHeader){
            request = when (apiMethod) {
                Utility.GET -> {
                    Request.Builder()
                        .url(url)
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization","Bearer "+ Utility.getForm(activity, Utility.key.auth_token)!!)
                        .get()
                        .build()
                }
                Utility.DELETE -> {
                    Request.Builder()
                        .url(url)
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization","Bearer "+ Utility.getForm(activity, Utility.key.auth_token)!!)
                        .delete(formBody)
                        .build()
                }
                Utility.PUT -> {
                    Request.Builder()
                        .url(url)
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization","Bearer "+ Utility.getForm(activity, Utility.key.auth_token)!!)
                        .put(formBody)
                        .build()
                }

                else -> {
                    Request.Builder()
                        .url(url)
                        .addHeader("Accept","application/json")
                        .addHeader("Authorization","Bearer "+ Utility.getForm(activity, Utility.key.auth_token)!!)
                        .post(formBody)
                        .build()
                }
            }

        } else {
            request = when (apiMethod) {
                Utility.GET -> {
                    Request.Builder()
                        .url(url)
                        .get()
                        .build()
                }
                Utility.DELETE -> {
                    Request.Builder()
                        .url(url)
                        .delete(formBody)
                        .build()
                }
                Utility.PUT -> {
                    Request.Builder()
                        .url(url)
                        .put(formBody)
                        .build()
                }

                else -> {
                    Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build()
                }
            }
        }

        Log.d("result>>>url", url)
        for (i in 0 until formBody.size){
            Log.d("result>>>"+formBody.name(i),formBody.value(i))
        }
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
                        if (type==Utility.saferpayPayment){
                            mCallback!!.onTaskComplete(objJson, type, true)
                        }else{
                            if (Utility.checkStringNullOrNot(objJson, Utility.key.message).contains("Unauthenticated")){
                                Utility.clear_detail(activity)
                                Utility.set_login(activity, false)
                                activity.startActivity(Intent(activity, LoginActivity::class.java))
                                activity.finishAffinity()
                            }else{

                                mCallback!!.onTaskComplete(objJson, type, objJson.getBoolean(Utility.key.success))
                            }
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
                mBuilder: FormBody.Builder,
                url: String,
                activity: Activity,
                type: String,
                showProgress: Boolean,
                apiMethod: String,
                sendHeader: Boolean
        ) {

            if (showProgress) {
                Utility.show_progress(activity)
            }
            val example = CallApi()
            example.doPostRequest(url, mBuilder, activity, type, showProgress, apiMethod, sendHeader)

        }

        @Throws(IOException::class)
        fun callAPiJson(
            mBuilder: JSONObject,
            url: String,
            activity: Activity,
            type: String,
            showProgress: Boolean,
            apiMethod: String,
            sendHeader: Boolean
        ) {
            if (showProgress) {
                Utility.show_progress(activity)
            }
            val example = CallApi()
            example.doPostRequestJson(url, mBuilder, activity, type, showProgress, apiMethod, sendHeader)
        }
    }

}