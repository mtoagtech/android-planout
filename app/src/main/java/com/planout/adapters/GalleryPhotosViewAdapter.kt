package com.planout.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.PhotoGalleryActivity
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.StoreMediaData

class GalleryPhotosViewAdapter(
    val activity: PhotoGalleryActivity?,
    val storeMediaItems: ArrayList<StoreMediaData>,
    val btnSave: Button
) : RecyclerView.Adapter<GalleryPhotosViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_gallery_photo_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=storeMediaItems[position]

        if (item.isUrl){
            Utility.SetImageSimple(activity!!,item.media_url,holder.mediaImage)
        }else{
            holder.mediaImage.setImageBitmap(item.imageBitmap)
        }

        holder.imgDelete.setOnClickListener {
            //dialog for delete photo confirmation
            activity!!.showDeletePopUp(activity!!.getString(R.string.delete_photo),
                activity!!.getString(R.string.sure_delete_photo),
                item.isUrl,item.id,
                position)
        }
        if (storeMediaItems.size == 0){
            btnSave.showOrGone(false)
        }else{
            btnSave.showOrGone(true)
        }
    }

    override fun getItemCount(): Int {
        return storeMediaItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgDelete = itemView.findViewById<ImageView>(R.id.imgDelete)
        val mediaImage = itemView.findViewById<ImageView>(R.id.mediaImage)
    }
}