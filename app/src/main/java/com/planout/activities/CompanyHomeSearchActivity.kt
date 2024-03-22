package com.planout.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.adapters.HomeConfirmedViewAdapter
import com.planout.adapters.HomeDeclinedViewAdapter
import com.planout.adapters.HomePendingViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.ReservationData
import kotlinx.android.synthetic.main.activity_company_home_search.*
import kotlinx.android.synthetic.main.activity_company_home_search.editSearch
import kotlinx.android.synthetic.main.activity_company_home_search.imgBack
import kotlinx.android.synthetic.main.activity_visitor_search.loadView
import okhttp3.FormBody
import org.json.JSONObject
import java.util.ArrayList

class CompanyHomeSearchActivity : AppCompatActivity() , ApiResponse {

    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var pendingViewAdapter: HomePendingViewAdapter
    var selectedStatus=""
    private var pageCount = 1
    private var lstCurrent = 0
    private var isLoading = false
    val resverationItems: ArrayList<ReservationData> = ArrayList()

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val storeId: String = intent.extras!!.getString("store_id").toString()
            val resId: String = intent.extras!!.getString("res_id").toString()
            val status: String = intent.extras!!.getString("status").toString()
            if (selectedStatus == Utility.RES_PENDING_STATUS){
                for (i in 0 until resverationItems.size){
                    if (resId == resverationItems[i].id){
                        resverationItems.removeAt(i)
                        (pendingViewAdapter as RecyclerView.Adapter<*>).notifyItemRemoved(i)
                        return
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_home_search)

        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        //open soft-input keyboard
        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            constTop.applicationWindowToken, InputMethodManager.SHOW_FORCED, 0)
        selectedStatus=intent.getStringExtra(Utility.key.status)!!
        viewData()
        clickView()
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
            IntentFilter("Reservation_Cancelled")
        )
    }
    fun resirvationListApi(type: String, status: String, requireActivity: Activity, text: String) {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.reservations + "?${Utility.key.page}=$pageCount&${Utility.key.type}=$type&${Utility.key.status}=$status&${Utility.key.searchkey}=$text"
        CallApi.callAPi(mBuilder, API, requireActivity, Utility.reservationsPending, pageCount==1, Utility.GET, true)

    }

    private fun viewData() {
        editSearch.requestFocus()
        editSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utility.hideSoftKeyboard(this)
                val text=editSearch.text.toString()
                if (text.isEmpty()){
                    Utility.customErrorToast(this, getString(R.string.search_by_reservation_no_and_phone))
                }else {
                    pageCount = 1
                    resirvationListApi("", selectedStatus, this, text)
                }
                true
            } else false
        }

        editSearch.doOnTextChanged { text, start, before, count ->
            if (text!!.length>3) {
                imgSearchCross.visibility = View.VISIBLE
            } else {
                imgSearchCross.visibility = View.GONE
            }
        }

        recyclerSearchRes.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            val total = recyclerSearchRes.layoutManager!!.itemCount
            val currentLastItem: Int = linearLayoutManager.findLastVisibleItemPosition()
            if (currentLastItem == total - 1 && isLoading ) {
                lstCurrent = currentLastItem
                loadView.showOrGone(true)
                pageCount += 1
                resirvationListApi("", selectedStatus, this, editSearch.text.toString())
            }
        }
    }

    private fun clickView() {
        imgBack.setOnClickListener {
            Utility.hideSoftInput(this)
            onBackPressed()
        }

        imgSearchCross.setOnClickListener {
            editSearch.setText("")
        }
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.reservationsPending){
            if (isData){

                val data=result.getJSONObject(Utility.key.data)
                val total=data.getInt(Utility.key.total)

                if (total<=0){
                    linNoMore.showOrGone(true)
                    horiScroll.showOrGone(false)
                }else{
                    linNoMore.showOrGone(false)
                    horiScroll.showOrGone(true)
                    val records=data.getJSONArray(Utility.key.records)
                    isLoading = records.length()==20
                    if (pageCount == 1) {
                        resverationItems.clear();
                    }
                    for (i in 0 until records.length()){
                        val item= ReservationData()
                        val recordsObj=records.getJSONObject(i)
                        item.id=recordsObj.getString(Utility.key.id)
                        item.store_id=recordsObj.getString(Utility.key.store_id)
                        item.res_id=recordsObj.getString(Utility.key.res_id)
                        item.contact_name=recordsObj.getString(Utility.key.contact_name)
                        item.contact_mobile=recordsObj.getString(Utility.key.contact_mobile)
                        item.total_people=recordsObj.getString(Utility.key.total_people)
                        item.resdate=recordsObj.getString(Utility.key.resdate)
                        item.restime=recordsObj.getString(Utility.key.restime)
                        item.store_name=recordsObj.getString(Utility.key.store_name)
                        item.name=recordsObj.getString(Utility.key.name)
                        item.status=recordsObj.getString(Utility.key.status)
                        item.location_id=recordsObj.getString(Utility.key.location_id)
                        item.preferred_table=recordsObj.getString(Utility.key.preferred_table)
                        item.extra_notes=recordsObj.getString(Utility.key.extra_notes)
                        item.store_image=recordsObj.getString(Utility.key.store_image)
                        item.remark=recordsObj.getString(Utility.key.remark)
                        item.is_arrived=recordsObj.getInt(Utility.key.is_arrived)
                        item.table_no = recordsObj.getString(Utility.key.table_no)
                        item.age_group = recordsObj.getString(Utility.key.age_group)
                        item.isEdit = false
                        if (recordsObj.getString(Utility.key.location_id)!="null"){
                            item.location=recordsObj.getJSONObject(Utility.key.location)
                        }
                        resverationItems.add(item)
                    }

                    if (pageCount == 1) {
                        pendingViewAdapter = HomePendingViewAdapter(this, resverationItems,this)

//                        recyclerSearchRes.setHasFixedSize(true)
                        linearLayoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                        recyclerSearchRes.layoutManager = linearLayoutManager
                        recyclerSearchRes.adapter = pendingViewAdapter as RecyclerView.Adapter<*>
                    }else{
                        loadView.showOrGone(false)
                        (pendingViewAdapter as RecyclerView.Adapter<*>).notifyDataSetChanged()
                    }
                }
            }else{
                linNoMore.showOrGone(true)
                horiScroll.showOrGone(false)
            }

        }else if (type==Utility.reservationStatusUpdate){
            if (isData){

                Utility.customSuccessToast(this@CompanyHomeSearchActivity, Utility.checkStringNullOrNot(result, Utility.key.message))
            }else{

                Utility.customErrorToast(this@CompanyHomeSearchActivity, Utility.checkStringNullOrNot(result, Utility.key.message))
            }

        }else if (type == Utility.reservationsDeclinedAdapter) {
            if (isData) {
                Utility.customSuccessToast(this@CompanyHomeSearchActivity, result.getString(Utility.key.message))
                getDataFromResult(
                    pendingViewAdapter.declineConfirmId,
                    pendingViewAdapter.declineConfirmPos,
                    true
                )
                pendingViewAdapter.notifyDataSetChanged()
            } else {
                Utility.customErrorToast(this@CompanyHomeSearchActivity, result.getString(Utility.key.message))
            }
        }


    }
    fun getDataFromResult(id: String?, pos: Int?, status: Boolean) {
        for (i in 0 until resverationItems.size) {
            if (id == resverationItems[i].id) {
                resverationItems.removeAt(i)
                pendingViewAdapter.notifyItemRemoved(i)

                return
            }
        }
    }

}