package com.planout.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.FavoritesActivity
import com.planout.activities.SavedSearchActivity
import com.planout.models.IndustriesData
import com.planout.models.TagData

class TagsViewAdapter(
    val activity: Activity?,
    val tags: ArrayList<IndustriesData> //TagData
) : RecyclerView.Adapter<TagsViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_tags_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=tags[position]
        holder.txtSelectTitle.text=item.industry_name
        when(position%3){
            0->{
                holder.txtSelectTitle.setBackgroundResource(R.drawable.bg_orange_drawable)
                holder.txtSelectTitle.setTextColor(ContextCompat.getColor(activity!!, R.color.app_orange))
            }
            1->{
                holder.txtSelectTitle.setBackgroundResource(R.drawable.bg_parrot_drawable)
                holder.txtSelectTitle.setTextColor(ContextCompat.getColor(activity!!, R.color.app_parrot))
            }
            2->{
                holder.txtSelectTitle.setBackgroundResource(R.drawable.bg_pink_drawable)
                holder.txtSelectTitle.setTextColor(ContextCompat.getColor(activity!!, R.color.app_pink))
            }
        }
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtSelectTitle:TextView=itemView.findViewById(R.id.txtSelectTitle)
    }
}