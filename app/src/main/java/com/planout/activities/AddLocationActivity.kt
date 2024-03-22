package com.planout.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.LocationViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.StoreLocationData
import kotlinx.android.synthetic.main.activity_add_location.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject

class AddLocationActivity : AppCompatActivity(), ApiResponse {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var locAdapter : LocationViewAdapter
    var strStoreId = ""

    var resultLauncherimage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // call api for store location listing
                locationList()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        strStoreId = intent.getStringExtra(Utility.key.store_id)!!
        //Utility.setStatusBarColor(this)
        Utility.animationClick(imgBackHeader).setOnClickListener { onBackPressed() }
        txtHeader.text = getString(R.string.locations)
        if(Utility.getForm(this, Utility.key.is_owner) == "1"){
            Utility.animationClick(linBtnAddLoc).setOnClickListener {
                val isPermission = checkLocationPermission()
                if (isPermission) {
                    val intent = Intent(this, CreateLocationScreen::class.java)
                        .putExtra(Utility.key.store_id, intent.getStringExtra(Utility.key.store_id)!!)
                        .putExtra(Utility.key.isFrom, Utility.key.add)
                    startActivity(intent)
                }
//            resultLauncherimage.launch(
//            )
            }
        }
        else{
            linBtnAddLoc.isEnabled = false
        }

    }

    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    3
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    3
                )
            }
            false
        } else {
            true
        }
    }

    override fun onResume() {
        super.onResume()
        locationList()

    }
    private fun locationList() {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.store_locations + "?${Utility.key.store_id}=${strStoreId}"
        CallApi.callAPi(mBuilder, API, this, Utility.store_locations, true, Utility.GET, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.store_locations){
            if (isData){
                val dataArray=result.getJSONArray(Utility.key.data)
                if (dataArray.length()>0){
                    val storeLocationItems: ArrayList<StoreLocationData> = ArrayList()
                    for (i in 0 until dataArray.length()){
                        val dataObj=dataArray.getJSONObject(i)
                        val item=StoreLocationData()
                        item.id=dataObj.getString(Utility.key.id)
                        item.store_id=dataObj.getString(Utility.key.store_id)
                        item.city_id=dataObj.getString(Utility.key.city_id)
                        item.city_name=dataObj.getString(Utility.key.city_name)
                        item.area=dataObj.getString(Utility.key.area)
                        item.address=dataObj.getString(Utility.key.address)
                        item.address1=dataObj.getString(Utility.key.address1)
                        item.postal_code=dataObj.getString(Utility.key.postal_code)
                        item.latitude=dataObj.getString(Utility.key.latitude)
                        item.longitude=dataObj.getString(Utility.key.longitude)
                        item.table_indoor=dataObj.getString(Utility.key.table_indoor)
                        item.table_outdoor=dataObj.getString(Utility.key.table_outdoor)
                        item.address_type=dataObj.getString(Utility.key.address_type)
                        item.is_default=dataObj.getBoolean(Utility.key.is_default)
                        item.locationObj=dataObj.toString()
                        storeLocationItems.add(item)
                    }
//                    recyclerAddLocList.setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    locAdapter = LocationViewAdapter(this,storeLocationItems,this)
                    recyclerAddLocList.layoutManager = layoutManager
                    recyclerAddLocList.adapter = locAdapter
                    recyclerAddLocList.showOrGone(true)
                }else{
                    recyclerAddLocList.showOrGone(false)
                }

            }
        }else if (type==Utility.storeDelete){
            if (isData){
                locAdapter.onTaskComplete(result,type,isData)
            }else{
                Utility.customErrorToast(this, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }
    }
}