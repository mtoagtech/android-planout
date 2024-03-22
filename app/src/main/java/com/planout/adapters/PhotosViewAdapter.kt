package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.planout.R
import com.planout.activities.FavoritesActivity
import com.planout.activities.SavedSearchActivity
import com.planout.activities.VisitorPhotoGalleryActivity
import com.planout.constant.Utility
import com.planout.models.StoreMediaData

class PhotosViewAdapter(
    val activity: Activity?,
    val storeMediaItems: ArrayList<StoreMediaData>,
    val type: String
) : RecyclerView.Adapter<PhotosViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_photos_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Utility.SetImageSimple(activity!!,storeMediaItems[position].media_url,holder.image)
        holder.itemView.setOnClickListener {
            if (type=="view"){
                activity.startActivity(
                    Intent(activity, VisitorPhotoGalleryActivity::class.java)
                        .putExtra("private_list", Gson().toJson(storeMediaItems))
                        .putExtra(Utility.key.isFrom, "image")
                        .putExtra(Utility.key.itemposition, position.toString())
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return storeMediaItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image:ImageView=itemView.findViewById(R.id.image)
    }
}