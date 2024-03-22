package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.planout.R
import com.planout.constant.Utility
import com.planout.models.StoreLocationData


class AddressLocateViewAdapter(
    val activity: Activity?,
    val locations: ArrayList<StoreLocationData>,
    val mMap: GoogleMap?
)
    : RecyclerView.Adapter<AddressLocateViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_locate_location_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = locations[position]
        if (item.isSelected){
            holder.constMain.backgroundTintList = ContextCompat.getColorStateList(activity!!, R.color.gray_EC_13)
            holder.txtAddress.setTextColor(ContextCompat.getColor(activity, R.color.gray_5B_FF))
            val latD: Double = item.latitude.toDouble()
            val longtD: Double = item.longitude.toDouble()
            if (mMap!=null){
                mMap.clear()
                val marker = MarkerOptions().position(LatLng(latD, longtD))
                mMap.addMarker(marker)
                val center = CameraUpdateFactory.newLatLng(LatLng(latD, longtD))
                val zoom =
                    CameraUpdateFactory.newLatLngZoom(LatLng(latD, longtD), 15f)
                mMap.animateCamera(center)
                mMap.animateCamera(zoom)
            }

        }else{
            holder.constMain.backgroundTintList = ContextCompat.getColorStateList(activity!!, R.color.gray_FF_14)
            holder.txtAddress.setTextColor(ContextCompat.getColor(activity, R.color.gray_5B))
        }
        holder.txtAddress.text=item.address
        holder.constMain.setOnClickListener {
            for (i in 0 until locations.size){
                locations[i].isSelected = i == position
            }
            notifyDataSetChanged()
        }
        holder.btnLocate.setOnClickListener {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=${item.latitude},${item.longitude}")
                )
                activity!!.startActivity(intent)
            }catch (e:Exception){
                Utility.customInfoToast(activity,"Info!!","Map supported application not found in your device")
            }

        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val constMain = itemView.findViewById<ConstraintLayout>(R.id.constMain)
        val txtAddress = itemView.findViewById<TextView>(R.id.txtAddress)
        val btnLocate = itemView.findViewById<TextView>(R.id.btnLocate)
    }
}