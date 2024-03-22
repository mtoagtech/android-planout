package com.planout.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.gson.Gson
import com.planout.R
import com.planout.activities.HomeCompanyActivity
import com.planout.activities.NotificationActivity
import com.planout.activities.PackageDetailActivity
import com.planout.adapters.SubscriptionPackagesViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.PackagesData
import com.planout.models.VouchersData
import kotlinx.android.synthetic.main.fragment_company_subscription.view.*
import okhttp3.FormBody
import org.json.JSONObject
import java.util.Locale

class CompanySubscriptionFragment(val activityBase: HomeCompanyActivity) : Fragment() , ApiResponse {

    lateinit var rootView: View
    lateinit var layoutManager: LinearLayoutManager
    private lateinit var subscriptionPackagesViewAdapter: SubscriptionPackagesViewAdapter
    override fun onResume() {
        super.onResume()
        subscriptionApi()
        languageChange()

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_company_subscription, container, false)

        clickView()



        return rootView
    }

    private fun languageChange() {
        val rememberLanguage = Utility.getForm(activityBase, Utility.key.language)
        val hashM: HashMap<String, String> = HashMap()
        var english: String = "EN"
        var greek: String = "EL"

        Log.d("jjlasfalsfj :- ",rememberLanguage.toString())


        if(rememberLanguage.isNullOrBlank()){
            val locale = Locale("en")
            Locale.setDefault(locale)

            val resources = activityBase.resources

            val configuration = resources.configuration
            configuration.locale = locale
            configuration.setLayoutDirection(locale)

            resources.updateConfiguration(configuration, resources.displayMetrics)

        }
        else {
            // Set the language based on the value retrieved from shared preferences
            when (rememberLanguage) {
                english -> {
                    val locale = Locale("en")
                    Locale.setDefault(locale)

                    val resources = activityBase.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
                greek -> {
                    val locale = Locale("el")
                    Locale.setDefault(locale)

                    val resources = activityBase.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
                else -> {
                    val locale = Locale("en")
                    Locale.setDefault(locale)

                    val resources = activityBase.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
            }

        }
    }

    private fun clickView() {
        rootView.imgNotify.setOnClickListener {
            startActivity(Intent(activityBase, NotificationActivity::class.java))
        }
    }

    private fun subscriptionApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.subscriptions_packages, activityBase, Utility.subscriptions_packages, true, Utility.GET, true)

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.subscriptions_packages){
            if (isData){
                val data=result.getJSONObject(Utility.key.data)
                val active_packageStr=data.getString(Utility.key.active_package)
                val total_unread_notifications = data.getInt(Utility.key.total_unread_notifications)
                setNotificationView(total_unread_notifications>0)
                var id=""
                if (active_packageStr!="null"){
                    rootView.constNoSubscription.showOrGone(false)
                    rootView.constSubscription.showOrGone(true)
                    val active_package=data.getJSONObject(Utility.key.active_package)
                    id=active_package.getString(Utility.key.id)
                    val package_name=active_package.getString(Utility.key.package_name)
                    val duration=active_package.getString(Utility.key.duration)
                    val package_price=active_package.getString(Utility.key.package_price)
                    val package_end_date=active_package.getString(Utility.key.package_end_date)

                    rootView.activePackMonth.text= "$duration ${getString(R.string.months)}"
                    rootView.activeDateVal.text=Utility.formatdate(package_end_date)

                }else{
                    rootView.constNoSubscription.showOrGone(true)
                    rootView.constSubscription.showOrGone(false)
                }

                val packages=data.getJSONArray(Utility.key.packages)

                val packagesArray: ArrayList<PackagesData> = ArrayList()
                var activePosition=0
                for (i in 0 until packages.length()){
                    val packagesObj=packages.getJSONObject(i)
                    val item= PackagesData()
                    item.id=packagesObj.getString(Utility.key.id)
                    item.package_name=packagesObj.getString(Utility.key.package_name)
                    item.duration=packagesObj.getString(Utility.key.duration)
                    item.price=packagesObj.getString(Utility.key.price)
                    item.tax_percent=packagesObj.getString(Utility.key.tax_percent)
                    item.isActive= packagesObj.getString(Utility.key.id) == id
                    if (packagesObj.getString(Utility.key.id) == id){
                        activePosition=i
                    }
                    val available_vouchersArray: ArrayList<VouchersData> = ArrayList()

                    val available_vouchers=packagesObj.getJSONArray(Utility.key.available_vouchers)
                    for (j in 0 until available_vouchers.length()){
                        val available_vouchersObj=available_vouchers.getJSONObject(j)
                        val available_vouchersItem=VouchersData()
                        available_vouchersItem.voucher_name=available_vouchersObj.getString(Utility.key.voucher_name)
                        available_vouchersItem.voucher_code=available_vouchersObj.getString(Utility.key.voucher_code)
                        available_vouchersItem.expired_at=available_vouchersObj.getString(Utility.key.expired_at)
                        available_vouchersItem.discount_percent=available_vouchersObj.getString(Utility.key.discount_percent)
                        available_vouchersItem.discount_amount=available_vouchersObj.getString(Utility.key.discount_amount)
                        available_vouchersArray.add(available_vouchersItem)

                    }
                    item.available_vouchers=available_vouchersArray
                    packagesArray.add(item)

                }

                rootView.btnRenew.setOnClickListener {
                    activityBase.startActivity(
                        Intent(activityBase, PackageDetailActivity::class.java)
                            .putExtra("packageList", Gson().toJson(packagesArray))
                            .putExtra(Utility.key.itemposition,activePosition.toString()))
                }

//                rootView.recyclerAvailPackages.setHasFixedSize(true)
                layoutManager = LinearLayoutManager(activityBase, LinearLayoutManager.VERTICAL, false)
                subscriptionPackagesViewAdapter = SubscriptionPackagesViewAdapter(activityBase,packagesArray)
                rootView.recyclerAvailPackages.layoutManager = layoutManager
                rootView.recyclerAvailPackages.adapter = subscriptionPackagesViewAdapter
            }
        } else if (type == Utility.subscriptions_cancel){
            if (isData) {
                Utility.customSuccessToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
                //call api for subscription detail
                subscriptionApi()
            }else{
                Utility.customErrorToast(activityBase, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }
    }

    fun setNotificationView(showDot: Boolean){
        if (showDot){
            rootView.imgNotify.setImageDrawable(ContextCompat.getDrawable(activityBase, R.drawable.ic_home_noti_dot))
        }else{
            rootView.imgNotify.setImageDrawable(ContextCompat.getDrawable(activityBase, R.drawable.ic_home_noti))
        }
    }


}