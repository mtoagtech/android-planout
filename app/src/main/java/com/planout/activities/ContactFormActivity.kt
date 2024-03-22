package com.planout.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import kotlinx.android.synthetic.main.activity_contact_form.*
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale


class ContactFormActivity : AppCompatActivity() , ApiResponse {
    val items = ArrayList<String> ()
    var selectedTopic=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_contact_form)
        //set default data at 0
        items.add(getString(R.string.select_topic_first))
        txtHeader.text = getString(R.string.contact)
        clickView()
        //call api for topic listing
        topicListApi()

        editMessage.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(linMsg, msgErr, false, "")
        }

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

    private fun topicListApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.parameters, this, Utility.parameters, true, Utility.GET, true)
    }

    private fun clickView() {
        editMessage.doOnTextChanged { text, start, before, count ->
            txtChar.text = "${text.toString().length}/300"
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectedTopic= items[position]
                topicErr.showOrGone(false)
                topicErr.text = ""
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                selectedTopic = ""
            }
        }

        Utility.animationClick(imgBackHeader).setOnClickListener { onBackPressed() }
        Utility.animationClick(btnSave).setOnClickListener {
            //validation for contactus api
            if (selectedTopic==getString(R.string.select_topic_first) || selectedTopic==""){
                //Utility.customErrorToast(this,"Select topic first")
                topicErr.showOrGone(true)
                topicErr.text = getString(R.string.select_topic_first)
            }else if (editMessage.text.toString()==""){
                //Utility.customErrorToast(this,"Enter your message")
                Utility.showGoneErrorView(linMsg, msgErr, true, getString(R.string.enter_message))
            }else{
                //call api for contact us
                contactUsApi()
            }
        }
    }

    private fun contactUsApi() {
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.topic,selectedTopic)
        mBuilder.add(Utility.key.message,editMessage.text.toString())
        CallApi.callAPi(mBuilder, ApiController.api.contactus, this, Utility.contactus, true, Utility.POST, true)

    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (isData){
            if (type==Utility.parameters){
                val data=result.getJSONObject(Utility.key.data)
                if (data.has(Utility.key.contact_topics)){
                    val contact_topicsObj=data.getJSONObject(Utility.key.contact_topics)
                    val x: Iterator<*> = contact_topicsObj.keys()
                    val jsonArray = JSONArray()

                    while (x.hasNext()) {
                        val key = x.next() as String
                        jsonArray.put(contact_topicsObj[key])
                    }
                    Log.d("JSONARRAY",jsonArray.toString())
                    for (i in 0 until jsonArray.length()){
                        items.add(jsonArray.get(i).toString())
                    }
                    val adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, items)
                    spinner.adapter = adapter
                }

            }else if (type==Utility.contactus){
                if (isData){
                    //reset widgets
                    editMessage.setText("")
                    spinner.setSelection(0)
                    onBackPressed()
                }else {
                    Utility.customErrorToast(
                        this,
                        Utility.checkStringNullOrNot(result, Utility.key.message)
                    )
                }
            }
        }
    }
}