package com.planout.adapters

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.R
import com.planout.activities.CompanyProfileActivity
import com.planout.activities.CreateLocationScreen
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.StoreLocationData
import com.skydoves.balloon.*
import okhttp3.FormBody
import org.json.JSONObject


class LocationViewAdapter(
    val activity: Activity?,
    private val storeLocationItems: ArrayList<StoreLocationData>,
    val addLocationActivity: LifecycleOwner
) : RecyclerView.Adapter<LocationViewAdapter.ViewHolder>() , ApiResponse {
    var deletedPos=0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_location_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=storeLocationItems[position]
        if (item.address_type=="1") {
            holder.txtAddress.text = item.address
        }else{
            holder.txtAddress.text = item.address1
        }
        if (item.table_indoor=="1"){
            holder.txtIndoor.showOrGone(true)
        }else{
            holder.txtIndoor.showOrGone(false)
        }

        if (item.table_outdoor=="1"){
            holder.txtOutdoor.showOrGone(true)
        }else{
            holder.txtOutdoor.showOrGone(false)
        }
        if (position==storeLocationItems.size-1){
            holder.viewBtm.showOrGone(false)
        }else{
            holder.viewBtm.showOrGone(true)

        }

        if(Utility.getForm(activity!!, Utility.key.is_owner) == "1"){
            holder.imgMenu.setOnClickListener {
                if (activity!!.localClassName == "activities.CompanyProfileActivity") {
                    (activity as CompanyProfileActivity).scrollDown(position)
                }
                //option menu for location |  #make it as default  #edit  #delete
                holder.imgMenu.showAlignLeft(showEditDeletePopUp(item.id,position,item.locationObj,item.is_default,position))
            }

        }
        else{
            holder.imgMenu.isEnabled = false
        }


        /*if (position == storeLocationItems.size-1){
            holder.viewBtm.visibility = View.INVISIBLE
        }*/
    }

    override fun getItemCount(): Int {
        return storeLocationItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val constMain = itemView.findViewById<ConstraintLayout>(R.id.constMain)
        val imgMenu = itemView.findViewById<ImageView>(R.id.imgMenu)
        val txtAddress = itemView.findViewById<TextView>(R.id.txtAddress)
        val txtIndoor = itemView.findViewById<TextView>(R.id.txtIndoor)
        val txtOutdoor = itemView.findViewById<TextView>(R.id.txtOutdoor)
        val viewBtm = itemView.findViewById<View>(R.id.viewBtm)

    }

    fun showEditDeletePopUp(
        id: String,
        position: Int,
        locationObj: String,
        isDefault: Boolean,
        position1: Int
    ) : Balloon {
        val balloon = Balloon.Builder(this.activity!!)
            .setHeight(BalloonSizeSpec.WRAP)
            .setWidth(BalloonSizeSpec.WRAP)
            .setLayout(R.layout.popup_edit_delete_view)
            .setArrowSize(10)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowOrientation(ArrowOrientation.END)
            .setArrowPosition(0.5f)
            .setCornerRadius(4f)
            .setBackgroundColor(ContextCompat.getColor(this.activity, R.color.background_color))
            .setBalloonAnimation(BalloonAnimation.ELASTIC)
            .setArrowOrientationRules(ArrowOrientationRules.ALIGN_ANCHOR)
            .setLifecycleOwner(addLocationActivity)
            .build()

        val txtDefault = balloon.getContentView().findViewById(R.id.txtDefault) as TextView
        val txtEdit = balloon.getContentView().findViewById(R.id.txtEdit) as TextView
        val txtDelete = balloon.getContentView().findViewById(R.id.txtDelete) as TextView
        if (isDefault){
            txtDefault.showOrGone(false)
        }else{
            //txtDefault.text="Make it as default location"
            txtDefault.showOrGone(true)
        }

        txtEdit.setOnClickListener {
            //edit location
            balloon.dismiss()
            val isPermission = checkLocationPermission()
            if (isPermission){
            activity.startActivity(Intent(activity!!, CreateLocationScreen::class.java)
                .putExtra(Utility.key.details,locationObj)
                .putExtra(Utility.key.isFrom,Utility.key.edit)
            )}}
        txtDefault.setOnClickListener { balloon.dismiss()
            //dialog for make as default location confirmation
            //deletedPos=position
            openMakeDefaultDialog(
                "Make it as default location",
                "Make it as default",
                id,
                "Are you sure you want to make as default location?",position)
        }
        txtDelete.setOnClickListener { balloon.dismiss()
            //dialog for delete location confirmation
            deletedPos=position
            openDeleteDialog("Delete location", "Delete", id, "Are you sure you want to delete location?")
        }

        return balloon
    }

    private fun defaultLocationApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, "${ApiController.api.store_locations}/$id/makedefault", activity!!, "makedefault", true, Utility.POST, true)

    }
    private fun deleteLocationApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, "${ApiController.api.store_locations}/$id", activity!!, Utility.storeDelete, true, Utility.DELETE, true)

    }

    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    3
                )
            } else {
                ActivityCompat.requestPermissions(
                    activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    3
                )
            }
            false
        } else {
            true
        }
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == "makedefault"){
            if (isData) {
                Utility.customSuccessToast(activity!!,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }else if(type == Utility.storeDelete){
            if (isData) {
                storeLocationItems.removeAt(deletedPos)
                notifyItemRemoved(deletedPos)
                notifyDataSetChanged()
                if (storeLocationItems.size <= 0) {
                    try {
                        (activity as CompanyProfileActivity).setLocationDefaultView(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun openDeleteDialog(header:String, btnName:String, id:String, strMsg:String) {
        val serviceInfo_dialog = BottomSheetDialog(
            activity!!,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(activity!!)
            .inflate(R.layout.bottom_delete_account_popup_view, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val dialogTitleTxt=dialogView.findViewById<TextView>(R.id.txtTitle)
        if (strMsg.isNotEmpty())
            dialogTitleTxt.text= strMsg

        val txtHead=dialogView.findViewById<TextView>(R.id.txtHead)
        val btnYes=dialogView.findViewById<Button>(R.id.btnDelete)
        val btnNo=dialogView.findViewById<Button>(R.id.btnCancel)
        val imgTop=dialogView.findViewById<ImageView>(R.id.imgTop)
        txtHead.text = header
        btnYes.text = btnName
        Utility.animationClick(btnYes).setOnClickListener {
            deleteLocationApi(id)
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

    fun openMakeDefaultDialog(
        header: String,
        btnName: String,
        id: String,
        strMsg: String,
        position: Int
    ) {
        val serviceInfo_dialog = BottomSheetDialog(
            activity!!,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(activity!!)
            .inflate(R.layout.bottom_delete_account_popup_view, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val dialogTitleTxt=dialogView.findViewById<TextView>(R.id.txtTitle)
        if (strMsg.isNotEmpty())
            dialogTitleTxt.text= strMsg

        val txtHead=dialogView.findViewById<TextView>(R.id.txtHead)
        val btnYes=dialogView.findViewById<Button>(R.id.btnDelete)
        val btnNo=dialogView.findViewById<Button>(R.id.btnCancel)
        val imgTop=dialogView.findViewById<ImageView>(R.id.imgTop)
        txtHead.text = header
        btnYes.text = btnName
        btnYes.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.app_green)
        Utility.animationClick(btnYes).setOnClickListener {
            for (i in 0 until storeLocationItems.size){
                storeLocationItems[i].is_default = i==position
            }
            notifyDataSetChanged()
            defaultLocationApi(id)
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
}