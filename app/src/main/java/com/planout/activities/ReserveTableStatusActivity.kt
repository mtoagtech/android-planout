package com.planout.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import kotlinx.android.synthetic.main.activity_reserve_table_status.*
import okhttp3.FormBody
import org.json.JSONObject


class ReserveTableStatusActivity : AppCompatActivity(), ApiResponse {
    var resID = ""
    var resPos = 0
    var resType = ""
    var id=""
    var res_id=""
    var contact_name=""
    var contact_mobile=""
    var total_people=""
    var resdate=""
    var isCount = false
    var lastStatus = ""

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val storeId: String = intent.extras!!.getString("store_id").toString()
            resID = intent.extras!!.getString("res_id").toString()
            val status: String = intent.extras!!.getString("status").toString()
            lastStatus = status
            if (id == resID){
                if (status == Utility.RES_PENDING_STATUS) {
                    txtStatusTitle.text = getString(R.string.confirmation_pending)
                    txtStatusSubTitle.text = getString(R.string.reservation_pending_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_pending)
                    btnCancel.showOrGone(true)
                } else if (status == Utility.RES_CONFIRMED_STATUS) {
                    txtStatusTitle.text = getString(R.string.confirmed)
                    txtStatusSubTitle.text =
                        getString(R.string.reservation_all_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_confirmed)
                    btnCancel.showOrGone(false)
                } else if (status == Utility.RES_CANCELLED_STATUS) {
                    txtStatusTitle.text = getString(R.string.canceled)
                    txtStatusSubTitle.text = getString(R.string.reservation_cancel_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_cancelled)
                    btnCancel.showOrGone(false)
                } else if (status == Utility.RES_DECLINED_STATUS) {
                    txtStatusTitle.text = getString(R.string.declined)
                    txtStatusSubTitle.text =  getString(R.string.reservation_declined_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_cancelled)
                    btnCancel.showOrGone(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserve_table_status)

        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        resID = intent.getStringExtra(Utility.key.id)!!
        if (intent.hasExtra(Utility.key.type)) {
            resPos = intent.getIntExtra(Utility.key.itemposition, 0)
            resType = intent.getStringExtra(Utility.key.type)!!
        }
        //call api for reservation status detail
        detailApi(true)
        viewData()
        clickView()
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
            IntentFilter("Reservation")
        )
    }

    private fun detailApi(isHide: Boolean) {
        if (isHide) {
            dataAria.showOrGone(false)
        }
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.reservations + "/$resID"
        CallApi.callAPi(mBuilder, API, this, Utility.reservations, true, Utility.GET, true)

    }

