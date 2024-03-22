package com.planout.constant

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.StrictMode
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.planout.services.NoInternetBroadcast

/**
 * Created by Atul Papneja on 12-May-22.
 */
class MyApplication: Application() {
    var internetAlert = NoInternetBroadcast()
    private var context: Context? = null

    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext);

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()

        PRDownloader.initialize(applicationContext)
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(this, config)

//        AppEventsLogger.activateApp(this)
        val filter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(internetAlert, filter)
        context = applicationContext
        setupActivityListener()
    }

    private fun setupActivityListener() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(internetAlert)
    }
    companion object {
        //    static AppEnvironment appEnvironment;
        var isActivityVisible = false
            private set

        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
            return isInBackground
        }

        fun activityResumed() {
            isActivityVisible = true
        }

        fun activityPaused() {
            isActivityVisible = false
        }
    }

}