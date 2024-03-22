package com.planout.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.SeeMoreViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.IndustriesData
import com.planout.models.StoreModel
import com.planout.models.TagData
import kotlinx.android.synthetic.main.activity_see_more.*
import kotlinx.android.synthetic.main.activity_see_more.loadView
import kotlinx.android.synthetic.main.header_normal_view.*
import org.json.JSONArray
import org.json.JSONObject

class SeeMoreActivity : AppCompatActivity(), ApiResponse {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var mAdapter : SeeMoreViewAdapter
    private var pageCount = 1
    private var lstCurrent = 0
    private var isLoading = false
    val recordsList: ArrayList<StoreModel> = ArrayList()
    var resultLauncherFavorite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val ID = data!!.getStringExtra("ID")!!
                    val isRemoveFav=data.getStringExtra("isRemoveFav")
                    if (isRemoveFav=="true"){
                        for (i in 0 until recordsList.size){
                            if (recordsList[i].id==ID){
                                recordsList[i].is_favorite = "false"
                                mAdapter.notifyDataSetChanged()
                                break
                            }
                        }
                    }else if (isRemoveFav=="false"){
                        for (i in 0 until recordsList.size){
                            if (recordsList[i].id==ID){
                                recordsList[i].is_favorite = "true"
                                mAdapter.notifyDataSetChanged()
                                break
                            }
                        }
                    }

                }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_more)

        //Utility.setStatusBarColor(this)
        if (intent.hasExtra("title")){
            txtHeader.text = intent.getStringExtra("title")
        }
        //call api for store listing
        storeListApi()

        clickView()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun storeListApi() {
        val jsonObj = JSONObject()
        val arrayind1: ArrayList<Int> = ArrayList()
        arrayind1.add((intent.getStringExtra(Utility.key.id))!!.toInt())
        val arraytag1: ArrayList<Int> = ArrayList()
        val indJArr = JSONArray(arrayind1)
        val cityJArr = JSONArray(arraytag1)
        jsonObj.put(Utility.key.page, pageCount)
        jsonObj.put(Utility.key.industries, indJArr)
        jsonObj.put(Utility.key.cities, cityJArr)
        jsonObj.put(Utility.key.tags, cityJArr)
        jsonObj.put(Utility.key.searchkey, "")
        Log.d("TAG", "storeListApi json: "+jsonObj.toString())
        //val arr = arrayOf(intent.getStringExtra(Utility.key.id)!!.toInt())
        val arrayind: ArrayList<Int> = ArrayList()
        val arraytag: ArrayList<Int> = ArrayList()
        arrayind.add((intent.getStringExtra(Utility.key.id))!!.toInt())
        val json = JSONObject()
        json.put(Utility.key.page,pageCount)
        json.put(Utility.key.industries, JSONArray(arrayind))
        json.put(Utility.key.tags,JSONArray(arraytag))
        json.put(Utility.key.cities,JSONArray(arraytag))
        json.put(Utility.key.searchkey,"")
        /*val api =
            ApiController.api.stores + "?${Utility.key.page}=${pageCount}&${Utility.key.industries}=${
                JSONArray(arrayind)
            }&${Utility.key.tags}=${JSONArray(arraytag)}&${Utility.key.cities}=${JSONArray(arraytag)}"
        Log.d("result>>>url", api)*/
        CallApi.callAPiJson(json, ApiController.api.stores, this, Utility.stores, pageCount == 1, Utility.POST, true)

    }

    private fun clickView() {
        imgBackHeader.setOnClickListener { onBackPressed() }

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.stores){
            if (isData){
                val dataObj=result.getJSONObject(Utility.key.data)
                val records=dataObj.getJSONArray("records")
                pageCount = dataObj.getInt("current_page")
                isLoading = records.length()<20
                for (r in 0 until records.length()){
                    val storeModel= StoreModel()
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
                        val tagData= TagData()
                        tagData.id=tagsObj.getString("id")
                        tagData.tag_name=tagsObj.getString("tag_name")
                        tagsList.add(tagData)
                    }

                    storeModel.tags=tagsList

                    val industriesArray=storeObj.getJSONArray("industries")
                    val industriessList: ArrayList<IndustriesData> = ArrayList()
                    for (k in 0 until industriesArray.length()){
                        val industriessObj=industriesArray.getJSONObject(k)
                        val industriesData= IndustriesData()
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
                if (pageCount == 1) {
                    loadView.showOrGone(false)
//                    recyclerSeeMore.setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    mAdapter = SeeMoreViewAdapter(this, recordsList, resultLauncherFavorite)
                    recyclerSeeMore.layoutManager = layoutManager
                    recyclerSeeMore.adapter = mAdapter

                }else{
                    loadView.showOrGone(false)
                    mAdapter.notifyDataSetChanged()
                }
                /*recyclerSeeMore.setOnScrollChangeListener { view, i, i2, i3, i4 ->
                    val total = ((pageCount-1)*20)+records.length()
                    val currentLastItem: Int = layoutManager.findLastVisibleItemPosition()
                    if (currentLastItem == total - 1 && isLoading) {
                        lstCurrent = currentLastItem
                        loadView.showOrGone(true)
                        pageCount += 1
                        storeListApi()
                    }
                }*/

                recyclerSeeMore.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val total = ((pageCount-1)*20)+records.length()
                        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                        if (!isLoading) {
                            if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == total - 1) {
                                //bottom of list!
                                loadView.showOrGone(true)
                                pageCount += 1
                                storeListApi()
                                isLoading = true
                            }
                        }
                    }
                })
            }
        }else if (type==Utility.favoritesRemove){
            if (isData){
                //Utility.customSuccessToast(this,result.getString(Utility.key.message))
            }else{
                Utility.customErrorToast(this,result.getString(Utility.key.message))
            }
        }else if (type==Utility.favoritesAdd){
            if (isData){
                //Utility.customSuccessToast(this,result.getString(Utility.key.message))
            }else{
                Utility.customErrorToast(this,result.getString(Utility.key.message))
            }
        }
    }
}