package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.EventDetailsActivity
import com.planout.constant.Utility
import com.planout.models.UpcomingEventData

class MyUpcomingAdapter(
    val requireActivity: Activity,
    val upcoming_eventsList: ArrayList<UpcomingEventData>
) :
    RecyclerView.Adapter<MyUpcomingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_upcoming_item_view, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=upcoming_eventsList[position]
        Utility.SetImageSimple(requireActivity,item.event_image,holder.eventImage)
        Utility.SetImageSimple(requireActivity,item.store_image,holder.storeImage)
        holder.txtTitle.text=item.event_title
        holder.txtSubTitle.text=item.description
        holder.txtAddress.text=item.location.getString("address")
        val apistarttime=item.starttime
        val apiendtime=item.endtime
        val apievent_date=item.event_date

        val event_date=Utility.formatdateevent(apievent_date)
        val starttime=Utility.formattimeevent(apistarttime)
        val endtime=Utility.formattimeevent(apiendtime)


        holder.txtDateTime.text= "$event_date, $starttime - $endtime"

        holder.itemView.setOnClickListener {
            val ev_date= Utility.formatdateevent(apievent_date)
            val sttime= Utility.formattimeevent(apistarttime)
            val entime= Utility.formattimeevent(apiendtime)
            requireActivity.startActivity(
                Intent(requireActivity, EventDetailsActivity::class.java)
                .putExtra("eventImg", item.event_image)
                .putExtra("storeImg", item.store_image)
                .putExtra("eventName", item.event_title)
                .putExtra("eventDateTime", "$ev_date, $sttime - $entime")
                .putExtra("eventAddress", item.location.getString("address"))
                .putExtra("eventDesc", item.description))
        }
    }

    override fun getItemCount() = upcoming_eventsList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventImage:ImageView=itemView.findViewById(R.id.eventImage)
        val storeImage:ImageView=itemView.findViewById(R.id.storeImage)
        val txtTitle:TextView=itemView.findViewById(R.id.txtTitle)
        val txtSubTitle:TextView=itemView.findViewById(R.id.txtSubTitle)
        val txtAddress:TextView=itemView.findViewById(R.id.txtAddress)
        val txtDateTime:TextView=itemView.findViewById(R.id.txtDateTime)
    }

}