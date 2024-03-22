package com.planout.fragments

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrewjapar.rangedatepicker.CalendarPicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.HomeCompanyActivity
import com.planout.adapters.EventsViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.UpcomingEventData
import kotlinx.android.synthetic.main.datepicker_bottom_final.view.*
import kotlinx.android.synthetic.main.fragment_event_past.view.*
import kotlinx.android.synthetic.main.fragment_event_past.view.linNoMore
import kotlinx.android.synthetic.main.fragment_event_past.view.loadView
import kotlinx.android.synthetic.main.fragment_event_past.view.txtDateRange
import okhttp3.FormBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EventPastFragment(val act: CompanyEventFragment, val activityBase: HomeCompanyActivity) : Fragment(), ApiResponse {

    lateinit var rootView: View
    lateinit var layoutManager: LinearLayoutManager
    lateinit var eventsViewAdapter: EventsViewAdapter
    val upcoming_eventsList: ArrayList<UpcomingEventData> = ArrayList()

    lateinit var handler: Handler
    var isClick = false
    var store_id=""
    var location_id=""
    var type="past"
    var startDate=""
    var endDate=""
    var pageCount = 1
    private var lstCurrent = 0
    private var totalCount = 0
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_event_past, container, false)
        store_id= Utility.getForm(activityBase, Utility.key.store_id)!!
        handler = Handler()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if(!act.isOpenAddEdit) {
            pageCount = 1
            startDate = ""
            endDate = ""
            //rootView.txtDateRange.text = Utility.first_date_month(Utility.date_format)+" - "+Utility.current_date(Utility.date_format)
            rootView.txtDateRange.text = getString(R.string.select_dates)
            //call api for past event listing
            eventListApi(store_id, location_id, type, startDate, endDate)
        }
        rootView.txtDateRange.setOnClickListener {
            if (!isClick) {
                //dialog for date range calender
                openRangePickerDialog()
            }
            clickForFew()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            handler.removeCallbacksAndMessages(null)
        }catch (e:Exception){e.printStackTrace()}
    }

    private fun clickForFew(){
        isClick = true
        handler.postDelayed(Runnable { isClick = false }, 1500)
    }

    private fun openRangePickerDialog() {
        if (startDate == ""){
            startDate = Utility.current_date(Utility.api_date_format)
            endDate = Utility.current_date(Utility.api_date_format)
        }
        val serviceInfoDialog = BottomSheetDialog(
            activityBase,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfoDialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(activityBase)
            .inflate(R.layout.datepicker_bottom_final, null)
        serviceInfoDialog.setContentView(dialogView)
        serviceInfoDialog.setCancelable(true)

        val from_dialog=dialogView.findViewById<TextView>(R.id.from_dialog)
        val to_dialog=dialogView.findViewById<TextView>(R.id.to_dialog)
        val txtClear=dialogView.findViewById<TextView>(R.id.txtClear)
        txtClear.setOnClickListener {
            pageCount = 1
            onResume()
            serviceInfoDialog.dismiss()}

        val startDate_range = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        val endDate_range = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        endDate_range[Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(
            Calendar.MONTH
        )]= Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        endDate_range.add(Calendar.MONTH, 3)
        val c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        c[2022, 0] = 1 //Year,Mounth -1,Day


        dialogView.calendar_view.apply {
            val sDate1 = startDate
            val sDate2 = endDate
            val date1 = SimpleDateFormat(Utility.api_date_format,Locale.ENGLISH).parse(sDate1)
            val date2 = SimpleDateFormat(Utility.api_date_format,Locale.ENGLISH).parse(sDate2)
            showDayOfWeekTitle(true) // If you want to disable day of the week title, just make it false
            setMode(CalendarPicker.SelectionMode.RANGE) // You can set it via XML
            setRangeDate(c.time, startDate_range.time)
            setSelectionDate(date1!!, date2!!)
            scrollToDate(date2)
        }

        dialogView.from_dialog.text = Utility.formatdatetime(startDate, Utility.api_date_format, Utility.date_format)
        dialogView.to_dialog.text = Utility.formatdatetime(endDate, Utility.api_date_format, Utility.date_format)
        dialogView.calendar_view.setOnRangeSelectedListener { startDate, endDate, startLabel, endLabel ->
            from_dialog.text = startLabel
            to_dialog.text = endLabel
        }

        dialogView.calendar_view.setOnStartSelectedListener { startDate, label ->
            from_dialog.text = label
            to_dialog.text = label
        }

        val apply_dates=dialogView.findViewById<Button>(R.id.apply_dates)
        val imgTop=dialogView.findViewById<ImageView>(R.id.imgTop)
        Utility.animationClick(apply_dates).setOnClickListener {
            if (from_dialog.text.toString() == "-" && to_dialog.text.toString() == "-" ){
                Utility.normal_toast(activityBase, activityBase.getString(R.string.select_date_range_first))
            }else if (from_dialog.text.toString() == "-" ){
                Utility.normal_toast(activityBase, activityBase.getString(R.string.select_from_date_first))
            }else if (to_dialog.text.toString() == "-" ){
                Utility.normal_toast(activityBase, activityBase.getString(R.string.select_to_date_first))
            }else {
                //set start date
                startDate = Utility.change_date_format(
                    from_dialog.text.toString(),
                    Utility.api_date_format
                )
                //set end date
                endDate = Utility.change_date_format(
                    to_dialog.text.toString(),
                    Utility.api_date_format
                )
                pageCount = 1
                rootView.txtDateRange.text = Utility.formatdatetime(startDate, Utility.api_date_format, Utility.date_format)+" - "+Utility.formatdatetime(endDate, Utility.api_date_format, Utility.date_format)
                eventListApi(store_id,location_id,type,startDate,endDate)
                serviceInfoDialog.dismiss()
            }
        }

        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfoDialog.dismiss()
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfoDialog.dismiss()
        }
        serviceInfoDialog.show()
    }

    private fun eventListApi(
        store_id: String,
        location_id: String,
        type: String,
        startDate: String,
        endDate: String
    ) {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.events + "?${Utility.key.store_id}=${store_id}&${Utility.key.location_id}=${location_id}&${Utility.key.type}=${type}&${Utility.key.page}=${pageCount}&${Utility.key.startdate}=${startDate}&${Utility.key.enddate}=${endDate}"
        CallApi.callAPi(mBuilder, API, activityBase, Utility.eventsPast, pageCount==1, Utility.GET, true)

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.eventsPast){
            if (isData){
                val dataObj=result.getJSONObject(Utility.key.data)
                val total=dataObj.getInt(Utility.key.total)
                val total_unread_notifications=dataObj.getInt(Utility.key.total_unread_notifications)
                act.setNotificationView(total_unread_notifications>0)
                val currentPage = dataObj.getInt(Utility.key.current_page)
                if (currentPage == 1) {
                    upcoming_eventsList.clear()
                }
                if (total==0){
                    rootView.recyclerEventPast.showOrGone(false)
                    rootView.linNoMore.showOrGone(true)
                }else{
                    rootView.recyclerEventPast.showOrGone(true)
                    rootView.linNoMore.showOrGone(false)
                    val upcoming_eventsArray=dataObj.getJSONArray(Utility.key.records)
                    totalCount = upcoming_eventsArray.length()
                    isLoading = upcoming_eventsArray.length() == 20
                    for (u in 0 until upcoming_eventsArray.length()){
                        val upcoming_eventsObj=upcoming_eventsArray.getJSONObject(u)
                        val upcomingEventData= UpcomingEventData()
                        upcomingEventData.id=upcoming_eventsObj.getString(Utility.key.id)
                        upcomingEventData.store_id=upcoming_eventsObj.getString(Utility.key.store_id)
                        upcomingEventData.location_id=upcoming_eventsObj.getString(Utility.key.location_id)
                        upcomingEventData.event_title=upcoming_eventsObj.getString(Utility.key.event_title)
                        upcomingEventData.description=upcoming_eventsObj.getString(Utility.key.description)
                        upcomingEventData.event_date=upcoming_eventsObj.getString(Utility.key.event_date)
                        upcomingEventData.starttime=upcoming_eventsObj.getString(Utility.key.starttime)
                        upcomingEventData.endtime=upcoming_eventsObj.getString(Utility.key.endtime)
                        upcomingEventData.event_image=upcoming_eventsObj.getString(Utility.key.event_image)
                        upcomingEventData.store_image=upcoming_eventsObj.getString(Utility.key.store_image)
                        upcomingEventData.location=upcoming_eventsObj.getJSONObject(Utility.key.location)
                        upcoming_eventsList.add(upcomingEventData)

                    }
                    if (pageCount == 1) {
//                        rootView.recyclerEventPast.setHasFixedSize(true)
                        layoutManager =
                            LinearLayoutManager(activityBase, LinearLayoutManager.VERTICAL, false)
                        eventsViewAdapter = EventsViewAdapter(
                            activityBase,
                            activityBase,
                            false,
                            upcoming_eventsList,
                            act
                        )
                        rootView.recyclerEventPast.layoutManager = layoutManager
                        rootView.recyclerEventPast.adapter = eventsViewAdapter

                    }else{
                        rootView.loadView.showOrGone(false)
                        eventsViewAdapter.notifyDataSetChanged()
                    }
                    rootView.recyclerEventPast.setOnScrollChangeListener { view, i, i2, i3, i4 ->
                        val total = totalCount
                        val currentLastItem: Int = layoutManager.findLastVisibleItemPosition()
                        if (currentLastItem == total - 1 && isLoading/* && lstCurrent<=currentLastItem*/) {
                            lstCurrent = currentLastItem
                            rootView.loadView.showOrGone(true)
                            pageCount += 1
                            eventListApi(store_id, location_id, type, startDate, endDate)
                        }
                    }
                }

            }
        }else if (type==Utility.eventsAdd){
            onResume()
            //eventListApi(store_id,location_id,type,startDate,endDate)
        }
    }

}