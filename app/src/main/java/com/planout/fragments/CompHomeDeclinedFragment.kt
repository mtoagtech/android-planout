package com.planout.fragments

import android.app.Activity
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
import com.planout.adapters.HomeDeclinedViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.ReservationData
import kotlinx.android.synthetic.main.datepicker_bottom_final.view.*
import kotlinx.android.synthetic.main.fragment_comp_home_declined.view.*
import kotlinx.android.synthetic.main.fragment_comp_home_declined.view.linNoMore
import kotlinx.android.synthetic.main.fragment_comp_home_declined.view.loadView
import kotlinx.android.synthetic.main.fragment_comp_home_declined.view.txtDateRange
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.*
import okhttp3.FormBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CompHomeDeclinedFragment(
    val companyHomeFragment: CompanyHomeFragment,
    val activityBase: HomeCompanyActivity
) : Fragment(),
    ApiResponse {
    var declinedCount: Int = 0
    lateinit var rootView: View
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var declinedViewAdapter: HomeDeclinedViewAdapter
    lateinit var handler: Handler
    var isClick = false
    var selectedStatus=""
    private var pageCount = 1
    private var lstCurrent = 0
    private var totalCount = 0
    private var isLoading = false
    val resverationItems: ArrayList<ReservationData> = ArrayList()

    var startDate=""
    var endDate=""

    override fun onResume() {
        super.onResume()
        pageCount = 1
        startDate = ""
        endDate = ""
        //rootView.txtDateRange.text = Utility.first_date_month(Utility.date_format)+" - "+Utility.current_date(Utility.date_format)
        rootView.txtDateRange.text = getString(R.string.select_dates)
        //call api for declined reservation listing
        resirvationListApi("", Utility.RES_DECLINED_STATUS,activityBase)

        handler = Handler()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_comp_home_declined, container, false)

        clickView()

        return rootView
    }

    private fun clickView() {
        rootView.recyclerDeclined.setOnScrollChangeListener { _, _, _, _, _ ->
            val total = totalCount
            val currentLastItem: Int = linearLayoutManager.findLastVisibleItemPosition()
            if (currentLastItem == total - 1 && isLoading /*&& lstCurrent<=currentLastItem*/) {
                lstCurrent = currentLastItem
                rootView.loadView.showOrGone(true)
                pageCount += 1
                resirvationListApi("", Utility.RES_DECLINED_STATUS,activityBase)
            }
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

        val serviceInfo_dialog = BottomSheetDialog(
            activityBase,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(activityBase)
            .inflate(R.layout.datepicker_bottom_final, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val from_dialog=dialogView.findViewById<TextView>(R.id.from_dialog)
        val to_dialog=dialogView.findViewById<TextView>(R.id.to_dialog)
        val txtClear=dialogView.findViewById<TextView>(R.id.txtClear)
        txtClear.setOnClickListener { onResume()
            serviceInfo_dialog.dismiss()}

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
            setRangeDate(c.time, endDate_range.time)
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
                rootView.txtDateRange.text = Utility.formatdatetime(startDate, Utility.api_date_format, Utility.date_format)+" - "+Utility.formatdatetime(endDate, Utility.api_date_format, Utility.date_format)
                pageCount = 1
                resirvationListApi("", Utility.RES_DECLINED_STATUS,activityBase)
                serviceInfo_dialog.dismiss()
            }
        }

        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    fun resirvationListApi(type: String, status: String, requireActivity: Activity) {
        selectedStatus=status
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.reservations + "?${Utility.key.page}=${pageCount}&${Utility.key.type}=$type&${Utility.key.status}=$status&${Utility.key.startdate}=${startDate}&${Utility.key.enddate}=${endDate}"
        CallApi.callAPi(mBuilder, API, requireActivity, Utility.reservationsDeclined, pageCount==1, Utility.GET, true)
    }


    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.reservationsDeclined){
            if (isData){

                val data=result.getJSONObject(Utility.key.data)
                val total=data.getInt(Utility.key.total)
                val currPage= Utility.checkStringNullOrNot(data, Utility.key.current_page)
                if(currPage.toInt() == 1){
                    resverationItems.clear()
                }
                val total_unread_notifications=data.getInt(Utility.key.total_unread_notifications)
                companyHomeFragment.setNotificationView(total_unread_notifications>0)
                declinedCount=data.getInt(Utility.key.total)
                try {
                    companyHomeFragment.tab_layout.getTabAt(2)!!.text = companyHomeFragment.tabsArray[2]+" (${declinedCount})"
                    companyHomeFragment.tab_layout.getTabAt(0)!!.text=companyHomeFragment.tabsArray[0]
                    companyHomeFragment.tab_layout.getTabAt(1)!!.text=companyHomeFragment.tabsArray[1]

                }catch (e:Exception){
                    e.printStackTrace()
                }

                if (total<=0){
                    rootView.linNoMore.showOrGone(true)
                    rootView.recyclerDeclined.showOrGone(false)
                }else{
                    rootView.linNoMore.showOrGone(false)
                    rootView.recyclerDeclined.showOrGone(true)
                    val records=data.getJSONArray(Utility.key.records)
                    totalCount = records.length()
                    isLoading = records.length()==20
                    for (i in 0 until records.length()){
                        val item= ReservationData()
                        val recordsObj=records.getJSONObject(i)
                        item.id=recordsObj.getString(Utility.key.id)
                        item.store_id=recordsObj.getString(Utility.key.store_id)
                        item.res_id=recordsObj.getString(Utility.key.res_id)
                        item.contact_name=recordsObj.getString(Utility.key.contact_name)
                        item.contact_mobile=recordsObj.getString(Utility.key.contact_mobile)
                        item.total_people=recordsObj.getString(Utility.key.total_people)
                        item.resdate=recordsObj.getString(Utility.key.resdate)
                        item.restime=recordsObj.getString(Utility.key.restime)
                        item.store_name=recordsObj.getString(Utility.key.store_name)
                        item.name=recordsObj.getString(Utility.key.name)
                        item.status=recordsObj.getString(Utility.key.status)
                        item.location_id=recordsObj.getString(Utility.key.location_id)
                        item.preferred_table=recordsObj.getString(Utility.key.preferred_table)
                        item.extra_notes=recordsObj.getString(Utility.key.extra_notes)
                        item.store_image=recordsObj.getString(Utility.key.store_image)
                        item.remark=recordsObj.getString(Utility.key.remark)
                        if (recordsObj.getString(Utility.key.location_id)!="null"){
                            item.location=recordsObj.getJSONObject(Utility.key.location)

                        }
                        resverationItems.add(item)
                    }
                    if (pageCount == 1) {
//                        rootView.recyclerDeclined.setHasFixedSize(true)
                        linearLayoutManager = LinearLayoutManager(
                            activityBase,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        declinedViewAdapter =
                            HomeDeclinedViewAdapter(activityBase, resverationItems)
                        rootView.recyclerDeclined.layoutManager = linearLayoutManager
                        rootView.recyclerDeclined.adapter = declinedViewAdapter
                    }else{
                        rootView.loadView.showOrGone(false)
                        declinedViewAdapter.notifyDataSetChanged()
                    }
                }

            }else{
                rootView.linNoMore.showOrGone(true)
                rootView.recyclerDeclined.showOrGone(false)

            }

        }

    }


}