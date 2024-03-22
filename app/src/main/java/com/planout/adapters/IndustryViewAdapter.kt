package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.planout.R
import com.planout.activities.FavoritesActivity
import com.planout.activities.SavedSearchActivity
import com.planout.activities.SeeMoreActivity
import com.planout.constant.Utility
import com.planout.models.IndusHomeModel

class IndustryViewAdapter(
    val activity: FragmentActivity?,
    val arrayList: ArrayList<IndusHomeModel>
) : RecyclerView.Adapter<IndustryViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_home_industry_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Utility.SetImageSimple(activity!!, arrayList[position].industry_image, holder.imgItem)
//        Glide.with(activity!!)
//            .load(arrayList[position].industry_image)
//            .placeholder(R.drawable.placeholder_img)
//            .error(R.drawable.placeholder_img)
//            .into(holder.imgItem)
        holder.txtItem.text = Utility.toTitleCase(arrayList[position].industry_name)
        holder.itemView.setOnClickListener {
            activity.startActivity(
                Intent(activity, SeeMoreActivity::class.java)
                    .putExtra(Utility.key.title, arrayList[position].industry_name)
                    .putExtra(Utility.key.id, arrayList[position].id)
            )
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgItem = itemView.findViewById<ImageView>(R.id.imgItem)
        val txtItem = itemView.findViewById<TextView>(R.id.txtItem)
    }
}