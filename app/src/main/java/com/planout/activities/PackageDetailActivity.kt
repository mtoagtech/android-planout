package com.planout.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.planout.R
import com.planout.adapters.PackageCouponListViewAdapter
import com.planout.adapters.PackagesListViewAdapter
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.PackagesData
import com.planout.models.VouchersData
import kotlinx.android.synthetic.main.activity_package_detail.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.util.Locale

class PackageDetailActivity : AppCompatActivity(), ApiResponse {

    var selectedPaymentMethod = ""
    lateinit var layoutManager: LinearLayoutManager
    lateinit var packagesListViewAdapter: PackagesListViewAdapter
    lateinit var layoutManager2: LinearLayoutManager
    lateinit var packageCouponListViewAdapter: PackageCouponListViewAdapter
    var PackageAmount=""
    var PackageId=""
    var CouponCode=""
    var TaxPercent=""
    val PAYMENT_RESULT = 500
    var order_unique_id=""
    var final_price=""
    var status=""
    companion object{
        lateinit var handler: Handler
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package_detail)

        txtHeader.text = getString(R.string.package_detail)

        handler = Handler()
        clickView()


        viewData()

        val rememberLanguage = Utility.getForm(this, Utility.key.language)
        val hashM: HashMap<String, String> = HashMap()
        var english: String = "EN"
        var greek: String = "EL"

        Log.d("jjlasfalsfj :- ",rememberLanguage.toString())


