package com.planout.activities

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.layoutDirection
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.planout.api_calling.ApiResponse
import com.planout.R
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.fragments.*
import kotlinx.android.synthetic.main.activity_home_company.*
import kotlinx.android.synthetic.main.fragment_company_more.view.switchReservation
import org.json.JSONObject
import java.util.Locale

class HomeCompanyActivity : AppCompatActivity(), ApiResponse {

    lateinit var strSelectedFrag: String
    var doubleBackToExitPressedOnce = false
    var isDisabled = false
    var eventStartTime = ""
    var eventEndTime = ""

    companion object{
        lateinit var homeFrag : CompHomePendingFragment
        lateinit var eventFrag : CompanyEventFragment
        lateinit var subscriptionFrag : CompanySubscriptionFragment
        lateinit var moreFrag : CompanyMoreFragment
        lateinit var handler: Handler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_company)

        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        homeFrag = CompHomePendingFragment(this)
        eventFrag = CompanyEventFragment(this)
        subscriptionFrag = CompanySubscriptionFragment(this)
        moreFrag = CompanyMoreFragment(this)
        handler = Handler()
        click()
        if (intent.hasExtra(Utility.key.isFrom)){
            if (intent.getStringExtra(Utility.key.isFrom)=="reservation.created"){
                setHomeBottomView()
            }
        }else{
            setHomeBottomView()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Will give the direction of the layout depending of the Locale you've just set
        window.decorView.layoutDirection = Locale.getDefault().layoutDirection
    }

    private fun click() {
        Utility.animationClick(linHome).setOnClickListener {
            if (!isDisabled) {
                setHomeBottomView()
            }
            disbleForFew()
        }
        Utility.animationClick(linEvent).setOnClickListener {
            if (!isDisabled) {
                setEventBottomView()
            }
            disbleForFew()
        }
        if(Utility.getForm(this, Utility.key.is_owner) == "1"){
            Utility.animationClick(linSubscription).setOnClickListener {
                if (!isDisabled) {
                    setSubscriptionBottomView()
                }
                disbleForFew()
            }
        }
        else{
            linSubscription.visibility = View.GONE
        }

        Utility.animationClick(linMore).setOnClickListener {
            if (!isDisabled) {
                setMoreBottomView()
            }
            disbleForFew()
        }
        if(Utility.getForm(this, Utility.key.is_owner) == "1"){
            Utility.animationClick(addReservationBtn).setOnClickListener {
                if(Utility.getForm(this, Utility.key.reservation_status) == "1"){
                    startActivity(Intent(this, AddReservationCompanyActivity::class.java))
                }else{
                    Utility.customErrorToast(this@HomeCompanyActivity, "Your reservation is disabled.")
                }
            }

        }
        else{
            Utility.animationClick(addReservationBtn).setOnClickListener {
                if(Utility.getForm(this, Utility.key.reservation_status) == "1"){
                    startActivity(Intent(this, AddReservationCompanyActivity::class.java))
                }else{
                    Utility.customErrorToast(this@HomeCompanyActivity, "Your reservation is disabled.")
                }
            }

        }

    }

