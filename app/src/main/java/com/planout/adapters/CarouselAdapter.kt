package com.planout.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.VisitorPhotoGalleryActivity
import com.planout.constant.TouchImageView
import com.planout.constant.Utility

/**
 * Created by Atul Papneja on 06-Jul-22.
 */
class CarouselAdapter(
    val photosurllist1: VisitorPhotoGalleryActivity,
    val photosurllist: ArrayList<String>
)
    : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carousel_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return photosurllist.size
    }

    override fun onBindViewHolder(holder: CarouselAdapter.ViewHolder, position: Int) {
        val item = photosurllist[position]

        Utility.SetImageSimple(photosurllist1,item,holder.img_view)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img_view: TouchImageView =itemView.findViewById(R.id.img_view)

    }
}
