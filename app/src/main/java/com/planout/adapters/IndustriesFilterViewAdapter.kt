package com.planout.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.VisitorSearchActivity
import com.planout.constant.Utility
import com.planout.models.IndustriesData

/**
 * Created by Atul Papneja on 11-Jul-22.
 */
class IndustriesFilterViewAdapter(
    internal var activity: VisitorSearchActivity,
    val industriesItems: ArrayList<IndustriesData>
) : RecyclerView.Adapter<IndustriesFilterViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.row_checked_filter_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=industriesItems[position]
        holder.checkView.text= Utility.toTitleCase(item.industry_name)
        holder.checkView.isChecked = item.isChecked
        holder.checkView.setOnCheckedChangeListener { buttonView, isChecked ->
            item.isChecked = isChecked
        }
    }

    override fun getItemCount(): Int {
        return industriesItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkView: AppCompatCheckBox =itemView.findViewById(R.id.checkView)


    }
}