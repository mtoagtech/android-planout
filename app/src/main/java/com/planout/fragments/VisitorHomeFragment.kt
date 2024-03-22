package com.planout.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.facebook.FacebookSdk
import com.planout.BuildConfig
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.*
import com.planout.adapters.*
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.isLoginCheck
import com.planout.constant.Utility.showOrGone
import com.planout.models.*
import com.planout.retrofit.DataInterface
import kotlinx.android.synthetic.main.fragment_visitor_home.view.*
import okhttp3.FormBody
import org.json.JSONObject
import kotlin.Exception

class VisitorHomeFragment(
    val activityBase: HomeVisitorActivity,
    val resultLauncherFavorite: ActivityResultLauncher<Intent>,
    val resultLauncherFavoriteInd: ActivityResultLauncher<Intent>
) : Fragment(), ApiResponse, DataInterface {

    lateinit var rootView: View
    var arrIndusList: ArrayList<IndusHomeModel> = ArrayList()
    lateinit var indusAdapter: IndustryViewAdapter
    lateinit var gridView: GridLayoutManager
    lateinit var restro2ViewAdapter: HomeBeachViewAdapter
    lateinit var industryDetailViewAdapter: IndustryDetailViewAdapter
    lateinit var layoutManager: LinearLayoutManager
    val recordsList: ArrayList<StoreModel> = ArrayList()
    val arrIndusDetailList: ArrayList<IndustryDetailModel> = ArrayList()

    val handler = Handler()
    val handler2 = Handler()
    val handler3 = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_visitor_home, container, false)
        activityBase.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        activityBase.window.statusBarColor = Color.TRANSPARENT
        try {
            handler.removeCallbacksAndMessages(null)

        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
        try {
            handler2.removeCallbacksAndMessages(null)

        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
        try {
            handler3.removeCallbacksAndMessages(null)

        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
        setView()
        clickView()
        //call api for visitor dashboard detail
        visitorDashboardAPi()
        return rootView
    }

    private fun clickView() {
        Utility.animationClick(rootView.upcoming_more).setOnClickListener {
            startActivity(Intent(activityBase, UpcomingEventsActivity::class.java))
        }
        Utility.animationClick(rootView.imgSavedSearch).setOnClickListener {
            if (isLoginCheck(activityBase)){
                startActivity(Intent(activityBase, SavedSearchActivity::class.java))

            }
        }
        Utility.animationClick(rootView.imgFavorites).setOnClickListener {
            if (isLoginCheck(activityBase)){
                startActivity(Intent(activityBase, FavoritesActivity::class.java))

            }
        }
        rootView.editSearching.setOnClickListener {
            startActivity(Intent(activityBase, VisitorSearchActivity::class.java))
        }
    }

    private fun setView() {
        rootView.dataAria.showOrGone(false)
    }

    override fun onResume() {
        super.onResume()
        try {
            if (industryDetailViewAdapter!=null) {
                industryDetailViewAdapter.notifyDataSetChanged()
                restro2ViewAdapter.notifyDataSetChanged()
            }
        }catch (e:Exception){e.printStackTrace()}
        //callAPIMain()
        //visitorDashboardAPi(Utility.stores_first)

    }


    private fun visitorDashboardAPi() {
        val mBuilder = FormBody.Builder()

        CallApi.callAPi(mBuilder, ApiController.api.stores, activityBase, Utility.stores, true, Utility.GET, true)

    }


    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.stores) {
            if (isData){
                activityBase.runOnUiThread {
                    try {
                        rootView.dataAria.showOrGone(true)

                        val dataObj=result.getJSONObject(Utility.key.data)
                        if (dataObj.has("app_params")){
                            val app_params = dataObj.getJSONObject("app_params")
                            val android = app_params.getJSONObject("android")
                            updateFromPlayStore(android.getString("app_version"), android.getString("force_update"))
                        }
                        val total_unread_notifications=dataObj.getInt(Utility.key.total_unread_notifications)
                        activityBase.setNotificationView(total_unread_notifications>0)
                        val popular_industriesArray=dataObj.getJSONArray(Utility.key.popular_industries)
                        arrIndusList.clear()
                        arrIndusDetailList.clear()
                        for (i in 0 until popular_industriesArray.length()){
                            val popular_industriesObj=popular_industriesArray.getJSONObject(i)
                            val id=popular_industriesObj.getString("id")
                            val industry_name=popular_industriesObj.getString(Utility.key.industry_name)
                            val industry_image=popular_industriesObj.getString(Utility.key.industry_image)

                            arrIndusList.add(IndusHomeModel(id, industry_name,industry_image))

                            val industryDetailModel =IndustryDetailModel()
                            industryDetailModel.id=popular_industriesObj.getString("id")
                            industryDetailModel.industry_name=popular_industriesObj.getString(Utility.key.industry_name)
                            val storesArray=popular_industriesObj.getJSONArray(Utility.key.stores)
                            val arrStoreDetailList: ArrayList<StoreModel> = ArrayList()

                            for (l in 0 until storesArray.length()){
                                val storeModel=StoreModel()
                                val storeObj=storesArray.getJSONObject(l)
                                storeModel.id=storeObj.getString("id")
                                storeModel.store_name=storeObj.getString("store_name")
                                storeModel.store_image=storeObj.getString("store_image")
                                storeModel.cover_image=storeObj.getString("cover_image")
                                storeModel.is_open=storeObj.getString("is_open")
                                storeModel.starttime=storeObj.getString("starttime")
                                storeModel.endtime=storeObj.getString("endtime")
                                storeModel.starttime1=storeObj.getString("starttime1")
                                storeModel.endtime1=storeObj.getString("endtime1")
                                storeModel.email=storeObj.getString("email")
                                storeModel.name=storeObj.getString("name")
                                storeModel.mobile=storeObj.getString("mobile")
                                storeModel.telephone=storeObj.getString("telephone")
                                storeModel.fax=storeObj.getString("fax")
                                storeModel.is_favorite=storeObj.getString("is_favorite")

                                val tagArray=storeObj.getJSONArray("tags")
                                val tagsList: ArrayList<TagData> = ArrayList()

                                for (j in 0 until tagArray.length()){
                                    val tagsObj=tagArray.getJSONObject(j)
                                    val tagData=TagData()
                                    tagData.id=tagsObj.getString("id")
                                    tagData.tag_name=tagsObj.getString("tag_name")
                                    tagsList.add(tagData)
                                }

                                storeModel.tags=tagsList

                                val industriesArray=storeObj.getJSONArray("industries")
                                val industriessList: ArrayList<IndustriesData> = ArrayList()
                                for (k in 0 until industriesArray.length()){
                                    val industriessObj=industriesArray.getJSONObject(k)
                                    val industriesData=IndustriesData()
                                    industriesData.id=industriessObj.getString("id")
                                    industriesData.industry_name=industriessObj.getString("industry_name")
                                    industriessList.add(industriesData)
                                }
                                storeModel.industries=industriessList

                                if (storeObj.getString("default_location")!="null"){
                                    storeModel.default_location_id=storeObj.getJSONObject("default_location").getString("id")

                                    storeModel.default_location=storeObj.getJSONObject("default_location")
                                }else{
                                    storeModel.default_location_id="null"
                                }
                                arrStoreDetailList.add(storeModel)
                            }
                            //store detail for listing is not exist
                            if (arrStoreDetailList.size>0) {
                                industryDetailModel.storeList = arrStoreDetailList
                                arrIndusDetailList.add(industryDetailModel)
                            }

                        }

                        gridView = GridLayoutManager(activityBase, 3, LinearLayoutManager.VERTICAL,false)
                        indusAdapter = IndustryViewAdapter(activityBase, arrIndusList)
                        rootView.recyclerIndustry.layoutManager = gridView
                        rootView.recyclerIndustry.adapter = indusAdapter


                        handler.postDelayed(Runnable {
                            rootView.recyclerIndustryDetail.showOrGone(true)
                            val industryDetailLayoutManager=LinearLayoutManager(activityBase, LinearLayoutManager.VERTICAL, false)
                            rootView.recyclerIndustryDetail.layoutManager=industryDetailLayoutManager
                            industryDetailViewAdapter=IndustryDetailViewAdapter(activityBase,arrIndusDetailList,resultLauncherFavorite)
                            rootView.recyclerIndustryDetail.adapter = industryDetailViewAdapter
//                            rootView.recyclerIndustryDetail.setHasFixedSize(true)

                            handler2.postDelayed(Runnable {
                                val records=dataObj.getJSONArray("featured_stores")
                                if (records.length() <= 0) {
                                    rootView.trendingView.showOrGone(false)
                                } else {
                                    rootView.trendingView.showOrGone(true)
                                }
                                recordsList.clear()
                                for (r in 0 until records.length()){
                                    val storeModel=StoreModel()
                                    val storeObj=records.getJSONObject(r)
                                    storeModel.id=storeObj.getString("id")
                                    storeModel.store_name=storeObj.getString("store_name")
                                    storeModel.store_image=storeObj.getString("store_image")
                                    storeModel.cover_image=storeObj.getString("cover_image")
                                    storeModel.is_open=storeObj.getString("is_open")
                                    storeModel.starttime=storeObj.getString("starttime")
                                    storeModel.endtime=storeObj.getString("endtime")
                                    storeModel.starttime1=storeObj.getString("starttime1")
                                    storeModel.endtime1=storeObj.getString("endtime1")
                                    storeModel.email=storeObj.getString("email")
                                    storeModel.name=storeObj.getString("name")
                                    storeModel.mobile=storeObj.getString("mobile")
                                    storeModel.telephone=storeObj.getString("telephone")
                                    storeModel.fax=storeObj.getString("fax")
                                    storeModel.is_favorite=storeObj.getString("is_favorite")

                                    val tagArray=storeObj.getJSONArray("tags")
                                    val tagsList: ArrayList<TagData> = ArrayList()

                                    for (j in 0 until tagArray.length()){
                                        val tagsObj=tagArray.getJSONObject(j)
                                        val tagData=TagData()
                                        tagData.id=tagsObj.getString("id")
                                        tagData.tag_name=tagsObj.getString("tag_name")
                                        tagsList.add(tagData)
                                    }

                                    storeModel.tags=tagsList

                                    val industriesArray=storeObj.getJSONArray("industries")
                                    val industriessList: ArrayList<IndustriesData> = ArrayList()
                                    for (k in 0 until industriesArray.length()){
                                        val industriessObj=industriesArray.getJSONObject(k)
                                        val industriesData=IndustriesData()
                                        industriesData.id=industriessObj.getString("id")
                                        industriesData.industry_name=industriessObj.getString("industry_name")
                                        industriessList.add(industriesData)
                                    }
                                    storeModel.industries=industriessList

                                    if (storeObj.getString("default_location")!="null"){
                                        storeModel.default_location_id=storeObj.getJSONObject("default_location").getString("id")

                                        storeModel.default_location=storeObj.getJSONObject("default_location")
                                    }else{
                                        storeModel.default_location_id="null"
                                    }
                                    recordsList.add(storeModel)
                                }

//                                rootView.recyclerRestro2.setHasFixedSize(true)
                                layoutManager = LinearLayoutManager(activityBase, LinearLayoutManager.HORIZONTAL, false)
                                restro2ViewAdapter = HomeBeachViewAdapter(activityBase, recordsList,resultLauncherFavorite)
                                rootView.recyclerRestro2.layoutManager = layoutManager
                                rootView.recyclerRestro2.adapter = restro2ViewAdapter


                                handler3.postDelayed(Runnable {
                                    val upcoming_eventsArray=dataObj.getJSONArray("upcoming_events")
                                    val upcoming_eventsList: ArrayList<UpcomingEventData> = ArrayList()
                                    if (upcoming_eventsArray.length() <= 0) {
                                        rootView.linBlack.showOrGone(false)

                                    } else {
                                        rootView.linBlack.showOrGone(true)

                                    }
                                    for (u in 0 until upcoming_eventsArray.length()){
                                        val upcoming_eventsObj=upcoming_eventsArray.getJSONObject(u)
                                        val upcomingEventData=UpcomingEventData()
                                        upcomingEventData.id=upcoming_eventsObj.getString("id")
                                        upcomingEventData.store_id=upcoming_eventsObj.getString("store_id")
                                        upcomingEventData.location_id=upcoming_eventsObj.getString("location_id")
                                        upcomingEventData.event_title=upcoming_eventsObj.getString("event_title")
                                        upcomingEventData.description=upcoming_eventsObj.getString("description")
                                        upcomingEventData.event_date=upcoming_eventsObj.getString("event_date")
                                        upcomingEventData.starttime=upcoming_eventsObj.getString("starttime")
                                        upcomingEventData.endtime=upcoming_eventsObj.getString("endtime")
                                        upcomingEventData.event_image=upcoming_eventsObj.getString("event_image")
                                        upcomingEventData.store_image=upcoming_eventsObj.getString("store_image")
                                        upcomingEventData.location=upcoming_eventsObj.getJSONObject("location")
                                        upcoming_eventsList.add(upcomingEventData)
                                    }
                                    rootView.loadView.showOrGone(false)

                                    rootView.recyclerUpcoming.layoutManager = LinearLayoutManager(activityBase, LinearLayoutManager.HORIZONTAL, false)
                                    rootView.recyclerUpcoming.adapter = MyUpcomingAdapter(activityBase,upcoming_eventsList)
                                    try {
                                        LinearSnapHelper().attachToRecyclerView(rootView.recyclerUpcoming)
                                    }catch (e:Exception){e.printStackTrace()}
                                }, 2000)
                            }, 2000)
                        }, 2000)
                    }catch (e:Exception){e.printStackTrace()}
                }
            }
        }else if (type.contains(Utility.favoritesAdd)){
            val likedId=type.replace(Utility.favoritesAdd+"_","")
            recordsListDataset(likedId,"false")
        }else if (type.contains(Utility.favoritesRemove)){
            val likedId=type.replace(Utility.favoritesRemove+"_","")
            recordsListDataset(likedId,"true")
        }
    }

    fun recordsListDataset(ID: String, isRemoveFav: String?) {

        if (isRemoveFav=="true"){
            for (i in 0 until arrIndusDetailList.size){
                for (j in 0 until arrIndusDetailList[i].storeList.size){
                    if (arrIndusDetailList[i].storeList[j].id==ID){
                        arrIndusDetailList[i].storeList[j].is_favorite = "false"
                        industryDetailViewAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }
            for (i in 0 until recordsList.size){
                if (recordsList[i].id==ID){
                    recordsList[i].is_favorite = "false"
                    restro2ViewAdapter.notifyItemChanged(i)
                    break
                }
            }
        }else if (isRemoveFav=="false"){
            for (i in 0 until arrIndusDetailList.size){
                for (j in 0 until arrIndusDetailList[i].storeList.size){
                    if (arrIndusDetailList[i].storeList[j].id==ID){
                        arrIndusDetailList[i].storeList[j].is_favorite = "true"
                        industryDetailViewAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }
            for (i in 0 until recordsList.size){
                if (recordsList[i].id==ID){
                    recordsList[i].is_favorite = "true"
                    restro2ViewAdapter.notifyItemChanged(i)
                    break
                }
            }
        }
    }

    fun arrIndusDetailListDataset(ID: String, isRemoveFav: String?) {
        if (isRemoveFav=="true"){
            for (i in 0 until arrIndusDetailList.size){
                for (j in 0 until arrIndusDetailList[i].storeList.size){
                    if (arrIndusDetailList[i].storeList[j].id==ID){
                        arrIndusDetailList[i].storeList[j].is_favorite = "false"
                        industryDetailViewAdapter.notifyDataSetChanged()
                        break
                    }
                }
            }
            for (i in 0 until recordsList.size){
                if (recordsList[i].id==ID){
                    recordsList[i].is_favorite = "false"
                    industryDetailViewAdapter.notifyDataSetChanged()
                    break
                }
            }
        }else if (isRemoveFav=="false"){
            for (i in 0 until arrIndusDetailList.size){
                for (j in 0 until arrIndusDetailList[i].storeList.size){
                    if (arrIndusDetailList[i].storeList[j].id==ID){
                        arrIndusDetailList[i].storeList[j].is_favorite = "true"
                        industryDetailViewAdapter.notifyDataSetChanged()
                        break
                    }
                }
            }
            for (i in 0 until recordsList.size){
                if (recordsList[i].id==ID){
                    recordsList[i].is_favorite = "true"
                    industryDetailViewAdapter.notifyDataSetChanged()
                    break
                }
            }
        }
    }

    fun updateFromPlayStore(appVersion: String, forceUpdate: String){
        val versionName: String = BuildConfig.VERSION_NAME
        Log.d("appVersion",appVersion)
        Log.d("versionName",versionName)


        if (versionName != appVersion) {
            showUpdatePopUp(
                getString(R.string.update),
                getString(R.string.available_update),
                forceUpdate
            )
        }
    }

    private fun showUpdatePopUp(
        title: String,
        subTitle: String,
        forceUpdate: String
    ) {
        val dialog = Dialog(activityBase)
        dialog.setContentView(R.layout.logout_popup_view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.text=getString(R.string.not_now)
        txtDelete.text=getString(R.string.update)
        if (forceUpdate == "1"){
            txtCancel.showOrGone(false)
        }
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.setOnClickListener { dialog.dismiss()
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${FacebookSdk.getApplicationContext().packageName}")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${FacebookSdk.getApplicationContext().packageName}")))
            }
        }
        dialog.show()

    }

    override fun addSetData(id: String, name: String, status: String) {
        recordsListDataset(id, status)
    }
}