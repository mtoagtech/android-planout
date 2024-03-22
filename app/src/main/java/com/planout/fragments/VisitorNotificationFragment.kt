package com.planout.fragments

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.*
import com.planout.adapters.NotificationViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.NotificationData
import kotlinx.android.synthetic.main.fragment_visitor_notification.view.*
import okhttp3.FormBody
import org.json.JSONObject
import java.util.ArrayList

class VisitorNotificationFragment(val activityBase: HomeVisitorActivity) : Fragment(), ApiResponse {

    lateinit var rootView: View
    lateinit var notificationViewAdapter: NotificationViewAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    val notificationItem: ArrayList<NotificationData> = ArrayList()
    private var pageCount = 1
    private var lstCurrent = 0
    private var totalCount = 0
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        rootView = inflater.inflate(R.layout.fragment_visitor_notification, container, false)
        val nightModeFlags = activityBase.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                activityBase.window.decorView.systemUiVisibility = View.VISIBLE
                activityBase.window.statusBarColor =
                    ContextCompat.getColor(activityBase, R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                activityBase.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
                activityBase.window.statusBarColor = ContextCompat.getColor(
                    activityBase,
                    R.color.white
                );// set status background white
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                activityBase.window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
                activityBase.window.statusBarColor = ContextCompat.getColor(
                    activityBase,
                    R.color.white
                );// set status background white
            }
        }
        clickView()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (Utility.isLoginCheck(activityBase)) {
            //call api for notification listing
            notificationApi()
        } else {
            rootView.linNoMore.showOrGone(true)
            rootView.contCount.showOrGone(false)
            rootView.recyclerNewNotify.showOrGone(false)
            rootView.txtViewAll.showOrGone(true)
        }
    }

    private fun notificationApi() {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.page, pageCount.toString())
        CallApi.callAPi(
            mBuilder,
            ApiController.api.notifications,
            activityBase,
            Utility.notifications,
            true,
            Utility.GET,
            true
        )
    }

    private fun clickView() {
        rootView.txtViewAll.setOnClickListener {
            if (Utility.isLoginCheck(activityBase)) {
                startActivity(Intent(activityBase, AllNotificationActivity::class.java))
            }
        }
        rootView.txtMarkViewAll.setOnClickListener {
            /*val dialog = Dialog(activityBase)
            dialog.setContentView(R.layout.logout_popup_view)

            val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
            val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
            val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
            val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
            txtTitle.text = "Confirmation!!"
            txtSubTitle.text = "Are you sure you want to\nread all notifications?"
            txtCancel.setOnClickListener { dialog.dismiss() }
            txtDelete.text = "Confirm"
            txtDelete.setOnClickListener {
                dialog.dismiss()
                readMarkAllNotificationApi()
            }
            dialog.show()*/
            readMarkAllNotificationApi()
        }
    }

    private fun readMarkAllNotificationApi() {
        val mBuilder = JSONObject()
        CallApi.callAPiJson(
            mBuilder,
            ApiController.api.notifications_markallread,
            activityBase,
            Utility.notifications_markallread,
            true,
            Utility.POST,
            true
        )

    }


    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.notifications) {
            try {
                if (isData) {
                    notificationItem.clear()
                    val data = result.getJSONObject(Utility.key.data)
                    val total_unread = data.getInt(Utility.key.total_unread)
                    val total = data.getInt(Utility.key.total)
                    if (total <= 0) {
                        rootView.linNoMore.showOrGone(true)
                        rootView.contCount.showOrGone(false)
                        rootView.recyclerNewNotify.showOrGone(false)
                        rootView.txtViewAll.showOrGone(false)

                    } else {
                        rootView.linNoMore.showOrGone(false)
                        rootView.recyclerNewNotify.showOrGone(true)
                        rootView.txtViewAll.showOrGone(true)

                        if (total_unread <= 0) {
                            rootView.contCount.showOrGone(false)

                        } else {
                            rootView.contCount.showOrGone(true)
                            rootView.txtNotiCount.text = "$total_unread ${getString(R.string.new_notifications)}"
                        }
                    }
                    val records = data.getJSONArray(Utility.key.records)
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
                        /*if (recordsObj.getString(Utility.key.is_read) == "null" || recordsObj.getString(
                                Utility.key.is_read
                            ) == "0"
                        ) {
                            notificationItem.add(item)
                        }*/
                        notificationItem.add(item)
                    }
//                    rootView.recyclerNewNotify.setHasFixedSize(true)
                    notificationViewAdapter =
                        NotificationViewAdapter(activityBase, notificationItem)
                    linearLayoutManager =
                        LinearLayoutManager(activityBase, LinearLayoutManager.VERTICAL, false)
                    rootView.recyclerNewNotify.layoutManager = linearLayoutManager
                    rootView.recyclerNewNotify.adapter = notificationViewAdapter
                    if (notificationItem.size == 0) {
                        rootView.linNoMore.showOrGone(true)
                        rootView.contCount.showOrGone(false)
                        rootView.recyclerNewNotify.showOrGone(false)
                        rootView.txtViewAll.showOrGone(true)
                    }
                } else {
                    rootView.linNoMore.showOrGone(true)
                    rootView.contCount.showOrGone(false)
                    rootView.recyclerNewNotify.showOrGone(false)
                    rootView.txtViewAll.showOrGone(false)
                }
                Utility.hide_progress(activityBase)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else if (type == Utility.markasread) {
            if (isData) {
                val type = Utility.getForm(activityBase, Utility.key.user_type)
                if (notificationViewAdapter.notify_type == "reservation.created") { //identify for reservation created
                    if (type == "202") { //followed by company
                        //homeCompanyActivity.setHomeBottomView()
                    }
                } else if (notificationViewAdapter.notify_type == "reservation.confirmed") { //identify for reservation confirmed
                    if (type == "201") { //followed by visitor
                        startActivity(
                            Intent(activityBase, ReserveTableStatusActivity::class.java)
                                .putExtra(Utility.key.id, notificationViewAdapter.reservationId)
                        )
                    }
                } else if (notificationViewAdapter.notify_type == "reservation.declined") { //identify for reservation declined
                    if (type == "201") { //followed by visitor
                        startActivity(
                            Intent(activityBase, ReserveTableStatusActivity::class.java)
                                .putExtra(Utility.key.id, notificationViewAdapter.reservationId)
                        )
                    }
                } else if (notificationViewAdapter.notify_type == "event.created") { //identify for event created
                    if (type == "201") { //followed by visitor
                        startActivity(
                            Intent(activityBase, BusinessDetailsActivity::class.java)
                                .putExtra(Utility.key.id, notificationViewAdapter.storeId)
                        )
                    }
                } else {
                    //Utility.customSuccessToast(activityBase,result.getString(Utility.key.message))
                    for (i in 0 until notificationItem.size) {
                        if (i == notificationViewAdapter.pos) {
                            notificationItem[i].is_read = "1"
                            notificationViewAdapter.notifyItemChanged(i)
                        }
                    }
                }
            } else {
                //Utility.customErrorToast(activityBase,result.getString(Utility.key.message))
            }
        } else if (type == Utility.notifications_markallread) {
            if (isData) {
                Utility.customSuccessToast(activityBase, result.getString(Utility.key.message))
                //call api for notification listing
                notificationApi()
            } else {
                Utility.customErrorToast(activityBase, result.getString(Utility.key.message))

            }
        }

    }


}