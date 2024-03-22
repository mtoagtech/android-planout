package com.planout.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.activity_favorites.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject

class FavoritesActivity : AppCompatActivity(), ApiResponse {

    lateinit var layoutManager: LinearLayoutManager
    val recordsList: ArrayList<StoreModel> = ArrayList()
    lateinit var restro2ViewAdapter:SeeMoreViewAdapter
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
                            restro2ViewAdapter.notifyDataSetChanged()
                            break
                        }
                    }
                }else if (isRemoveFav=="false"){
                    for (i in 0 until recordsList.size){
                        if (recordsList[i].id==ID){
                            recordsList[i].is_favorite = "true"
                            restro2ViewAdapter.notifyDataSetChanged()
                            break
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        txtHeader.text = getString(R.string.favorite_title)
        //call api for favourites
        clickView()

    }

    override fun onResume() {
        super.onResume()
        favApi()
    }

    private fun favApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.favorites, this, Utility.favorites, true, Utility.GET, true)
    }


    private fun clickView() {
        Utility.animationClick(imgBackHeader).setOnClickListener { onBackPressed() }
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.favorites){
            if (isData){
                val dataObj=result.getJSONArray(Utility.key.data)
                if (dataObj.length()==0){
                    constNoData.showOrGone(true)
                }else{
                    constNoData.showOrGone(false)
                }
                recordsList.clear()
                for (r in 0 until dataObj.length()){
                    val storeModel= StoreModel()
                    val storeObj=dataObj.getJSONObject(r)
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
                    /////////tags////////////////////
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
                    /////////industries//////////////////
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

//                recyclerFav.setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                restro2ViewAdapter = SeeMoreViewAdapter(this, recordsList,resultLauncherFavorite)
                recyclerFav.layoutManager = layoutManager
                recyclerFav.adapter = restro2ViewAdapter
            }else{
                constNoData.showOrGone(true)
            }
        }else if (type==Utility.favoritesRemove){
            if (isData){
                //Utility.customSuccessToast(this,result.getString(Utility.key.message))
                //call api for changes
                favApi()
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