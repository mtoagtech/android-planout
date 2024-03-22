package com.planout.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.planout.R
import com.planout.adapters.SavedSearchAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.SavedSearchData
import kotlinx.android.synthetic.main.activity_saved_search.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONObject

class SavedSearchActivity : AppCompatActivity(), ApiResponse {

    lateinit var layoutManager: LinearLayoutManager
    lateinit var savedSearchAdapter: SavedSearchAdapter

    val savedSearchItems: ArrayList<SavedSearchData> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Utility.setStatusBarColor(this)
        setContentView(R.layout.activity_saved_search)
        //window.statusBarColor = ContextCompat.getColor(this, R.color.gray_F8_48)

        txtHeader.text = getString(R.string.saved_search_title)
        clickView()
        //call api for saved search
        savedSearchApi()
    }

    private fun savedSearchApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.searches, this, Utility.searches, true, Utility.GET, true)
    }


    private fun clickView() {
        Utility.animationClick(imgBackHeader).setOnClickListener { onBackPressed() }
    }


    fun openDeleteDialog(id:String) {
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_delete_popup_view, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val dialogTitleTxt=dialogView.findViewById<TextView>(R.id.txtTitle)
        dialogTitleTxt.text= getString(R.string.are_you_sure_you_want_to_delete_saved_search)

        val btnYes=dialogView.findViewById<Button>(R.id.btnDelete)
        val btnNo=dialogView.findViewById<Button>(R.id.btnCancel)
        val imgTop=dialogView.findViewById<ImageView>(R.id.imgTop)
        Utility.animationClick(btnYes).setOnClickListener {
            //call api for delete particular saved search
            deleteSavedSearchAPi(id)
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(btnNo).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }

    private fun deleteSavedSearchAPi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, "${ApiController.api.searches}/$id", this, Utility.searchesDelete, true, Utility.DELETE, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.searches){
            if (isData){
                savedSearchItems.clear()
                val data=result.getJSONArray(Utility.key.data)
                if (data.length()!=0){
                    constNoData.showOrGone(false)
                    dataAria.showOrGone(true)
                    for (i in 0 until data.length()) {
                        val dataObj=data.getJSONObject(i)
                        val savedSearchItem = SavedSearchData()
                        savedSearchItem.id=dataObj.getString("id")
                        savedSearchItem.user_id=dataObj.getString("user_id")
                        savedSearchItem.search_title=dataObj.getString("search_title")
                        savedSearchItem.search_data=dataObj.getString("search_data")
                        savedSearchItem.created_at=dataObj.getString("created_at")
                        savedSearchItem.updated_at=dataObj.getString("updated_at")
                        savedSearchItems.add(savedSearchItem)
                    }
//                    recyclerSaved.setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    savedSearchAdapter = SavedSearchAdapter(this,savedSearchItems)
                    recyclerSaved.layoutManager = layoutManager
                    recyclerSaved.adapter = savedSearchAdapter
                }else{
                    constNoData.showOrGone(true)
                    dataAria.showOrGone(false)
                }
            }else{
                constNoData.showOrGone(true)
                dataAria.showOrGone(false)
            }
        }else if (type==Utility.searchesDelete){
            if (isData){
                //call api for reservation status detail
                savedSearchApi()
            }else {
                Utility.customErrorToast(
                    this,
                    Utility.checkStringNullOrNot(result, Utility.key.message)
                )
            }
        }
    }
}