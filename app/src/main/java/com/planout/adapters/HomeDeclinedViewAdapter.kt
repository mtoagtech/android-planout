package com.planout.adapters

import android.app.Activity
import android.text.Html
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.FavoritesActivity
import com.planout.activities.SavedSearchActivity
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.ReservationData

class HomeDeclinedViewAdapter(
    val activity: Activity?,
    val resverationItems: ArrayList<ReservationData>
    ) : RecyclerView.Adapter<HomeDeclinedViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_company_declined_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=resverationItems[position]
        holder.txtTime.text= Utility.formatdatetime(item.restime, Utility.api_time, Utility.time_format)
        holder.txtDate.text= Utility.formatdatetime(item.resdate, Utility.api_date_format,"dd")
        holder.txtDay.text= Utility.formatdatetime(item.resdate, Utility.api_date_format,"EEE")
        holder.txtMonth.text= Utility.formatdatetime(item.resdate, Utility.api_date_format,"MMM")
        holder.txtName.text=Utility.toTitleCase(item.contact_name)
        holder.txtReservNo.text="${activity!!.getString(R.string.reservation_no_small)} "+item.res_id
        holder.txtPeople.text=item.total_people+" ${activity!!.getString(R.string.people)}"
        when (item.preferred_table) {
            "1" -> {
                holder.txtType.showOrGone(true)
                holder.txtType.text=activity!!.getString(R.string.indoor)
            }
            "2" -> {
                holder.txtType.showOrGone(true)
                holder.txtType.text=activity!!.getString(R.string.outdoor)
            }
            else -> {
                holder.txtType.showOrGone(false)
            }
        }
        if (item.extra_notes.isNotEmpty() && item.extra_notes!="null") {
            /*val ss = SpannableString(Html.fromHtml(activity!!.getString(R.string.notes)))
            ss.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(activity, R.color.gray_FF_14)),
                0,
                12,
                0
            )*/
            holder.txtExtraNotes.text = "                         ${item.extra_notes}"
            holder.txtExtraTitle.showOrGone(true)
            holder.txtExtraNotes.showOrGone(true)
            holder.view5.showOrGone(true)
        }
        if (item.location_id=="null"){
            holder.txtAddress.text=activity!!.getString(R.string.not_available)
        }else{
            val address=item.location.getString("address")
            holder.txtAddress.text=address
        }
        holder.txtStatus.text=item.remark

    }

    override fun getItemCount(): Int {
        return resverationItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDay = itemView.findViewById<TextView>(R.id.txtDay)
        val txtDate = itemView.findViewById<TextView>(R.id.txtDate)
        val txtMonth = itemView.findViewById<TextView>(R.id.txtMonth)
        val txtName = itemView.findViewById<TextView>(R.id.txtName)
        val txtReservNo = itemView.findViewById<TextView>(R.id.txtReservNo)
        val txtTime = itemView.findViewById<TextView>(R.id.txtTime)
        val txtPeople = itemView.findViewById<TextView>(R.id.txtPeople)
        val txtType = itemView.findViewById<TextView>(R.id.txtType)
        val txtAddress = itemView.findViewById<TextView>(R.id.txtAddress)
        val txtStatus = itemView.findViewById<TextView>(R.id.txtStatus)
        val txtExtraTitle = itemView.findViewById<TextView>(R.id.txtExtraTitle)
        val txtExtraNotes = itemView.findViewById<TextView>(R.id.txtExtraNotes)
        val view5 = itemView.findViewById<View>(R.id.view5)
    }
}