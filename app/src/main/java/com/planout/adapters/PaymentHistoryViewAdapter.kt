package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.PaymentOrderDetailActivity
import com.planout.constant.Utility
import com.planout.models.PaymentHistoryData

class PaymentHistoryViewAdapter(
    val activity: Activity?,
    val paymentItems: ArrayList<PaymentHistoryData>
) : RecyclerView.Adapter<PaymentHistoryViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_pay_history_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = paymentItems[position]
        holder.txtTitle.text = item.item_name
        holder.txtAmount.text = activity!!.getString(R.string.currency, item.final_price)
        holder.txtSubTitle.text = Utility.formatdatetime(item.order_date, Utility.api_full_date_format, Utility.date_format_subscription)

        holder.itemView.setOnClickListener {
            activity.startActivity(
                Intent(activity, PaymentOrderDetailActivity::class.java)
                    .putExtra(Utility.key.item_name, item.item_name)
                    .putExtra(Utility.key.order_date, item.order_date)
                    .putExtra(Utility.key.payment_method, item.payment_method)
                    .putExtra(Utility.key.subtotal, item.subtotal)
                    .putExtra(Utility.key.tax, item.tax)
                    .putExtra(Utility.key.tax_percent, item.tax_percent)
                    .putExtra(Utility.key.package_price, item.package_price)
                    .putExtra(Utility.key.final_price, item.final_price)
                    .putExtra(Utility.key.discount, item.discount)
                    .putExtra(Utility.key.discount_percent, item.discount_percent)
                    .putExtra(Utility.key.download_link, item.download_link)
                    .putExtra(Utility.key.view_link, item.view_link)
            )
        }

        if (position == paymentItems.size-1){
            holder.view.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return paymentItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val txtSubTitle: TextView = itemView.findViewById(R.id.txtSubTitle)
        val view: View = itemView.findViewById(R.id.view)

    }
}