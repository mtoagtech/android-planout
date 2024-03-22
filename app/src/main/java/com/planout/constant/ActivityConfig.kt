package com.planout.constant

import android.content.Context
import com.planout.constant.ActivityConfig
import java.io.File

object ActivityConfig {
    fun getAppFolder(context: Context): String {
        return getAppFolderForDownloadList(context) + File.separator
    }

    fun getAppFolderForDownloader(context: Context): String {
        return getAppFolderForDownloadList(context)
    }

    fun getAppFolderForDownloadList(context: Context): String {
        return context.getExternalFilesDir("Downloads").toString() + ""
    }
}