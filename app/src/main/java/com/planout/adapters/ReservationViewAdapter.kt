package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.FavoritesActivity
import com.planout.activities.HomeVisitorActivity
import com.planout.activities.ReserveTableStatusActivity
import com.planout.constant.Utility
import com.planout.constant.Utility.RES_CANCELLED_STATUS
import com.planout.constant.Utility.RES_CONFIRMED_STATUS
import com.planout.constant.Utility.RES_DECLINED_STATUS
import com.planout.constant.Utility.RES_PENDING_STATUS
import com.planout.constant.Utility.showOrGone
import com.planout.fragments.ReservationUpcomingFragment
import com.planout.models.ReservationData

class ReservationViewAdapter(
    val activity: HomeVisitorActivity?,
    val resverationItems: ArrayList<ReservationData>,
    val type: String,
) : RecyclerView.Adapter<ReservationViewAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_reservation_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = resverationItems[position]
        Utility.SetImageSimple(activity!!,item.store_image,holder.imgRes)
        holder.txtTitle.text=item.store_name
        holder.txtPeople.text=item.total_people+" ${activity!!.getString(R.string.people)}"
        if (item.location_id=="null"){
            holder.txtAddress.text=activity!!.getString(R.string.not_available)
        }else{
            val address=item.location.getString("address")
            holder.txtAddress.text=address
        }

//        val dateTime=Utility.formatdateres(item.resdate)
        val timing = Utility.formatdatetime(item.restime,Utility.api_time,Utility.time_format)!!.replace("am", "AM").replace("pm", "PM")
        val dateTime=Utility.formatdatetime(item.resdate,Utility.api_date_format,Utility.date_format_with_day)+", "+timing

        holder.txDateTime.text=dateTime
        // 1-- indore   2-- outdoor
        when (item.preferred_table) {
            "1" -> {
                holder.txtType.text= activity.getString(R.string.indoor)
            }
            "2" -> {
                holder.txtType.text= activity.getString(R.string.outdoor)
            }
            else -> {
                holder.txtType.showOrGone(false)
            }
        }


        if (item.status== RES_PENDING_STATUS){
            holder.txtStatus.text="● ${activity.getString(R.string.confirmation_pending)}"
            holder.txtStatus.setTextColor(ContextCompat.getColor(activity, R.color.status_orange));
            holder.view4.showOrGone(false)
            holder.txtRemark.showOrGone(false)
        }else if (item.status==RES_CONFIRMED_STATUS){
            holder.txtStatus.text="● ${activity.getString(R.string.confirmed)}"
            holder.txtStatus.setTextColor(ContextCompat.getColor(activity, R.color.status_green));
            holder.view4.showOrGone(false)
            holder.txtRemark.showOrGone(false)
        }else if (item.status==RES_DECLINED_STATUS){
            holder.txtStatus.text="● ${activity.getString(R.string.declined)}"
            holder.txtStatus.setTextColor(ContextCompat.getColor(activity, R.color.status_red));
            holder.view4.showOrGone(true)
            holder.txtRemark.showOrGone(true)
            holder.txtRemark.text=item.remark
        }else if (item.status==RES_CANCELLED_STATUS){
            holder.txtStatus.text="● ${activity.getString(R.string.canceled)}"
            holder.txtStatus.setTextColor(ContextCompat.getColor(activity, R.color.status_red));
            holder.view4.showOrGone(false)
            holder.txtRemark.showOrGone(false)
        }
        holder.itemView.setOnClickListener {
            activity.startForResult.launch(Intent(activity, ReserveTableStatusActivity::class.java)
                .putExtra(Utility.key.id,item.id)
                .putExtra(Utility.key.itemposition,position)
                .putExtra(Utility.key.type,type))

        }

    }

    override fun getItemCount(): Int {
        return resverationItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgRes:ImageView=itemView.findViewById(R.id.imgRes)
        val txtTitle:TextView=itemView.findViewById(R.id.txtTitle)
        val txtAddress:TextView=itemView.findViewById(R.id.txtAddress)
        val txtPeople:TextView=itemView.findViewById(R.id.txtPeople)
        val txtType:TextView=itemView.findViewById(R.id.txtType)
        val txDateTime:TextView=itemView.findViewById(R.id.txDateTime)
        val txtStatus:TextView=itemView.findViewById(R.id.txtStatus)
        val view4:View=itemView.findViewById(R.id.view4)
        val txtRemark:TextView=itemView.findViewById(R.id.txtRemark)

    }
}