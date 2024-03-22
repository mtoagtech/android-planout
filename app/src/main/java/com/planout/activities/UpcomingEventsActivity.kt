package com.planout.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.UpcomingEventViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.UpcomingEventData
import kotlinx.android.synthetic.main.activity_upcoming_events.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject

class UpcomingEventsActivity : AppCompatActivity(), ApiResponse {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var upcomingEventAdapter: UpcomingEventViewAdapter
    val upcoming_eventsList: ArrayList<UpcomingEventData> = ArrayList()

    var type="upcoming"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upcoming_events)

        txtHeader.text = getString(R.string.upcoming_events)
        imgBackHeader.setOnClickListener {
            onBackPressed()
        }
        viewData()
    }

    private fun viewData() {
        //call api for upcoming listing
        upcomingEventListApi(type)
    }

    private fun upcomingEventListApi(type: String) {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.events + "?${Utility.key.type}=${type}"
        CallApi.callAPi(mBuilder, API, this, Utility.eventsUpcoming, true, Utility.GET, true)

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.eventsUpcoming){
            if (isData){
                val dataObj=result.getJSONObject(Utility.key.data)
                val total=dataObj.getInt(Utility.key.total)
                if (total==0){
                    recyclerUpcomingEvent.showOrGone(false)
                    constNoData.showOrGone(true)
                }else{
                    recyclerUpcomingEvent.showOrGone(true)
                    constNoData.showOrGone(false)
                    val upcoming_eventsArray=dataObj.getJSONArray(Utility.key.records)
                    upcoming_eventsList.clear()
                    for (u in 0 until upcoming_eventsArray.length()){
                        val upcoming_eventsObj=upcoming_eventsArray.getJSONObject(u)
                        val upcomingEventData= UpcomingEventData()
                        upcomingEventData.id=upcoming_eventsObj.getString(Utility.key.id)
                        upcomingEventData.store_id=upcoming_eventsObj.getString(Utility.key.store_id)
                        upcomingEventData.location_id=upcoming_eventsObj.getString(Utility.key.location_id)
                        upcomingEventData.event_title=upcoming_eventsObj.getString(Utility.key.event_title)
                        upcomingEventData.description=upcoming_eventsObj.getString(Utility.key.description)
                        upcomingEventData.event_date=upcoming_eventsObj.getString(Utility.key.event_date)
                        upcomingEventData.starttime=upcoming_eventsObj.getString(Utility.key.starttime)
                        upcomingEventData.endtime=upcoming_eventsObj.getString(Utility.key.endtime)
                        upcomingEventData.event_image=upcoming_eventsObj.getString(Utility.key.event_image)
                        upcomingEventData.store_image=upcoming_eventsObj.getString(Utility.key.store_image)
                        upcomingEventData.location=upcoming_eventsObj.getJSONObject(Utility.key.location)
                        upcoming_eventsList.add(upcomingEventData)

                    }
//                    recyclerUpcomingEvent.setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    upcomingEventAdapter = UpcomingEventViewAdapter(this,upcoming_eventsList)
                    recyclerUpcomingEvent.layoutManager = layoutManager
                    recyclerUpcomingEvent.adapter = upcomingEventAdapter
                }
            }
        }
    }


}