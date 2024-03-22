package com.planout.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.CitySearchAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.models.CityModel
import kotlinx.android.synthetic.main.activity_city_list_screen.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject

class CityListScreen : AppCompatActivity(), ApiResponse {
    val list = ArrayList<CityModel>()
    lateinit var citySearchAdapter: CitySearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_list_screen)

        txtHeader.text=getString(R.string.select_city)
        imgBackHeader.setOnClickListener {
            onBackPressed()
        }
        edittext_search.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Call back the Adapter with current character to Filter
                if (list.size > 0) {
                    citySearchAdapter.filter!!.filter(s)
                    citySearchAdapter.notifyDataSetChanged()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {}
        })

        //call api for city listing
        cityListApi()

    }
    private fun cityListApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            ApiController.api.cities,
            this,
            Utility.cities,
            true,
            Utility.GET,
            true
        )
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.cities){
            if (isData){
                val dataJSONArray=result.getJSONArray(Utility.key.data)
                for (i in 0 until dataJSONArray.length()){
                    val dataJSONObject=dataJSONArray.getJSONObject(i)
                    val adventureSearchCityModel = CityModel()
                    adventureSearchCityModel.cityID = dataJSONObject.getString(Utility.key.id)
                    adventureSearchCityModel.cityName = dataJSONObject.getString(Utility.key.city_name)
                    list.add(adventureSearchCityModel)
                }
                val linearLayoutManager = LinearLayoutManager(this)
                recycler_city_search.layoutManager = linearLayoutManager
                citySearchAdapter = CitySearchAdapter(this, list,recycler_city_search,no_data,no_data_txt)
                recycler_city_search.adapter = citySearchAdapter
            }
        }
    }

}