    private fun clickView() {
        btnCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CALL_PHONE),
                    5
                )
            } else {
                val call_number = contactNum.text.toString()
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$call_number"))
                startActivity(intent)
            }
        }

        imgBack.setOnClickListener {
            onBackPressed()
        }

        btnCancel.setOnClickListener {
            openCancelDialog(
                "Cancel reservation", "Yes, I’d like to cancel",
                "No, keep my reservation", "0", "Are you sure you want to\ncancel this reservation?"
            )
        }
    }


    private fun viewData() {

    }

    fun openCancelDialog(
        header: String,
        btnName: String,
        btn2Name: String,
        id: String,
        strMsg: String
    ) {
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_delete_account_popup_view, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val dialogTitleTxt = dialogView.findViewById<TextView>(R.id.txtTitle)
        if (strMsg.isNotEmpty())
            dialogTitleTxt.text = strMsg

        val txtHead = dialogView.findViewById<TextView>(R.id.txtHead)
        val btnYes = dialogView.findViewById<Button>(R.id.btnDelete)
        val btnNo = dialogView.findViewById<Button>(R.id.btnCancel)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        txtHead.text = header
        btnYes.text = btnName
        btnNo.text = btn2Name
        btnNo.backgroundTintList = ContextCompat.getColorStateList(this, R.color.app_green)
        btnNo.setTextColor(ContextCompat.getColor(this, R.color.white))
        Utility.animationClick(btnYes).setOnClickListener {
            cancelResApi()
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(btnNo).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun cancelResApi() {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.contact_name,contact_name)
        mBuilder.add(Utility.key.contact_mobile,contact_mobile)
        mBuilder.add(Utility.key.total_people,total_people)
        mBuilder.add(Utility.key.res_date,resdate)
        val API = ApiController.api.reservations_cancel + "/$resID"
        CallApi.callAPi(mBuilder, API, this, Utility.reservationsCancel, true, Utility.POST, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {

        if (isData) {
            dataAria.showOrGone(true)
            if (type == Utility.reservations) {
                val data = result.getJSONObject(Utility.key.data)
                id = data.getString(Utility.key.id)
                res_id = data.getString(Utility.key.res_id)
                contact_name = data.getString(Utility.key.contact_name)
                contact_mobile = data.getString(Utility.key.contact_mobile)
                val storeMobile = data.getString(Utility.key.store_mobile)
                total_people = data.getString(Utility.key.total_people)
                val store_name = data.getString(Utility.key.store_name)
                val name = data.getString(Utility.key.name)
                val status = data.getString(Utility.key.status)
                val location_id = data.getString(Utility.key.location_id)
                val preferred_table = data.getString(Utility.key.preferred_table)
                val extra_notes = data.getString(Utility.key.extra_notes)
                val remark = data.getString(Utility.key.remark)
                resdate = data.getString(Utility.key.resdate)
                val restime = data.getString(Utility.key.restime)
                lastStatus = status
                txtResNumber.text = "${getString(R.string.reservation_no_small)} $res_id"
                contactNum.text = storeMobile
                if (extra_notes == "") {
                    constExtra.showOrGone(false)
                    txtExtraTitle.showOrGone(false)
                    txtExtraNotes.showOrGone(false)
                } else if (extra_notes == "null") {
                    constExtra.showOrGone(false)
                    txtExtraTitle.showOrGone(false)
                    txtExtraNotes.showOrGone(false)
                } else {
                    txtExtraNotes.text = "                          $extra_notes"
                    txtExtraTitle.showOrGone(true)
                    txtExtraNotes.showOrGone(true)
                }
                txtPeople.text = "$total_people ${getString(R.string.people)}"
                if (preferred_table == "1") {
                    txtType.text = getString(R.string.indoor)
                } else {
                    txtType.text = getString(R.string.outdoor)
                }

                txtDate.text =
                    Utility.formatdatetime(resdate, Utility.api_date_format, Utility.date_format)
                txtTime.text =
                    Utility.formatdatetime(restime, Utility.api_time, Utility.time_format)

                if (status == Utility.RES_PENDING_STATUS) {
                    txtStatusTitle.text = getString(R.string.confirmation_pending)
                    txtStatusSubTitle.text = getString(R.string.reservation_pending_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_pending)
                    btnCancel.showOrGone(true)
                } else if (status == Utility.RES_CONFIRMED_STATUS) {
                    txtStatusTitle.text = getString(R.string.confirmed)
                    txtStatusSubTitle.text =
                        getString(R.string.reservation_all_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_confirmed)
                    btnCancel.showOrGone(false)
                } else if (status == Utility.RES_CANCELLED_STATUS) {
                    txtStatusTitle.text = getString(R.string.canceled)
                    txtStatusSubTitle.text = getString(R.string.reservation_cancel_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_cancelled)
                    btnCancel.showOrGone(false)
                } else if (status == Utility.RES_DECLINED_STATUS) {
                    txtStatusTitle.text = getString(R.string.declined)
                    txtStatusSubTitle.text = getString(R.string.reservation_declined_text)
                    imgStatus.setImageResource(R.drawable.ic_reserve_cancelled)
                    btnCancel.showOrGone(false)
                }
                if (data.getString(Utility.key.location)!="null"){
                    val location = data.getJSONObject(Utility.key.location)
                    addressVal.text = location.getString(Utility.key.address)

                    btnLocate.setOnClickListener {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=${location.getString(Utility.key.latitude)},${location.getString(Utility.key.longitude)}")
                            )
                            startActivity(intent)
                        }catch (e:Exception){
                            Utility.customInfoToast(this,"Info!!","Map supported application not found in your device")
                        }

                    }
                    if (isCount){
                        onBackPressed()
                    }
                    isCount = true

                }else{
                    constAddress.showOrGone(false)
                }

            }else if (type==Utility.reservationsCancel){
                if (isData){
                    Utility.customSuccessToast(this,result.getString(Utility.key.message))
                    //call api for reservation status detail
                    detailApi(false)
                }else{
                    Utility.customSuccessToast(this,result.getString(Utility.key.message))
                }
                /*txtStatusTitle.text = "Cancelled"
                txtStatusSubTitle.text = "Your reservation has been cancelled"
                imgStatus.setImageResource(R.drawable.ic_reserve_cancelled)
                btnCancel.showOrGone(false)
                Utility.customSuccessToast(this,result.getString(Utility.key.message))*/
            }
        }else{
            Utility.customErrorToast(this,result.getString(Utility.key.message))
        }
    }

    override fun onBackPressed() {
        if (lastStatus != ""){
            val intent = Intent()
                .putExtra(Utility.key.id,resID)
                .putExtra(Utility.key.itemposition,resPos)
                .putExtra(Utility.key.type,resType)
                .putExtra(Utility.key.status,lastStatus)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}