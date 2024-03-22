package com.planout.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.core.widget.doOnTextChanged
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.GalleryPhotosViewAdapter
import com.planout.api_calling.ApiController
import com.planout.api_calling.CallFileApi
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.StoreMediaData
import com.zfdang.multiple_images_selector.ImagesSelectorActivity
import com.zfdang.multiple_images_selector.SelectorSettings
import kotlinx.android.synthetic.main.activity_photo_gallery.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File


class PhotoGalleryActivity : AppCompatActivity(), ApiResponse {

    private val GALLERYIMAGE = 124
    private var profile_image_file: File? = null
    var bitmapSelect: Bitmap? = null
    lateinit var layoutManager1: GridLayoutManager
    lateinit var photoAdapter: GalleryPhotosViewAdapter
    private val storeMediaItems: ArrayList<StoreMediaData> = ArrayList()
    private val storeFilesList: ArrayList<File> = ArrayList()
    var removedPosition = 0
    var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)
        val editMessage = findViewById<EditText>(R.id.editMessage)


        Fresco.initialize(applicationContext)
        txtHeader.text = getString(R.string.photo_gallery)

        Utility.animationClick(imgBackHeader).setOnClickListener {
            onBackPressed()
        }

        if (Utility.getForm(this, Utility.key.is_owner) == "1") {

            Utility.animationClick(txtSelectDoc).setOnClickListener {

                if (storeMediaItems.size >= 10) {
                    Utility.customErrorToast(this, "Max. images added")
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

                        val intent = Intent(this, ImagesSelectorActivity::class.java)
                        intent.putExtra(
                            SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER,
                            10 - storeMediaItems.size
                        )
                        intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, false)
                        startActivityForResult(intent, GALLERYIMAGE)
                    } else {
                        if (PermissionChecker.checkSelfPermission(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED!!
                        ) {
                            requestPermissions(
                                arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                                1
                            )
                        } else if (PermissionChecker.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED!!
                        ) {
                            requestPermissions(
                                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                2
                            )
                        } else {
                            val intent = Intent(this, ImagesSelectorActivity::class.java)
                            intent.putExtra(
                                SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER,
                                10 - storeMediaItems.size
                            )
                            intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, false)
                            startActivityForResult(intent, GALLERYIMAGE)
                        }
                    }
                }
            }
        } else {
            txtSelectDoc.isEnabled = false
        }


        Utility.animationClick(btnSave).setOnClickListener {
            storeFilesList.clear()
            for (i in 0 until storeMediaItems.size) {
                val item = storeMediaItems[i]
                if (!item.isUrl) {
                    storeFilesList.add(item.imageFile!!)
                }
            }
            if (storeFilesList.size > 0) {
                //call api for save photos
                saveDataApi()
            }
        }
        //call api to get all gallery photos
        listApi()
    }

    private fun saveDataApi() {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        requestBody.addFormDataPart(
            Utility.key.store_id,
            intent.getStringExtra(Utility.key.store_id)!!
        )
        for (i in 0 until storeFilesList.size) {
            requestBody.addFormDataPart(
                Utility.key.filename,
                storeFilesList[i].name,
                storeFilesList[i].asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }
        CallFileApi.callAPi(
            requestBody,
            ApiController.api.store_media,
            this,
            Utility.store_media_add,
            true,
            Utility.POST,
            true
        )

    }

    private fun listApi() {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.store_media + "?${Utility.key.store_id}=${
            intent.getStringExtra(Utility.key.store_id)!!
        }"
        CallApi.callAPi(mBuilder, API, this, Utility.store_media, true, Utility.GET, true)
    }


    fun showDeletePopUp(
        title: String,
        subTitle: String,
        isUrl: Boolean,
        id: String,
        position: Int
    ) {
        removedPosition = position
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.setOnClickListener {
            if (isUrl) {
                //call api for delete a photo at a time
                deleteMediaApi(id)
            } else {
                //currently selected photo delete
                storeMediaItems.removeAt(position)
                photoAdapter.notifyItemRemoved(position)
                photoAdapter.notifyDataSetChanged()
                //visible or hide save button
                if (storeMediaItems.size == 0) {
                    btnSave.showOrGone(false)
                } else {
                    btnSave.showOrGone(true)
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteMediaApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            ApiController.api.store_media + "/" + id,
            this,
            Utility.store_media_delete,
            true,
            Utility.DELETE,
            true
        )
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.store_media) {
            storeMediaItems.clear()
            if (isData) {
                val data = result.getJSONArray(Utility.key.data)
                for (i in 0 until data.length()) {
                    val dataObj = data.getJSONObject(i)
                    val item = StoreMediaData()
                    item.id = dataObj.getString(Utility.key.id)
                    item.media_url = dataObj.getString(Utility.key.media_url)
                    item.isUrl = true
                    item.imageBitmap = null
                    storeMediaItems.add(item)
                }
//                recyclerPhotos.setHasFixedSize(true)
                layoutManager1 = GridLayoutManager(this, 3)
                photoAdapter = GalleryPhotosViewAdapter(this, storeMediaItems, btnSave)
                recyclerPhotos.layoutManager = layoutManager1
                recyclerPhotos.adapter = photoAdapter
                //visible or hide save button
                if (storeMediaItems.size == 0) {
                    btnSave.showOrGone(false)
                } else {
                    btnSave.showOrGone(true)
                }
            } else {
                Utility.customErrorToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
                //hide save button
                btnSave.showOrGone(false)
            }
        } else if (type == Utility.store_media_delete) {
            if (isData) {
                storeMediaItems.removeAt(removedPosition)
                photoAdapter.notifyItemRemoved(removedPosition)
                photoAdapter.notifyDataSetChanged()
                Utility.customSuccessToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            } else {
                Utility.customErrorToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
            //visible or hide save button
            if (storeMediaItems.size == 0) {
                btnSave.showOrGone(false)
            } else {
                btnSave.showOrGone(true)
            }
        } else if (type == Utility.store_media_add) {
            isEdit = true
            storeFilesList.clear()
            if (isData) {
                //listApi()
                Utility.customSuccessToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
                onBackPressed()
            } else {
                Utility.customErrorToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == GALLERYIMAGE) {
                    val image_uris: ArrayList<String> =
                        data!!.getStringArrayListExtra("selector_results")!!
                    Utility.show_progress(this)
                    Handler().postDelayed(Runnable { viewImages(image_uris) }, 200)

                    /*val selectedImageUri = data!!.data
                    imagePath = Utility.getPath(selectedImageUri, this)!!
                    bitmapSelect =
                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    //val out = ByteArrayOutputStream()
                    //bitmapSelect!!.compress(Bitmap.CompressFormat.PNG, 50, out)
                    //val decoded = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
                    try {
                        if (bitmapSelect!!.width > bitmapSelect!!.height && abs(bitmapSelect!!.width-bitmapSelect!!.height) >950) {
                            val matrix = Matrix()
                            matrix.postRotate(90f)
                            bitmapSelect = Bitmap.createBitmap(
                                bitmapSelect!!,
                                0,
                                0,
                                bitmapSelect!!.width,
                                bitmapSelect!!.height,
                                matrix,
                                true
                            )
                        }else if(bitmapSelect!!.width > bitmapSelect!!.height){
                            val matrix = Matrix()
                            matrix.postRotate(-90f)
                            bitmapSelect = Bitmap.createBitmap(
                                bitmapSelect!!,
                                0,
                                0,
                                bitmapSelect!!.width,
                                bitmapSelect!!.height,
                                matrix,
                                true
                            )
                        }
                    }catch (e: Exception){e.printStackTrace()}
                    profile_image_file = Utility.bitmapToFile(bitmapSelect!!, this)
                    val item=StoreMediaData()
                    item.id=""
                    item.media_url=""
                    item.isUrl=false
                    item.imageBitmap=bitmapSelect
                    item.imageFile=profile_image_file
                    storeMediaItems.add(item)
                    layoutManager1 = GridLayoutManager(this, 3)
                    photoAdapter = GalleryPhotosViewAdapter(this,storeMediaItems)
                    recyclerPhotos.layoutManager = layoutManager1
                    recyclerPhotos.adapter = photoAdapter*/

                }
            } catch (e: Exception) {
                e.printStackTrace()
                Utility.customErrorToast(
                    this@PhotoGalleryActivity,
                    getString(R.string.use_another_image)
                )
            }
        }
    }

    private fun viewImages(imageUris: java.util.ArrayList<String>) {
        for (i in 0 until imageUris.size) {
            val image: File = File(imageUris[i])
            val bmOptions = BitmapFactory.Options()
            bitmapSelect = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
            /*val out = ByteArrayOutputStream()
            bitmapSelect!!.compress(Bitmap.CompressFormat.PNG, 10, out)
            bitmapSelect = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))*/
            try {
                val exif = ExifInterface(image.path)
                val rotation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val matrix = Matrix()
                matrix.postRotate(exifToDegrees(rotation).toFloat())
                bitmapSelect = Bitmap.createBitmap(
                    bitmapSelect!!,
                    0,
                    0,
                    bitmapSelect!!.width,
                    bitmapSelect!!.height,
                    matrix,
                    true
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            profile_image_file = Utility.bitmapToFile(bitmapSelect!!, this)
            val item = StoreMediaData()
            item.id = ""
            item.media_url = ""
            item.isUrl = false
            item.imageBitmap = bitmapSelect
            item.imageFile = profile_image_file
            storeMediaItems.add(item)
            if (i == imageUris.size - 1) {
                Utility.hide_progress(this)
            }
        }
        layoutManager1 = GridLayoutManager(this, 3)
        photoAdapter = GalleryPhotosViewAdapter(this, storeMediaItems, btnSave)
        recyclerPhotos.layoutManager = layoutManager1
        recyclerPhotos.adapter = photoAdapter
        if (storeMediaItems.size == 0) {
            btnSave.showOrGone(false)
        } else {
            btnSave.showOrGone(true)
        }
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    override fun onBackPressed() {
        if (intent.hasExtra(Utility.key.isFrom)) {
            val intent = Intent()
            intent.putExtra("Result", isEdit.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            super.onBackPressed()

        }
    }
}