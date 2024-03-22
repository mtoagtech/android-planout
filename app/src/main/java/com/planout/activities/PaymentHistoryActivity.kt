package com.planout.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
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
import com.planout.adapters.PaymentHistoryViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.PaymentHistoryData
import kotlinx.android.synthetic.main.activity_payment_history.*
import kotlinx.android.synthetic.main.datepicker_bottom_final.view.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PaymentHistoryActivity : AppCompatActivity(), ApiResponse {

    lateinit var paymentHistoryViewAdapter: PaymentHistoryViewAdapter
    lateinit var layoutManager: LinearLayoutManager
    var startDate = ""
    var endDate = ""
    lateinit var handler: Handler
    var isClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)
        txtHeader.text = getString(R.string.payment_history)
        imgBackHeader.setOnClickListener {
            onBackPressed()
        }
        handler = Handler()
        //call api for payment history
        callHistApi()

        txtDateRange.setOnClickListener {
            if (!isClick) {
                //dialog for range picker calender
                openRangePickerDialog()
            }
            clickForFew()
        }
    }

    private fun callHistApi() {
        startDate = ""
        endDate = ""
        //txtDateRange.text = Utility.first_date_month(Utility.date_format)+" - "+Utility.current_date(Utility.date_format)
        txtDateRange.text = getString(R.string.select_dates)
        PaymentListApi()
    }

    fun openRangePickerDialog() {
        if (startDate == ""){
            startDate = Utility.current_date(Utility.api_date_format)
            endDate = Utility.current_date(Utility.api_date_format)
        }
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.datepicker_bottom_final, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val from_dialog=dialogView.findViewById<TextView>(R.id.from_dialog)
        val to_dialog=dialogView.findViewById<TextView>(R.id.to_dialog)
        val txtClear=dialogView.findViewById<TextView>(R.id.txtClear)
        txtClear.setOnClickListener { callHistApi()
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
                Utility.normal_toast(this, getString(R.string.select_date_range_first))
            }else if (from_dialog.text.toString() == "-" ){
                Utility.normal_toast(this, getString(R.string.select_from_date_first))
            }else if (to_dialog.text.toString() == "-" ){
                Utility.normal_toast(this, getString(R.string.select_to_date_first))
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
                txtDateRange.text = Utility.formatdatetime(startDate, Utility.api_date_format, Utility.date_format)+" - "+Utility.formatdatetime(endDate, Utility.api_date_format, Utility.date_format)
                //call api for payment history listing
                PaymentListApi()
                serviceInfo_dialog.dismiss()
            }
        }

        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun PaymentListApi() {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.subscriptions_payment_history + "?${Utility.key.startdate}=${startDate}"+"&${Utility.key.enddate}=${endDate}"
        CallApi.callAPi(
            mBuilder,
            API,
            this,
            Utility.subscriptions_payment_history,
            true,
            Utility.GET,
            true
        )
    }


    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.subscriptions_payment_history) {
            if (isData) {

                val data = result.getJSONArray(Utility.key.data)
                if (data.length() > 0) {
                    linNoMore.showOrGone(false)
                    recyclerPayHistory.showOrGone(true)
                    val paymentItems: ArrayList<PaymentHistoryData> = ArrayList()
                    for (i in 0 until data.length()) {
                        val dataObj = data.getJSONObject(i)
                        val item = PaymentHistoryData()
                        item.id = dataObj.getString(Utility.key.id)
                        item.package_price = dataObj.getString(Utility.key.package_price)
                        item.final_price = dataObj.getString(Utility.key.final_price)
                        item.user_id = dataObj.getString(Utility.key.user_id)
                        item.user_type = dataObj.getString(Utility.key.user_type)
                        item.name = dataObj.getString(Utility.key.name)
                        item.status = dataObj.getString(Utility.key.status)
                        item.store_name = dataObj.getString(Utility.key.store_name)
                        item.item_name = dataObj.getString(Utility.key.item_name)
                        item.subtotal = dataObj.getString(Utility.key.subtotal)
                        item.tax = dataObj.getString(Utility.key.tax)
                        item.tax_percent = dataObj.getString(Utility.key.tax_percent)
                        item.discount = dataObj.getString(Utility.key.discount)
                        item.store_id = dataObj.getString(Utility.key.store_id)
                        item.transaction_id = dataObj.getString(Utility.key.transaction_id)
                        item.order_unique_id = dataObj.getString(Utility.key.order_unique_id)
                        item.order_date = dataObj.getString(Utility.key.order_date)
                        item.payment_method = dataObj.getString(Utility.key.payment_method)
                        item.discount_percent = dataObj.getString(Utility.key.discount_percent)
                        item.download_link = dataObj.getString(Utility.key.download_link)
                        item.view_link = dataObj.getString(Utility.key.view_link)
                        paymentItems.add(item)
                    }
//                    recyclerPayHistory.setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    paymentHistoryViewAdapter = PaymentHistoryViewAdapter(this, paymentItems)
                    recyclerPayHistory.layoutManager = layoutManager
                    recyclerPayHistory.adapter = paymentHistoryViewAdapter
                } else {
                    linNoMore.showOrGone(true)
                    recyclerPayHistory.showOrGone(false)
                }
            } else {
                linNoMore.showOrGone(true)
                recyclerPayHistory.showOrGone(false)
            }
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
}