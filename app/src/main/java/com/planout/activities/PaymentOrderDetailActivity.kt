package com.planout.activities

import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Progress
import com.planout.R
import com.planout.constant.ActivityConfig
import com.planout.constant.Utility
import com.planout.constant.Utility.getFileNameFromURL
import com.planout.constant.Utility.showOrGone
import kotlinx.android.synthetic.main.activity_payment_order_detail.*
import kotlinx.android.synthetic.main.header_normal_view.*
import java.io.File
import java.util.*

class PaymentOrderDetailActivity : AppCompatActivity() {
    var dataFolder: File? = null
    var download_link=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_order_detail)
        //set folder for download invoice
        dataFolder = File(ActivityConfig.getAppFolder(this@PaymentOrderDetailActivity))
        download_link=intent.getStringExtra(Utility.key.download_link)!!

        txtHeader.text = getString(R.string.order_detail)

        clickView()
        //set order detail
        val item_name=intent.getStringExtra(Utility.key.item_name)
        val order_date=intent.getStringExtra(Utility.key.order_date)
        val payment_method=intent.getStringExtra(Utility.key.payment_method)
        val subtotal=intent.getStringExtra(Utility.key.subtotal)
        val tax=intent.getStringExtra(Utility.key.tax)
        val tax_percent=intent.getStringExtra(Utility.key.tax_percent)
        val package_price=intent.getStringExtra(Utility.key.package_price)
        val final_price=intent.getStringExtra(Utility.key.final_price)
        var discount= "0"
        var discountPercent= "0"
        if (intent.getStringExtra(Utility.key.discount) != "" && intent.getStringExtra(Utility.key.discount) != "null"){
            discount = intent.getStringExtra(Utility.key.discount).toString()
            discountPercent = intent.getStringExtra(Utility.key.discount_percent).toString()
            txtDiscountName.showOrGone(true)
            txtDiscountVal.showOrGone(true)
            txtDiscountName.text = "Discount ${discountPercent}%"
            txtDiscountVal.text = "-"+getString(R.string.currency,discount)
        }

        txtPackageName.text=item_name
        txtPackName.text=item_name
        txtPayType.text=Utility.toTitleCase(payment_method)
        txtPackageDate.text=Utility.formatdatetime(order_date, Utility.api_full_date_format, Utility.date_format_subscription)
        txtTaxName.text="TAX ${tax_percent}%"
        txtTaxVal.text=getString(R.string.currency,tax)
        txtPackVal.text=getString(R.string.currency,package_price)
        txtTotalVal.text=getString(R.string.currency,final_price)


    }

    private fun clickView() {
        imgBackHeader.setOnClickListener { onBackPressed() }
        btnDownload.setOnClickListener {
            //dialog for download invoice confirmation
            showDownloadPopUp(getString(R.string.download_confirmation), getString(R.string.download_invoice))
        }
        btnViewBrowse.setOnClickListener {
            //show invoice on browser
            var view_link=intent.getStringExtra(Utility.key.view_link)!!
            view_link = "https://drive.google.com/viewerng/viewer?embedded=true&url=$view_link"
            startActivity(
                Intent(this, InformativeActivity::class.java)
                .putExtra("Title", "Invoice")
                .putExtra("invoice", true)
                .putExtra("URLs", view_link))
        }
    }

    private fun showDownloadPopUp(title: String, subTitle: String) {
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
        txtDelete.text="Download"
        txtDelete.setOnClickListener { dialog.dismiss()
            Utility.show_progress_doanload(this)
            //download invoice in external storage
            downloadFile(download_link!!, getFileNameFromURL(download_link)!!)
        }
        dialog.show()
    }

    override fun onPostResume() {
        super.onPostResume()
        //Create App Folder To Save Files - Start
        if (!dataFolder!!.exists()) {
            dataFolder!!.mkdirs()
        }
    }
    fun openFile(): Intent? {
        try {
            val file = File("${dataFolder!!.absolutePath}/${getFileNameFromURL(download_link)!!}")
            val install = Intent(Intent.ACTION_VIEW)
            val contentUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                FileProvider.getUriForFile(
                    this,
                    "$packageName.provider_paths",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            install.setDataAndType(contentUri, "application/pdf")
            return install

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun downloadFile(fileLink: String, fileName: String) {
        val downloadLocation: String = dataFolder!!.absolutePath
        PRDownloader.download(fileLink, downloadLocation, fileName)
            .build()
            .setOnStartOrResumeListener {
                btnDownload.isEnabled = false
                txDownloadStatus.text = "Downloading..."
                Log.d("logDownloadInfo", "Download Start/Resume")
                btnDownload.text="Download"
            }
            .setOnPauseListener {
                btnDownload.isEnabled = true
                txDownloadStatus.text = "Paused..."
                Log.d("logDownloadInfo", "Download Paused")
                btnDownload.text="Download"
            }
            .setOnCancelListener {
                btnDownload.isEnabled = true
                txDownloadStatus.text = "Cancelled..."
                Log.d("logDownloadInfo", "Download Cancelled")
                Utility.hide_progress_doanload(this)
                btnDownload.text="Download"
            }
            .setOnProgressListener { progress: Progress ->
                txDownloadStatus.text = "Downloading..."
                btnDownload.text="Downloading..."
                val progressPercent = progress.currentBytes * 100 / progress.totalBytes
                pbDownloadProgress.progress = progressPercent.toInt()
                txDownloadStatus.text = "$progressPercent%"
                txMbCurrent.text = getBytesToMBString(progress.currentBytes)
                txMbTotal.text = getBytesToMBString(progress.totalBytes)
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Utility.hide_progress_doanload(this@PaymentOrderDetailActivity)
                    btnDownload.setEnabled(true)
                    txDownloadStatus.text = "File Downloaded!"
                    btnDownload.text="Open file"
                    btnDownload.setOnClickListener {
                        startActivity(openFile())
                    }
                    Log.d("logDownloadInfo", "Download Complete")
                }

                override fun onError(error: Error) {
                    btnDownload.text=getString(R.string.download)
                    Utility.hide_progress_doanload(this@PaymentOrderDetailActivity)
                    txDownloadStatus.text = "Error: " + error.serverErrorMessage
                    Log.d("logDownloadInfo", "Download Error: " + error.serverErrorMessage)
                    Log.d("logDownloadInfo", "Download Error: " + error.connectionException)
                    Log.d("logDownloadInfo", "Download Error: " + error.responseCode)
                }
            })
    }
    private fun getBytesToMBString(bytes: Long): String? {
        return String.format(Locale.ENGLISH, "%.2fMB", bytes / (1024.00 * 1024.00))
    }


}