package com.planout.activities

import android.R.attr.left
import android.R.attr.right
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.github.angads25.toggle.widget.LabeledSwitch
import com.google.gson.Gson
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.CustomTimePicker
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.TimingJSONData
import kotlinx.android.synthetic.main.activity_company_work_day_hour.*
import kotlinx.android.synthetic.main.header_normal_view.*
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class CompanyWorkDayHourActivity : AppCompatActivity(), ApiResponse {

    var isMonOn = false
    var isTueOn = false
    var isWedOn = false
    var isThuOn = false
    var isFriOn = false
    var isSatOn = false
    var isSunOn = false
    val timings: ArrayList<TimingJSONData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_company_work_day_hour)
        dataAria.showOrGone(false)
        StoreTimingGetApi()

        txtHeader.text = getString(R.string.working_days_hrs)
        imgBackHeader.setOnClickListener {
            onBackPressed()
        }
        clickView()

        if(Utility.getForm(this, Utility.key.is_owner) == "1"){
            btnSave.setOnClickListener {
                Log.d("TimingDATA", Gson().toJson(timings).toString())
                val JSONooooo = JSONObject()
                JSONooooo.put("timing", JSONArray(Gson().toJson(timings)))
                Log.d("TimingDATA", JSONooooo.toString())
                CallApi.callAPiJson(
                    JSONooooo,
                    ApiController.api.stores_updatetiming,
                    this,
                    Utility.stores_updatetiming,
                    true,
                    Utility.POST,
                    true
                )
            }
        }
        else{
            btnSave.isEnabled = false
        }


    }

    private fun StoreTimingGetApi() {
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.stores + "/${Utility.getForm(this, Utility.key.store_id)}"
        CallApi.callAPi(mBuilder, API, this, Utility.storeDetail, true, Utility.GET, true)
    }

    private fun clickView() {
        //switch on-off for working days/hrs

        if(Utility.getForm(this, Utility.key.is_owner) == "1"){
            switchMon.setOnToggledListener { _, isOn ->
                timings[0].is_open = isOn
                constTimeMon.showOrGone(isOn)
                linSide1.showOrGone(isOn)
                setSwitchColor(switchMon, isOn)
            }
            switchTue.setOnToggledListener { _, isOn ->
                timings[1].is_open = isOn
                constTimeTue.showOrGone(isOn)
                linSide2.showOrGone(isOn)
                setSwitchColor(switchTue, isOn)
            }

            switchWed.setOnToggledListener { _, isOn ->
                timings[2].is_open = isOn
                constTimeWed.showOrGone(isOn)
                linSide3.showOrGone(isOn)
                setSwitchColor(switchWed, isOn)
            }

            switchThu.setOnToggledListener { _, isOn ->
                timings[3].is_open = isOn
                constTimeThu.showOrGone(isOn)
                linSide4.showOrGone(isOn)
                setSwitchColor(switchThu, isOn)
            }

            switchFri.setOnToggledListener { _, isOn ->
                timings[4].is_open = isOn
                constTimeFri.showOrGone(isOn)
                linSide5.showOrGone(isOn)
                setSwitchColor(switchFri, isOn)
            }

            switchSat.setOnToggledListener { _, isOn ->
                timings[5].is_open = isOn
                constTimeSat.showOrGone(isOn)
                linSide6.showOrGone(isOn)
                setSwitchColor(switchSat, isOn)
            }

            switchSun.setOnToggledListener { _, isOn ->
                timings[6].is_open = isOn
                constTimeSun.showOrGone(isOn)
                linSide7.showOrGone(isOn)
                setSwitchColor(switchSun, isOn)
            }




            switchMonSec.setOnToggledListener { _, isOn ->
                txtMon3.showOrGone(isOn)
                txtMon4.showOrGone(isOn)
                setSwitchColor(switchMonSec, isOn)
                if (!isOn) {
                    timings[0].starttime1 = ""
                    timings[0].endtime1 = ""
                } else {
                    timings[0].starttime1 = "16:00"
                    timings[0].endtime1 = "21:00"
                }
            }
            switchTueSec.setOnToggledListener { _, isOn ->
                txtTue3.showOrGone(isOn)
                txtTue4.showOrGone(isOn)
                setSwitchColor(switchTueSec, isOn)
                if (!isOn) {
                    timings[1].starttime1 = ""
                    timings[1].endtime1 = ""
                } else {
                    timings[1].starttime1 = "16:00"
                    timings[1].endtime1 = "21:00"
                }

            }
            switchWedSec.setOnToggledListener { _, isOn ->
                txtWed3.showOrGone(isOn)
                txtWed4.showOrGone(isOn)
                setSwitchColor(switchWedSec, isOn)
                if (!isOn) {
                    timings[2].starttime1 = ""
                    timings[2].endtime1 = ""
                } else {
                    timings[2].starttime1 = "16:00"
                    timings[2].endtime1 = "21:00"
                }

            }
            switchThuSec.setOnToggledListener { _, isOn ->
                txtThu3.showOrGone(isOn)
                txtThu4.showOrGone(isOn)
                setSwitchColor(switchThuSec, isOn)
                if (!isOn) {
                    timings[3].starttime1 = ""
                    timings[3].endtime1 = ""
                } else {
                    timings[3].starttime1 = "16:00"
                    timings[3].endtime1 = "21:00"
                }

            }
            switchFriSec.setOnToggledListener { _, isOn ->
                txtFri3.showOrGone(isOn)
                txtFri4.showOrGone(isOn)
                setSwitchColor(switchFriSec, isOn)
                if (!isOn) {
                    timings[4].starttime1 = ""
                    timings[4].endtime1 = ""
                } else {
                    timings[4].starttime1 = "16:00"
                    timings[4].endtime1 = "21:00"
                }

            }
            switchSatSec.setOnToggledListener { _, isOn ->
                txtSat3.showOrGone(isOn)
                txtSat4.showOrGone(isOn)
                setSwitchColor(switchSatSec, isOn)
                if (!isOn) {
                    timings[5].starttime1 = ""
                    timings[5].endtime1 = ""
                } else {
                    timings[5].starttime1 = "16:00"
                    timings[5].endtime1 = "21:00"
                }

            }
            switchSunSec.setOnToggledListener { _, isOn ->
                txtSun3.showOrGone(isOn)
                txtSun4.showOrGone(isOn)
                setSwitchColor(switchSunSec, isOn)
                if (!isOn) {
                    timings[6].starttime1 = ""
                    timings[6].endtime1 = ""
                } else {
                    timings[6].starttime1 = "16:00"
                    timings[6].endtime1 = "21:00"
                }

            }



        //set timing for particular click on time selection

        txtMon1.setOnClickListener {
            getTime(txtMon1, txtMon1, false, 0, 1, true, txtMon2)
        }
        txtMon2.setOnClickListener {
            getTime(txtMon2, txtMon1, true, 0, 2, true, txtMon3)
        }
        txtMon3.setOnClickListener {
            getTime(txtMon3, txtMon2, true, 0, 3, true, txtMon4)
        }
        txtMon4.setOnClickListener {
            getTime(txtMon4, txtMon3, true, 0, 4, false, txtMon4)
        }

        txtTue1.setOnClickListener {
            getTime(txtTue1, txtTue1, false, 1, 1, true, txtTue2)
        }
        txtTue2.setOnClickListener {
            getTime(txtTue2, txtTue1, true, 1, 2, true, txtTue3)
        }
        txtTue3.setOnClickListener {
            getTime(txtTue3, txtTue2, true, 1, 3, true, txtTue4)
        }
        txtTue4.setOnClickListener {
            getTime(txtTue4, txtTue3, true, 1, 4, false, txtTue4)
        }

        txtWed1.setOnClickListener {
            getTime(txtWed1, txtWed1, false, 2, 1, true, txtWed2)
        }
        txtWed2.setOnClickListener {
            getTime(txtWed2, txtWed1, true, 2, 2, true, txtWed3)
        }
        txtWed3.setOnClickListener {
            getTime(txtWed3, txtWed2, true, 2, 3, true, txtWed4)
        }
        txtWed4.setOnClickListener {
            getTime(txtWed4, txtWed3, true, 2, 4, false, txtWed4)
        }

        txtThu1.setOnClickListener {
            getTime(txtThu1, txtThu1, false, 3, 1, true, txtThu2)
        }
        txtThu2.setOnClickListener {
            getTime(txtThu2, txtThu1, true, 3, 2, true, txtThu3)
        }
        txtThu3.setOnClickListener {
            getTime(txtThu3, txtThu2, true, 3, 3, true, txtThu4)
        }
        txtThu4.setOnClickListener {
            getTime(txtThu4, txtThu3, true, 3, 4, false, txtThu4)
        }

        txtFri1.setOnClickListener {
            getTime(txtFri1, txtFri1, false, 4, 1, true, txtFri2)
        }
        txtFri2.setOnClickListener {
            getTime(txtFri2, txtFri1, true, 4, 2, true, txtFri3)
        }
        txtFri3.setOnClickListener {
            getTime(txtFri3, txtFri2, true, 4, 3, true, txtFri4)
        }
        txtFri4.setOnClickListener {
            getTime(txtFri4, txtFri3, true, 4, 4, false, txtFri4)
        }

        txtSat1.setOnClickListener {
            getTime(txtSat1, txtSat1, false, 5, 1, true, txtSat2)
        }
        txtSat2.setOnClickListener {
            getTime(txtSat2, txtSat1, true, 5, 2, true, txtSat3)
        }
        txtSat3.setOnClickListener {
            getTime(txtSat3, txtSat2, true, 5, 3, true, txtSat4)
        }
        txtSat4.setOnClickListener {
            getTime(txtSat4, txtSat3, true, 5, 4, false, txtSat4)
        }

        txtSun1.setOnClickListener {
            getTime(txtSun1, txtSun1, false, 6, 1, true, txtSun2)
        }
        txtSun2.setOnClickListener {
            getTime(txtSun2, txtSun1, true, 6, 2, true, txtSun3)
        }
        txtSun3.setOnClickListener {
            getTime(txtSun3, txtSun2, true, 6, 3, true, txtSun4)
        }
        txtSun4.setOnClickListener {
            getTime(txtSun4, txtSun3, true, 6, 4, false, txtSun4)
        }

        }
        else{
            switchMon.isEnabled = false
            switchTue.isEnabled = false
            switchWed.isEnabled = false
            switchThu.isEnabled = false
            switchFri.isEnabled = false
            switchSat.isEnabled = false
            switchSun.isEnabled = false


            switchMonSec.isEnabled = false
            switchTueSec.isEnabled = false
            switchWedSec.isEnabled = false
            switchThuSec.isEnabled = false
            switchFriSec.isEnabled = false
            switchSatSec.isEnabled = false
            switchSunSec.isEnabled = false

            txtMon1.isEnabled = false
            txtMon2.isEnabled = false
            txtMon3.isEnabled = false
            txtMon4.isEnabled = false

            txtTue1.isEnabled = false
            txtTue2.isEnabled = false
            txtTue3.isEnabled = false
            txtTue4.isEnabled = false

            txtWed1.isEnabled = false
            txtWed2.isEnabled = false
            txtWed3.isEnabled = false
            txtWed4.isEnabled = false

            txtThu1.isEnabled = false
            txtThu2.isEnabled = false
            txtThu3.isEnabled = false
            txtThu4.isEnabled = false

            txtFri1.isEnabled = false
            txtFri2.isEnabled = false
            txtFri3.isEnabled = false
            txtFri4.isEnabled = false

            txtSat1.isEnabled = false
            txtSat2.isEnabled = false
            txtSat3.isEnabled = false
            txtSat4.isEnabled = false

            txtSun1.isEnabled = false
            txtSun2.isEnabled = false
            txtSun3.isEnabled = false
            txtSun4.isEnabled = false

        }
    }

    private fun setSwitchColor(switchView: LabeledSwitch?, isOn: Boolean) {
        switchView!!.isOn = isOn
        if (isOn) {
            switchView.colorOn = ContextCompat.getColor(this, R.color.app_green)
            switchView.colorOff = ContextCompat.getColor(this, R.color.white)
        } else {
            switchView.colorOn = ContextCompat.getColor(this, R.color.white)
            switchView.colorOff = ContextCompat.getColor(this, R.color.gray_E7_48)
        }
    }

    fun getTime(
        setView: TextView,
        textView: TextView,
        setMin: Boolean,
        position: Int,
        timeNo: Int,
        setMax: Boolean,
        maxView: TextView
    ) {
        val cal = Calendar.getInstance()
        val lis = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            runOnUiThread {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                val timing =
                    SimpleDateFormat(Utility.time_format, Locale.ENGLISH).format(cal.time)
                        .replace("am", "AM")
                        .replace("pm", "PM")
                setView.text = timing.toString()
                when (timeNo) {
                    1 -> {
                        timings[position].starttime =
                            Utility.formatdatetime(timing, Utility.time_format, Utility.api_time)!!
                    }

                    2 -> {
                        timings[position].endtime =
                            Utility.formatdatetime(timing, Utility.time_format, Utility.api_time)!!
                    }

                    3 -> {
                        timings[position].starttime1 =
                            Utility.formatdatetime(timing, Utility.time_format, Utility.api_time)!!
                    }

                    4 -> {
                        timings[position].endtime1 =
                            Utility.formatdatetime(timing, Utility.time_format, Utility.api_time)!!
                    }
                }
            }
        }

        val customTimePicker: CustomTimePicker
        if (textView.text.toString() == "") {
            customTimePicker = CustomTimePicker(
                this,
                lis,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            )
        } else {
            val time = timeCoversion12to24(setView.text.toString())
            val hour = time!!.split(":")[0].toInt()
            val min = time.split(":")[1].toInt()
            customTimePicker = CustomTimePicker(this, lis, hour, min, true)
        }
