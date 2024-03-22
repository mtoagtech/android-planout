package com.planout.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.planout.MainActivity
import com.planout.activities.*
import com.planout.constant.Utility
import org.json.JSONObject
import java.util.*

/**
 * Created by Atul Papneja on 4/22/2022.
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = MyFirebaseMessagingService::class.java.simpleName

    override fun onNewToken(s: String) {
        Log.e("NEW_TOKEN", s)
        // Saving reg id to shared preferences
        storeRegIdInPref(s)

        // sending reg id to your server
        sendRegistrationToServer(s)
    }

    private fun sendRegistrationToServer(token: String) {
        Log.e("MyMessagingService", "sendRegistrationToServer: $token")
    }

    private fun storeRegIdInPref(token: String) {
        Log.d("fcm_id", token)
        val pref = applicationContext.getSharedPreferences("FIREBASE_TOKEN", 0)
        val editor = pref.edit()
        editor.putString("regId", token)
        editor.apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("MyMessagingService", "From: " + remoteMessage.from)

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {

            Log.e("MyMessagingService_body",  remoteMessage.notification!!.body!!)
            Log.e("MyMessagingService_titl",  remoteMessage.notification!!.title!!)

            handleNotification(remoteMessage.notification!!.body,remoteMessage.notification!!.title)
        }else{
            if (remoteMessage.data.isNotEmpty()) {
                Log.e(TAG, "Data Payload: " + remoteMessage.data.toString())
                try {
                    val jsonObj: JSONObject = JSONObject(remoteMessage.data["body"]!!.toString())
                    val title = remoteMessage.data["title"]!!.toString()
                    val message = remoteMessage.data["title"]!!.toString()
                    //val message = Utility.checkStringNullOrNot(jsonObj, "message")
                    val reservationId = Utility.checkStringNullOrNot(jsonObj, "reservation_id")
                    val eventId = Utility.checkStringNullOrNot(jsonObj, "event_id")
                    val storeId = Utility.checkStringNullOrNot(jsonObj, "store_id")
                    val userId = Utility.checkStringNullOrNot(jsonObj, "user_id")
                    val status = Utility.checkStringNullOrNot(jsonObj, "status")
                    val type = Utility.getForm(this, Utility.key.user_type)
                    var intent2 = Intent()
                    if (type == "201"){
                        intent2 = Intent(applicationContext, HomeVisitorActivity::class.java)
                        intent2.putExtra(Utility.key.isFrom, "notify")
                    }else if (type == "202"){
                        intent2 = Intent(applicationContext, NotificationActivity::class.java)
                    }else{
                        intent2 = Intent(applicationContext, AllNotificationActivity::class.java)
                    }
                    when(status){
                        "reservation.created" ->{
                            if (type == "202"){
                                intent2 = Intent(this, HomeCompanyActivity::class.java)
                                    .putExtra(Utility.key.isFrom, "reservation.created")
                            }
                        }"reservation.confirmed" ->{
                            if (type == "201"){
                                intent2 = Intent(this, ReserveTableStatusActivity::class.java)
                                    .putExtra(Utility.key.id, reservationId)
                                val intent = Intent("Reservation")
                                intent.putExtra("store_id", jsonObj.getString("store_id"))
                                intent.putExtra("res_id", jsonObj.getString("reservation_id"))
                                intent.putExtra("status", Utility.RES_CONFIRMED_STATUS)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                            }
                        }"reservation.declined" ->{
                            if (type == "201"){
                                intent2 = Intent(this, ReserveTableStatusActivity::class.java)
                                    .putExtra(Utility.key.id, reservationId)
                                val intent = Intent("Reservation")
                                intent.putExtra("store_id", jsonObj.getString("store_id"))
                                intent.putExtra("res_id", jsonObj.getString("reservation_id"))
                                intent.putExtra("status", Utility.RES_DECLINED_STATUS)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                            }
                        }"reservation.cancelled" ->{
                            // not gone away
                            if (type == "202"){
                                val intent = Intent("Reservation_Cancelled")
                                intent.putExtra("store_id", jsonObj.getString("store_id"))
                                intent.putExtra("res_id", jsonObj.getString("reservation_id"))
                                intent.putExtra("status", Utility.RES_DECLINED_STATUS)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                            }
                        }"event.created" ->{
                            if (type == "201"){
                                intent2 = Intent(this, BusinessDetailsActivity::class.java)
                                intent2.putExtra(Utility.key.id, storeId)
                            }
                        }
                    }
                    showNotificationMessage(
                        applicationContext,
                        title,
                        message,
                        (System.currentTimeMillis() / 1000).toString(),
                        intent2,
                    )

                } catch (e: Exception) {
                    Log.e(TAG, "Exception: " + e.message)
                }
            }
        }

        // Check if message contains a data payload.

    }

    private fun handleNotification(message: String?, title: String?) {
        // app is in foreground, broadcast the push message
        val intent = Intent(applicationContext, SplashScreen::class.java)
        showNotificationMessage(applicationContext, title!!, message!!, (System.currentTimeMillis()/1000).toString(), intent)
    }

    @SuppressLint("NewApi")
    private fun handleDataMessage(json: MutableMap<String, String>) {
        Log.e(TAG, "push json: $json")
        try {
            //val data = json.getValue("data")
            val title = json.getValue("title")
            val message = json.getValue("message")
            val status = json.getValue("status")

            // boolean isBackground = data.getBoolean("is_background");
//            String imageUrl = data.getString("image");
            val timestamp = json.getValue("timestamp")

            // JSONObject payload = data.getJSONObject("payload");
            Log.e(TAG, "title: $title")
            Log.e(TAG, "status: $status")
            Log.e(TAG, "message: $message")
            val intent = Intent(applicationContext, MainActivity::class.java)
            showNotificationMessage(applicationContext, title, message, timestamp, intent)
        //            val intent = Intent(applicationContext, FirstScreen::class.java)
//            showNotificationMessage(applicationContext, title, message, timestamp, intent)

//            normal_order, logout, gift_sent, subscription_order, wallet_amount, send_admin, wallet_warning
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val NOTIFICATION_REPLY = "NotificationReply"

    val KEY_INTENT_MORE = "keyintentmore"
    val KEY_INTENT_HELP = "keyintenthelp"

    val REQUEST_CODE_MORE = 100
    val REQUEST_CODE_HELP = 101
    val NOTIFICATION_ID = 200
//    private fun createInlineNotification(
//        mContext: Context,
//        title: String,
//        message: String,
//        timestamp: String,
//        intent: Intent
//    ) {
//        val icon: Int = R.drawable.notification_icon
//        val CHANNEL_ID = "FlyingFreelyTravelerChannel"
//        val alarmSound =
//            Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.notification_tone);
//
//        //Pending intent for a notification button named More
//        val morePendingIntent = PendingIntent.getBroadcast(
//            mContext,
//            REQUEST_CODE_MORE,
//            Intent(mContext, NotificationReceiver::class.java)
//                .putExtra(KEY_INTENT_MORE, REQUEST_CODE_MORE),
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        //Pending intent for a notification button help
//        val helpPendingIntent = PendingIntent.getBroadcast(
//            mContext,
//            REQUEST_CODE_HELP,
//            Intent(mContext, NotificationReceiver::class.java)
//                .putExtra(KEY_INTENT_HELP, REQUEST_CODE_HELP),
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )!!
//
//
//        //We need this object for getting direct input from notification
//        val remoteInput = RemoteInput.Builder(NOTIFICATION_REPLY)
//            .setLabel("Please enter your reply")
//            .build()
//
//        //For the remote input we need this action object
//        val action = NotificationCompat.Action.Builder(
//            android.R.drawable.ic_delete,
//            "Reply Now...", helpPendingIntent
//        )
//            .addRemoteInput(remoteInput)
//            .build()
//
//        //Creating the notifiction builder object
//        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setAutoCancel(true)
//            .setSound(alarmSound)
//            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE)
//            .setSmallIcon(R.drawable.notification_icon)
//            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
//            .setContentIntent(helpPendingIntent)
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .addAction(action)
//            .setLargeIcon(BitmapFactory.decodeResource(application.resources, icon))
//        mBuilder.setChannelId(CHANNEL_ID)
//
//
//        //finally displaying the notification
//        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
//    }

    private fun other_notification(
        title: String?,
        message: String?
    ) {
        val notificationUtils = NotificationUtils(applicationContext)
        notificationUtils.playNotificationSound()
    }

    //     Showing notification with text and image
    private fun showNotificationMessageWithImage(
        context: Context,
        title: String,
        message: String,
        timeStamp: String,
        intent: Intent,
        image: String
    ) {
        val notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils.big_notification(title, message, timeStamp, intent, image)
    }

    private var replyPendingIntent: PendingIntent? = null
    private var mNotificationId = 1234567890
    private var mMessageId = 0
    private val MAX_MESSAGE_ID = 100
    private val MAX_NOTIFICATION_ID = 10000
    var REPLY_ACTION = "com.flyingfreely.traveler"
    private val KEY_NOTIFICATION_ID = "key_noticiation_id"
    private val KEY_MESSAGE_ID = "key_message_id"

    fun getReplyMessageIntent(context: Context?, notificationId: Int, messageId: Int): Intent? {
        val intent = Intent(context, MyFirebaseMessagingService::class.java)
        intent.action = REPLY_ACTION
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId)
        intent.putExtra(KEY_MESSAGE_ID, messageId)
        return intent
    }


    private fun buildIntentToNotification(): PendingIntent? {
        val randomGenerator = Random()
        mMessageId = randomGenerator.nextInt(MAX_MESSAGE_ID)
        mNotificationId = randomGenerator.nextInt(MAX_NOTIFICATION_ID)
        val intent: Intent
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // start a
            // (i)  broadcast receiver which runs on the UI thread or
            // (ii) service for a background task to b executed , but for the purpose of this codelab, will be doing a broadcast receiver
            intent = getReplyMessageIntent(this, mNotificationId, mMessageId)!!
            PendingIntent.getBroadcast(
                applicationContext, 100, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            // start your activity
            intent = getReplyMessageIntent(this, mNotificationId, mMessageId)!!
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            PendingIntent.getActivity(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    private val KEY_TEXT_REPLY = "key_text_reply"

//    private fun buildInlineReplyNotification(title: String, message: String, applicationContext: Context) {
//        // Create an instance of RemoteInput.Builder that you can add to your notification action.
//        // This class's constructor accepts a string that the system uses as the key for the text input.
//        // Later, your handheld app uses that key to retrieve the text of the input.
//        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).setLabel(
//                resources.getString(R.string.reply_label)).build()
//
//        // Attach the RemoteInput object to an action using addRemoteInput().
//        val compatAction: NotificationCompat.Action = NotificationCompat.Action.Builder(R.mipmap.ic_reply,
//                resources.getString(R.string.reply), replyPendingIntent).addRemoteInput(
//                remoteInput).setAllowGeneratedReplies(true).build()
//        val alarmSound = Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.notification_tone);
//
//        // Build the notification and add the action.
//        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this).setSmallIcon(R.drawable.notification_icon)
//                .setContentTitle(title)
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setSound(alarmSound)
//                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setShowWhen(true)
//                .addAction(compatAction)
//        mBuilder.setChannelId("NativMilkChannel")
//
//        // Issue the notification.
//        val `when` = System.currentTimeMillis()
//        val notificationManager2 =
//                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager2.notify(`when`.toInt(), mBuilder.build())
//    }


    //  Showing notification with text only
    private fun showNotificationMessage(
        context: Context,
        title: String,
        message: String,
        timeStamp: String,
        intent: Intent
    ) {
        val notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent)
    }

    private fun clearnotification() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}