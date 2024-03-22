package com.planout.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.planout.R
import com.planout.activities.SavedSearchActivity
import com.planout.activities.VisitorSearchActivity
import com.planout.constant.Utility
import com.planout.models.SavedSearchData
import org.json.JSONObject

class SavedSearchAdapter(
    val activity: SavedSearchActivity?,
    val savedSearchItems: ArrayList<SavedSearchData>
) : RecyclerView.Adapter<SavedSearchAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_saved_search_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item=savedSearchItems[position]
        holder.txtTitle.text=item.search_title
        holder.txtFirst.text=item.search_data
        holder.txtFirst.text= getJsonData(item.search_data)

        holder.imgDelete.setOnClickListener {
            activity!!.openDeleteDialog(item.id)
        }

        holder.itemView.setOnClickListener {
            val jsonObj = JsonObject()
            if (item.search_data.contains("{")) {
                val jsonObject = JSONObject(item.search_data)
                jsonObj.addProperty("searchText", jsonObject.getString("searchText"))
                jsonObj.addProperty("cityId", jsonObject.getString("cityId"))
                jsonObj.addProperty("cityName", jsonObject.getString("cityName"))
                jsonObj.addProperty("indusId", jsonObject.getString("indusId"))
                jsonObj.addProperty("indusName", jsonObject.getString("indusName"))
                jsonObj.addProperty("tagsId", jsonObject.getString("tagsId"))
                jsonObj.addProperty("tagsName", jsonObject.getString("tagsName"))
            }
            activity!!.startActivity(Intent(activity, VisitorSearchActivity::class.java)
                .putExtra("Title", item.search_title)
                .putExtra("jsonVal", jsonObj.toString()))
        }

    }

    private fun getJsonData(searchData: String): String {
        var strData = ""
        if (searchData.contains("{")){
            val jsonObj = JSONObject(searchData)
            if (jsonObj.getString("cityName") != "City"){
                val arrCity =
                    jsonObj.getString("cityId").replace("[", "").replace("]", "").split(",")
                val added = if (arrCity.size-1 <= 0){
                    ""
                }else{
                    if (jsonObj.getString("cityName").contains("+")) {
                        ""
                    }else{
                        " +"+(arrCity.size-1)
                    }
                }
                strData = jsonObj.getString("cityName")+added
            }
            if (jsonObj.getString("indusName") != "Industry"){
                val arrIndus =
                    jsonObj.getString("indusId").replace("[", "").replace("]", "").split(",")
                val added = if (arrIndus.size-1 <= 0){
                    ""
                }else{
                    if (jsonObj.getString("indusName").contains("+")) {
                        ""
                    }else{
                        " +"+(arrIndus.size-1)
                    }
                }
                if (strData.trim().isEmpty()){
                    strData =jsonObj.getString("indusName")+added
                }else{
                    strData = strData+" - "+jsonObj.getString("indusName")+added
                }
            }
            if (jsonObj.getString("tagsName") != "Tags"){
                val arrTags =
                    jsonObj.getString("tagsId").replace("[", "").replace("]", "").split(",")
                val added = if (arrTags.size-1 <= 0){
                    ""
                }else{
                    if (jsonObj.getString("tagsName").contains("+")) {
                        ""
                    }else{
                        " +"+(arrTags.size-1)
                    }
                }
                if (strData.trim().isEmpty()){
                    strData = jsonObj.getString("tagsName")+added
                }else{
                    strData = strData+" - "+jsonObj.getString("tagsName")+added
                }
            }
        }else{
            strData = searchData
        }
        return strData
    }

    override fun getItemCount(): Int {
        return savedSearchItems.size
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)
        val txtFirst = itemView.findViewById<TextView>(R.id.txtFirst)
        val imgDelete = itemView.findViewById<ImageView>(R.id.imgDelete)
    }
}