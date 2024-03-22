package com.planout.constant

import android.app.*
import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.planout.R
import com.planout.services.NotificationUtils
import java.io.File
import java.net.MalformedURLException
import java.net.URL


class DownloadManager(var activity: Activity, fileUrl: String) {
    private var downloadManager: DownloadManager
    private var refid: Long = 0
    private var Download_Uri: Uri
    private lateinit var fileName: String
    private lateinit var file_name: String
    var list: ArrayList<Long> = ArrayList<Long>()

    fun main(fileName: String) {
        list.clear()
        Log.d("fileName", fileName)
        this.fileName = fileName
        val request = DownloadManager.Request(Download_Uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(false)
        request.setTitle(activity.getString(R.string.app_name) + " Downloading File")
        request.setDescription("Downloading $fileName")
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "/PlanOut/$fileName.csv"
        )
        refid = downloadManager.enqueue(request)
        Log.e("OUT", "" + refid)
        list.add(refid)
    }

    var onComplete: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                ctxt: Context,
                intent: Intent
            ) {
                val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                Log.e("IN", "" + referenceId)
                list.remove(referenceId)
                val `when` = System.currentTimeMillis()

                val notificationManager =
                    ctxt.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancelAll()

                val pendingIntent = openFile(referenceId)

                if (list.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createNotificationChannel()
                        val notificationManager: NotificationManagerCompat =
                            NotificationManagerCompat.from(activity)
                        val mBuilder1: NotificationCompat.Builder =
                            NotificationCompat.Builder(activity, NotificationUtils.CHANNEL_ID)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("PlanOut")
                                .setContentText(activity.getString(R.string.download_complete_path) + Environment.DIRECTORY_DOWNLOADS + "/PlanOut/" + fileName)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setStyle(
                                    NotificationCompat.BigTextStyle()
                                        .bigText(activity.getString(R.string.download_complete_path) + Environment.DIRECTORY_DOWNLOADS + "/PlanOut/" + fileName)
                                )

                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                        mBuilder1.setChannelId(NotificationUtils.CHANNEL_ID)
                        if (pendingIntent != null)
                            mBuilder1.setContentIntent(pendingIntent)
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(`when`.toInt(), mBuilder1.build())
                    } else {
                        Log.e("INSIDE", "" + referenceId)
                        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
                            activity
                        )
                            .setContentTitle("PlanOut")
                            .setContentText(activity.getString(R.string.download_complete_path) + Environment.DIRECTORY_DOWNLOADS + "/PlanOut/" + fileName)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setStyle(
                                NotificationCompat.BigTextStyle()
                                    .bigText(activity.getString(R.string.download_complete_path)+ Environment.DIRECTORY_DOWNLOADS + "/PlanOut/" + fileName)
                            )
                            .setAutoCancel(true)
                        if (pendingIntent != null)
                            mBuilder.setContentIntent(pendingIntent)
                        val notificationManager =
                            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                        notificationManager!!.notify(`when`.toInt(), mBuilder.build())
                    }

                }
            }
        }


    val direct = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + "/PlanOut"
    )

    init {

        if (!direct.exists()) {
            direct.mkdirs()
        }

        downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        activity.registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
        Download_Uri = Uri.parse(fileUrl)
        file_name = getFileNameFromURL(fileUrl)!!
//        file_name = "MyPdf.pdf"
        main(getFileNameFromURL(fileUrl)!!)
    }

    fun openFile(referenceId: Long): PendingIntent? {
        try {
            Log.d("Download File Name ", file_name)
            val file = File(direct.path + "/" + file_name + ".csv")
            val install = Intent(Intent.ACTION_VIEW)
            val contentUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                FileProvider.getUriForFile(
                    activity,
                    activity.applicationContext.packageName + ".provider_paths",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            install.setDataAndType(contentUri, getMimeType(contentUri))
            return PendingIntent.getActivity(activity, referenceId.toInt(), install, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    fun getMimeType(uri: Uri): String? {
        var mimeType: String? = null
        mimeType = if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val cr: ContentResolver = activity.applicationContext.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri
                    .toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase()
            )
        }
        return mimeType
    }
    fun getFileNameFromURL(url: String?): String? {
        if (url == null) {
            return ""
        }
        try {
            val resource = URL(url)
            val host: String = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                // handle ...example.com
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')

        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // calculate the end index
        val endIndex = Math.min(lastQMPos, lastHashPos)
        return url.substring(startIndex, endIndex)
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "PlanOut"
            val description = "PlanOutBookNotification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(NotificationUtils.CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =
                activity.getSystemService(
                    NotificationManager::class.java
                )
            notificationManager.createNotificationChannel(channel)
        }
    }

}
