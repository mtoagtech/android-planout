package com.planout.activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.exifinterface.media.ExifInterface
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.CallFileApi
import com.planout.constant.Utility
import com.planout.constant.Utility.bitmapToFile
import com.planout.constant.Utility.showOrGone
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class EditProfileActivity : AppCompatActivity(), ApiResponse {
    val myCalendar: Calendar = Calendar.getInstance()
    private val GALLERYIMAGE = 124
    private val CAMERA_CODE = 125
    var profile_image_file: File? = null
    var bitmapSelect: Bitmap? = null
    var imagePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        txtHeader.text = getString(R.string.edit_profile)
        setData()
        clickView()

    }

    private fun setData() {
        editNameVisitor.setText(intent.getStringExtra(Utility.key.name))
        editEmail.text = intent.getStringExtra(Utility.key.email)
        if (intent.getStringExtra(Utility.key.dob)!="null"){
            editDob.text = Utility.formatdate(intent.getStringExtra(Utility.key.dob))
        }
        Utility.SetImageSimple(this,intent.getStringExtra(Utility.key.profile_image)!!,profile_image)
    }

    private fun clickView() {

        Utility.animationClick(btnDelete).setOnClickListener {
            //dialog for delete account
            openDeleteDialog("")
        }

        Utility.animationClick(imgBackHeader).setOnClickListener {
           onBackPressed()
        }

        Utility.animationClick(editDob).setOnClickListener {
            //dialog for select dob
            openDobDialog()
        }
        Utility.animationClick(imgEyeOn).setOnClickListener {
            editDob.performClick()
        }

        Utility.animationClick(profile_image).setOnClickListener {
            //dialog for select image | #gallery  #camera
            openCameraGalleryDialog(getString(R.string.choose_one), getString(R.string.select_one_for_profile))
        }
        Utility.animationClick(btnSave).setOnClickListener {
            //validation for update visitor profile
            if (editNameVisitor.text.toString().trim()==""){
                //Utility.customErrorToast(this,"Enter your name")
                editNameVisitor.requestFocus()
                Utility.showGoneErrorView(editNameVisitor, nameVisitorErr, true, getString(R.string.msg_name_valid_sub))
            }else if (editDob.text.toString().trim()==""){
                //Utility.customErrorToast(this,"Enter your date of birth")
                Utility.showGoneErrorView(linDOB, dobVisitorErr, true, getString(R.string.enter_dob))
            }else{
                //hide keyboard
                Utility.hideSoftKeyboard(this)
                //call api for update visitor profile
                updateProfileApi()
            }
        }

    }

    private fun updateProfileApi() {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        requestBody.addFormDataPart(Utility.key.name,editNameVisitor.text.toString())
        if (editDob.text.toString()!=""){
            requestBody.addFormDataPart(Utility.key.dob,Utility.change_date_format(editDob.text.toString(),Utility.api_date_format))
        }
        if (profile_image_file != null) {
            requestBody.addFormDataPart(
                Utility.key.profile_image,
                profile_image_file!!.name,
                profile_image_file!!.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        CallFileApi.callAPi(requestBody, ApiController.api.update_visitor, this, Utility.update_visitor, true, Utility.POST, true)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == GALLERYIMAGE) {
                    val selectedImageUri = data!!.data
                    imagePath = Utility.getPath(selectedImageUri, this)!!
                    bitmapSelect =
                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    try {
                        val exif = ExifInterface(imagePath)
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
                    }catch (e: Exception){e.printStackTrace()}
                    profile_image_file = bitmapToFile(bitmapSelect!!, this)
                    Utility.SetImageSimple(this,imagePath,profile_image)

                } else if (requestCode == CAMERA_CODE) {
                    try {
                        bitmapSelect = data!!.extras!!["data"] as Bitmap?
                        profile_image_file = bitmapToFile(bitmapSelect!!, this)
                        imagePath = profile_image_file!!.absolutePath
                        Utility.SetImageSimple(this,imagePath,profile_image)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Utility.customErrorToast(
                    this@EditProfileActivity,
                    getString(R.string.use_another_image)
                )
            }
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

    private fun openDobDialog() {
        if (editDob.text.toString() == "") {
            val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                myCalendar.set(Calendar.HOUR, 0)

                val myFormat = Utility.date_format //In which you need put here
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
                editDob.text = sdf.format(myCalendar.time)
            }

            val dialog_picker = DatePickerDialog(
                this@EditProfileActivity,
                R.style.DialogTheme,
                date,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH])

            val c = Calendar.getInstance();
            dialog_picker.datePicker.maxDate = c.timeInMillis

            dialog_picker.show()
        }else{
            val selectedDate=Utility.change_date_format(editDob.text.toString(),Utility.api_date_format)

            val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                myCalendar.set(Calendar.HOUR, 0)

                val myFormat = Utility.date_format //In which you need put here
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)
                editDob.text = sdf.format(myCalendar.time)
            }

            val dialog_picker = DatePickerDialog(
                this@EditProfileActivity,
                R.style.DialogTheme,
                date,
                selectedDate.split("-")[0].toInt(),
                selectedDate.split("-")[1].toInt()-1,
                selectedDate.split("-")[2].toInt()
            )
            val c = Calendar.getInstance();
            dialog_picker.datePicker.maxDate = c.timeInMillis
            dialog_picker.show()
        }

    }


    fun openCameraGalleryDialog(title: String, strMsg:String) {
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_delete_popup_view, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val dialogTitleTxt=dialogView.findViewById<TextView>(R.id.txtTitle)
        /*if (strMsg.isNotEmpty())
            dialogTitleTxt.text= strMsg*/
        dialogTitleTxt.showOrGone(false)

        val txtHead=dialogView.findViewById<TextView>(R.id.txtHead)
        if (title.isNotEmpty())
            txtHead.text= title
        val btnGallery=dialogView.findViewById<Button>(R.id.btnDelete)
        val btnCamera=dialogView.findViewById<Button>(R.id.btnCancel)
        btnCamera.text = getString(R.string.camera)
        btnGallery.text = getString(R.string.gallery)
        btnGallery.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_green)
        val imgTop=dialogView.findViewById<ImageView>(R.id.imgTop)
        Utility.animationClick(btnGallery).setOnClickListener {
            serviceInfo_dialog.dismiss()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (PermissionChecker.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED!!
                ) {
                    requestPermissions(
                        arrayOf<String>(Manifest.permission.READ_MEDIA_IMAGES),
                        1
                    )
                }
                else{
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, GALLERYIMAGE)
                }
            }
            else{
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

                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, GALLERYIMAGE)

                }
            }


        }
        Utility.animationClick(btnCamera).setOnClickListener {
            serviceInfo_dialog.dismiss()
            if (PermissionChecker.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED!!
            ) {
                requestPermissions(
                    arrayOf<String>(Manifest.permission.CAMERA),
                    3
                )
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                run {
                    startActivityForResult(
                        intent,
                        CAMERA_CODE
                    )
                }
            }
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }


    fun openDeleteDialog(strMsg:String) {
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_delete_account_popup_view, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val dialogTitleTxt=dialogView.findViewById<TextView>(R.id.txtTitle)
        if (strMsg.isNotEmpty())
            dialogTitleTxt.text= strMsg

        val btnYes=dialogView.findViewById<Button>(R.id.btnDelete)
        val btnNo=dialogView.findViewById<Button>(R.id.btnCancel)
        val imgTop=dialogView.findViewById<ImageView>(R.id.imgTop)
        Utility.animationClick(btnYes).setOnClickListener {
            //call api for delete account
            callDeleteAccountApi()
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(btnNo).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun callDeleteAccountApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.deleteaccount, this, Utility.deleteaccount, true, Utility.DELETE, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.update_visitor){
            if (isData){
                Utility.customSuccessToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
                onBackPressed()
            }else {
                Utility.customErrorToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }else if (type==Utility.deleteaccount){
            if (isData){
                Utility.clear_detail(this)
                Utility.set_login(this, false)
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                Utility.customSuccessToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }else {
                Utility.customErrorToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeVisitorActivity::class.java)
        intent.putExtra(Utility.key.isFrom, "editProfile")
        startActivity(intent)
        finishAffinity()
        Animatoo.animateSlideRight(this)
    }
}