package com.planout.adapters

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.BusinessDetailsActivity
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.models.StoreModel
import kotlinx.android.synthetic.main.activity_business_details.*
import okhttp3.FormBody

class HomeBeachViewAdapter(
    val activity: Activity?,
    val storeList: ArrayList<StoreModel>,
    val resultLauncherFavorite: ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<HomeBeachViewAdapter.ViewHolder>() {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var tagsAdapter : TagsViewAdapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_beach_restro_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=storeList[position]
        Utility.SetImageSimple(activity!!,item.cover_image,holder.bannerImage)
        Utility.SetImageSimple(activity!!,item.store_image,holder.storeImage)
        holder.txtTitle.text=item.store_name
        if (item.default_location_id!="null"){
            holder.txtAddress.text=item.default_location.getString("address")
        }else{
            holder.txtAddress.text=activity!!.getString(R.string.not_available)
        }

//        holder.recyclerTags.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        tagsAdapter = TagsViewAdapter(activity!!,item.industries)
        holder.recyclerTags.layoutManager = layoutManager
        holder.recyclerTags.adapter = tagsAdapter

        /*val mScrollTouchListener: RecyclerView.OnItemTouchListener = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                when (action) {
                    MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(true)
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        }
        holder.recyclerTags.addOnItemTouchListener(mScrollTouchListener)*/

        holder.itemView.setOnClickListener {
            resultLauncherFavorite.launch(Intent(activity, BusinessDetailsActivity::class.java)
                .putExtra(Utility.key.id,item.id))
//            activity.startActivity(Intent(activity,BusinessDetailsActivity::class.java)
//                .putExtra(Utility.key.id,item.id))
        }

        if (item.is_favorite=="true"){
            holder.imgFav.setImageResource(R.drawable.ic_icon_fav_true)
        }else{
            holder.imgFav.setImageResource(R.drawable.ic_icon_fav_false)
        }

        holder.imgPhone.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.CALL_PHONE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.CALL_PHONE),
                    5
                )
            } else {
                val call_number = item.mobile
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$call_number"))
                activity.startActivity(intent)
            }
        }
        holder.imgFav.setOnClickListener {
            if (Utility.isLoginCheck(activity)) {
                if (Utility.hasConnection(activity)) {
                    if (item.is_favorite == "true") {
                        holder.imgFav.setImageResource(R.drawable.ic_icon_fav_false)
                        item.is_favorite = "false"
                        removeFavApi(item.id)
                    } else {
                        item.is_favorite = "true"
                        holder.imgFav.setImageResource(R.drawable.ic_icon_fav_true)
                        addFavApi(item.id)
                    }
                }
            }
        }
    }

    private fun addFavApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, "${ApiController.api.favorites}/$id", activity!!, Utility.favoritesAdd+"_$id", true, Utility.POST, true)
    }

    private fun removeFavApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, "${ApiController.api.favorites}/$id", activity!!, Utility.favoritesRemove+"_$id", true, Utility.DELETE, true)

    }

    override fun getItemCount(): Int {
        return storeList.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerTags = itemView.findViewById<RecyclerView>(R.id.recyclerTags)
        val bannerImage = itemView.findViewById<ImageView>(R.id.bannerImage)
        val storeImage = itemView.findViewById<ImageView>(R.id.storeImage)
        val txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)
        val txtAddress = itemView.findViewById<TextView>(R.id.txtAddress)
        val imgPhone = itemView.findViewById<ImageView>(R.id.imgPhone)
        val imgFav = itemView.findViewById<ImageView>(R.id.imgFav)
        val btnBook = itemView.findViewById<TextView>(R.id.btnBook)
    }
}