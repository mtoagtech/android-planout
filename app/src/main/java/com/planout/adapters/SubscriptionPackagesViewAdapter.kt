package com.planout.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.CallApi
import com.google.gson.Gson
import com.planout.R
import com.planout.activities.PackageDetailActivity
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.PackagesData
import com.planout.models.VouchersData
import okhttp3.FormBody
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SubscriptionPackagesViewAdapter(
    val activity: Activity?,
    val packagesArray: ArrayList<PackagesData>
) : RecyclerView.Adapter<SubscriptionPackagesViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_subscription_package_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
  
        val item=packagesArray[position]
        holder.packMonth.text=item.duration+" ${activity!!.getString(R.string.months)}"
        holder.packAmount.text=activity!!.getString(R.string.currency,item.price).replace(".00", "")

        if (item.isActive){
            holder.packActive.showOrGone(true)
            holder.btnBuyNow.text=activity.getString(R.string.cancel)
            holder.btnBuyNow.setBackgroundResource(R.drawable.bg_round_btn_red_drawable)
            holder.constMain.setBackgroundResource(R.drawable.bg_tags_selected_drawable)

        }else{
            holder.packActive.showOrGone(false)
            holder.btnBuyNow.text=activity.getString(R.string.buy_now)
            holder.btnBuyNow.setBackgroundResource(R.drawable.bg_round_btn_green_drawable)
            holder.constMain.setBackgroundColor(ContextCompat.getColor(activity, R.color.gray_FF_14))

        }
        holder.btnBuyNow.setOnClickListener {
            if(Utility.getForm(activity, Utility.key.is_owner) == "1"){
                if (!item.isActive){
                    activity.startActivity(Intent(activity, PackageDetailActivity::class.java)
                        .putExtra("packageList", Gson().toJson(packagesArray))
                        .putExtra(Utility.key.itemposition,position.toString()))
                }else{
                    //dialog for cancel subscription
                    showCancelSubscriptionPopUp(activity!!.getString(R.string.cancel_subscription),activity!!.getString(R.string.cancel_subscription_text),item)
                }
            }
            else{
                holder.btnBuyNow.isEnabled = false
            }

        }

        if (item.available_vouchers.size==0){
            holder.packOfferPercentOff.showOrGone(false)
            holder.packOfferExpireDay.showOrGone(false)
        }else{
            try {
                holder.packOfferPercentOff.showOrGone(true)
                holder.packOfferExpireDay.showOrGone(true)
                val ii = getHighest(item.available_vouchers)
                holder.packOfferPercentOff.text=item.available_vouchers[ii].discount_percent.replace(".00", "")+"% ${activity!!.getString(R.string.off)}"
                //get expiring days
                val sdf = SimpleDateFormat(Utility.api_full_date_format,Locale.ENGLISH)
                val endDateValue: Date = sdf.parse(item.available_vouchers[ii].expired_at)
                val current=Utility.current_date(Utility.api_full_date_format)
                val startDateValue:Date=sdf.parse(current)
                val diff: Long = endDateValue.time - startDateValue.time
                System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))
                holder.packOfferExpireDay.text="${activity!!.getString(R.string.offer_expires_in)} ${TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)} ${activity!!.getString(R.string.days)}"
            }catch (e:Exception){e.printStackTrace()}

        }
    }

    private fun getHighest(availableVouchers: ArrayList<VouchersData>): Int {
        var highPer = ""
        var ii = 0
        for (i in 0 until availableVouchers.size){
            if (highPer.isEmpty()){
                highPer = availableVouchers[i].discount_percent
                ii = i
            }else{
                if(highPer.toDouble()<availableVouchers[i].discount_percent.toDouble()){
                    highPer = availableVouchers[i].discount_percent
                    ii = i
                }
            }
        }
        return ii
    }



    override fun getItemCount(): Int {
        return packagesArray.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val packMonth=itemView.findViewById<TextView>(R.id.packMonth)
        val packAmount=itemView.findViewById<TextView>(R.id.packAmount)
        val packOfferPercentOff=itemView.findViewById<TextView>(R.id.packOfferPercentOff)
        val packActive=itemView.findViewById<TextView>(R.id.packActive)
        val packOfferExpireDay=itemView.findViewById<TextView>(R.id.packOfferExpireDay)
        val btnBuyNow=itemView.findViewById<TextView>(R.id.btnBuyNow)
        val constMain=itemView.findViewById<ConstraintLayout>(R.id.constMain)
    }

    private fun showCancelSubscriptionPopUp(title: String, subTitle: String, item: PackagesData) {
        val dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.text = activity.getString(R.string.confirm)
        txtDelete.setOnClickListener {
            //call api for cancel subscription
            subscriptionCancelApi()
            dialog.dismiss()
        }
        dialog.show()
    }
    //odr_uniq_id:String, amt:String, transId:String, payRes:String, payResTxt:String, payMethod:String
    fun subscriptionCancelApi() {
        val mBuilder = FormBody.Builder()
        /*mBuilder.add(Utility.key.order_unique_id, odr_uniq_id)
        mBuilder.add(Utility.key.amount, amt)
        mBuilder.add(Utility.key.status, "802")
        mBuilder.add(Utility.key.transaction_id, transId)
        mBuilder.add(Utility.key.payment_response, payRes)
        mBuilder.add(Utility.key.payment_response_text, payResTxt)
        mBuilder.add(Utility.key.payment_method, payMethod)*/
        CallApi.callAPi(mBuilder, ApiController.api.subscriptions_cancel, activity!!, Utility.subscriptions_cancel, true, Utility.POST, true)

    }
}