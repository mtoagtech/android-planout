package com.planout.activities

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.planout.api_calling.ApiResponse
import com.planout.R
import com.planout.constant.Utility
import com.planout.fragments.VisitorHomeFragment
import com.planout.fragments.VisitorNotificationFragment
import com.planout.fragments.VisitorProfileFragment
import com.planout.fragments.VisitorReservationFragment
import com.planout.retrofit.DataInterface
import kotlinx.android.synthetic.main.activity_home_visitor.*
import org.json.JSONObject

class HomeVisitorActivity : AppCompatActivity(), ApiResponse, DataInterface {

    var resultLauncherFavoriteInd =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val ID = data!!.getStringExtra("ID")!!
                val isRemoveFav=data.getStringExtra("isRemoveFav")
                homeFragVisitor.arrIndusDetailListDataset(ID,isRemoveFav)

            }
        }

    var resultLauncherFavorite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val data: Intent? = result.data
                val ID = data!!.getStringExtra("ID")!!
                val isRemoveFav=data.getStringExtra("isRemoveFav")
                homeFragVisitor.recordsListDataset(ID,isRemoveFav)

            }
        }

    var strId: String = ""
    lateinit var strSelectedFrag: String
    var doubleBackToExitPressedOnce = false

    lateinit var handler: Handler
    var isDisabled = false
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data!!.getStringExtra(Utility.key.type)=="1"){ //for upcoming reservation
                reservationFrag.upcomingFrag.getDataFromResult(result.data!!.getStringExtra(Utility.key.id),
                    result.data!!.getIntExtra(Utility.key.itemposition, 0), result.data!!.getStringExtra(Utility.key.status))
            }else if (result.data!!.getStringExtra(Utility.key.type)=="2"){ //for past reservation
                reservationFrag.pastFrag.getDataFromResult(result.data!!.getStringExtra(Utility.key.id),
                    result.data!!.getIntExtra(Utility.key.itemposition, 0), result.data!!.getStringExtra(Utility.key.status))
            }
        }
    }

    companion object{
        fun myObject(activity: Activity, id: String, storeName: String, b: String) {
            homeFragVisitor.addSetData(id, storeName, b)
        }

        lateinit var homeFragVisitor : VisitorHomeFragment
        lateinit var reservationFrag : VisitorReservationFragment
        lateinit var notificationFrag : VisitorNotificationFragment
        lateinit var profileFrag : VisitorProfileFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_visitor)
        handler = Handler()
        homeFragVisitor = VisitorHomeFragment(this,resultLauncherFavorite,resultLauncherFavoriteInd)
        reservationFrag = VisitorReservationFragment(this)
        notificationFrag = VisitorNotificationFragment(this)
        profileFrag = VisitorProfileFragment(this)
        click()
        if (intent.hasExtra(Utility.key.isFrom)){
            if (intent.getStringExtra(Utility.key.isFrom) == "notify"){
                setNotificationBottomView()
            }else if (intent.getStringExtra(Utility.key.isFrom) == "editProfile"){
                setProfileBottomView()
            }
        }else {
            setHomeBottomView()
        }
    }

    private fun click() {
        Utility.animationClick(linHome).setOnClickListener {
            if (!isDisabled) {
                setHomeBottomView()
            }
            disbleForFew()
        }
        Utility.animationClick(linReservation).setOnClickListener {
            if (!isDisabled) {
                setReservationBottomView()
            }
            disbleForFew()
        }
        Utility.animationClick(linNotification).setOnClickListener {
            if (!isDisabled) {
                setNotificationBottomView()
            }
            disbleForFew()
        }
        Utility.animationClick(linProfile).setOnClickListener {
            if (!isDisabled) {
                setProfileBottomView()
            }
            disbleForFew()
        }
    }

    fun setNotificationView(showDot: Boolean){
        //change notification status and view
        if (showDot){
            imgNotification!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_notification_dot))
        }else{
            imgNotification!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_notification))
        }
    }

    private fun setHomeBottomView() {
        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_home_on))
        imgReservation!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_reservation))
        imgNotification!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_notification))
        imgProfile!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_profile))
        setViewBottomView(txtHome, txtReservation, txtNotification, txtProfile)
        loadFragment(homeFragVisitor, "home")
    }
    fun setReservationBottomView() {
        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this@HomeVisitorActivity, R.drawable.ic_tab_home))
        imgReservation!!.setImageDrawable(ContextCompat.getDrawable(this@HomeVisitorActivity, R.drawable.ic_tab_reservation_on))
        imgNotification!!.setImageDrawable(ContextCompat.getDrawable(this@HomeVisitorActivity, R.drawable.ic_tab_notification))
        imgProfile!!.setImageDrawable(ContextCompat.getDrawable(this@HomeVisitorActivity, R.drawable.ic_tab_profile))
        setViewBottomView(txtReservation, txtHome, txtNotification, txtProfile)
        loadFragment(reservationFrag, "history")
    }
    private fun setNotificationBottomView() {
        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_home))
        imgReservation!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_reservation))
        imgNotification!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_notification_on))
        imgProfile!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_profile))
        setViewBottomView(txtNotification, txtReservation, txtHome, txtProfile)
        loadFragment(notificationFrag, "payment")
    }
    private fun setProfileBottomView() {
        imgHome!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_home))
        imgReservation!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_reservation))
        imgNotification!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_notification))
        imgProfile!!.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tab_profile_on))
        setViewBottomView(txtProfile, txtHome, txtReservation, txtNotification)
        loadFragment(profileFrag, "profile")
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
        isDisabled = true
        handler.postDelayed(Runnable { isDisabled = false }, 1000)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.logout){
            profileFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.profile){
            profileFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.notifications_enable){
            profileFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.notifications_disable){
            profileFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.stores){
            homeFragVisitor.onTaskComplete(result,type,isData)
        }else if (type==Utility.stores_first || type==Utility.stores_second ||
            type==Utility.stores_third || type==Utility.stores_forth){
            homeFragVisitor.onTaskComplete(result,type,isData)
        }else if (type==Utility.reservationsUpcoming){
            reservationFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.reservationsPast){
            reservationFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.notifications || type==Utility.markasread || type== Utility.notifications_markallread){
            notificationFrag.onTaskComplete(result,type,isData)
        }else if (type.contains(Utility.favoritesRemove)){
            if (isData){
                homeFragVisitor.onTaskComplete(result,type,isData)
            }else{
                Utility.customErrorToast(this,result.getString(Utility.key.message))
            }
        }else if (type.contains(Utility.favoritesAdd)){
            if (isData){
                homeFragVisitor.onTaskComplete(result,type,isData)
                //Utility.customSuccessToast(this,result.getString(Utility.key.message))
            }else{
                Utility.customErrorToast(this,result.getString(Utility.key.message))
            }
        }
        else if(type==Utility.update_language){
            profileFrag.onTaskComplete(result,type,isData)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent);
        overridePendingTransition(0, 0)
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {

            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {

            } // Night mode is active, we're using dark theme
        }
    }

    override fun addSetData(id: String, name: String, status: String) {
        homeFragVisitor.addSetData(id, name, status)
    }
}