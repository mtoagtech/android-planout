package com.planout.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.util.Patterns
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.planout.R
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Atul Papneja on 18-Apr-22.
 */
class NotificationUtils(private var mContext: Context) {
    private val TAG = NotificationUtils::class.java.simpleName
    companion object{
        val CHANNEL_ID = "PlanOutChannel"

    }
    var soundUri = Uri.parse(
            "android.resource://" +
                    mContext.packageName +
                    "/" +
                    R.raw.notification_tone)

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses =
                    am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo =
                    am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }

    fun clearNotifications(context: Context) {
        val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun getTimeMilliSec(timeStamp: String?): Long {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        try {
            val date = format.parse(timeStamp!!)
            return date!!.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    fun showNotificationMessage(
            title: String?,
            message: String?,
            timeStamp: String?,
            intent: Intent?
    ) {
        showNotificationMessage(title, message, timeStamp, intent, null)
    }

    fun big_notification(
            title: String?,
            message: String?,
            timeStamp: String?,
            intent: Intent,
            imageUrl: String?
    ) {
        // notification icon
        val icon: Int = R.drawable.notification_icon
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("pos", "0")
        val resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mBuilder =
                NotificationCompat.Builder(
                        mContext
                )
        val alarmSound = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.notification_tone);
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl != null && imageUrl.length > 4 && Patterns.WEB_URL.matcher(
                            imageUrl
                    ).matches()
            ) {
                val bitmap: Bitmap? = getBitmapFromURL(imageUrl)
                bitmap?.let {
                    showBigNotification(
                            it,
                            mBuilder,
                            icon,
                            title,
                            message,
                            timeStamp,
                            resultPendingIntent,
                            alarmSound
                    )
                }
                        ?: showSmallNotification(
                                mBuilder,
                                icon,
                                title,
                                message,
                                timeStamp,
                                resultPendingIntent,
                                alarmSound
                        )
            }
        } else {
            showSmallNotification(
                    mBuilder,
                    icon,
                    title,
                    message,
                    timeStamp,
                    resultPendingIntent,
                    alarmSound
            )
        }
    }

    private fun showNotificationMessage(
            title: String?,
            message: String?,
            timeStamp: String?,
            intent: Intent?,
            imageUrl: String?
    ) {
        // Check for empty push message
        if (TextUtils.isEmpty(message)) return
        // notification icon
        val icon: Int = R.drawable.notification_icon
        //        final int large_icon = R.drawable.notification_icon;
        intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("pos", "0")
        val resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mBuilder =
                NotificationCompat.Builder(
                        mContext
                )
        val alarmSound =
                Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.notification_tone)
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl != null && imageUrl.length > 4 && Patterns.WEB_URL.matcher(
                            imageUrl
                    ).matches()
            ) {
                val bitmap: Bitmap? = getBitmapFromURL(imageUrl)
                bitmap?.let {
                    showBigNotification(
                            it,
                            mBuilder,
                            icon,
                            title,
                            message,
                            timeStamp,
                            resultPendingIntent,
                            alarmSound
                    )
                }
                        ?: showSmallNotification(
                                mBuilder,
                                icon,
                                title,
                                message,
                                timeStamp,
                                resultPendingIntent,
                                alarmSound
                        )
            }
        } else {
            showSmallNotification(
                    mBuilder,
                    icon,
                    title,
                    message,
                    timeStamp,
                    resultPendingIntent,
                    alarmSound
            )

            mBuilder.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.notification_tone))
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
//            playNotificationSound()
        }
    }

    private fun showSmallNotification(
            mBuilder: NotificationCompat.Builder,
            icon: Int,
            title: String?,
            message: String?,
            timeStamp: String?,
            resultPendingIntent: PendingIntent,
            alarmSound: Uri
    ) {
        val `when` = System.currentTimeMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            val notificationManager =
                    NotificationManagerCompat.from(mContext)
            val mBuilder1 =
                    NotificationCompat.Builder(mContext, CHANNEL_ID)
                            .setContentIntent(resultPendingIntent)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setSound(alarmSound)
                            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setLargeIcon(
                                    BitmapFactory.decodeResource(
                                            mContext.resources,
                                            icon
                                    )
                            )
            mBuilder1.setChannelId(CHANNEL_ID)

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(`when`.toInt(), mBuilder1.build())
        } else {
            val inboxStyle =
                    NotificationCompat.InboxStyle()
            inboxStyle.addLine(message)
            val notification: Notification
            notification = mBuilder.setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setWhen(getTimeMilliSec(timeStamp))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(
                                    mContext.resources,
                                    icon
                            )
                    )
                    .setContentText(message)
                    .build()
            val notificationManager =
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(`when`.toInt(), notification)
        }
    }

    private fun showBigNotification(
            bitmap: Bitmap,
            mBuilder: NotificationCompat.Builder,
            icon: Int,
            title: String?,
            message: String?,
            timeStamp: String?,
            resultPendingIntent: PendingIntent,
            alarmSound: Uri
    ) {
        val `when` = System.currentTimeMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            val notificationManager =
                    NotificationCompat.BigPictureStyle()
            notificationManager.setBigContentTitle(title)
            notificationManager.setSummaryText(Html.fromHtml(message).toString())
            notificationManager.bigPicture(bitmap)
            val mBuilder1 =
                    NotificationCompat.Builder(mContext, CHANNEL_ID)
                            .setContentIntent(resultPendingIntent)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setSound(alarmSound)
                            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setStyle(notificationManager)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setLargeIcon(
                                    BitmapFactory.decodeResource(
                                            mContext.resources,
                                            icon
                                    )
                            )
                            .setLargeIcon(
                                    BitmapFactory.decodeResource(
                                            mContext.resources,
                                            icon
                                    )
                            )
            mBuilder1.setChannelId(CHANNEL_ID)

            // notificationId is a unique int for each notification that you must define

//            notificationManager.notify((int) when, mBuilder1.build());
            val notificationManager2 =
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager2.notify(`when`.toInt(), mBuilder1.build())
        } else {
            val bigPictureStyle =
                    NotificationCompat.BigPictureStyle()
            bigPictureStyle.setBigContentTitle(title)
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString())
            bigPictureStyle.bigPicture(bitmap)
            val notification: Notification
            notification = mBuilder.setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentIntent(resultPendingIntent)
                    .setSound(alarmSound)
                    .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
                    .setStyle(bigPictureStyle)
                    .setWhen(getTimeMilliSec(timeStamp))
                    .setSmallIcon(R.drawable.notification_icon)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(
                                    mContext.resources,
                                    icon
                            )
                    )
                    .setContentText(message)
                    .build()
            val notificationManager =
                    mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(`when`.toInt(), notification)
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()

            val name: CharSequence = "FlyingFreelyTraveler"
            val description = "FlyingFreelyTravelerNotification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                    NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            channel.setSound(soundUri, audioAttributes);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            val notificationManager = mContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getBitmapFromURL(strURL: String?): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection =
                    url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse("android.resource://" + mContext.packageName + "/" + R.raw.notification_tone)
            val r = RingtoneManager.getRingtone(mContext, alarmSound)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}