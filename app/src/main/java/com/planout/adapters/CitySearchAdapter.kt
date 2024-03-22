package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.activities.CityListScreen
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.CityModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Atul Papneja on 21-Jun-22.
 */
class CitySearchAdapter(
    val cityListScreen: CityListScreen,
    var cityList: ArrayList<CityModel>,
    val recycler_city_search: RecyclerView,
    val no_data: LinearLayout,
    val no_data_txt: TextView
)
    : RecyclerView.Adapter<CitySearchAdapter.MyViewHolder>(), Filterable {

    private val mStringFilterList: ArrayList<CityModel> = cityList
    private var valueFilter: ValueFilter? = null
    private var searchString = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_search_city, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cityModel = cityList[position]
        holder.adventureCityName.text = cityModel.cityName

        Utility.animationClick(holder.itemView).setOnClickListener {
            val intent = Intent()
            intent.putExtra("CityName", cityModel.cityName)
            intent.putExtra("CityID", cityModel.cityID)
            cityListScreen.setResult(Activity.RESULT_OK, intent)
            cityListScreen.finish()
        }
    }

    override fun getItemCount(): Int {
        return cityList.size
    }


    inner class MyViewHolder
    internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var adventureCityName: TextView = itemView.findViewById(R.id.txt_cityname)
    }
    override fun getFilter(): Filter? {
        if (valueFilter == null) {
            valueFilter = ValueFilter()
        }
        return valueFilter
    }
    private inner class ValueFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            searchString = constraint.toString()
            val results = FilterResults()
            val filterList = ArrayList<CityModel>()
            if (constraint.isNotEmpty()) {
                for (g in mStringFilterList) {
                    if (g.cityName.lowercase(Locale.getDefault()).trim()
                            .contains(constraint.toString().lowercase(Locale.getDefault()))
                    )
                        filterList.add(g)
                }
                results.count = filterList.size
                results.values = filterList
            } else {
                results.count = mStringFilterList.size
                results.values = mStringFilterList
            }
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            cityList = results.values as ArrayList<CityModel>
            notifyDataSetChanged()
            if (cityList.size == 0) {
                recycler_city_search.showOrGone(false)
                no_data.showOrGone(true)
                no_data_txt.text = "No City found as '$searchString'"
            } else {
                recycler_city_search.showOrGone(true)
                no_data.showOrGone(false)
            }
        }

    }
}