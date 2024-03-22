package com.planout.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.NotificationViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.NotificationData
import kotlinx.android.synthetic.main.activity_all_notification.*
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject
import java.util.ArrayList

class AllNotificationActivity : AppCompatActivity(), ApiResponse {

    lateinit var notificationViewAdapter: NotificationViewAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var homeCompanyActivity: HomeCompanyActivity
    val notificationItem: ArrayList<NotificationData> = ArrayList()
    private var pageCount = 1
    private var lstCurrent = 0
    private var totalCount = 0
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_notification)

        homeCompanyActivity = HomeCompanyActivity()
        txtHeader.text = getString(R.string.all_notifications)
        //Call api for notification listing
        notificationApi()
        clickView()

    }

    private fun clickView() {
        imgBackHeader.setOnClickListener { onBackPressed() }
    }

    private fun notificationApi() {
        val mBuilder = FormBody.Builder()
        //mBuilder.add(Utility.key.page, pageCount.toString())
        val API = ApiController.api.notifications + "?${Utility.key.page}=${pageCount}"
        CallApi.callAPi(mBuilder, API, this, Utility.notifications, pageCount==1, Utility.GET, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.notifications) {
            if (isData) {
                val data = result.getJSONObject(Utility.key.data)
                val total = data.getInt(Utility.key.total)
                pageCount = Utility.checkStringNullOrNot(data, Utility.key.current_page).toInt()
                if(pageCount == 1){
                    notificationItem.clear()
                }
                if (total <= 0) {
                    linNoMore.showOrGone(true)
                    recyclerAllNotify.showOrGone(false)

                } else {
                    linNoMore.showOrGone(false)
                    recyclerAllNotify.showOrGone(true)
                    val records = data.getJSONArray(Utility.key.records)
                    totalCount = records.length()
                    isLoading = records.length()==20
                    for (i in 0 until records.length()) {
                        val recordsObj = records.getJSONObject(i)
                        val item = NotificationData()
                        item.id = recordsObj.getString(Utility.key.id)
                        item.title = recordsObj.getString(Utility.key.title)
                        item.message = recordsObj.getString(Utility.key.message)
                        item.action_type = recordsObj.getString(Utility.key.action_type)
                        item.store_id = recordsObj.getString(Utility.key.store_id)
                        item.reservation_id = recordsObj.getString(Utility.key.reservation_id)
                        item.event_id = recordsObj.getString(Utility.key.event_id)
                        item.is_read = recordsObj.getString(Utility.key.is_read)
                        item.noti_date = recordsObj.getString(Utility.key.noti_date)
                        notificationItem.add(item)
                        /*if (recordsObj.getString(Utility.key.is_read)=="1"){
                        notificationItem.add(item)
                    }*/
                    }
                    if (pageCount == 1) {
//                        recyclerAllNotify.setHasFixedSize(true)
                        notificationViewAdapter = NotificationViewAdapter(this, notificationItem)
                        linearLayoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        recyclerAllNotify.layoutManager = linearLayoutManager
                        recyclerAllNotify.adapter = notificationViewAdapter
                    }else{
                        loadView.showOrGone(false)
                        notificationViewAdapter.notifyDataSetChanged()
                    }
                    recyclerAllNotify.setOnScrollChangeListener { view, i, i2, i3, i4 ->
                        val total = totalCount
                        val currentLastItem: Int = linearLayoutManager.findLastVisibleItemPosition()
                        if (currentLastItem == total - 1 && isLoading) {
                            lstCurrent = currentLastItem
                            loadView.showOrGone(true)
                            pageCount += 1
                            notificationApi()
                        }
                    }
                }
            } else {
                linNoMore.showOrGone(true)
                recyclerAllNotify.showOrGone(false)
            }
        }else if(type == Utility.markasread){
            if (isData) {
                val type = Utility.getForm(this, Utility.key.user_type)
                if (notificationViewAdapter.notify_type == "reservation.created"){ //identify for reservation created
                    if (type == "202"){ //followed by company
                        startActivity(Intent(this, HomeCompanyActivity::class.java)
                            .putExtra(Utility.key.isFrom, "reservation.created"))
                        finishAffinity()
                    }
                }else if (notificationViewAdapter.notify_type == "reservation.confirmed"){ //identify for reservation confirmed
                    if (type == "201"){ //followed by visitor
                        startActivity(Intent(this, ReserveTableStatusActivity::class.java)
                            .putExtra(Utility.key.id, notificationViewAdapter.reservationId))
                    }
                }else if (notificationViewAdapter.notify_type == "reservation.declined"){ //identify for new reservation declined
                    if (type == "201"){ //followed by visitor
                        startActivity(Intent(this, ReserveTableStatusActivity::class.java)
                            .putExtra(Utility.key.id, notificationViewAdapter.reservationId))
                    }
                }else if (notificationViewAdapter.notify_type == "event.created"){ //identify for new event created
                    if (type == "201"){ //followed by visitor
                        startActivity(Intent(this, BusinessDetailsActivity::class.java)
                            .putExtra(Utility.key.id, notificationViewAdapter.storeId))
                    }
                }else{
                    for (i in 0 until notificationItem.size){
                        if (i == notificationViewAdapter.pos){
                            notificationItem[i].is_read = "1"
                            notificationViewAdapter.notifyItemChanged(i)
                        }
                    }
                }
            }else{
                //Utility.customErrorToast(this,result.getString(Utility.key.message))
            }
        }

    }

}