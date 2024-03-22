package com.planout.fragments

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrewjapar.rangedatepicker.CalendarPicker
import com.facebook.FacebookSdk
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.BuildConfig
import com.planout.R
import com.planout.activities.CompanyHomeSearchActivity
import com.planout.activities.HomeCompanyActivity
import com.planout.activities.NotificationActivity
import com.planout.adapters.HomePendingViewAdapter
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.DownloadManager
import com.planout.constant.Utility
import com.planout.constant.Utility.RES_CONFIRMED_STATUS
import com.planout.constant.Utility.RES_DECLINED_STATUS
import com.planout.constant.Utility.RES_PENDING_STATUS
import com.planout.constant.Utility.showOrGone
import com.planout.models.ReservationData
import kotlinx.android.synthetic.main.datepicker_bottom_final.view.calendar_view
import kotlinx.android.synthetic.main.datepicker_bottom_final.view.from_dialog
import kotlinx.android.synthetic.main.datepicker_bottom_final.view.to_dialog
import kotlinx.android.synthetic.main.fragment_comp_home_pending.totalPeopleTxt
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.dataCard
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.downloadExcel
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.editSerach
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.imgNotify
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.linNoMore
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.loadView
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.recyclerPending
import kotlinx.android.synthetic.main.fragment_comp_home_pending.view.txtDateRange
import okhttp3.FormBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class CompHomePendingFragment(
    val activityBase: HomeCompanyActivity
) : Fragment(),
    ApiResponse {
    var pendingCount: Int = 0
    lateinit var rootView: View
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var pendingViewAdapter: HomePendingViewAdapter
    lateinit var handler: Handler
    var isClick = false
    var selectedStatus: String = RES_PENDING_STATUS
    val statusArr = ArrayList<String>()

    private var pageCount = 1
    private var lstCurrent = 0
    private var totalCount = 0
    private var isLoading = false
    private var downloadUrl = ""
    val resverationItems: ArrayList<ReservationData> = ArrayList()

    var startDate = ""
    var endDate = ""

    var check = 0
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val storeId: String = intent.extras!!.getString("store_id").toString()
            val resId: String = intent.extras!!.getString("res_id").toString()
            val status: String = intent.extras!!.getString("status").toString()
            getDataFromResult(resId, 0, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isSpinnerTouch = false
    }

    override fun onPause() {
        super.onPause()
        isSpinnerTouch = false
    }

    override fun onResume() {
        super.onResume()


//        pageCount = 1
         startDate = Utility.current_date(Utility.api_date_format)
        endDate = Utility.current_date(Utility.api_date_format)
       rootView.txtDateRange.text = Utility.current_date(Utility.date_format)
//        rootView.txtDateRange.text = "Select dates"
//
//        resirvationListApi("", selectedStatus, activityBase)


    }

    fun setNotificationView(showDot: Boolean) {

        if (showDot) {
            rootView.imgNotify.setImageDrawable(
                ContextCompat.getDrawable(
                    activityBase,
                    R.drawable.ic_home_noti_dot
                )
            )
        } else {
            rootView.imgNotify.setImageDrawable(
                ContextCompat.getDrawable(
                    activityBase,
                    R.drawable.ic_home_noti
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_comp_home_pending, container, false)

        handler = Handler()

        clikcView()
        pageCount = 1
        startDate = ""
        endDate = ""
        //rootView.txtDateRange.text = Utility.first_date_month(Utility.date_format)+" - "+Utility.current_date(Utility.date_format)
        rootView.txtDateRange.text = getString(R.string.select_dates)

        resirvationListApi("", selectedStatus, activityBase)
        return rootView
    }

    var isSpinnerTouch = false
    private fun clikcView() {
        rootView.editSerach.setOnClickListener {

            startActivity(
                Intent(activityBase, CompanyHomeSearchActivity::class.java)
                    .putExtra(Utility.key.status, selectedStatus)
            )

        }

        if (Utility.getForm(activityBase, Utility.key.is_owner) == "1") {
            rootView.downloadExcel.setOnClickListener {
                Log.d("Click", downloadUrl)
                //DownloadManager(activityBase,downloadUrl)
                DownloadManager(activityBase, downloadUrl)
            }
        } else {
            rootView.downloadExcel.isEnabled = false
        }




        rootView.imgNotify.setOnClickListener {
            startActivity(Intent(activityBase, NotificationActivity::class.java))
        }
        if (statusArr.isEmpty()) {
            statusArr.add(getString(R.string.pending))
            statusArr.add(getString(R.string.confirmed))
            statusArr.add(getString(R.string.declined))
        }

        rootView.txtDateRange.setOnClickListener {
            if (!isClick) {
                //dialog for date range calender
                openRangePickerDialog()
            }
            clickForFew()
        }

        val spinnerStatus = rootView.findViewById<Spinner>(R.id.spinnerStatus)
        val adapterStatus =
            ArrayAdapter(activityBase, R.layout.simple_spinner_dropdown_item2, statusArr)
        spinnerStatus.adapter = adapterStatus
        spinnerStatus.setOnTouchListener(View.OnTouchListener { _, _ ->
            isSpinnerTouch = true
            false
        })

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isSpinnerTouch) {
                    Log.d("SELECETS>>>>", statusArr[position])
//                statusArr.add("Status")
//                statusArr.add("Pending")
//                statusArr.add("Confirmed")
//                statusArr.add("Declined")

                    if (statusArr[position] == getString(R.string.pending)) {
                        selectedStatus = RES_PENDING_STATUS

                    } else if (statusArr[position] == getString(R.string.confirmed)) {
                        selectedStatus = RES_CONFIRMED_STATUS

                    } else if (statusArr[position] == getString(R.string.declined)) {
                        selectedStatus = RES_DECLINED_STATUS

                    }
                    pageCount = 1
                    resirvationListApi("", selectedStatus, activityBase)


                } else {

                }


            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }


    }


    override fun onStop() {
        super.onStop()
        try {
            handler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clickForFew() {
        isClick = true
        handler.postDelayed(Runnable { isClick = false }, 1500)
    }

    private fun openRangePickerDialog() {
        if (startDate == "") {
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

        val from_dialog = dialogView.findViewById<TextView>(R.id.from_dialog)
        val to_dialog = dialogView.findViewById<TextView>(R.id.to_dialog)
        val txtClear = dialogView.findViewById<TextView>(R.id.txtClear)
        txtClear.setOnClickListener {
            pageCount = 1
            startDate = ""
            endDate = ""
            //rootView.txtDateRange.text = Utility.first_date_month(Utility.date_format)+" - "+Utility.current_date(Utility.date_format)
            rootView.txtDateRange.text = getString(R.string.select_date)
            resirvationListApi("", selectedStatus, activityBase)
            serviceInfo_dialog.dismiss()
        }

        val endDate_range = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        endDate_range[Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(
            Calendar.MONTH
        )] = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        endDate_range.add(Calendar.MONTH, 3)
        val c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        c[2022, 0] = 1 //Year,Mounth -1,Day

        dialogView.calendar_view.apply {
            val sDate1 = startDate
            val sDate2 = endDate
            val date1 = SimpleDateFormat(Utility.api_date_format, Locale.ENGLISH).parse(sDate1)
            val date2 = SimpleDateFormat(Utility.api_date_format, Locale.ENGLISH).parse(sDate2)
            showDayOfWeekTitle(true) // If you want to disable day of the week title, just make it false
            setMode(CalendarPicker.SelectionMode.RANGE) // You can set it via XML
            setRangeDate(c.time, endDate_range.time)
            setSelectionDate(date1!!, date2!!)
            scrollToDate(date2)
        }

        dialogView.from_dialog.text =
            Utility.formatdatetime(startDate, Utility.api_date_format, Utility.date_format)
        dialogView.to_dialog.text =
            Utility.formatdatetime(endDate, Utility.api_date_format, Utility.date_format)
        dialogView.calendar_view.setOnRangeSelectedListener { startDate, endDate, startLabel, endLabel ->
            from_dialog.text = startLabel
            to_dialog.text = endLabel
        }

        dialogView.calendar_view.setOnStartSelectedListener { startDate, label ->
            from_dialog.text = label
            to_dialog.text = label
        }

        val apply_dates = dialogView.findViewById<Button>(R.id.apply_dates)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        Utility.animationClick(apply_dates).setOnClickListener {
            if (from_dialog.text.toString() == "-" && to_dialog.text.toString() == "-") {
                Utility.normal_toast(
                    activityBase,
                    activityBase.getString(R.string.select_date_range_first)
                )
            } else if (from_dialog.text.toString() == "-") {
                Utility.normal_toast(
                    activityBase,
                    activityBase.getString(R.string.select_from_date_first)
                )
            } else if (to_dialog.text.toString() == "-") {
                Utility.normal_toast(
                    activityBase,
                    activityBase.getString(R.string.select_to_date_first)
                )
            } else {
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
                rootView.txtDateRange.text = Utility.formatdatetime(
                    startDate,
                    Utility.api_date_format,
                    Utility.date_format
                ) + " - " + Utility.formatdatetime(
                    endDate,
                    Utility.api_date_format,
                    Utility.date_format
                )
                pageCount = 1
                resirvationListApi("", selectedStatus, activityBase)
                serviceInfo_dialog.dismiss()
            }
        }

        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun resirvationListApi(type: String, status: String, requireActivity: Activity) {
        val mBuilder = FormBody.Builder()
        val API =
            ApiController.api.reservations + "?${Utility.key.page}=${pageCount}&${Utility.key.type}=$type&${Utility.key.status}=$status&${Utility.key.startdate}=${startDate}&${Utility.key.enddate}=${endDate}"
        CallApi.callAPi(
            mBuilder,
            API,
            requireActivity,
            Utility.reservationsPending,
            pageCount == 1,
            Utility.GET,
            true
        )

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.reservationsPending) {
            if (isData) {

                val data = result.getJSONObject(Utility.key.data)
                System.out.println("All api data :- " + data)
                val total_people = data.getString("total_people")
                totalPeopleTxt.text = total_people;
                val total = data.getInt(Utility.key.total)
                downloadUrl = data.getString("export_link")
                pageCount = Utility.checkStringNullOrNot(data, Utility.key.current_page).toInt()
                if (pageCount == 1) {
                    resverationItems.clear()
                }

                if (data.has("app_params")) {
                    val app_params = data.getJSONObject("app_params")
                    val android = app_params.getJSONObject("android")
                    updateFromPlayStore(
                        android.getString("app_version"),
                        android.getString("force_update")
                    )
                }

                val total_unread_notifications = data.getInt(Utility.key.total_unread_notifications)
                setNotificationView(total_unread_notifications > 0)
                pendingCount = data.getInt(Utility.key.total)

//                try {
//                    companyHomeFragment.tab_layout.getTabAt(0)!!.text =
//                        companyHomeFragment.tabsArray[0] + " (${pendingCount})"
//                    companyHomeFragment.tab_layout.getTabAt(1)!!.text =
//                        companyHomeFragment.tabsArray[1]
//                    companyHomeFragment.tab_layout.getTabAt(2)!!.text =
//                        companyHomeFragment.tabsArray[2]
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }

                if (total <= 0) {
                    rootView.linNoMore.showOrGone(true)
                    rootView.dataCard.showOrGone(false)
                } else {
                    rootView.linNoMore.showOrGone(false)
                    rootView.dataCard.showOrGone(true)
                    val records = data.getJSONArray(Utility.key.records)
                    totalCount = records.length()
                    isLoading = records.length() == 20
                    for (i in 0 until records.length()) {
                        val item = ReservationData()
                        val recordsObj = records.getJSONObject(i)
                        item.id = recordsObj.getString(Utility.key.id)
                        item.store_id = recordsObj.getString(Utility.key.store_id)
                        item.res_id = recordsObj.getString(Utility.key.res_id)
                        item.contact_name = recordsObj.getString(Utility.key.contact_name)
                        item.contact_mobile = recordsObj.getString(Utility.key.contact_mobile)
                        item.total_people = recordsObj.getString(Utility.key.total_people)
                        item.resdate = recordsObj.getString(Utility.key.resdate)
                        item.restime = recordsObj.getString(Utility.key.restime)
                        item.store_name = recordsObj.getString(Utility.key.store_name)
                        item.name = recordsObj.getString(Utility.key.name)
                        item.status = recordsObj.getString(Utility.key.status)
                        item.location_id = recordsObj.getString(Utility.key.location_id)
                        item.preferred_table = recordsObj.getString(Utility.key.preferred_table)
                        item.extra_notes = recordsObj.getString(Utility.key.extra_notes)
                        item.store_image = recordsObj.getString(Utility.key.store_image)
                        item.remark = recordsObj.getString(Utility.key.remark)
                        item.table_no = recordsObj.getString(Utility.key.table_no)
                        item.age_group = recordsObj.getString(Utility.key.age_group)
                        item.is_arrived=recordsObj.getInt(Utility.key.is_arrived)
                        item.isEdit = false
                        if (recordsObj.getString(Utility.key.location_id) != "null") {
                            item.location = recordsObj.getJSONObject(Utility.key.location)

                        }
                        resverationItems.add(item)
                    }
                    if (pageCount == 1) {
//                        rootView.recyclerPending.setHasFixedSize(true)
                        linearLayoutManager = LinearLayoutManager(
                            activityBase,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        pendingViewAdapter =
                            HomePendingViewAdapter(activityBase, resverationItems, activityBase)
                        rootView.recyclerPending.layoutManager = linearLayoutManager
                        rootView.recyclerPending.adapter = pendingViewAdapter
                    } else {
                        rootView.loadView.showOrGone(false)
                        pendingViewAdapter.notifyDataSetChanged()
                    }
                    rootView.recyclerPending.setOnScrollChangeListener { view, i, i2, i3, i4 ->
                        val total = totalCount
                        val currentLastItem: Int = linearLayoutManager.findLastVisibleItemPosition()
                        if (currentLastItem == total - 1 && isLoading) {
                            lstCurrent = currentLastItem
                            rootView.loadView.showOrGone(true)
                            pageCount += 1
                            resirvationListApi("", selectedStatus, activityBase)
                        }
                    }
                }

            } else {
                rootView.linNoMore.showOrGone(true)
                rootView.dataCard.showOrGone(false)

            }

        } else if (type == Utility.reservationsDeclinedAdapter) {
            if (isData) {
                Utility.customSuccessToast(activityBase, result.getString(Utility.key.message))
                getDataFromResult(
                    pendingViewAdapter.declineConfirmId,
                    pendingViewAdapter.declineConfirmPos,
                    true
                )
                pendingViewAdapter.notifyDataSetChanged()
            } else {
                Utility.customErrorToast(activityBase, result.getString(Utility.key.message))
            }
        }

    }

    fun updateFromPlayStore(appVersion: String, forceUpdate: String) {
        val versionName: String = BuildConfig.VERSION_NAME
        if (versionName != appVersion) {
            showUpdatePopUp(
                getString(R.string.update),
                getString(R.string.available_update),
                forceUpdate
            )
        }
    }

    private fun showUpdatePopUp(
        title: String,
        subTitle: String,
        forceUpdate: String
    ) {
        val dialog = Dialog(activityBase)
        dialog.setContentView(R.layout.logout_popup_view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.text = getString(R.string.not_now)
        txtDelete.text = getString(R.string.update)
        if (forceUpdate == "1") {
            txtCancel.showOrGone(false)
        }
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.setOnClickListener {
            dialog.dismiss()
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${FacebookSdk.getApplicationContext().packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${FacebookSdk.getApplicationContext().packageName}")
                    )
                )
            }
        }
        dialog.show()

    }


    private fun setTabCount() {
        pendingCount = resverationItems.size
//        try {
//            companyHomeFragment.tab_layout.getTabAt(0)!!.text =
//                companyHomeFragment.tabsArray[0] + " (${pendingCount})"
//            companyHomeFragment.tab_layout.getTabAt(1)!!.text = companyHomeFragment.tabsArray[1]
//            companyHomeFragment.tab_layout.getTabAt(2)!!.text = companyHomeFragment.tabsArray[2]
//            if (pendingCount == 0) {
//                rootView.linNoMore.showOrGone(true)
//                rootView.dataCard.showOrGone(false)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    fun getDataFromResult(id: String?, pos: Int?, status: Boolean) {
        for (i in 0 until resverationItems.size) {
            if (id == resverationItems[i].id) {
                resverationItems.removeAt(i)
                pendingViewAdapter.notifyItemRemoved(i)
                if (status) {
                    //refreshing tab count
                    setTabCount()
                }
                return
            }
        }
    }

}