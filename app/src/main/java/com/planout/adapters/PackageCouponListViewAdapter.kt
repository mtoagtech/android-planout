package com.planout.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.CallApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.models.VouchersData
import okhttp3.FormBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PackageCouponListViewAdapter(
    val activity: Activity?,
    val availableVouchers: ArrayList<VouchersData>,
    val package_id: String,
    val serviceInfo_dialog: BottomSheetDialog
) : RecyclerView.Adapter<PackageCouponListViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_package_coupon_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item=availableVouchers[position]
        holder.packMonth.text=item.voucher_name
        //get expiring days
        val sdf = SimpleDateFormat(Utility.api_full_date_format,Locale.ENGLISH)
        val endDateValue: Date = sdf.parse(item.expired_at)
        val current= Utility.current_date(Utility.api_full_date_format)
        val startDateValue: Date =sdf.parse(current)
        val diff: Long = endDateValue.getTime() - startDateValue.getTime()
        System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))
        holder.packOfferExpireDay.text="Expire in ${TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)} days"

        holder.packAmount.text=item.discount_percent+"% ${activity!!.getString(R.string.off)}"
        holder.btnApply.setOnClickListener {
            serviceInfo_dialog.dismiss()
            //call api for apply coupon code
            applyCouponAPi(item.voucher_code)

        }

    }

    private fun applyCouponAPi(voucherCode: String) {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.coupon_code,voucherCode)
        mBuilder.add(Utility.key.package_id,package_id)
        mBuilder.add(Utility.key.store_id,Utility.getForm(activity!!,Utility.key.store_id)!!)
        CallApi.callAPi(mBuilder, ApiController.api.subscriptions_applycoupon, activity!!, Utility.subscriptions_applycoupon, true, Utility.POST, true)

    }

    override fun getItemCount(): Int {
        return availableVouchers.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val packMonth:TextView=itemView.findViewById(R.id.packMonth)
        val packOfferExpireDay:TextView=itemView.findViewById(R.id.packOfferExpireDay)
        val packAmount:TextView=itemView.findViewById(R.id.packAmount)
        val btnApply:Button=itemView.findViewById(R.id.btnApply)


    }
}