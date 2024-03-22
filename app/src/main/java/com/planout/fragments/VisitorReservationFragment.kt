package com.planout.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.planout.R
import com.planout.activities.HomeVisitorActivity
import com.planout.api_calling.ApiResponse
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import kotlinx.android.synthetic.main.activity_reserve_table_status.*
import kotlinx.android.synthetic.main.fragment_company_event.view.*
import kotlinx.android.synthetic.main.fragment_visitor_reservation.view.*
import kotlinx.android.synthetic.main.fragment_visitor_reservation.view.pager
import kotlinx.android.synthetic.main.fragment_visitor_reservation.view.tab_layout
import org.json.JSONObject


class VisitorReservationFragment(val activityBase: HomeVisitorActivity) : Fragment(), ApiResponse {
    var tabPosition =0

    lateinit var rootView: View
    private var arrFragList : ArrayList<Fragment> = ArrayList()

    val tabsArray = arrayOf(
        R.string.upcoming.toString(),
        R.string.past.toString(),
    )

    lateinit var upcomingFrag: ReservationUpcomingFragment
    lateinit var pastFrag: ReservationPastFragment

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val storeId: String = intent.extras!!.getString("store_id").toString()
            val resId: String = intent.extras!!.getString("res_id").toString()
            val status: String = intent.extras!!.getString("status").toString()
            upcomingFrag.getDataFromResult(resId, 0, status)
            pastFrag.getDataFromResult(resId, 0, status)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_visitor_reservation, container, false)
        val nightModeFlags = activityBase.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                activityBase.window.decorView.systemUiVisibility = View.VISIBLE
                activityBase.window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                activityBase.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
                activityBase.window.statusBarColor = ContextCompat.getColor(activityBase,R.color.white);// set status background white
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                activityBase.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;//  set status text dark
                activityBase.window.statusBarColor = ContextCompat.getColor(activityBase,R.color.white);// set status background white
            }
        }
        clickView()
        upcomingFrag = ReservationUpcomingFragment(activityBase)
        pastFrag = ReservationPastFragment(activityBase)
        setViewData()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver((mMessageReceiver),
            IntentFilter("Reservation")
        )
        return rootView
    }

    override fun onResume() {
        super.onResume()
//        clickOnTab()
    }

    override fun onStop() {
        super.onStop()
        rootView.pager.isSaveFromParentEnabled = false
    }

    private fun setViewData() {
        arrFragList.clear()
        arrFragList.add(upcomingFrag)
        arrFragList.add(pastFrag)
        setFragmentsView()
    }

    private fun setFragmentsView() {
        val tabsArray = arrayOf(
            getString(R.string.upcoming),
            getString(R.string.past),
        )
        val adapter =
            com.planout.adapters.ViewPagerAdapter(childFragmentManager, lifecycle, arrFragList)
        rootView.pager.adapter = adapter
        rootView.pager.isUserInputEnabled = false

        TabLayoutMediator(rootView.tab_layout, rootView.pager) { tab, position ->
            tab.text = tabsArray[position]
            Log.d("All tab text :- ",tabsArray[position])
        }.attach()
        rootView.tab_layout.getTabAt(0)!!.select()
    }

    private fun clickView() {
        rootView.imgFilter.setOnClickListener {
            if (Utility.isLoginCheck(activityBase)) {
                //filter for reservation detail
                openFilterDialog()
            }
        }
    }

    fun clickOnTab(){
        try {
            //show filter apply or not
            if (isFilteredOrNot(rootView.tab_layout.selectedTabPosition)){
                rootView.imgFilter.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_img_filter_on))
            }else{
                rootView.imgFilter.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_img_filter_off))
            }
        }catch (e:Exception){
            e.printStackTrace()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

    }

    private fun isFilteredOrNot(selectedTabPosition: Int): Boolean {
        var selectedStatus=""
        selectedStatus = if (selectedTabPosition==0){
            upcomingFrag.selectedStatus
        }else{
            pastFrag.selectedStatus
        }
        return selectedStatus!=""
    }

    private fun openFilterDialog() {
        tabPosition=rootView.tab_layout.selectedTabPosition

        val serviceInfoDialog = BottomSheetDialog(
            requireContext(),
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfoDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_filter_checkbox_view_popup, null)
        serviceInfoDialog.setContentView(dialogView)
        serviceInfoDialog.setCancelable(true)

        val btnApply=dialogView.findViewById<Button>(R.id.btnApply)
        val txtClear=dialogView.findViewById<TextView>(R.id.txtClear)
        val imgTop=dialogView.findViewById<ImageView>(R.id.imgTop)
        val checkAll=dialogView.findViewById<AppCompatCheckBox>(R.id.checkAll)
        val checkPending=dialogView.findViewById<AppCompatCheckBox>(R.id.checkPending)
        val checkConfirmed=dialogView.findViewById<AppCompatCheckBox>(R.id.checkConfirmed)
        val checkCanceled=dialogView.findViewById<AppCompatCheckBox>(R.id.checkCanceled)
        val checkDeclined=dialogView.findViewById<AppCompatCheckBox>(R.id.checkDeclined)

        checkAll.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                checkPending.isChecked = false
                checkConfirmed.isChecked = false
                checkCanceled.isChecked = false
                checkDeclined.isChecked = false
            }
        }
        checkPending.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                checkAll.isChecked = false
                checkConfirmed.isChecked = false
                checkCanceled.isChecked = false
                checkDeclined.isChecked = false
            }
        }
        checkConfirmed.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                checkAll.isChecked = false
                checkPending.isChecked = false
                checkCanceled.isChecked = false
                checkDeclined.isChecked = false
            }
        }
        checkCanceled.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                checkAll.isChecked = false
                checkPending.isChecked = false
                checkConfirmed.isChecked = false
                checkDeclined.isChecked = false
            }
        }
        checkDeclined.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                checkAll.isChecked = false
                checkPending.isChecked = false
                checkConfirmed.isChecked = false
                checkCanceled.isChecked = false
            }
        }

        var selectedStatus=""
        selectedStatus = if (tabPosition==0){
            upcomingFrag.selectedStatus
        }else{
            pastFrag.selectedStatus
        }
        when (selectedStatus) {
            "" -> {
                checkAll.isChecked=true
            }
            Utility.RES_PENDING_STATUS -> {
                checkPending.isChecked=true
            }
            Utility.RES_DECLINED_STATUS -> {
                checkDeclined.isChecked=true
            }
            Utility.RES_CANCELLED_STATUS -> {
                checkCanceled.isChecked=true
            }
            Utility.RES_CONFIRMED_STATUS -> {
                checkConfirmed.isChecked=true
            }
        }
        //action for apply filter |  #single selection apply
        Utility.animationClick(btnApply).setOnClickListener {
            var resStatus=""
            when {
                checkAll.isChecked -> {
                    resStatus=""
                }
                checkPending.isChecked -> {
                    resStatus=Utility.RES_PENDING_STATUS
                }
                checkConfirmed.isChecked -> {
                    resStatus=Utility.RES_CONFIRMED_STATUS
                }
                checkCanceled.isChecked -> {
                    resStatus=Utility.RES_CANCELLED_STATUS
                }
                checkDeclined.isChecked -> {
                    resStatus=Utility.RES_DECLINED_STATUS
                }
            }
            if (!checkAll.isChecked){
                if (resStatus == ""){
                    checkAll.isChecked = true
                    return@setOnClickListener
                }
            }else {
                if (resStatus == "") {
                    checkAll.isChecked = true
                }
            }
            if (tabPosition==0){
                upcomingFrag.pageCount=1
                upcomingFrag.resirvationListApi("upcoming",resStatus,activityBase)
            }else{
                pastFrag.pageCount=1
                pastFrag.resirvationListApi("past", resStatus, activityBase)
            }
            rootView.imgFilter.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_img_filter_on))

            serviceInfoDialog.dismiss()
        }
        //action for clear filter |  #all selected
        Utility.animationClick(txtClear).setOnClickListener {
            if (tabPosition==0){
                upcomingFrag.resirvationListApi("upcoming", "", activityBase)
            }else{
                pastFrag.resirvationListApi("past", "", activityBase)
            }
            rootView.imgFilter.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_img_filter_off))
            serviceInfoDialog.dismiss()
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfoDialog.dismiss()
        }
        serviceInfoDialog.show()
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.reservationsUpcoming){
            upcomingFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.reservationsPast){
            pastFrag.onTaskComplete(result,type,isData)
        }
    }



}