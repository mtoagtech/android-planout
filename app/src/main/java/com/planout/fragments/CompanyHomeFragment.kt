package com.planout.fragments

import android.app.Dialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.planout.BuildConfig
import com.planout.R
import com.planout.activities.CompanyHomeSearchActivity
import com.planout.activities.HomeCompanyActivity
import com.planout.activities.NotificationActivity
import com.planout.adapters.ViewPagerCompAdapter
import com.planout.api_calling.ApiResponse
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import kotlinx.android.synthetic.main.fragment_company_home.view.*
import org.json.JSONObject

class CompanyHomeFragment(val activityBase: HomeCompanyActivity) : Fragment(), ApiResponse {

    lateinit var rootView: View
    private var arrFragList : ArrayList<Fragment> = ArrayList()
    val tabsArray = arrayOf(
        "Pending",
        "Confirmed",
        "Declined"
    )
    var tabPosition =0
    lateinit var pendingFragment: CompHomePendingFragment
    lateinit var confirmedFragment: CompHomeConfirmedFragment
    lateinit var declinedFragment: CompHomeDeclinedFragment
    lateinit var tab_layout:TabLayout

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val storeId: String = intent.extras!!.getString("store_id").toString()
            val resId: String = intent.extras!!.getString("res_id").toString()
            val status: String = intent.extras!!.getString("status").toString()
            pendingFragment.getDataFromResult(resId, 0, tab_layout.selectedTabPosition == 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_company_home, container, false)
        tab_layout=rootView.findViewById(R.id.tab_layout)
        clickView()
        viewData()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver((mMessageReceiver),
            IntentFilter("Reservation_Cancelled")
        )
        return rootView
    }

    private fun clickView() {

        rootView.editSerach.setOnClickListener {
            tabPosition=rootView.tab_layout.selectedTabPosition
            var selectedStatus=""
            if (tabPosition==0){
                selectedStatus=Utility.RES_PENDING_STATUS
            }else if (tabPosition==1){
                selectedStatus=Utility.RES_CONFIRMED_STATUS
            }else if (tabPosition==2){
                selectedStatus=Utility.RES_DECLINED_STATUS
            }
            startActivity(Intent(activityBase, CompanyHomeSearchActivity::class.java)
                .putExtra(Utility.key.status,selectedStatus))
        }

        rootView.imgNotify.setOnClickListener {
            startActivity(Intent(activityBase, NotificationActivity::class.java))
        }
    }

    private fun viewData() {
//        pendingFragment = CompHomePendingFragment(this,activityBase)
        confirmedFragment = CompHomeConfirmedFragment(this,activityBase)
        declinedFragment = CompHomeDeclinedFragment(this,activityBase)

        arrFragList.clear()
        arrFragList.add(pendingFragment)
        arrFragList.add(confirmedFragment)
        arrFragList.add(declinedFragment)

        setFragmentsView()
    }

    private fun setFragmentsView() {

        val adapter =
            ViewPagerCompAdapter(childFragmentManager, lifecycle, arrFragList)
        rootView.pager.adapter = adapter
        rootView.pager.isUserInputEnabled = false
        TabLayoutMediator(tab_layout, rootView.pager) { tab, position ->
            tab.text = tabsArray[position]
        }.attach()


        tab_layout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { //pending tab
                        tab.text = tabsArray[0]+" (${pendingFragment.pendingCount})"
                        tab_layout.getTabAt(1)!!.text=tabsArray[1]
                        tab_layout.getTabAt(2)!!.text=tabsArray[2]
                    }
                    1 -> { //confimed tab
                        tab.text = tabsArray[1]+" (${confirmedFragment.confirmedCount})"
                        tab_layout.getTabAt(0)!!.text=tabsArray[0]
                        tab_layout.getTabAt(2)!!.text=tabsArray[2]
                    }
                    else -> { //declined tab
                        tab.text = tabsArray[2]+" (${declinedFragment.declinedCount})"
                        tab_layout.getTabAt(0)!!.text=tabsArray[0]
                        tab_layout.getTabAt(1)!!.text=tabsArray[1]
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tab_layout.getTabAt(0)!!.select() //select default pending tab
    }

    fun setNotificationView(showDot: Boolean){
        if (showDot){
            rootView.imgNotify.setImageDrawable(ContextCompat.getDrawable(activityBase, R.drawable.ic_home_noti_dot))
        }else{
            rootView.imgNotify.setImageDrawable(ContextCompat.getDrawable(activityBase, R.drawable.ic_home_noti))
        }
    }

    override fun onStop() {
        super.onStop()
        rootView.pager.isSaveFromParentEnabled = false
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type== Utility.reservationsPending || type == Utility.reservationsDeclinedAdapter){
            pendingFragment.onTaskComplete(result,type,isData)

        }else if (type== Utility.reservationsConfirmed){
            confirmedFragment.onTaskComplete(result,type,isData)

        }else if (type== Utility.reservationsDeclined){
            declinedFragment.onTaskComplete(result,type,isData)

        }
    }

    fun updateFromPlayStore(appVersion: String, forceUpdate: String){
        val versionName: String = BuildConfig.VERSION_NAME
        if (versionName != appVersion) {
            showUpdatePopUp(
                getString(R.string.update),
                getString(R.string.available_update),
                forceUpdate
            )
        }
    }

    private fun showUpdatePopUp(
        title: String,
        subTitle: String,
        forceUpdate: String
    ) {
        val dialog = Dialog(activityBase)
        dialog.setContentView(R.layout.logout_popup_view)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.text=getString(R.string.not_now)
        txtDelete.text=getString(R.string.update)
        if (forceUpdate == "1"){
            txtCancel.showOrGone(false)
        }
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.setOnClickListener { dialog.dismiss()
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${getApplicationContext().packageName}")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${getApplicationContext().packageName}")))
            }
        }
        dialog.show()

    }
}