//        if (setMin) {
//            val time = timeCoversion12to24(textView.text.toString())
//            val hour = time!!.split(":")[0].toInt()
//            val min = time.split(":")[1].toInt()
//            customTimePicker.setMin(hour, min)
//        }
//        if (setMax) {
//            val time = timeCoversion12to24(maxView.text.toString())
//            val hour = time!!.split(":")[0].toInt()
//            val min = time.split(":")[1].toInt()
//            customTimePicker.setMax(hour, min)
//        }
        customTimePicker.show()
    }

    @Throws(ParseException::class)
    fun timeCoversion12to24(twelveHoursTime: String?): String? {

        //Date/time pattern of input date (12 Hours format - hh used for 12 hours)
        val df: DateFormat = SimpleDateFormat(Utility.time_format, Locale.ENGLISH)

        //Date/time pattern of desired output date (24 Hours format HH - Used for 24 hours)
        val outputformat: DateFormat = SimpleDateFormat(Utility.api_time, Locale.ENGLISH)
        var date: Date? = null
        var output: String? = null

        //Returns Date object
        date = df.parse(twelveHoursTime!!)

        //old date format to new date format
        output = outputformat.format(date!!)
        println(output)
        return output
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.stores_updatetiming) {
            if (isData) {
                Utility.customSuccessToast(this, result.getString(Utility.key.message))
                onBackPressed()
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        } else if (type == Utility.storeDetail) {
            dataAria.showOrGone(isData)

            val data = result.getJSONObject(Utility.key.data)
            val timingsArray = data.getJSONArray(Utility.key.timings)
            if (timingsArray.length() == 0) {
                constTimeMon.showOrGone(false)
                constTimeTue.showOrGone(false)
                constTimeWed.showOrGone(false)
                constTimeThu.showOrGone(false)
                constTimeFri.showOrGone(false)
                constTimeSat.showOrGone(false)
                constTimeSun.showOrGone(false)
                switchMon.isOn = false
                switchTue.isOn = false
                switchWed.isOn = false
                switchThu.isOn = false
                switchFri.isOn = false
                switchSat.isOn = false
                switchSun.isOn = false

                switchMonSec.isOn = false
                switchTueSec.isOn = false
                switchWedSec.isOn = false
                switchThuSec.isOn = false
                switchFriSec.isOn = false
                switchSatSec.isOn = false
                switchSunSec.isOn = false

                //set default timing for all days
                for (i in 0 until 7) {
                    val item = TimingJSONData()
                    item.day_of_week = i + 1
                    item.starttime = "10:00"
                    item.endtime = "14:00"
                    item.starttime1 = "16:00"
                    item.endtime1 = "21:00"
                    item.is_open = false
                    timings.add(item)
                }
            } else {
                for (i in 0 until timingsArray.length()) {
                    val item = TimingJSONData()
                    val timingObj = timingsArray.getJSONObject(i)
                    item.day_of_week = timingObj.getInt("day_of_week")
                    item.starttime = Utility.formatdatetime(
                        timingObj.getString("starttime"),
                        Utility.time_format_hms,
                        Utility.api_time
                    )!!
                    item.endtime = Utility.formatdatetime(
                        timingObj.getString("endtime"),
                        Utility.time_format_hms,
                        Utility.api_time
                    )!!
                    if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                        item.starttime1 = "";
                        item.endtime1 = ""
                    } else {
                        item.starttime1 = Utility.formatdatetime(
                            timingObj.getString("starttime1"),
                            Utility.time_format_hms,
                            Utility.api_time
                        )!!
                        item.endtime1 = Utility.formatdatetime(
                            timingObj.getString("endtime1"),
                            Utility.time_format_hms,
                            Utility.api_time
                        )!!
                    }

                    item.is_open = timingObj.getInt("is_open") != 0
                    timings.add(item)

                    //set all timing data when we are getting from api response
                    when (i) {
                        0 -> {
                            constTimeMon.showOrGone(timingObj.getInt("is_open") == 1)
                            linSide1.showOrGone(timingObj.getInt("is_open") == 1)
                            setSwitchColor(switchMon, timingObj.getInt("is_open") == 1)
                            txtMon1.text = Utility.formatdatetime(
                                timingObj.getString("starttime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            txtMon2.text = Utility.formatdatetime(
                                timingObj.getString("endtime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                                switchMonSec.isOn = false
                                setSwitchColor(switchMonSec, false)
                                txtMon3.showOrGone(false)
                                txtMon4.showOrGone(false)
                            } else {
                                switchMonSec.isOn = true
                                setSwitchColor(switchMonSec, true)
                                txtMon3.showOrGone(true)
                                txtMon4.showOrGone(true)
                                txtMon3.text = Utility.formatdatetime(
                                    timingObj.getString("starttime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                                txtMon4.text = Utility.formatdatetime(
                                    timingObj.getString("endtime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                            }

                        }

                        1 -> {
                            constTimeTue.showOrGone(timingObj.getInt("is_open") == 1)
                            linSide2.showOrGone(timingObj.getInt("is_open") == 1)
                            setSwitchColor(switchTue, timingObj.getInt("is_open") == 1)
                            txtTue1.text = Utility.formatdatetime(
                                timingObj.getString("starttime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            txtTue2.text = Utility.formatdatetime(
                                timingObj.getString("endtime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                                switchTueSec.isOn = false
                                setSwitchColor(switchTueSec, false)
                                txtTue3.showOrGone(false)
                                txtTue4.showOrGone(false)
                            } else {

                                switchTueSec.isOn = true
                                setSwitchColor(switchTueSec, true)
                                txtTue3.showOrGone(true)
                                txtTue4.showOrGone(true)
                                txtTue3.text = Utility.formatdatetime(
                                    timingObj.getString("starttime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                                txtTue4.text = Utility.formatdatetime(
                                    timingObj.getString("endtime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                            }
                        }

                        2 -> {
                            constTimeWed.showOrGone(timingObj.getInt("is_open") == 1)
                            linSide3.showOrGone(timingObj.getInt("is_open") == 1)
                            setSwitchColor(switchWed, timingObj.getInt("is_open") == 1)
                            txtWed1.text = Utility.formatdatetime(
                                timingObj.getString("starttime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            txtWed2.text = Utility.formatdatetime(
                                timingObj.getString("endtime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                                switchWedSec.isOn = false
                                setSwitchColor(switchWedSec, false)
                                txtWed3.showOrGone(false)
                                txtWed4.showOrGone(false)
                            } else {
                                switchWedSec.isOn = true
                                setSwitchColor(switchWedSec, true)
                                txtWed3.showOrGone(true)
                                txtWed4.showOrGone(true)
                                txtWed3.text = Utility.formatdatetime(
                                    timingObj.getString("starttime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                                txtWed4.text = Utility.formatdatetime(
                                    timingObj.getString("endtime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                            }
                        }

                        3 -> {
                            constTimeThu.showOrGone(timingObj.getInt("is_open") == 1)
                            linSide4.showOrGone(timingObj.getInt("is_open") == 1)
                            setSwitchColor(switchThu, timingObj.getInt("is_open") == 1)
                            txtThu1.text = Utility.formatdatetime(
                                timingObj.getString("starttime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            txtThu2.text = Utility.formatdatetime(
                                timingObj.getString("endtime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                                switchThuSec.isOn = false
                                setSwitchColor(switchThuSec, false)
                                txtThu3.showOrGone(false)
                                txtThu4.showOrGone(false)
                            } else {
                                switchThuSec.isOn = true
                                setSwitchColor(switchThuSec, true)
                                txtThu3.showOrGone(true)
                                txtThu4.showOrGone(true)
                                txtThu3.text = Utility.formatdatetime(
                                    timingObj.getString("starttime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                                txtThu4.text = Utility.formatdatetime(
                                    timingObj.getString("endtime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                            }
                        }

                        4 -> {
                            constTimeFri.showOrGone(timingObj.getInt("is_open") == 1)
                            linSide5.showOrGone(timingObj.getInt("is_open") == 1)
                            setSwitchColor(switchFri, timingObj.getInt("is_open") == 1)
                            txtFri1.text = Utility.formatdatetime(
                                timingObj.getString("starttime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            txtFri2.text = Utility.formatdatetime(
                                timingObj.getString("endtime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                                switchFriSec.isOn = false
                                setSwitchColor(switchFriSec, false)
                                txtFri3.showOrGone(false)
                                txtFri4.showOrGone(false)
                            } else {
                                switchFriSec.isOn = true
                                setSwitchColor(switchFriSec, true)
                                txtFri3.showOrGone(true)
                                txtFri4.showOrGone(true)

                                txtFri3.text = Utility.formatdatetime(
                                    timingObj.getString("starttime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                                txtFri4.text = Utility.formatdatetime(
                                    timingObj.getString("endtime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                            }
                        }

                        5 -> {
                            constTimeSat.showOrGone(timingObj.getInt("is_open") == 1)
                            linSide6.showOrGone(timingObj.getInt("is_open") == 1)
                            setSwitchColor(switchSat, timingObj.getInt("is_open") == 1)
                            txtSat1.text = Utility.formatdatetime(
                                timingObj.getString("starttime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            txtSat2.text = Utility.formatdatetime(
                                timingObj.getString("endtime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                                switchSatSec.isOn = false
                                setSwitchColor(switchSatSec, false)
                                txtSat3.showOrGone(false)
                                txtSat4.showOrGone(false)
                            } else {
                                switchSatSec.isOn = true
                                setSwitchColor(switchSatSec, true)
                                txtSat3.showOrGone(true)
                                txtSat4.showOrGone(true)

                                txtSat3.text = Utility.formatdatetime(
                                    timingObj.getString("starttime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                                txtSat4.text = Utility.formatdatetime(
                                    timingObj.getString("endtime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                            }
                        }

                        6 -> {
                            constTimeSun.showOrGone(timingObj.getInt("is_open") == 1)
                            linSide7.showOrGone(timingObj.getInt("is_open") == 1)
                            setSwitchColor(switchSun, timingObj.getInt("is_open") == 1)
                            txtSun1.text = Utility.formatdatetime(
                                timingObj.getString("starttime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            txtSun2.text = Utility.formatdatetime(
                                timingObj.getString("endtime"),
                                Utility.time_format_hms,
                                Utility.time_format
                            )!!.replace("am", "AM").replace("pm", "PM")
                            if (timingObj.getString("starttime1") == "null" || timingObj.getString("endtime1") == "null") {
                                switchSunSec.isOn = false
                                setSwitchColor(switchSunSec, false)
                                txtSun3.showOrGone(false)
                                txtSun4.showOrGone(false)
                            } else {
                                switchSunSec.isOn = true
                                setSwitchColor(switchSunSec, true)
                                txtSun3.showOrGone(true)
                                txtSun4.showOrGone(true)

                                txtSun3.text = Utility.formatdatetime(
                                    timingObj.getString("starttime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                                txtSun4.text = Utility.formatdatetime(
                                    timingObj.getString("endtime1"),
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )!!.replace("am", "AM").replace("pm", "PM")
                            }
                        }
                    }
                }
            }
        }
    }
}