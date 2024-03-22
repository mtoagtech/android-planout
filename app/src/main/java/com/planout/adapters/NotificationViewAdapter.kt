package com.planout.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.NotificationData
import org.json.JSONArray
import org.json.JSONObject


class NotificationViewAdapter(
    val activity: Activity?,
    val notificationItem: ArrayList<NotificationData>
) : RecyclerView.Adapter<NotificationViewAdapter.ViewHolder>() {

    var pos = 0
    var notify_type = ""
    var storeId = ""
    var reservationId = ""
    var eventId = ""
    var isReservation = false
    var isEvent = false
    lateinit var layoutManager: LinearLayoutManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_notification_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notificationItem[position]
        holder.txtTitle.text=item.title
        holder.txtSubTitle.text=item.message
        holder.txtTime.text=item.noti_date

        if (item.is_read == "1") {
            holder.notiImg.setImageDrawable(
                ContextCompat.getDrawable(activity!!, R.drawable.ic_all_noti_ring)
            )
            holder.txtTitle.setTextAppearance(activity, R.style.regular_helvetica_16dp)
        }else {
            holder.notiImg.setImageDrawable(
                ContextCompat.getDrawable(activity!!, R.drawable.ic_new_noti_ring)
            )
            holder.txtTitle.setTextAppearance(activity, R.style.bold_helvetica_16dp)
        }
        /*if (activity!!.localClassName == "activities.AllNotificationActivity"){
            holder.notiImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_all_noti_ring))
        }else{
            holder.notiImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_new_noti_ring))
        }*/

        holder.itemView.setOnClickListener {
            //action for read notification
            pos = position
            setIdNotify(item.action_type, item.reservation_id, item.event_id, item.store_id)
            clickNotificationRead(item.id)
        }

        if (position == notificationItem.size-1){
            holder.view.visibility = View.INVISIBLE
        }
    }

    private fun setIdNotify(
        actionType: String,
        resId: String,
        evenId: String,
        storId: String
    ) {
        isReservation = resId != "null" || resId.isNotEmpty()
        isEvent = evenId != "null" || evenId.isNotEmpty()
        notify_type = actionType
        when(actionType){
            "reservation.created" ->{
                if (isReservation){
                    reservationId = resId
                    eventId = ""
                    storeId = storId
                }
            }"reservation.confirmed" ->{
                if (isReservation){
                    reservationId = resId
                    eventId = ""
                    storeId = storId
                }
            }"reservation.declined" ->{
                if (isReservation){
                    reservationId = resId
                    eventId = ""
                    storeId = storId
                }
            }"reservation.cancelled" ->{
                if (isReservation){
                    reservationId = ""
                    eventId = ""
                    storeId = ""
                }
            }"event.created" ->{
                if (isEvent){
                    reservationId = ""
                    eventId = evenId
                    storeId = storId
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return notificationItem.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtSubTitle: TextView = itemView.findViewById(R.id.txtSubTitle)
        val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        val notiImg = itemView.findViewById<ImageView>(R.id.notiImg)
        val view = itemView.findViewById<View>(R.id.view)
    }

    fun clickNotificationRead(id: String) {
        val strNotificationIds :ArrayList<Int> = ArrayList()
        /*for (i in 0 until notificationItem.size){
            strNotificationIds.add(notificationItem[i].id.toInt())
        }*/
        strNotificationIds.add(id.toInt())
        //call api for read notification
        readNotificationApi(strNotificationIds)
    }

    private fun readNotificationApi(strNotificationIds: ArrayList<Int>) {
        val mBuilder = JSONObject()
        mBuilder.put(Utility.key.notification_ids, JSONArray(strNotificationIds))
        CallApi.callAPiJson(mBuilder, ApiController.api.markasread, activity!!, Utility.markasread, true, Utility.POST, true)
    }
}