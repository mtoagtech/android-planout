package com.planout.activities

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.NotificationViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.NotificationData
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.fragment_visitor_notification.view.*
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class NotificationActivity : AppCompatActivity(), ApiResponse {

    val notificationItem: ArrayList<NotificationData> = ArrayList()
    private var pageCount = 1
    private var lstCurrent = 0
    private var totalCount = 0
    private var isLoading = false

    lateinit var notificationViewAdapter: NotificationViewAdapter
    lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        clickView()

    }

    override fun onResume() {
        super.onResume()
        //call api for notifications
        notificationApi()
    }

    private fun clickView() {
        imgBack.setOnClickListener { onBackPressed() }
        txtViewAll.setOnClickListener {
            startActivity(Intent(this, AllNotificationActivity::class.java))
        }
        txtMarkViewAll.setOnClickListener {
            /*val dialog = Dialog(this)
            dialog.setContentView(R.layout.logout_popup_view)

            val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
            val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
            val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
            val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
            txtTitle.text = "Confirmation!!"
            txtSubTitle.text = "Are you sure you want to\nread all notifications?"
            txtCancel.setOnClickListener { dialog.dismiss() }
            txtDelete.text="Confirm"
            txtDelete.setOnClickListener { dialog.dismiss()
                readMarkAllNotificationApi()

            }
            dialog.show()*/
            //call api for read mark all notifications
            readMarkAllNotificationApi()

        }
    }

    private fun readMarkAllNotificationApi() {
        val mBuilder = JSONObject()
        CallApi.callAPiJson(mBuilder, ApiController.api.notifications_markallread, this, Utility.notifications_markallread, true, Utility.POST, true)

    }

    private fun readNotificationApi(strNotificationIds: ArrayList<Int>) {
        val mBuilder = JSONObject()
        mBuilder.put(Utility.key.notification_ids, JSONArray(strNotificationIds))
        CallApi.callAPiJson(mBuilder, ApiController.api.markasread, this, Utility.markasread, true, Utility.POST, false)
    }

    private fun notificationApi() {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.page, pageCount.toString())
        CallApi.callAPi(mBuilder, ApiController.api.notifications, this, Utility.notifications, true, Utility.GET, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.notifications){
            if (isData){
                notificationItem.clear()
                val data=result.getJSONObject(Utility.key.data)
                val total_unread=data.getInt(Utility.key.total_unread)
                val total=data.getInt(Utility.key.total)
                if (total<=0){
                    linNoMore.showOrGone(true)
                    contCount.showOrGone(false)
                    recyclerNewNotify.showOrGone(false)
                    txtViewAll.showOrGone(false)

                }else{
                    linNoMore.showOrGone(false)
                    recyclerNewNotify.showOrGone(true)
                    txtViewAll.showOrGone(true)

                    if (total_unread<=0){
                        contCount.showOrGone(false)
                    }else{
                        contCount.showOrGone(true)
                        txtNotiCount.text= "$total_unread ${getString(R.string.new_notifications)}"
                    }
                }
                val records=data.getJSONArray(Utility.key.records)
                for (i in 0 until records.length()){
                    val recordsObj=records.getJSONObject(i)
                    val item= NotificationData()
                    item.id=recordsObj.getString(Utility.key.id)
                    item.title=recordsObj.getString(Utility.key.title)
                    item.message=recordsObj.getString(Utility.key.message)
                    item.action_type=recordsObj.getString(Utility.key.action_type)
                    item.store_id=recordsObj.getString(Utility.key.store_id)
                    item.reservation_id=recordsObj.getString(Utility.key.reservation_id)
                    item.event_id=recordsObj.getString(Utility.key.event_id)
                    item.is_read=recordsObj.getString(Utility.key.is_read)
                    item.noti_date=recordsObj.getString(Utility.key.noti_date)
                    /*if (recordsObj.getString(Utility.key.is_read)=="null" || recordsObj.getString(Utility.key.is_read)=="0"){
                        notificationItem.add(item)
                    }*/
                    notificationItem.add(item)
                }
//                recyclerNewNotify.setHasFixedSize(true)
                notificationViewAdapter = NotificationViewAdapter(this,notificationItem)
                linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                recyclerNewNotify.layoutManager = linearLayoutManager
                recyclerNewNotify.adapter = notificationViewAdapter

                if (notificationItem.size == 0){
                    linNoMore.showOrGone(true)
                    contCount.showOrGone(false)
                    recyclerNewNotify.showOrGone(false)
                    txtViewAll.showOrGone(true)
                }
            }else{
                linNoMore.showOrGone(true)
                contCount.showOrGone(false)
                recyclerNewNotify.showOrGone(false)
                txtViewAll.showOrGone(false)
            }

        }else if (type==Utility.markasread){
            if (isData){
                val type = Utility.getForm(this, Utility.key.user_type)
                if (notificationViewAdapter.notify_type == "reservation.created"){ //identify for reservation created
                    if (type == "202"){ //followed by company
                        startActivity(Intent(this, HomeCompanyActivity::class.java)
                            .putExtra(Utility.key.isFrom, "reservation.created")) //identify for reservation created
                        finishAffinity()
                    }
                }else if (notificationViewAdapter.notify_type == "reservation.confirmed"){ //identify for reservation confirmed
                    if (type == "201"){ //followed by visitor
                        startActivity(Intent(this, ReserveTableStatusActivity::class.java)
                            .putExtra(Utility.key.id, notificationViewAdapter.reservationId))
                    }
                }else if (notificationViewAdapter.notify_type == "reservation.declined"){ //identify for reservation declined
                    if (type == "201"){ //followed by visitor
                        startActivity(Intent(this, ReserveTableStatusActivity::class.java)
                            .putExtra(Utility.key.id, notificationViewAdapter.reservationId))
                    }
                }else if (notificationViewAdapter.notify_type == "event.created"){ //identify for event created
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
        }else if (type== Utility.notifications_markallread){
            if (isData){
                Utility.customSuccessToast(this,result.getString(Utility.key.message))
                //call api for notifications
                notificationApi()
            }else{
                Utility.customErrorToast(this,result.getString(Utility.key.message))
            }
        }
    }
}