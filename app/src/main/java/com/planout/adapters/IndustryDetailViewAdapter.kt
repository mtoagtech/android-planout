package com.planout.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.SeeMoreActivity
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.IndustryDetailModel

/**
 * Created by Atul Papneja on 04-Jul-22.
 */
class IndustryDetailViewAdapter(
    val requireActivity: FragmentActivity,
    val arrIndusDetailList: ArrayList<IndustryDetailModel>,
    val resultLauncherFavorite: ActivityResultLauncher<Intent>
)  : RecyclerView.Adapter<IndustryDetailViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.industry_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = arrIndusDetailList[position]
        holder.industryName.text=item.industry_name

        if (item.storeList.size<=0){
            holder.itemView.showOrGone(false)
        }else{
//            holder.recyclerBeach.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(requireActivity, LinearLayoutManager.HORIZONTAL, false)
            val beachViewAdapter = HomeBeachViewAdapter(requireActivity,item.storeList,resultLauncherFavorite)
            holder.recyclerBeach.layoutManager = layoutManager
            holder.recyclerBeach.adapter = beachViewAdapter
        }

        holder.beachbar_more.setOnClickListener {
            requireActivity.startActivity(
                Intent(requireActivity, SeeMoreActivity::class.java)
                    .putExtra(Utility.key.title, item.industry_name)
                    .putExtra(Utility.key.id, item.id)
            )
        }

    }

    override fun getItemCount(): Int {
        return arrIndusDetailList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val recyclerBeach:RecyclerView=itemView.findViewById(R.id.recyclerBeach)
        val industryName:TextView=itemView.findViewById(R.id.industryName)
        val beachbar_more:TextView=itemView.findViewById(R.id.beachbar_more)

    }
}