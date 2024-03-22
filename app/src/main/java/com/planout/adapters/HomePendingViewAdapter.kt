package com.planout.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import com.planout.constant.Utility.RES_CANCELLED_STATUS
import com.planout.constant.Utility.RES_CONFIRMED_STATUS
import com.planout.constant.Utility.RES_DECLINED_STATUS
import com.planout.constant.Utility.RES_PENDING_STATUS
import com.planout.constant.Utility.showOrGone
import com.planout.models.ReservationData

import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.showAlignBottom
import okhttp3.FormBody


class HomePendingViewAdapter(
    val activity: Activity?,
    val resverationItems: ArrayList<ReservationData>,
    val lyf: LifecycleOwner?,

    ) : RecyclerView.Adapter<HomePendingViewAdapter.ViewHolder>() {

    var declineConfirmPos = 0
    var declineConfirmId = ""
    var clickedText = ""
    var clickedID = ""
    var clickedPos = 0
    lateinit var clickedTextView: TextView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_company_pending_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = resverationItems[position]

        Log.d("reservition data ","All reservation data :- "+item)
        if (position == 0) {
            holder.headingLine.showOrGone(true)
        } else {
            holder.headingLine.showOrGone(false)
        }

        Log.d("sjajsfjasifiasf","lfklsfasfjas :- "+item.is_arrived)

        if(item.is_arrived == 1){
            holder.switchArrived.setToggleOn()
            holder.mainView.setBackgroundResource(R.color.app_parrot_light)

        }else{
            holder.switchArrived.setToggleOff()
            holder.mainView.setBackgroundResource(R.color.white_black)
        }
        var date = Utility.formatdatetime(item.resdate,Utility.api_date_format,"dd MMM yyyy");
        var time = Utility.formatdatetime(item.resdate,Utility.api_date_format,Utility.time_format);

        Log.d("reservation time","reservation time :-"+date+" "+time )

        holder.txtTime.text =date+" "+time
        holder.txtReservation.text = item.res_id
        holder.txtMobile.text = item.contact_mobile
            //holder.txtDate.text=Utility.formatdatetime(item.resdate,Utility.api_date_format,"dd")
//        holder.txtDay.text=Utility.formatdatetime(item.resdate,Utility.api_date_format,"EEE")
//        holder.txtMonth.text=Utility.formatdatetime(item.resdate,Utility.api_date_format,"MMM")
        holder.txtName.text = Utility.toTitleCase(item.contact_name)
//        holder.txtReservNo.text="Reservation no. : "+item.res_id
        holder.txtPeople.text = item.total_people + " ${activity!!.getString(R.string.people)}"


        when (item.status) {
            RES_PENDING_STATUS -> {
                holder.txtStatus.text = activity!!.getString(R.string.pending)
                holder.txtStatus.setBackgroundResource(R.color.app_orange_light);
                holder.txtStatus.setTextColor(Color.parseColor("#F0783C"));
                holder.reasion.showOrGone(false)
                holder.editTableNo.showOrGone(true)
                holder.switchArrived.visibility = View.GONE
                holder.arrivedText.visibility = View.GONE
                holder.viewArrived.visibility = View.GONE
            }

            RES_CONFIRMED_STATUS -> {
                holder.txtStatus.text = activity!!.getString(R.string.confirmed)
                holder.txtStatus.setBackgroundResource(R.color.app_parrot_light);
                holder.txtStatus.setTextColor(Color.parseColor("#3B857A"));
                holder.reasion.showOrGone(false)
                holder.editTableNo.showOrGone(true)
                holder.switchArrived.showOrGone(true)
                holder.arrivedText.visibility = View.VISIBLE
                holder.viewArrived.visibility = View.VISIBLE


            }

            RES_DECLINED_STATUS -> {
                holder.txtStatus.text = activity!!.getString(R.string.declined)
                holder.txtStatus.setBackgroundResource(R.color.app_pink_light);
                holder.txtStatus.setTextColor(Color.parseColor("#F04B69"));
                holder.reasion.showOrGone(true)
                holder.editTableNo.showOrGone(false)
                holder.reasion.text = item.remark
                holder.switchArrived.showOrGone(false)
                holder.arrivedText.visibility = View.GONE
                holder.viewArrived.visibility = View.GONE

            }

            RES_CANCELLED_STATUS -> {
                holder.txtStatus.text = activity!!.getString(R.string.declined)
                holder.txtStatus.setBackgroundResource(R.color.app_pink_light);
                holder.txtStatus.setTextColor(Color.parseColor("#F04B69"));
                holder.reasion.showOrGone(true)
                holder.editTableNo.showOrGone(false)
                holder.reasion.text = item.remark
                holder.switchArrived.showOrGone(false)
                holder.arrivedText.visibility = View.GONE
                holder.viewArrived.visibility = View.GONE

            }


        }

        holder.switchArrived.setOnToggleChanged{
            if (it){
                chageReservationStatus("1",item.id)
                holder.switchArrived.setToggleOn()
                holder.mainView.setBackgroundResource(R.color.app_parrot_light)


            }else{
                chageReservationStatus("0",item.id)
                holder.switchArrived.setToggleOff()
                holder.mainView.setBackgroundResource(R.color.white_black)
            }
        }


        holder.txtStatus.setOnClickListener {
            if (item.status == RES_PENDING_STATUS) {
                holder.txtStatus.showAlignBottom(
                    getStatusBalloon(
                        activity!!.baseContext,
                        lyf!!,
                        item.id,
                        item.status,
                        position
                    )
                )

            }
        }

        when (item.preferred_table) {
            "1" -> {
                holder.txtType.showOrGone(true)
                holder.txtType.text = activity!!.getString(R.string.indoor)
            }

            "2" -> {
                holder.txtType.showOrGone(true)
                holder.txtType.text = activity!!.getString(R.string.indoor)
            }

            else -> {
                holder.txtType.showOrGone(false)
            }
        }
        if (item.extra_notes.length > 30) {
            val textShort = item.extra_notes.substring(0, 30) + "...";
            holder.txtExtraNotes.text =
                Html.fromHtml(textShort + "<font color='#3B857A'> <u>More</u></font>");
            holder.txtExtraNotes.setOnClickListener {
                openExtraNoteDialog(item.extra_notes)
            }
        } else {
            holder.txtExtraNotes.text = Utility.checkStringNullOrEmpty(item.extra_notes)
        }
//        holder.txtExtraNotes.text = item.extra_notes
        holder.txtTableNo.text = Utility.checkStringNullOrEmpty(item.table_no)
        holder.txtAgeGroup.text = Utility.checkStringNullOrEmpty(item.age_group)
        holder.txtTableNo.text = Utility.checkStringNullOrEmpty(item.table_no)


        holder.editTableNo.setOnClickListener {
            var found = false;
            for (i in 0 until resverationItems.size){
                if (resverationItems[i].isEdit){
                    found = true
                    break
                }

            }
            if (found){
                val dialog = Dialog(activity!!)
                dialog.setContentView(R.layout.logout_popup_view)
                val txtTitle = dialog.findViewById<TextView>(R.id.txtTitle)
                val txtSubTitle = dialog.findViewById<TextView>(R.id.txtSubTitle)
                val txtCancel = dialog.findViewById<TextView>(R.id.txtCancel)
                val txtConfirm = dialog.findViewById<TextView>(R.id.txtDelete)

                txtTitle.text = "Leave changes?"
                txtSubTitle.text = "Changes you made may not be saved"
                txtConfirm.text = "Leave"
                txtCancel.text = "Cancel"

                txtCancel.setOnClickListener {
                    dialog.dismiss()
                }

                txtConfirm.setOnClickListener {
                    try {
                        resverationItems[clickedPos].isEdit=false
                        clickedTextView.text = clickedText;
                        this.notifyItemChanged(clickedPos);
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    dialog.dismiss()
                }
                dialog.show()
            }else{
                clickedTextView = holder.txtTableNo
                clickedID = item.id;
                clickedPos = position;
                item.isEdit = true;
                holder.showTable.showOrGone(false)
                holder.EditTable.showOrGone(true)
            }
        }

        holder.edttxtTableNo.doOnTextChanged { text, start, before, count ->
            clickedText = text.toString()
        }
        holder.showTable.showOrGone(true)
        holder.EditTable.showOrGone(false)
        holder.saveTableNo.setOnClickListener {
            item.isEdit = false;
            holder.showTable.showOrGone(true)
            holder.EditTable.showOrGone(false)
            holder.txtTableNo.text = holder.edttxtTableNo.text.toString()
            updateTableNoApi(item.id, holder.edttxtTableNo.text.toString())
        }

        holder.edttxtTableNo.setText(Utility.checkStringNullOrEmpty(item.table_no))


    }

    private fun chageReservationStatus(status: String, id: String) {
        val mBuilder = FormBody.Builder()
        mBuilder.add("is_arrived",status)
        CallApi.callAPi(mBuilder, ApiController.api.reservationStatusUpdate+id, activity!!, Utility.reservationStatusUpdate, true, Utility.POST, true)

    }

    private fun openExtraNoteDialog(extraNotes: String) {
        val serviceInfo_dialog = BottomSheetDialog(
            activity!!,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(activity!!)
            .inflate(R.layout.bottom_extra_note_view_popup, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val extraNotesTxt = dialogView.findViewById<TextView>(R.id.extraNotesTxt)
        extraNotesTxt.text = extraNotes;

        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun updateTableNoApi(id: String, edttxtTableNo: String) {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.table_no, edttxtTableNo)
        CallApi.callAPi(
            mBuilder,
            "${ApiController.api.reservations_updatetableno}/$id",
            activity!!,
            Utility.updatetableno,
            true,
            Utility.POST,
            true
        )

    }

    fun getStatusBalloon(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        id: String,
        status: String,
        position: Int,

        ): Balloon {
        val typeface = ResourcesCompat.getFont(context, R.font.helvetica_bold)
        var text = ""
        text = "Including Tax"

        val balloon = Balloon.Builder(context)
            .setLayout(R.layout.layout_status_change)
            .setArrowSize(10)
            .setWidthRatio(0.0f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setWidth(BalloonSizeSpec.WRAP)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowPosition(0.5f)
            .setPadding(12)
            .setTextGravity(Gravity.START)
            .setMarginRight(12)
            .setMarginLeft(12)
            .setTextSize(15f)
            .setCornerRadius(8f)
            .setBackgroundColorResource(R.color.white_black)
            .setBalloonAnimation(BalloonAnimation.ELASTIC)
            .setTextTypeface(typeface!!)
            .setLifecycleOwner(lifecycleOwner)
            .build()

        val pendingTxt: TextView =
            balloon.getContentView().findViewById(R.id.pendingTxt)
        pendingTxt.setOnClickListener {
            balloon.dismiss()
        }
        val confirmedTxt: TextView =
            balloon.getContentView().findViewById(R.id.confirmedTxt)
        confirmedTxt.setOnClickListener {
            openConfirmationPop(
                id,
                "Confirmation",
                "Are you sure you want to confirm this reservation? ",
                activity!!.getString(R.string.confirm),
                position
            )
            balloon.dismiss()
        }
        val declinedTxt: TextView =
            balloon.getContentView().findViewById(R.id.declinedTxt)
        declinedTxt.setOnClickListener {
            openDeleteDialog(id, position)
            balloon.dismiss()
        }


        return balloon

    }

    override fun getItemCount(): Int {
        return resverationItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtName = itemView.findViewById<TextView>(R.id.txtName)

        //        val txtReservNo = itemView.findViewById<TextView>(R.id.txtReservNo)
        val txtTime = itemView.findViewById<TextView>(R.id.txtTime)
        val txtPeople = itemView.findViewById<TextView>(R.id.txtPeople)
        val txtType = itemView.findViewById<TextView>(R.id.txtType)

        //        val txtAddress = itemView.findViewById<TextView>(R.id.txtAddress)
//        val txtExtraTitle = itemView.findViewById<TextView>(R.id.txtExtraTitle)
        val txtExtraNotes = itemView.findViewById<TextView>(R.id.txtExtraNotes)
        val headingLine = itemView.findViewById<LinearLayout>(R.id.headingLine)
        val txtStatus = itemView.findViewById<TextView>(R.id.txtStatus)
        val txtTableNo = itemView.findViewById<TextView>(R.id.txtTableNo)
        val txtAgeGroup = itemView.findViewById<TextView>(R.id.txtAgeGroup)
        val reasion = itemView.findViewById<TextView>(R.id.reasion)

        val editTableNo = itemView.findViewById<ImageView>(R.id.editTableNo)
        val saveTableNo = itemView.findViewById<ImageView>(R.id.saveTableNo)
        val showTable = itemView.findViewById<ConstraintLayout>(R.id.showTable)
        val EditTable = itemView.findViewById<ConstraintLayout>(R.id.EditTable)
        val edttxtTableNo = itemView.findViewById<EditText>(R.id.edttxtTableNo)
        val switchArrived = itemView.findViewById<com.zcw.togglebutton.ToggleButton>(R.id.switchArrived)
        val viewArrived = itemView.findViewById<LinearLayout>(R.id.viewArrived)
        val arrivedText = itemView.findViewById<TextView>(R.id.arrivedText)
        val txtReservation = itemView.findViewById<TextView>(R.id.txtReservation)
        val txtMobile = itemView.findViewById<TextView>(R.id.txtMobile)
        val mainView = itemView.findViewById<LinearLayout>(R.id.mainView)
    }

    fun openDeleteDialog(id: String, position: Int) {
        val serviceInfo_dialog = BottomSheetDialog(
            activity!!,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(activity!!)
            .inflate(R.layout.bottom_reserve_declined_view_popup, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val wrongInfo = dialogView.findViewById<AppCompatCheckBox>(R.id.wrongInfo)
        val overBook = dialogView.findViewById<AppCompatCheckBox>(R.id.overBook)
        val checkClosed = dialogView.findViewById<AppCompatCheckBox>(R.id.checkClosed)
        wrongInfo.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                overBook.isChecked = false
                checkClosed.isChecked = false
            }
        }

        overBook.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                wrongInfo.isChecked = false
                checkClosed.isChecked = false
            }
        }

        checkClosed.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                wrongInfo.isChecked = false
                overBook.isChecked = false
            }
        }

        val btnYes = dialogView.findViewById<Button>(R.id.btnDeclined)
        val btnNo = dialogView.findViewById<Button>(R.id.btnCancel)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)

        Utility.animationClick(btnYes).setOnClickListener {
            var remark = ""
            if (checkClosed.isChecked) {
                remark = activity!!.getString(R.string.we_are_close)
            } else if (overBook.isChecked) {
                remark = activity!!.getString(R.string.we_are_over_booked)

            } else if (wrongInfo.isChecked) {
                remark = activity!!.getString(R.string.wrong_info)
            }

            if (remark == "") {
                Utility.normal_toast(activity, "Select your declined reason!!")
            } else {
                try {
                    //resverationItems.removeAt(position)
                    //notifyItemRemoved(position)
                    declineConfirmPos = position
                    declineConfirmId = id
                    declinedApi(id, remark)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                serviceInfo_dialog.dismiss()
            }
        }
        Utility.animationClick(btnNo).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun declinedApi(resID: String, remark: String) {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.remark, remark)
        val API = ApiController.api.reservations_decline + "/$resID"
        CallApi.callAPi(
            mBuilder,
            API,
            activity!!,
            Utility.reservationsDeclinedAdapter,
            true,
            Utility.POST,
            true
        )

    }
    fun openConfirmationPopForTable(id: String, title: String, msg: String, btn: String, position: Int) {

    }
    fun openConfirmationPop(id: String, title: String, msg: String, btn: String, position: Int) {
        val dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.logout_popup_view)
        val txtTitle = dialog.findViewById<TextView>(R.id.txtTitle)
        val txtSubTitle = dialog.findViewById<TextView>(R.id.txtSubTitle)
        val txtCancel = dialog.findViewById<TextView>(R.id.txtCancel)
        val txtDelete = dialog.findViewById<TextView>(R.id.txtDelete)

        txtTitle.text = title
        txtSubTitle.text = msg
        txtDelete.text = btn
        txtCancel.setOnClickListener {
            dialog.dismiss()
        }
        txtDelete.setOnClickListener {
            try {
                //resverationItems.removeAt(position)
                //notifyItemRemoved(position)
                declineConfirmPos = position
                declineConfirmId = id
                confirmReservationApi(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun confirmReservationApi(resID: String) {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.reservations_confirm + "/$resID"
        CallApi.callAPi(
            mBuilder,
            API,
            activity!!,
            Utility.reservationsDeclinedAdapter,
            true,
            Utility.POST,
            true
        )

    }
}