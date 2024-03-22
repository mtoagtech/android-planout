package com.planout.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.models.TimeData
import java.util.*
import kotlin.collections.ArrayList

class DayTimeViewAdapter(
    val activity: Activity?,
    val times: ArrayList<TimeData>
) : RecyclerView.Adapter<DayTimeViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_day_time_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDate = Date()
        val isafter: Boolean = times[position].fullDate.after(currentDate)


        if (isafter){
            if (times[position].isSelected){
                holder.item_blue.background = ContextCompat.getDrawable(activity!!, R.drawable.bg_tags_selected_drawable)
                holder.item_blue.backgroundTintList  = ContextCompat.getColorStateList(activity, R.color.app_green)
                holder.day_blue.setTextColor(ContextCompat.getColor(activity, R.color.app_green))
            }else{
                holder.item_blue.background = ContextCompat.getDrawable(activity!!, R.drawable.bg_round_edit_drawable)
                holder.item_blue.backgroundTintList  = ContextCompat.getColorStateList(activity, R.color.gray_F7_33)
                holder.day_blue.setTextColor(ContextCompat.getColor(activity, R.color.gray_5B_FF))
            }
        }else{
            if (times[position].isSelected){
                times[position].isSelected = false
            }
            holder.item_blue.background = ContextCompat.getDrawable(activity!!, R.drawable.bg_round_edit_drawable_close)
            holder.day_blue.setTextColor(ContextCompat.getColor(activity, R.color.gray_96_96))
        }


        holder.day_blue.text=times[position].time.replace("am", "AM").replace("pm", "PM")

        holder.itemView.setOnClickListener {
            if (isafter){
                for (i in 0 until times.size){
                    times[i].isSelected = i == position
                }
                notifyDataSetChanged()
            }

        }
    }

    override fun getItemCount(): Int {
        return times.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_blue = itemView.findViewById<LinearLayout>(R.id.item_blue)
        val day_blue = itemView.findViewById<TextView>(R.id.day_blue)

    }
}