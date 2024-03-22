package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.EventDetailsActivity
import com.planout.activities.FavoritesActivity
import com.planout.activities.SavedSearchActivity
import com.planout.constant.Utility
import com.planout.models.UpcomingEventData

class UpcomingEventViewAdapter(
    val activity: Activity?,
    val events: ArrayList<UpcomingEventData>
    ) : RecyclerView.Adapter<UpcomingEventViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_upcoming_events_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=events[position]
        Utility.SetImageSimple(activity!!,item.event_image,holder.imgRes)
        holder.txtTitle.text=item.event_title
        holder.txtAddress.text=item.location.getString("address")
        val apistarttime=item.starttime
        val apiendtime=item.endtime
        val apievent_date=item.event_date

        val event_date= Utility.formatdateevent(apievent_date)
        val starttime= Utility.formattimeevent(apistarttime)
        val endtime= Utility.formattimeevent(apiendtime)


        holder.txDateTime.text= "$event_date, $starttime - $endtime"

        if (position == events.size-1){
            holder.view1.visibility = View.INVISIBLE
        }

        holder.itemView.setOnClickListener {
            val ev_date= Utility.formatdateevent(apievent_date)
            val sttime= Utility.formattimeevent(apistarttime)
            val entime= Utility.formattimeevent(apiendtime)
            activity.startActivity(Intent(activity, EventDetailsActivity::class.java)
                .putExtra("eventImg", item.event_image)
                .putExtra("storeImg", item.store_image)
                .putExtra("eventName", item.event_title)
                .putExtra("eventDateTime", "$ev_date, $sttime - $entime")
                .putExtra("eventAddress", item.location.getString("address"))
                .putExtra("eventDesc", item.description))
        }
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val view1 = itemView.findViewById<View>(R.id.view1)
        val imgRes = itemView.findViewById<ImageView>(R.id.imgRes)
        val txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)
        val txtAddress = itemView.findViewById<TextView>(R.id.txtAddress)
        val txDateTime = itemView.findViewById<TextView>(R.id.txDateTime)
    }
}