    fun setHomeBottomView() {
        addReservationBtn.showOrGone(true)
        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_home_on))
        imgEvent!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_event))
        imgSubscription!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_subscription))
        imgMore!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_more))
        setViewBottomView(txtHome, txtEvent, txtSubscription, txtMore)
        loadFragment(homeFrag, "home")
    }
    fun setEventBottomView() {
        addReservationBtn.showOrGone(false)

        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_home))
        imgEvent!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_event_on))
        imgSubscription!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_subscription))
        imgMore!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_more))
        setViewBottomView(txtEvent, txtHome, txtSubscription, txtMore)
        loadFragment(eventFrag, "event")
    }

    fun setSubscriptionBottomView() {
        addReservationBtn.showOrGone(false)

        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_home))
        imgEvent!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_event))
        imgSubscription!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_subscription_on))
        imgMore!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_more))
        setViewBottomView(txtSubscription, txtEvent, txtHome, txtMore)
        loadFragment(subscriptionFrag, "subscription")
    }

    private fun setMoreBottomView() {
        addReservationBtn.showOrGone(false)

        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_home))
        imgEvent!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_event))
        imgSubscription!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_subscription))
        imgMore!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_more_on))
        setViewBottomView(txtMore, txtHome, txtEvent, txtSubscription)
        loadFragment(moreFrag, "more")
    }

    private fun setViewBottomView(
        view1: TextView?,
        view2: TextView?,
        view3: TextView?,
        view4: TextView?
    ) {
        view1!!.setTextColor(ContextCompat.getColor(this, R.color.black_white))
        view2!!.setTextColor(ContextCompat.getColor(this, R.color.gray_5B))
        view3!!.setTextColor(ContextCompat.getColor(this, R.color.gray_5B))
        view4!!.setTextColor(ContextCompat.getColor(this, R.color.gray_5B))
    }

    private fun loadFragment(fragment: Fragment, name: String) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
        strSelectedFrag = name
    }

    override fun onBackPressed() {
        eventFrag.onBackPressed()
        if (strSelectedFrag == "home") {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            this.doubleBackToExitPressedOnce = true
            Utility.customWarningToast(this, "Please click BACK again to exit")
            Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
        }else{
            setHomeBottomView()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            handler.removeCallbacksAndMessages(null)
        }catch (e:Exception){e.printStackTrace()}
    }

    fun disbleForFew(){
        if (handler != null) {
            isDisabled = true
            handler.postDelayed(Runnable { isDisabled = false }, 1000)
        }
    }

    override fun onApplyThemeResource(theme: Resources.Theme?, resid: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resid, first)

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.logout){
            moreFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.profile){
            moreFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.profileLocation){
            moreFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.notifications_enable){
            moreFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.notifications_disable){
            moreFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.reservation_enable){
            moreFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.reservation_disable){
            moreFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.reservationsPending){
            homeFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.reservationsConfirmed){
            homeFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.reservationsDeclined){
            homeFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.eventsUpcoming){
            eventFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.eventsPast){
            eventFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.eventsDelete){
            eventFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.subscriptions_packages) {
            subscriptionFrag.onTaskComplete(result, type, isData)

        }else if (type==Utility.subscriptions_cancel){
            subscriptionFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.store_locations){
            eventFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.eventsAdd || type==Utility.eventsUpdate){
            eventFrag.onTaskComplete(result,type,isData)

        }else if (type==Utility.reservationsDeclinedAdapter){
           homeFrag.onTaskComplete(result, type, isData)

        }else if (type==Utility.reservationStatusUpdate){
            if (isData){

                Utility.customSuccessToast(this@HomeCompanyActivity, Utility.checkStringNullOrNot(result, Utility.key.message))
            }else{

                Utility.customErrorToast(this@HomeCompanyActivity, Utility.checkStringNullOrNot(result, Utility.key.message))
            }

        }
        else if(type==Utility.stores_account){
            moreFrag.onTaskComplete(result, type, isData)
        }
        else if(type==Utility.stores_account_create){
            moreFrag.onTaskComplete(result, type, isData)
        }
        else if(type==Utility.stores_account_edit){
            moreFrag.onTaskComplete(result, type, isData)
        }
        else if(type==Utility.stores_account_delete){
            moreFrag.onTaskComplete(result, type, isData)
        }
        else if(type==Utility.stores_account_change_status){
            moreFrag.onTaskComplete(result, type, isData)
        }
        else if(type==Utility.profile){
            moreFrag.onTaskComplete(result, type, isData)
        }
        else if(type==Utility.update_language){
            moreFrag.onTaskComplete(result, type, isData)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {

            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {

            } // Night mode is active, we're using dark theme
        }
    }
}