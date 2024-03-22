package com.planout.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.FavoritesActivity

class FavoritesAdapter(val activity: FavoritesActivity?) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var tagsAdapter : TagsViewAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_favorite_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        holder.recyclerTags.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
//        tagsAdapter = TagsViewAdapter(activity!!, item.tags)
//        holder.recyclerTags.layoutManager = layoutManager
//        holder.recyclerTags.adapter = tagsAdapter
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerTags = itemView.findViewById<RecyclerView>(R.id.recyclerTags)
    }
}