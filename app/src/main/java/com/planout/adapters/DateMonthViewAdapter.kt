package com.planout.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.DayDateData
import com.planout.models.TimeData
import com.planout.models.TimingData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

class DateMonthViewAdapter(
    val activity: Activity?,
    val items: ArrayList<DayDateData>,
    val timings: java.util.ArrayList<TimingData>,
    val recyclerTimeList: RecyclerView,
    val txtTime: TextView,
    val times: ArrayList<TimeData>,
) : RecyclerView.Adapter<DateMonthViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_date_month_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listdata = items[position]
        if (listdata.getClicked()){
            showDayOrNot(listdata.getDay(),listdata.getFull_Date(), listdata.getFull_Date()==Utility.current_date(Utility.api_date_format))

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


        if (!listdata.getisAvailable()){
            holder.closeView.showOrGone(true)
        }else{
            holder.closeView.showOrGone(false)
        }

        holder.item_blue.setOnClickListener {
            if (listdata.getisAvailable()){
                for (i in 0 until items.size) {
                    if (i == position) {
                        items[i].setClicked(true)
                    } else {
                        items[i].setClicked(false)
                    }
                }
                notifyDataSetChanged()
            }

        }

//        for (i in 0 until timings.size){
//            showDayOrNot(holder.itemView,listdata.getDay())
//        }
    }

    private fun showDayOrNot(day: String, fullDate: String, isToday: Boolean) {
        Log.d("FullDateIs",fullDate);

// now do something with the cal
        var pos=0
        if (day=="Mon"){
            pos=0
        }else if (day=="Tue"){
            pos=1
        }else if (day=="Wed"){
            pos=2
        }else if (day=="Thu"){
            pos=3
        }else if (day=="Fri"){
            pos=4
        }else if (day=="Sat"){
            pos=5
        }else if (day=="Sun"){
            pos=6
        }

        val starttime=timings[pos].starttime
        val endtime=timings[pos].endtime
        val endtime1=timings[pos].endtime1
        val starttime1=timings[pos].starttime1

            val sdf1: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH)
        val sdf = SimpleDateFormat(Utility.time_format,Locale.ENGLISH)
        var scrollPos = 0

        try {
            val starttimedateCheck: Date = sdf1.parse("$fullDate $starttime")!!
            var endtimedate: Date = sdf1.parse("$fullDate $endtime")!!
            Log.d("Full date time","Full date time :- "+starttimedateCheck)
            Log.d("Full date time","Full date time :- "+endtimedate)
            times.clear()
            if (starttimedateCheck.after(endtimedate)){
                val starttimedate: Date = sdf1.parse("$fullDate $starttime")!!
                val gc = GregorianCalendar(fullDate.split("-")[0].toInt(),fullDate.split("-")[1].toInt()-1,fullDate.split("-")[2].toInt())
                gc.add(Calendar.DATE, 1)
                val date = SimpleDateFormat(Utility.api_date_format,Locale.ENGLISH).format(gc.time)
                endtimedate = sdf1.parse("$date $endtime")!!
                val calendar: Calendar = Calendar.getInstance()
                calendar.time = starttimedate
                val itemdata = TimeData()
                itemdata.time = sdf.format(calendar.time)
                itemdata.fullDate = calendar.time
                itemdata.isSelected = true
                times.add(itemdata)

                while (calendar.time.before(endtimedate)) {
                    calendar.add(Calendar.MINUTE, 30)
                    val itemdataloop=TimeData()
                    itemdataloop.time=sdf.format(calendar.time)
                    itemdataloop.fullDate=calendar.time
                    itemdataloop.isSelected=false
                    times.add(itemdataloop)
                }
            }else{
                val starttimedate: Date = sdf1.parse("$fullDate $starttime")!!

                endtimedate = sdf1.parse("$fullDate $endtime")!!
                val calendar: Calendar = Calendar.getInstance()
                calendar.time = starttimedate
                val itemdata = TimeData()
                itemdata.time = sdf.format(calendar.time)
                itemdata.fullDate = calendar.time
                itemdata.isSelected = true
                times.add(itemdata)

                while (calendar.time.before(endtimedate)) {
                    calendar.add(Calendar.MINUTE, 30)
                    val itemdataloop=TimeData()
                    itemdataloop.time=sdf.format(calendar.time)
                    itemdataloop.fullDate=calendar.time
                    itemdataloop.isSelected=false
                    times.add(itemdataloop)
                }
            }




            if (starttime1=="null" || endtime1=="null"){
                Log.d("Response","Second Shift is closed")
            }else{
                val starttime1date: Date = sdf1.parse("$fullDate $starttime1")!!
                var endtime1date: Date = sdf1.parse("$fullDate $endtime1")!!

                if (starttime1date.after(endtime1date)){
                    val gc = GregorianCalendar(fullDate.split("-")[0].toInt(),fullDate.split("-")[1].toInt()-1,fullDate.split("-")[2].toInt())
                    gc.add(Calendar.DATE, 1)
                    val date = SimpleDateFormat(Utility.api_date_format,Locale.ENGLISH).format(gc.time)
                    endtime1date = sdf1.parse("$date $endtime1")!!
                    Log.d("NewDate",date)

                }else{
                    endtime1date = sdf1.parse("$fullDate $endtime1")!!
                }

                val calendar1: Calendar = Calendar.getInstance()
                calendar1.time = starttime1date
                val itemdata2=TimeData()
                itemdata2.time=sdf.format(calendar1.time)
                itemdata2.fullDate=calendar1.time
                itemdata2.isSelected=false
                times.add(itemdata2)

                while (calendar1.time.before(endtime1date)) {
                    calendar1.add(Calendar.MINUTE, 30)
                    val itemdataloop=TimeData()
                    itemdataloop.time=sdf.format(calendar1.time)
                    itemdataloop.fullDate=calendar1.time
                    itemdataloop.isSelected=false
                    times.add(itemdataloop)
                }
            }





//            recyclerTimeList.setHasFixedSize(true)
            val layoutManager2 = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            recyclerTimeList.layoutManager = layoutManager2
            val dayTimeAdapter = DayTimeViewAdapter(activity,times)
            recyclerTimeList.adapter = dayTimeAdapter
            if (isToday){
                val currentDate = Date()
                for (i in 0 until times.size){
                    if (times[i].fullDate.after(currentDate)){
                        recyclerTimeList.scrollToPosition(i)
                        times[i].isSelected = true
                        notifyItemChanged(i)
                        return
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_blue = itemView.findViewById<LinearLayout>(R.id.item_blue)
        val day_blue = itemView.findViewById<TextView>(R.id.day_blue)
        val date_blue = itemView.findViewById<TextView>(R.id.date_blue)
        val closeView = itemView.findViewById<ConstraintLayout>(R.id.closeView)
    }
}