        if(rememberLanguage.isNullOrBlank()){
            val locale = Locale("en") // or "el" for Greek
            Locale.setDefault(locale)

            val resources = this.resources

            val configuration = resources.configuration
            configuration.locale = locale
            configuration.setLayoutDirection(locale)

            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        else {
            // Set the language based on the value retrieved from shared preferences
            when (rememberLanguage) {
                english -> {
                    val locale = Locale("en") // or "el" for Greek
                    Locale.setDefault(locale)

                    val resources = this.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
                greek -> {
                    val locale = Locale("el") // or "el" for Greek
                    Locale.setDefault(locale)

                    val resources = this.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
                else -> {
                    val locale = Locale("en") // or "el" for Greek
                    Locale.setDefault(locale)

                    val resources = this.resources

                    val configuration = resources.configuration
                    configuration.locale = locale
                    configuration.setLayoutDirection(locale)

                    resources.updateConfiguration(configuration, resources.displayMetrics)
                }
            }
        }
    }

    private fun viewData() {
        val packageList: ArrayList<PackagesData>
        val type: Type = object : TypeToken<List<PackagesData?>?>() {}.type
        packageList = Gson().fromJson(intent.getStringExtra("packageList"), type)
        var SelectedItem = PackagesData()
        for (i in 0 until packageList.size){
            packageList[i].isSelected = i == intent.getStringExtra(Utility.key.itemposition)!!.toInt()
            if (i == intent.getStringExtra(Utility.key.itemposition)!!.toInt()){
                SelectedItem=packageList[i]
            }
        }
//        recyclerPackageList.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        packagesListViewAdapter = PackagesListViewAdapter(this,packageList, txtCouponCode)
        recyclerPackageList.layoutManager = layoutManager
        recyclerPackageList.adapter = packagesListViewAdapter

        setDataSelectedPackage(SelectedItem)
    }

    fun setDataSelectedPackage(selectedItem: PackagesData) {
        txtCouponName.showOrGone(false)
        txtCouponVal.showOrGone(false)
        txtPackName.text=selectedItem.package_name
        PackageAmount=selectedItem.price
        PackageId=selectedItem.id
        TaxPercent=selectedItem.tax_percent
        txtTaxName.text = "TAX ${TaxPercent.replace(".00", "")}%"
        txtTaxVal.text=getString(R.string.currency,getTaxAmount(PackageAmount, "0", TaxPercent).replace(".00", ""))
        txtPackVal.text=getString(R.string.currency,PackageAmount).replace(".00", "")
        txtTotalVal.text=getString(R.string.currency,getTotalAmount(PackageAmount, "0", TaxPercent).replace(".00", ""))
        txtCouponName.text=""
        txtCouponVal.text=""
        CouponCode=""
        txtCouponCode.setOnClickListener {
            /*if (packagesListViewAdapter.isVoucher) {
                //dialog for coupon is exist in this package
                openCouponDialog(selectedItem.available_vouchers, selectedItem.id)
            }else{
                //dialog for coupon is not exist in this package
                showAlertPopUp("Coupon info", "Coupon is not exist in this package.")
            }*/}
        Utility.show_progress(this)
        handler.postDelayed(Runnable {
            Utility.hide_progress(this)
            if (packagesListViewAdapter.isVoucher) {
                val ii = getHighest(selectedItem.available_vouchers)
                CouponCode=selectedItem.available_vouchers[ii].voucher_code
                applyCouponAPi(selectedItem.available_vouchers[ii].voucher_code, selectedItem.id)
            }
        }, 1500)
    }

    private fun getHighest(availableVouchers: java.util.ArrayList<VouchersData>): Int {
        var highPer = ""
        var ii = 0
        for (i in 0 until availableVouchers.size){
            if (highPer.isEmpty()){
                highPer = availableVouchers[i].discount_percent
                ii = i
            }else{
                if(highPer.toDouble()<availableVouchers[i].discount_percent.toDouble()){
                    highPer = availableVouchers[i].discount_percent
                    ii = i
                }
            }
        }
        return ii
    }

    private fun applyCouponAPi(voucherCode: String, package_id: String) {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.coupon_code,voucherCode)
        mBuilder.add(Utility.key.package_id,package_id)
        mBuilder.add(Utility.key.store_id,Utility.getForm(this,Utility.key.store_id)!!)
        CallApi.callAPi(mBuilder, ApiController.api.subscriptions_applycoupon, this, Utility.subscriptions_applycoupon, true, Utility.POST, true)

    }

    private fun showAlertPopUp(title: String, subTitle: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtCancel.showOrGone(false)
        txtDelete.text="Ok"
        txtDelete.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    //calculate tax amount
    private fun getTaxAmount(packageAmount: String, discount_amount: String, taxPercent: String): String {
        var taxAmt = 0.0
        val subtotal = packageAmount.toDouble()-discount_amount.toDouble()
        taxAmt = (subtotal*taxPercent.toDouble())/100
        return DecimalFormat("##.##").format(taxAmt).toString()
    }
    //calculate total amount
    private fun getTotalAmount(packageAmount: String, discount_amount: String, taxPercent: String): String {
        var totalAmt = 0.0
        val subtotal = packageAmount.toDouble()-discount_amount.toDouble()
        val tax = (subtotal*taxPercent.toDouble())/100
        totalAmt = subtotal+tax
        return DecimalFormat("##.##").format(totalAmt).toString()
    }

    private fun clickView() {
        imgBackHeader.setOnClickListener { onBackPressed() }
        txtCouponName.setOnClickListener {
            if (CouponCode.isNotEmpty()) {
                //confirmation dialog for coupon remove
                showClearCouponPopUp(getString(R.string.remove_confirmation), getString(R.string.remove_coupon))
            }
        }
        txtTermsConditions.setOnClickListener {
            //dialog for accept terms and conditions
            openTermsConditionDialog()
        }
        btnPay.setOnClickListener {
            //validation for payment checkout
            if (selectedPaymentMethod==""){
                //Utility.customErrorToast(this,"Select your preferred payment method")
                cardPaymentMethod.showOrGone(false)
                paymentErr.showOrGone(true)
                paymentErr.text = getString(R.string.preferred_payment_method)
            }else if (!checkTerms.isChecked){
                //Utility.customErrorToast(this,"Accept terms and conditions first")
                termsErr.showOrGone(true)
                termsErr.text = getString(R.string.accept_terms_and_conditions)
            }else{
                //confirmation dialog for payment checkout
                showCheckoutPopUp(getString(R.string.confirm_payment), getString(R.string.proceed_to_payment))
            }
        }
        txtSelectMethod.setOnClickListener {
            cardPaymentMethod.showOrGone(true)
        }
        constMain.setOnClickListener {
            cardPaymentMethod.showOrGone(false)
        }
        checkTerms.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                checkTerms.isChecked
                termsErr.showOrGone(false)
                termsErr.text = ""
            }
        }
        //selection for saferpay/jcc
        txtViva.setOnClickListener {
            paymentErr.showOrGone(false)
            paymentErr.text = ""
            cardPaymentMethod.showOrGone(false)
            selectedPaymentMethod = "jcc"//saferpay
            txtSelectMethod.text = txtViva.text
            //txtSelectMethod.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.saferpay_logo), null, ContextCompat.getDrawable(this, R.drawable.ic_arrow_down), null)
            txtSelectMethod.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.jcc_img), null, ContextCompat.getDrawable(this, R.drawable.ic_arrow_down), null)
        }
        //selection for paypal
        txtPaypal.setOnClickListener {
            paymentErr.showOrGone(false)
            paymentErr.text = ""
            cardPaymentMethod.showOrGone(false)
            selectedPaymentMethod = "paypal"
            txtSelectMethod.text = txtPaypal.text
            txtSelectMethod.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_icon_paypal), null, ContextCompat.getDrawable(this, R.drawable.ic_arrow_down), null)
        }
    }

    private fun PAyment(order_unique_id: String, paymnetType: String) {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.order_unique_id,order_unique_id)
        CallApi.callAPi(
            mBuilder,
            ApiController.api.payments_process+paymnetType,
            this,
            Utility.payments_process,
            true,
            Utility.POST,
            true
        )
    }

    private fun showClearCouponPopUp(title: String, subTitle: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.text=getString(R.string.remove)
        txtDelete.setOnClickListener { dialog.dismiss()
            CouponCode=""
            txtCouponName.text=""
            txtCouponVal.text=""
            txtCouponName.showOrGone(false)
            txtCouponVal.showOrGone(false)
            txtTaxVal.text=getString(R.string.currency,getTaxAmount(PackageAmount, "0", TaxPercent)).replace(".00", "")
            txtTotalVal.text=getString(R.string.currency,getTotalAmount(PackageAmount, "0", TaxPercent)).replace(".00", "")
        }
        dialog.show()
    }


    private fun showCheckoutPopUp(title: String, subTitle: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.logout_popup_view)

        val txtTitle = dialog.findViewById(R.id.txtTitle) as TextView
        val txtSubTitle = dialog.findViewById(R.id.txtSubTitle) as TextView
        val txtCancel = dialog.findViewById(R.id.txtCancel) as TextView
        val txtDelete = dialog.findViewById(R.id.txtDelete) as TextView
        if (title.isNotEmpty())
            txtTitle.text = title
        if (subTitle.isNotEmpty())
            txtSubTitle.text = subTitle
        txtCancel.setOnClickListener { dialog.dismiss() }
        txtDelete.text=getString(R.string.proceed)
        txtDelete.setOnClickListener { dialog.dismiss()
            //call api for payment checkout
            paymentCheckoutApiApi()
        }
        dialog.show()
    }

    private fun paymentCheckoutApiApi() {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.coupon_code,CouponCode)
        mBuilder.add(Utility.key.package_id,PackageId)
        mBuilder.add(Utility.key.store_id,Utility.getForm(this,Utility.key.store_id)!!)
        CallApi.callAPi(mBuilder, ApiController.api.subscriptions_payment_checkout, this, Utility.subscriptions_payment_checkout, true, Utility.POST, true)
    }

    fun openCouponDialog(availableVouchers: ArrayList<VouchersData>, id: String) {
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_coupon_list_view_popup, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val recyclerCouponList = dialogView.findViewById<RecyclerView>(R.id.recyclerCouponList)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        layoutManager2 = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        packageCouponListViewAdapter = PackageCouponListViewAdapter(this,availableVouchers,id,serviceInfo_dialog)
        recyclerCouponList.layoutManager = layoutManager2
        recyclerCouponList.adapter = packageCouponListViewAdapter

        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
        serviceInfo_dialog.show()
    }
    fun openTermsConditionDialog() {
        Utility.show_progress(this@PackageDetailActivity)
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_terms_condition_view_popup, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val btnAccept = dialogView.findViewById<Button>(R.id.btnAccept)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        val webView = dialogView.findViewById<WebView>(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.builtInZoomControls = false
        webView.settings.useWideViewPort = true
        webView.settings.pluginState = WebSettings.PluginState.ON
        webView.setBackgroundColor(Color.TRANSPARENT)

        webView.settings.defaultFontSize = 15
//        webView.settings.setAppCacheEnabled(false)
        webView.settings.blockNetworkImage = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.setGeolocationEnabled(false)
        webView.settings.setNeedInitialFocus(false)
        webView.settings.saveFormData = false
        // wb.settings.setPluginsEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: android.webkit.WebView?, url: String, favicon: Bitmap?) {

            }
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                Handler().postDelayed(Runnable {
                    Utility.hide_progress(this@PackageDetailActivity)
                    serviceInfo_dialog.show()
                }, 1000)
            }
        }
        webView.loadUrl(ApiController.api.terms_url)

        Utility.animationClick(btnAccept).setOnClickListener {
            //accept terms and conditions
            termsErr.showOrGone(false)
            termsErr.text = ""
            checkTerms.isChecked = true
            serviceInfo_dialog.dismiss()
        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }
    }
    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.subscriptions_applycoupon){
            if (isData){
                txtCouponName.showOrGone(true)
                txtCouponVal.showOrGone(true)
                txtCouponName.text=result.getJSONObject(Utility.key.data).getString(Utility.key.voucher_code)//voucher_name
                val couponAmount=result.getJSONObject(Utility.key.data).getString(Utility.key.discount_amount)
                CouponCode=result.getJSONObject(Utility.key.data).getString(Utility.key.voucher_code)
                txtCouponVal.text="-${getString(R.string.currency,couponAmount).replace(".00", "")}"
                val total = PackageAmount.toDouble() - couponAmount.toDouble()
                txtTaxVal.text=getString(R.string.currency,getTaxAmount(PackageAmount, couponAmount, TaxPercent)).replace(".00", "")
                txtTotalVal.text=getString(R.string.currency,getTotalAmount(PackageAmount, couponAmount, TaxPercent)).replace(".00", "")

            }else{
                Utility.customErrorToast(this,result.getString(Utility.key.message))
            }
        }else if (type==Utility.subscriptions_payment_checkout){
            if (isData) {
                val data = result.getJSONObject(Utility.key.data)
                order_unique_id = data.getString(Utility.key.order_unique_id)
                val subtotal = data.getString(Utility.key.subtotal)
                val coupon_code = data.getString(Utility.key.coupon_code)
                final_price = data.getString(Utility.key.final_price)
                val paid_by = data.getString(Utility.key.paid_by)
                status = data.getString(Utility.key.status)
                val package_id = data.getString(Utility.key.package_id)
                val store_id = data.getString(Utility.key.store_id)
                val updated_at = data.getString(Utility.key.updated_at)
                val created_at = data.getString(Utility.key.created_at)
                val id = data.getString(Utility.key.id)
                //call api for payment process
                if (selectedPaymentMethod == "paypal") {
                    PAyment(order_unique_id,"paypal")
                } else {
                    PAyment(order_unique_id,"jcc")//saferpay
                }
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        } else if (type == Utility.payments_process){
            if (isData){
                val data=result.getJSONObject(Utility.key.data)
                val url=data.getString(Utility.key.url)
                val order_id=data.getString(Utility.key.order_id)
                startActivityForResult(
                    Intent(this@PackageDetailActivity, com.planout.activities.payment.WebView::class.java)
                        .putExtra("URL", url)
                        .putExtra("title", getString(R.string.payment))
                        .putExtra("order_id", order_id)
                    , PAYMENT_RESULT
                )
            }else{
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        }else if (type==Utility.payments_checkstatus){
            if (isData){
                onBackPressed()
//                val jObj = result.getJSONObject(Utility.key.data)
//                val j = Intent(this, ResetSuccessActivity::class.java)
//                j.putExtra(Utility.key.message,result.getString(Utility.key.message))
//                j.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(j)
//                Utility.customSuccessToast(this, jObj.getString(Utility.key.message))
//                handler.postDelayed(Runnable {
//                    onBackPressed()
//                }, 2000)
            }else{
                val jObj = result.getJSONObject(Utility.key.data)
                val iter: Iterator<String> = jObj.keys()
                val key: String = iter.next()
                val arrObj: JSONArray = jObj.getJSONArray(key)
                val strMsg = arrObj[0].toString()
                Utility.customErrorToast(this, strMsg)
                /*val j = Intent(this, ResetFailedActivity::class.java)
                j.putExtra(Utility.key.message,strMsg)
                j.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(j)*/
//                if (handler!=null){
//                    handler.postDelayed(Runnable {
//                        onBackPressed()
//                    }, 2000)
//                }else{
//                    onBackPressed()
//                }
            }
        }
    }

    private fun paymentResponseApi(orderUniqueId: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            ApiController.api.payments_checkstatus+"/"+orderUniqueId,
            this,
            Utility.payments_checkstatus,
            true,
            Utility.GET,
            true
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val value = data!!.extras!!.getString("Payment_status")
                    val order_id = data.extras!!.getString("order_id")
                    if (value == "success") {
                        //call api for payment response
                        paymentResponseApi(order_id!!)
                    } else {
                        /*val j = Intent(this, ResetFailedActivity::class.java)
                        j.putExtra(Utility.key.message, "Payment Failed Please Try Again!!")
                        j.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(j)*/
                        Utility.customErrorToast(this, getString(R.string.payment_failed))
                        if (handler!=null){
                            handler.postDelayed(Runnable {
                                onBackPressed()
                            }, 2000)
                        }else{
                            onBackPressed()
                        }
                    }
                }catch (e:Exception){e.printStackTrace()}
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            handler.removeCallbacksAndMessages(null)
        }catch (e: Exception){e.printStackTrace()}
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

