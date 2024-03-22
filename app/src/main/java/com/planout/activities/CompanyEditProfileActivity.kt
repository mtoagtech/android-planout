package com.planout.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.widget.doOnTextChanged
import androidx.exifinterface.media.ExifInterface
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.CallFileApi
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.IndustryModel
import kotlinx.android.synthetic.main.activity_company_edit_profile.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File


class CompanyEditProfileActivity : AppCompatActivity(), ApiResponse {
    private var arrIndustry: ArrayList<IndustryModel> = ArrayList()
    private var arrTags: ArrayList<IndustryModel> = ArrayList()
    private var selectedIndArray: ArrayList<String> = ArrayList()
    private var selectedTagArray: ArrayList<String> = ArrayList()

    private val GALLERYIMAGE = 124
    var profile_image_file: File? = null
    var bitmapSelect: Bitmap? = null
    var imagePath = ""

    private val GALLERYIMAGE_Store = 125
    var _Store_image_file: File? = null
    var _StorebitmapSelect: Bitmap? = null
    var _StoreimagePath = ""

    var isEdit=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_edit_profile)

        txtHeader.text = getString(R.string.edit_company_profile)
        clickView()

        val storeObj=JSONObject(intent.getStringExtra(Utility.key.storeData)!!)
        Log.d("storeObj",storeObj.toString())
        val id=storeObj.getString(Utility.key.id)
        val store_name=storeObj.getString(Utility.key.store_name)
        val store_image=storeObj.getString(Utility.key.store_image)
        val cover_image=storeObj.getString(Utility.key.cover_image)
        val is_open=storeObj.getString(Utility.key.is_open)
        val starttime=storeObj.getString(Utility.key.starttime)
        val endtime=storeObj.getString(Utility.key.endtime)
        val starttime1=storeObj.getString(Utility.key.starttime1)
        val endtime1=storeObj.getString(Utility.key.endtime1)
        val email=storeObj.getString(Utility.key.email)
        val name=storeObj.getString(Utility.key.name)
        val mobile=storeObj.getString(Utility.key.mobile)
        val telephone=storeObj.getString(Utility.key.telephone)
        val fax=storeObj.getString(Utility.key.fax)
        val extra_notes=storeObj.getString(Utility.key.extra_notes)

        Utility.SetImageSimple(this,store_image,profile_image_click)
        Utility.SetImageSimple(this,cover_image,imgCoverPhoto)

        editCompName.setText(Utility.checkStringNullOrEmpty(store_name))
        editName.setText(Utility.checkStringNullOrEmpty(name))
        editEmail.setText(Utility.checkStringNullOrEmpty(email))
        editTelephone.setText(Utility.checkStringNullOrEmpty(telephone))
        editMobile.setText(Utility.checkStringNullOrEmpty(mobile))
        editFax.setText(Utility.checkStringNullOrEmpty(fax))
        editMessage.setText(Utility.checkStringNullOrEmpty(extra_notes))
        editMessage.doOnTextChanged { text, start, before, count ->
            txtChar.text = "${text.toString().length}/300"
        }

        val industriesArray=storeObj.getJSONArray(Utility.key.industries)
        for (i in 0 until industriesArray.length()){
            val indObj=industriesArray.getJSONObject(i)
            val ind=indObj.getString(Utility.key.industry_name)
            selectedIndArray.add(ind)
        }

        val tagsArray=storeObj.getJSONArray(Utility.key.tags)
        for (i in 0 until tagsArray.length()){
            val tagObj=tagsArray.getJSONObject(i)
            val ind=tagObj.getString(Utility.key.tag_name)
            selectedTagArray.add(ind)
        }

        //call api for industry listing
        callIndustryApi()

    }
    private fun callIndustryApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.industries, this, Utility.industries, true, Utility.GET, false)
    }

    private fun callTagListApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.tags, this, Utility.tags, true, Utility.GET, true)
    }


    private fun clickView() {
        editCompName.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editCompName, compNameErr, false, "")
        }
        editName.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editName, nameErr, false, "")
        }
        editTelephone.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editTelephone, telephoneErr, false, "")
        }
        editMobile.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editMobile, mobileErr, false, "")
        }
        editFax.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editFax, faxErr, false, "")
        }
        Utility.animationClick(btnDelete).setOnClickListener { openDeleteDialog("") } //dialog for delete account
        Utility.animationClick(imgBackHeader).setOnClickListener { onBackPressed() }
        Utility.animationClick(btnSave).setOnClickListener {
            updateCompanyDetailApi() //call api for update company details
        }
        Utility.animationClick(profile_image_click).setOnClickListener {
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
                }  else {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, GALLERYIMAGE)
                }

            }else{
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
        Utility.animationClick(linImgClickCover).setOnClickListener {
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
                }  else {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, GALLERYIMAGE_Store)
                }
            }else{
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
                    startActivityForResult(intent, GALLERYIMAGE_Store)
                }

            }
        }


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
                    profile_image_file = Utility.bitmapToFile(bitmapSelect!!, this)
                    Utility.SetImageSimple(this,imagePath,profile_image_click)

                }else if (requestCode == GALLERYIMAGE_Store) {
                    val selectedImageUri = data!!.data
                    _StoreimagePath = Utility.getPath(selectedImageUri, this)!!
                    _StorebitmapSelect =
                        MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    try {
                        val exif = ExifInterface(_StoreimagePath)
                        val rotation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        val matrix = Matrix()
                        matrix.postRotate(exifToDegrees(rotation).toFloat())
                        _StorebitmapSelect = Bitmap.createBitmap(
                            _StorebitmapSelect!!,
                            0,
                            0,
                            _StorebitmapSelect!!.width,
                            _StorebitmapSelect!!.height,
                            matrix,
                            true
                        )
                    }catch (e: Exception){e.printStackTrace()}
                    _Store_image_file = Utility.bitmapToFile(_StorebitmapSelect!!, this)
                    Utility.SetImageSimple(this,_StoreimagePath,imgCoverPhoto)

                }

            } catch (e: Exception) {
                e.printStackTrace()
                Utility.customErrorToast(
                    this@CompanyEditProfileActivity,
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

    private fun updateCompanyDetailApi() {
        //validation for update company details
        if (TextUtils.isEmpty(editCompName.text.toString().trim())){
            editCompName.requestFocus()
            //Utility.customErrorToast(this, Utility.msg_company_valid)
            Utility.showGoneErrorView(editCompName, compNameErr, true, getString(R.string.msg_company_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
        }else if (TextUtils.isEmpty(editName.text.toString().trim())){
            editName.requestFocus()
            Utility.showGoneErrorView(editName, nameErr, true, getString(R.string.msg_name_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
            //Utility.customErrorToast(this, Utility.msg_name_valid)
        }else if (TextUtils.isEmpty(editTelephone.text.toString().trim())){
            editTelephone.requestFocus()
            Utility.showGoneErrorView(editTelephone, telephoneErr, true, getString(R.string.msg_telephone_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
            //Utility.customErrorToast(this, Utility.msg_telephone_valid)
        }else if (editTelephone.text.toString().trim().length<8){
            editTelephone.requestFocus()
            Utility.showGoneErrorView(editTelephone, telephoneErr, true, getString(R.string.telephone_mus_be_8_char))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
            //Utility.customErrorToast(this, getString(R.string.telephone_mus_be_8_char))
        }else if (TextUtils.isEmpty(editMobile.text.toString().trim())){
            editMobile.requestFocus()
            Utility.showGoneErrorView(editMobile, mobileErr, true, getString(R.string.msg_mobile_valid))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
            //Utility.customErrorToast(this, Utility.msg_mobile_valid)
        }else if (editMobile.text.toString().trim().length<8) {
            editMobile.requestFocus()
            Utility.showGoneErrorView(editMobile, mobileErr, true, getString(R.string.mobile_must_be_8_char))
            nestedView.fullScroll(ScrollView.FOCUS_UP)
            //Utility.customErrorToast(this, getString(R.string.mobile_must_be_8_char))
        }
//        else if (TextUtils.isEmpty(editFax.text.toString().trim())){
//            editFax.requestFocus()
//            Utility.showGoneErrorView(editFax, faxErr, true, Utility.msg_fax)
//            nestedView.fullScroll(ScrollView.FOCUS_UP)
//            //Utility.customErrorToast(this, Utility.msg_fax)
//        }
//        else if (editFax.text.toString().trim().length<8) {
//            editFax.requestFocus()
//            Utility.showGoneErrorView(editFax, faxErr, true, Utility.msg_fax_valid)
//            nestedView.fullScroll(ScrollView.FOCUS_UP)
//            //Utility.customErrorToast(this, Utility.msg_fax_valid)
//        }
        else if (getSelectedIndustries().size<=0){
            industryErr.showOrGone(true)
            industryErr.text = getString(R.string.msg_industry)
            nestedView.smoothScrollTo(0, industryErr.bottom)
            //Utility.customErrorToast(this, Utility.msg_industry)
        }else if (getSelectedTags().size<=0){
            tagsErr.showOrGone(true)
            tagsErr.text = getString(R.string.msg_tags)
            nestedView.smoothScrollTo(0, tagsErr.bottom)
            //Utility.customErrorToast(this, Utility.msg_tags)
        }else{
            Utility.hideSoftKeyboard(this)
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            requestBody.addFormDataPart(Utility.key.name,editName.text.toString().trim())
            requestBody.addFormDataPart(Utility.key.mobile,editMobile.text.toString().trim())
            requestBody.addFormDataPart(Utility.key.telephone,editTelephone.text.toString().trim())
            requestBody.addFormDataPart(Utility.key.fax,editFax.text.toString().trim())
            requestBody.addFormDataPart(Utility.key.extra_notes,editMessage.text.toString().trim())
            requestBody.addFormDataPart(Utility.key.store_name,editCompName.text.toString().trim())
            requestBody.addFormDataPart(Utility.key.industries,getSelectedIndustries().toString().replace("[","").replace("]",""))
            requestBody.addFormDataPart(Utility.key.tags,getSelectedTags().toString().replace("[","").replace("]",""))

            if (profile_image_file != null) {
                requestBody.addFormDataPart(
                    Utility.key.store_image,
                    profile_image_file!!.name,
                    profile_image_file!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
            }

            if (_Store_image_file != null) {
                requestBody.addFormDataPart(
                    Utility.key.cover_image,
                    _Store_image_file!!.name,
                    _Store_image_file!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
            }

            CallFileApi.callAPi(requestBody, ApiController.api.update_store, this, Utility.update_store, true, Utility.POST, true)

        }
    }


    private fun addChipToGroupIndustries(str: String, chipGrp: ChipGroup) {
        val chip = Chip(this)

        chip.text = str
        chip.setTextAppearance(R.style.chipText)
        chip.setTextAppearance(R.style.chipText_bold)
        chip.setTextColor(ContextCompat.getColor(this, R.color.gray_5B))
        chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8))
        chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.gray_E9_FF))
        chip.chipStrokeWidth = 2f
        chip.chipCornerRadius = 10f
        chip.setPadding(50, 50, 50, 50)
        chipGrp.chipSpacingVertical = 5
        chipGrp.chipSpacingHorizontal = 25
        chipGrp.addView(chip as View)
        if (selectedIndArray.contains(str)){
            setExistChipIndustries(str, chip)
        }
        //click on particular chip
        chip.setOnClickListener {
            industryErr.showOrGone(false)
            industryErr.text = ""
            val txtName = chip.text.toString()
            if (isZeroSelectedIndustries()){
                setExistChipIndustries(txtName, chip)
            }else {
                when {
                    isSelIndus_1_3Industries() -> {
                        setExistChipIndustries(txtName, chip)
                    }
                    isSelectedIndustries(txtName) -> {
                        setExistChipIndustries(txtName, chip)
                    }
                    else -> {
                        Utility.customErrorToast(this, getString(R.string.msg_industry_valid))
                    }
                }
            }
        }
    }
    private fun addChipToGroupTags(str: String, chipGrp: ChipGroup) {
        val chip = Chip(this)

        chip.text = str
        chip.setTextAppearance(R.style.chipText)
        chip.setTextAppearance(R.style.chipText_bold)
        chip.setTextColor(ContextCompat.getColor(this, R.color.gray_5B))
        chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8))
        chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.gray_E9_FF))
        chip.chipStrokeWidth = 2f
        chip.chipCornerRadius = 10f
        chip.setPadding(50, 50, 50, 50)
        chipGrp.chipSpacingVertical = 5
        chipGrp.chipSpacingHorizontal = 30
        chipGrp.addView(chip as View)
        if (selectedTagArray.contains(str)){
            setExistChipTags(str, chip)
        }
        //click on particular chip
        chip.setOnClickListener {
            tagsErr.showOrGone(false)
            tagsErr.text = ""
            val txtName = chip.text.toString()
            if (isZeroSelectedTags()){
                setExistChipTags(txtName, chip)
            }else {
                when {
                    isSelIndus_1_5Tags() -> {
                        setExistChipTags(txtName, chip)
                    }
                    isSelectedTags(txtName) -> {
                        setExistChipTags(txtName, chip)
                    }
                    else -> {
                        Utility.customErrorToast(this, getString(R.string.msg_tag_valid))
                    }
                }
            }
        }
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

    private fun getSelectedIndustries(): ArrayList<Int> {
        val list: ArrayList<Int> = ArrayList()
        for (i in 0 until arrIndustry.size){
            if (arrIndustry[i].isSelected == true){
                list.add(arrIndustry[i].id!!.toInt())
            }
        }
        return list
    }
    private fun isZeroSelectedIndustries(): Boolean {
        var count = 0
        for (i in 0 until arrIndustry.size) {
            if (arrIndustry[i].isSelected == true) {
                count++
            }
        }
        return count == 0
    }
    private fun isSelectedIndustries(name: String): Boolean {
        for (i in 0 until arrIndustry.size) {
            if (name == arrIndustry[i].industryName) {
                if (arrIndustry[i].isSelected == true) {
                    return true
                }
            }
        }
        return false
    }

    private fun isSelIndus_1_3Industries(): Boolean {
        var count = 0
        for (i in 0 until arrIndustry.size) {
            if (arrIndustry[i].isSelected == true) {
                count++
            }
        }
        return count<3
    }
    private fun setExistChipIndustries(name: String, chip: Chip) {
        for (i in 0 until arrIndustry.size) {
            if (name == arrIndustry[i].industryName) {
                if (arrIndustry[i].isSelected == true) {
                    chip.setPadding(50, 50, 50, 50)
                    chip.setTextColor(ContextCompat.getColor(this, R.color.gray_5B))
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8))
                    chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.gray_E9_FF))
                    chip.chipStrokeWidth = 2f
                    arrIndustry[i] = IndustryModel(arrIndustry[i].industryName, arrIndustry[i].industryImage, arrIndustry[i].id, false)
                } else {
                    chip.setPadding(50, 50, 50, 50)
                    chip.setTextColor(ContextCompat.getColor(this, R.color.app_green))
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_42))
                    chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.app_green))
                    chip.chipStrokeWidth = 2f
                    arrIndustry[i] = IndustryModel(arrIndustry[i].industryName, arrIndustry[i].industryImage, arrIndustry[i].id, true)
                }
            }
        }
    }

    private fun getSelectedTags(): ArrayList<Int> {
        val list: ArrayList<Int> = ArrayList()
        for (i in 0 until arrTags.size){
            if (arrTags[i].isSelected == true){
                list.add(arrTags[i].id!!.toInt())
            }
        }
        return list
    }
    private fun isZeroSelectedTags(): Boolean {
        var count = 0
        for (i in 0 until arrTags.size) {
            if (arrTags[i].isSelected == true) {
                count++
            }
        }
        return count == 0
    }
    private fun isSelectedTags(name: String): Boolean {
        for (i in 0 until arrTags.size) {
            if (name == arrTags[i].industryName) {
                if (arrTags[i].isSelected == true) {
                    return true
                }
            }
        }
        return false
    }

    private fun isSelIndus_1_5Tags(): Boolean {
        var count = 0
        for (i in 0 until arrTags.size) {
            if (arrTags[i].isSelected == true) {
                count++
            }
        }
        return count<5
    }
    private fun setExistChipTags(name: String, chip: Chip) {
        for (i in 0 until arrTags.size) {
            if (name == arrTags[i].industryName) {
                if (arrTags[i].isSelected == true) {
                    chip.setPadding(50, 50, 50, 50)
                    chip.setTextColor(ContextCompat.getColor(this, R.color.gray_5B))
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8))
                    chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.gray_E9_FF))
                    chip.chipStrokeWidth = 2f
                    arrTags[i] = IndustryModel(arrTags[i].industryName, arrTags[i].industryImage, arrTags[i].id, false)
                } else {
                    chip.setPadding(50, 50, 50, 50)
                    chip.setTextColor(ContextCompat.getColor(this, R.color.app_green))
                    chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_42))
                    chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.app_green))
                    chip.chipStrokeWidth = 2f
                    arrTags[i] = IndustryModel(arrTags[i].industryName, arrTags[i].industryImage, arrTags[i].id, true)
                }
            }
        }
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.deleteaccount){
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
        }else if (type == Utility.industries){
            if (isData){
                val data = result.getJSONArray(Utility.key.data)
                for (i in 0 until data.length()){
                    val obj = data.getJSONObject(i)
                    arrIndustry.add(IndustryModel(Utility.checkStringNullOrNot(obj, Utility.key.industry_name), Utility.checkStringNullOrNot(obj, Utility.key.industry_image), Utility.checkStringNullOrNot(obj, Utility.key.id), false))
                    //show listing as industry chips
                    addChipToGroupIndustries(Utility.checkStringNullOrNot(obj, Utility.key.industry_name), chipGrpIndustry)
                }
                callTagListApi()
            }else{
                Utility.customErrorToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )

            }
        }else if (type==Utility.tags){
            if (isData){
                val data = result.getJSONArray(Utility.key.data)
                for (i in 0 until data.length()){
                    val obj = data.getJSONObject(i)
                    arrTags.add(IndustryModel(Utility.checkStringNullOrNot(obj, Utility.key.tag_name),"", Utility.checkStringNullOrNot(obj, Utility.key.id), false))
                    //show listing as tags chips
                    addChipToGroupTags(Utility.checkStringNullOrNot(obj, Utility.key.tag_name), chipGrpTags)
                }
            }else{
                Utility.customErrorToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )

            }

        }else if (type==Utility.update_store){
            if (isData) {
                isEdit = true
                Utility.customSuccessToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
                onBackPressed()
            }else{
                Utility.showApiMessageError(this, result, "data")
            }

        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("Result", isEdit.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}