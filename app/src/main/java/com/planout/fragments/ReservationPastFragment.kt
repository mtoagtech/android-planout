package com.planout.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.activities.HomeVisitorActivity
import com.planout.activities.HomeVisitorActivity.Companion.reservationFrag
import com.planout.adapters.ReservationViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.ReservationData
import kotlinx.android.synthetic.main.fragment_reservation_past.view.*
import okhttp3.FormBody
import org.json.JSONObject

class ReservationPastFragment(val activityBase: HomeVisitorActivity) : Fragment(), ApiResponse {
    var selectedStatus: String=""
    lateinit var rootView: View
    lateinit var reservationViewAdapter: ReservationViewAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    var pageCount = 1
    private var lstCurrent = 0
    private var totalCount = 0
    private var isLoading = false
    val resverationItems: ArrayList<ReservationData> = ArrayList()
    override fun onResume() {
        super.onResume()
        try {
            reservationFrag.clickOnTab()

        }catch (e:Exception){
            e.printStackTrace()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_reservation_past, container, false)

        if (Utility.isLoginCheck(activityBase)) {
            resirvationListApi("past", "", activityBase)
        }else{
            rootView.linNoMore.showOrGone(true)
            rootView.recyclerPastRes.showOrGone(false)
        }
        viewData()
        return rootView
    }

    fun resirvationListApi(type: String, status: String, requireActivity: Activity) {

        selectedStatus=status
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.reservations + "?${Utility.key.page}=${pageCount}&${Utility.key.type}=$type&${Utility.key.status}=$status"
        CallApi.callAPi(mBuilder, API, requireActivity, Utility.reservationsPast, pageCount==1, Utility.GET, true)

    }

    private fun viewData() {
        rootView.recyclerPastRes.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            val total = totalCount
            val currentLastItem: Int = linearLayoutManager.findLastVisibleItemPosition()
            if (currentLastItem == total - 1 && isLoading && lstCurrent<=currentLastItem) {
                lstCurrent = currentLastItem
                rootView.loadView.showOrGone(true)
                pageCount += 1
                isLoading = false
                resirvationListApi("past","",activityBase)
            }
        }
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.reservationsPast){
            if (isData){
                val data=result.getJSONObject(Utility.key.data)
                val total=data.getInt(Utility.key.total)
                val currentPage=data.getInt(Utility.key.current_page)
                if (currentPage == 1){
                    resverationItems.clear()
                }
                val total_unread_notifications=data.getInt(Utility.key.total_unread_notifications)
                activityBase.setNotificationView(total_unread_notifications>0)
                if (total<=0){
                    rootView.linNoMore.showOrGone(true)
                    rootView.recyclerPastRes.showOrGone(false)
                }else{
                    rootView.linNoMore.showOrGone(false)
                    rootView.recyclerPastRes.showOrGone(true)
                    val records=data.getJSONArray(Utility.key.records)
                    totalCount = records.length()
                    isLoading = records.length()==20
                    for (i in 0 until records.length()){
                        val item=ReservationData()
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
                        if (recordsObj.getString(Utility.key.location_id)!="null"){
                            item.location=recordsObj.getJSONObject(Utility.key.location)

                        }
                        resverationItems.add(item)
                    }

                    if (pageCount == 1) {
//                        rootView.recyclerPastRes.setHasFixedSize(true)
                        reservationViewAdapter =
                            ReservationViewAdapter(activityBase, resverationItems, "2")
                        linearLayoutManager = LinearLayoutManager(
                            activityBase,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        rootView.recyclerPastRes.layoutManager = linearLayoutManager
                        rootView.recyclerPastRes.adapter = reservationViewAdapter
                    }else{
                        rootView.loadView.showOrGone(false)
                        reservationViewAdapter.notifyDataSetChanged()
                    }

                }

            }else{
                rootView.linNoMore.showOrGone(true)
                rootView.recyclerPastRes.showOrGone(false)

            }
        }
    }

    fun getDataFromResult(id: String?, pos: Int?, status: String?) {
        for (i in 0 until resverationItems.size){
            if (id == resverationItems[i].id){
                resverationItems[i].status = status!!
                reservationViewAdapter.notifyItemChanged(i)
            }
        }
    }

}