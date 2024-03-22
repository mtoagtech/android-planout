package com.planout.models

import android.graphics.Bitmap
import java.io.File

/**
 * Created by Atul Papneja on 16-Jun-22.
 */
class StoreMediaData {
    lateinit var id:String
    lateinit var media_url:String
    var isUrl:Boolean = false
    var imageBitmap: Bitmap? = null
    var imageFile: File? = null
}