package com.planout.activities

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.planout.R
import com.planout.adapters.LocationViewAdapter
import com.planout.adapters.PhotosViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.StoreLocationData
import com.planout.models.StoreMediaData
import kotlinx.android.synthetic.main.activity_company_profile.*
import okhttp3.FormBody
import org.json.JSONObject
import java.lang.Exception

class CompanyProfileActivity : AppCompatActivity(), ApiResponse {

    lateinit var layoutManager1: LinearLayoutManager
    lateinit var photoAdapter : PhotosViewAdapter
    lateinit var layoutManager2: LinearLayoutManager
    lateinit var locAdapter : LocationViewAdapter

    var storeID=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_profile)
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT

        setView()
    }

    private fun setPhotoDefaultView(bool: Boolean) {
        if (bool){
            photoSeeAll.visibility = View.INVISIBLE
            linSelectPhoto.visibility = View.VISIBLE
            recyclerPhoto.visibility = View.GONE
        }else{
            photoSeeAll.visibility = View.VISIBLE
            recyclerPhoto.visibility = View.VISIBLE
            linSelectPhoto.visibility = View.GONE
        }
    }
    fun setLocationDefaultView(bool: Boolean) {
        if (bool){
            locSeeAll.visibility = View.INVISIBLE
            linBtnAddLoc.visibility = View.VISIBLE
            recyclerLoc.visibility = View.GONE
        }else{
            locSeeAll.visibility = View.VISIBLE
            recyclerLoc.visibility = View.VISIBLE
            linBtnAddLoc.visibility = View.GONE
        }
    }

    private fun setView() {

        Utility.animationClick(imgBack).setOnClickListener {
            onBackPressed()
        }

        if(Utility.getForm(this, Utility.key.is_owner) == "1"){
            Utility.animationClick(linBtnAddLoc).setOnClickListener {
                startActivity(Intent(this,AddLocationActivity::class.java)
                    .putExtra(Utility.key.store_id,storeID))
            }
        }
        else{
            linBtnAddLoc.isEnabled = false
        }


        Utility.animationClick(locSeeAll).setOnClickListener {
            startActivity(Intent(this,AddLocationActivity::class.java)
                .putExtra(Utility.key.store_id,storeID))
        }





//        companyDetailApi()


    }

    private fun companyDetailApi() {
        dataAria.showOrGone(false)
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.profile, this, Utility.profile, true, Utility.GET, true)
    }

    private fun addChipToGroup(str: String, chipGrp: ChipGroup) {
        val chip = Chip(this)
        chip.text = str
        chip.setTextAppearance(R.style.chipText)
        chip.setTextColor(ContextCompat.getColor(this, R.color.black_white))
        chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8_48))
        chip.chipCornerRadius = 10f
        chip.isChipIconVisible = false
        chip.setPadding(50, 50, 50, 50)
        chipGrp.chipSpacingVertical = 5
        chipGrp.chipSpacingHorizontal = 20
        chip.isCheckable = false
        chipGrp.addView(chip as View)
    }

    var resultLauncherTip =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data!!.getStringExtra("Result")=="true") {
                    companyDetailApi()
                }
            }
        }

    var resultLauncherimage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data!!.getStringExtra("Result")=="true") {
                    companyDetailApi()
                }
            }
        }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.profile){
            if (isData){
                dataAria.showOrGone(true)
                val dataObj=result.getJSONObject(Utility.key.data)
                try {
                    val hashM: HashMap<String, String> = HashMap()
                    hashM[Utility.key.allow_notification] = Utility.checkStringNullOrNot(dataObj, Utility.key.allow_notification)
                    Utility.saveForm(hashM, this)
                }catch (e:Exception){e.printStackTrace()}

                val storeObj=dataObj.getJSONObject(Utility.key.store)

                if(Utility.getForm(this, Utility.key.is_owner) == "1"){

                    Utility.animationClick(txtEdit).setOnClickListener {
//                    resultLauncherTip.launch(
//                        Intent(this, CompanyEditProfileActivity::class.java)
//                            .putExtra(Utility.key.storeData,storeObj.toString())
//                    )
                        startActivity(Intent(this, CompanyEditProfileActivity::class.java)
                            .putExtra(Utility.key.storeData,storeObj.toString()))
                    }


                    Utility.animationClick(txtSelectDoc).setOnClickListener {
                        startActivity(Intent(this,PhotoGalleryActivity::class.java)
                            .putExtra(Utility.key.store_id,storeID))
                    }
                }
                else{
                    txtEdit.visibility = View.GONE
                    txtSelectDoc.isEnabled = false
                }


                storeID=storeObj.getString(Utility.key.id)
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


                Utility.SetImageSimple(this,store_image,profile_image)

                txtProfileName.text=Utility.checkStringNullOrEmpty(store_name)
                txtMail.text=Utility.checkStringNullOrEmpty(email)
                storeMobileTXt.text=Utility.checkStringNullOrEmpty(mobile)
                storePhoneTxt.text=Utility.checkStringNullOrEmpty(telephone)
                storeFaxTxt.text=Utility.checkStringNullOrEmpty(fax)
                if(Utility.checkStringNullOrEmpty(extra_notes) == ""){
                    constExtraNotes.showOrGone(false)
                }else{
                    constExtraNotes.showOrGone(true)

                    extraNotesTxt.text=Utility.checkStringNullOrEmpty(extra_notes)

                }

                val industriesArray=storeObj.getJSONArray(Utility.key.industries)
                if (industriesArray.length()==0){
                    industriesTxt.showOrGone(false)
                    chipGrp.showOrGone(false)
                    chipGrp.removeAllViews()
                }else{
                    industriesTxt.showOrGone(true)
                    chipGrp.showOrGone(true)
                    chipGrp.removeAllViews()
                    for (i in 0 until industriesArray.length()){
                        val indObj=industriesArray.getJSONObject(i)
                        val ind=indObj.getString(Utility.key.industry_name)
                        //show listing for industry chips
                        addChipToGroup(ind, chipGrp)
                    }

                }

                val tagsArray=storeObj.getJSONArray(Utility.key.tags)
                if (tagsArray.length()==0){
                    tagsTxt.showOrGone(false)
                    chipGrpTag.showOrGone(false)
                    chipGrpTag.removeAllViews()
                }else{
                    tagsTxt.showOrGone(true)
                    chipGrpTag.showOrGone(true)
                    chipGrpTag.removeAllViews()
                    for (i in 0 until tagsArray.length()){
                        val tagObj=tagsArray.getJSONObject(i)
                        val tag=tagObj.getString(Utility.key.tag_name)
                        //show listing for tag chips
                        addChipToGroup(tag, chipGrpTag)
                    }
                }
                try {
                    val locationsArray = storeObj.getJSONArray(Utility.key.locations)
                    if (locationsArray.length() > 0) {
                        setLocationDefaultView(false)
                        val storeLocationItems: ArrayList<StoreLocationData> = ArrayList()
                        for (i in 0 until locationsArray.length()) {
                            val locationdataObj = locationsArray.getJSONObject(i)
                            val item = StoreLocationData()
                            item.id = locationdataObj.getString(Utility.key.id)
                            item.store_id = locationdataObj.getString(Utility.key.store_id)
                            item.city_id = locationdataObj.getString(Utility.key.city_id)
                            item.city_name = locationdataObj.getString(Utility.key.city_name)
                            item.area = locationdataObj.getString(Utility.key.area)
                            item.address = locationdataObj.getString(Utility.key.address)
                            item.address1 = locationdataObj.getString(Utility.key.address1)
                            item.postal_code = locationdataObj.getString(Utility.key.postal_code)
                            item.latitude = locationdataObj.getString(Utility.key.latitude)
                            item.longitude = locationdataObj.getString(Utility.key.longitude)
                            item.address_type=locationdataObj.getString(Utility.key.address_type)
                            item.table_indoor = locationdataObj.getString(Utility.key.table_indoor)
                            item.table_outdoor =
                                locationdataObj.getString(Utility.key.table_outdoor)
                            item.is_default = locationdataObj.getBoolean(Utility.key.is_default)

                            item.locationObj = locationdataObj.toString()

                            storeLocationItems.add(item)
                        }
//                        recyclerLoc.setHasFixedSize(true)
                        layoutManager2 =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        locAdapter = LocationViewAdapter(this, storeLocationItems, this)
                        recyclerLoc.layoutManager = layoutManager2
                        recyclerLoc.adapter = locAdapter
                    } else {
                        setLocationDefaultView(true)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }

                try {
                    val media = storeObj.getJSONArray(Utility.key.media)
                    if (media.length() == 0) {
                        setPhotoDefaultView(true)

                    } else {
                        setPhotoDefaultView(false)
//                        recyclerPhoto.setHasFixedSize(true)
                        val storeMediaItems: ArrayList<StoreMediaData> = ArrayList()
                        for (i in 0 until media.length()) {
                            val mediaObj = media.getJSONObject(i)
                            val item = StoreMediaData()
                            item.id = mediaObj.getString(Utility.key.id)
                            item.media_url = mediaObj.getString(Utility.key.media_url)
                            item.isUrl = true
                            item.imageBitmap = null
                            storeMediaItems.add(item)
                        }

                        layoutManager1 =
                            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        photoAdapter = PhotosViewAdapter(this, storeMediaItems, "")
                        recyclerPhoto.layoutManager = layoutManager1
                        recyclerPhoto.adapter = photoAdapter

                        Utility.animationClick(photoSeeAll).setOnClickListener {
//                        resultLauncherimage.launch(
//                            Intent(this, PhotoGalleryActivity::class.java)
//                                .putExtra(Utility.key.isFrom,"detail")
//                                .putExtra(Utility.key.store_id,storeID)
//                        )
                            startActivity(
                                Intent(this, PhotoGalleryActivity::class.java)
                                    .putExtra(Utility.key.isFrom, "detail")
                                    .putExtra(Utility.key.store_id, storeID)
                            )
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }else{
                Utility.customErrorToast(this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )

            }
        }else if (type==Utility.storeDelete){
            if (isData){
                locAdapter.onTaskComplete(result,type,isData)
            }else{
                Utility.customErrorToast(this, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }
    }

    fun scrollDown(pos: Int){
        dataAria.smoothScrollTo(0, dataAria.scrollY+120)
    }

    override fun onResume() {
        super.onResume()
        //call api for company details
        companyDetailApi()
    }
}