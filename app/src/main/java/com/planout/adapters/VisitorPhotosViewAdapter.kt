package com.planout.adapters

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.planout.R
import com.planout.activities.VisitorPhotoGalleryActivity
import com.planout.constant.Utility
import com.planout.models.StoreMediaData
import com.yarolegovich.discretescrollview.DiscreteScrollView
import kotlinx.android.synthetic.main.activity_visitor_photo_gallery.*

/**
 * Created by Atul Papneja on 06-Jul-22.
 */
class VisitorPhotosViewAdapter(
    val visitorPhotoGalleryActivity: VisitorPhotoGalleryActivity,
    val listPrivate: ArrayList<StoreMediaData>,
    val carouselView: DiscreteScrollView
) : RecyclerView.Adapter<VisitorPhotosViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_visitot_photos_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=listPrivate[position]
        if (item.isUrl){
            val sdk = Build.VERSION.SDK_INT;
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                holder.itemViewMain.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        visitorPhotoGalleryActivity,
                        R.drawable.rounded_green_image_back
                    )
                )
            } else {
                holder.itemViewMain.background =
                    ContextCompat.getDrawable(visitorPhotoGalleryActivity, R.drawable.rounded_green_image_back)
            }
        }else{
            holder.itemViewMain.background =null
        }

        Utility.SetImageSimple(visitorPhotoGalleryActivity!!,listPrivate[position].media_url,holder.image)
        holder.itemView.setOnClickListener {
            for (i in 0 until listPrivate.size){
                listPrivate[i].isUrl = i == position
            }
            notifyDataSetChanged()
            carouselView.scrollToPosition(position)

        }
    }

    override fun getItemCount(): Int {
        return listPrivate.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView =itemView.findViewById(R.id.image)
        val itemViewMain: ConstraintLayout =itemView.findViewById(R.id.itemViewMain)
    }
}