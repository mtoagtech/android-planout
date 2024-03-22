package com.planout.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.HomeCompanyActivity
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.fragments.CompanyEventFragment
import com.planout.models.DayDateData
import com.planout.models.TimeData
import com.planout.models.TimingData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DateMonthViewAdapter2(
    val activity: HomeCompanyActivity?,
    val items: ArrayList<DayDateData>,
    val timings: java.util.ArrayList<TimingData>,
    val txtStartTime: TextView,
    val txtEndTime: TextView,
    val isEditE: Boolean,
    val eventStartTime: String,
    val eventEndTime: String
) : RecyclerView.Adapter<DateMonthViewAdapter2.ViewHolder>() {
    val times = ArrayList<TimeData>()
    var selectedDate = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_row_date_month_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listdata = items[position]
        if (listdata.getClicked()){
            selectedDate = items[position].getFull_Date()
            holder.item_blue.background = ContextCompat.getDrawable(activity!!, R.drawable.bg_tags_selected_drawable)
            holder.item_blue.backgroundTintList  = ContextCompat.getColorStateList(activity, R.color.app_green)
            holder.day_blue.setTextColor(ContextCompat.getColor(activity, R.color.app_green))
            holder.date_blue.setTextColor(ContextCompat.getColor(activity, R.color.app_green))
        }else{
            holder.item_blue.background = ContextCompat.getDrawable(activity!!, R.drawable.bg_round_edit_drawable)
            holder.item_blue.backgroundTintList  = ContextCompat.getColorStateList(activity, R.color.gray_F7_33)
            holder.day_blue.setTextColor(ContextCompat.getColor(activity, R.color.gray_5B_FF))
            holder.date_blue.setTextColor(ContextCompat.getColor(activity, R.color.gray_5B_FF))
        }

        holder.date_blue.text = listdata.getDate()
        if(listdata.getDay() == "Mon"){
            holder.day_blue.text = activity.getString(R.string.calendar_monday)
        }
        else if (listdata.getDay() == "Tue"){
            holder.day_blue.text = activity.getString(R.string.calendar_tuesday)
        }
        else if (listdata.getDay() == "Wed"){
            holder.day_blue.text = activity.getString(R.string.calendar_wednesday)
        }
        else if (listdata.getDay() == "Thu"){
            holder.day_blue.text = activity.getString(R.string.calendar_thursday)
        }
        else if (listdata.getDay() == "Fri"){
            holder.day_blue.text = activity.getString(R.string.calendar_friday)
        }
        else if (listdata.getDay() == "Sat"){
            holder.day_blue.text = activity.getString(R.string.calendar_saturday)
        }
        else if (listdata.getDay() == "Sun"){
            holder.day_blue.text = activity.getString(R.string.calendar_sunday)
        }



        holder.item_blue.setOnClickListener {
            for (i in 0 until items.size) {
                if (i == position) {
                    items[i].setClicked(true)
                    selectedDate = items[position].getFull_Date()
                    if (isEditE){
                        if (Utility.current_date(Utility.api_date_format)==selectedDate){
                            txtStartTime.text = "00:00"
                            txtEndTime.text = "00:00"
                        }else{
                            txtStartTime.text = activity.eventStartTime
                            txtEndTime.text = activity.eventEndTime
                        }
                    }
                } else {
                    items[i].setClicked(false)
                }
            }
            notifyDataSetChanged()
        }

//        for (i in 0 until timings.size){
//            showDayOrNot(holder.itemView,listdata.getDay())
//        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_blue = itemView.findViewById<LinearLayout>(R.id.item_blue)
        val day_blue = itemView.findViewById<TextView>(R.id.day_blue)
        val date_blue = itemView.findViewById<TextView>(R.id.date_blue)
    }
}