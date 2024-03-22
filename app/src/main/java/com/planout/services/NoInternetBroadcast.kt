package com.planout.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.planout.activities.NoInternetActivity
import com.planout.constant.MyApplication
import com.planout.constant.Utility.hasConnection

/**
 * Created by Atul Papneja on 07-Jun-22.
 */

class NoInternetBroadcast : BroadcastReceiver() {
    var activity: AppCompatActivity? = null
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onReceive(context: Context, intent: Intent) {
        if (!MyApplication.isAppIsInBackground(context)) {
            if (hasConnection(context)) {
                /* Intent j = new Intent(context, Splash.class);
                j.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(j);*/
                /*Activity activity = (Activity) context;
                Intent j = new Intent();
                activity.setResult(3, j);*/
            } else {
                val j = Intent(context, NoInternetActivity::class.java)
                j.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(j)
            }
        }

    }
}