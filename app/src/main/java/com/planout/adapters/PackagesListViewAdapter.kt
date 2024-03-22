package com.planout.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.PackageDetailActivity
import com.planout.constant.Utility
import com.planout.models.PackagesData

class PackagesListViewAdapter(
    val activity: Activity?,
    val packageList: ArrayList<PackagesData>,
    val txtCouponCode: TextView
) : RecyclerView.Adapter<PackagesListViewAdapter.ViewHolder>() {
    var isVoucher = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_package_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = packageList[position]

        if (item.isSelected){
            holder.item_blue.background = ContextCompat.getDrawable(activity!!, R.drawable.bg_tags_selected_drawable)
            holder.item_blue.backgroundTintList  = ContextCompat.getColorStateList(activity, R.color.app_green)
            holder.pack_month.setTextColor(ContextCompat.getColor(activity, R.color.app_green))
            holder.pack_amount.setTextColor(ContextCompat.getColor(activity, R.color.app_green))
            /*if (item.available_vouchers.size<=0){
                txtCouponCode.visibility = View.GONE
            }else{
                txtCouponCode.visibility = View.VISIBLE
            }*/
            isVoucher = item.available_vouchers.size>0
        }else{
            holder.item_blue.background = ContextCompat.getDrawable(activity!!, R.drawable.bg_round_edit_drawable)
            holder.item_blue.backgroundTintList  = ContextCompat.getColorStateList(activity, R.color.gray_F7_33)
            holder.pack_month.setTextColor(ContextCompat.getColor(activity, R.color.gray_5B_FF))
            holder.pack_amount.setTextColor(ContextCompat.getColor(activity, R.color.gray_5B_FF))
        }

        holder.itemView.setOnClickListener {
            var SelectedItem = PackagesData()
            for (i in 0 until packageList.size){
                packageList[i].isSelected = i==position
                if (i == position){
                    SelectedItem=packageList[i]
                }
            }
            //set package data
            (activity as PackageDetailActivity).setDataSelectedPackage(SelectedItem)
            notifyDataSetChanged()
        }

        holder.pack_month.text=item.duration+" ${activity!!.getString(R.string.months)}"
        holder.pack_amount.text=activity.getString(R.string.currency,item.price)

    }

    override fun getItemCount(): Int {
        return packageList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item_blue = itemView.findViewById<LinearLayout>(R.id.item_blue)
        val pack_month = itemView.findViewById<TextView>(R.id.pack_month)
        val pack_amount = itemView.findViewById<TextView>(R.id.pack_amount)

    }
}