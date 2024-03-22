package com.planout.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.planout.R
import com.planout.adapters.DateMonthViewAdapter
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.DayDateData
import com.planout.models.StoreLocationData
import com.planout.models.TimeData
import com.planout.models.TimingData
import com.whiteelephant.monthpicker.MonthPickerDialog
import kotlinx.android.synthetic.main.activity_add_reservation_company.dataAria
import kotlinx.android.synthetic.main.activity_add_reservation_company.editTableNo
import kotlinx.android.synthetic.main.activity_add_reservation_company.tablenoErr
import kotlinx.android.synthetic.main.header_normal_view.imgBackHeader
import kotlinx.android.synthetic.main.header_normal_view.txtHeader
import okhttp3.FormBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddReservationCompanyActivity : AppCompatActivity(), ApiResponse {
    val cal: Calendar = Calendar.getInstance()
    lateinit var recyclerDateList: RecyclerView
    lateinit var recyclerTimeList: RecyclerView
    lateinit var txtTime: TextView
    lateinit var layoutManager1: LinearLayoutManager
    val timings: ArrayList<TimingData> = ArrayList()
    val items: ArrayList<DayDateData> = ArrayList()
    val times = ArrayList<TimeData>()

    var selectedPeople = ""
    var selectedTable = ""
    var selectedAge = ""
    var selectedTableNo = ""
    lateinit var dateMonthAdapter: DateMonthViewAdapter
    val loctionArr = ArrayList<String>()
    val locInOutDoorArrList = ArrayList<Int>()
    val peopleArr = ArrayList<String>()
    val tableArr = ArrayList<String>()
    val ageArr = ArrayList<String>()
    val locations: ArrayList<StoreLocationData> = ArrayList()
    var selectedLocationId = ""
    var selectedLocation = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reservation_company)
        txtHeader.text = getString(R.string.reservation)
        imgBackHeader.setOnClickListener {
            onBackPressed()
        }
        storeDetailApi()
    }

    override fun onResume() {
        super.onResume()
        for (i in 0 until 10) {
            peopleArr.add((i + 1).toString())
        }
        //add default values
        tableArr.add(getString(R.string.indoor))
        tableArr.add(getString(R.string.outdoor))

        ageArr.add(getString(R.string.select))
        ageArr.add("18-25")
        ageArr.add("25-35")
        ageArr.add("35+")



    }


    private fun storeDetailApi() {
        dataAria.showOrGone(false)
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.stores + "/${Utility.getForm(this, Utility.key.store_id)!!}"
        CallApi.callAPi(mBuilder, API, this, Utility.storeDetail, true, Utility.GET, true)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.storeDetail) {
            Log.d("Response ------", result.toString())
            if (isData) {
                dataAria.showOrGone(true)

                val data = result.getJSONObject(Utility.key.data)

                /////////////////location data//////////////////
                val locationsArray = data.getJSONArray(Utility.key.locations)
                locations.clear()
                val arrLoc = ArrayList<StoreLocationData>()
                val locArr = ArrayList<String>()
                val locInOutDoorArr = ArrayList<Int>()
                for (i in 0 until locationsArray.length()) {
                    val locationdataObj = locationsArray.getJSONObject(i)
                    val item = StoreLocationData()
                    item.id = locationdataObj.getString(Utility.key.id)
                    item.store_id = locationdataObj.getString(Utility.key.store_id)
                    item.city_id = locationdataObj.getString(Utility.key.city_id)
                    item.city_name = locationdataObj.getString(Utility.key.city_name)
                    item.area = locationdataObj.getString(Utility.key.area)
                    item.address = locationdataObj.getString(Utility.key.address)
                    item.address1 = locationdataObj.getString(Utility.key.address1)
                    item.postal_code = locationdataObj.getString(Utility.key.postal_code)
                    item.latitude = locationdataObj.getString(Utility.key.latitude)
                    item.longitude = locationdataObj.getString(Utility.key.longitude)
                    item.address_type = locationdataObj.getString(Utility.key.address_type)
                    item.table_indoor = locationdataObj.getString(Utility.key.table_indoor)
                    item.table_outdoor = locationdataObj.getString(Utility.key.table_outdoor)
                    item.is_default = locationdataObj.getBoolean(Utility.key.is_default)
                    item.locationObj = locationdataObj.toString()
                    if (locationdataObj.getBoolean(Utility.key.is_default)) {
                        item.isSelected = true
                        locations.add(item)
                        loctionArr.add(locationdataObj.getString(Utility.key.address))
                        locInOutDoorArrList.add(
                            getIndoorOutdoor(
                                locationdataObj.getInt(Utility.key.table_indoor),
                                locationdataObj.getInt(Utility.key.table_outdoor)
                            )
                        )
                    } else {
                        arrLoc.add(item)
                        locArr.add(locationdataObj.getString(Utility.key.address))
                        locInOutDoorArr.add(
                            getIndoorOutdoor(
                                locationdataObj.getInt(Utility.key.table_indoor),
                                locationdataObj.getInt(Utility.key.table_outdoor)
                            )
                        )
                    }
                }
                locations.addAll(arrLoc)
                loctionArr.addAll(locArr)
                locInOutDoorArrList.addAll(locInOutDoorArr)


                /////////////timing data///////////////////////////////
                val timingsArray = data.getJSONArray(Utility.key.timings)
                for (i in 0 until timingsArray.length()) {
                    val item = TimingData()
                    val timingObj = timingsArray.getJSONObject(i)
                    item.store_id = timingObj.getString("store_id")
                    item.day_of_week = timingObj.getString("day_of_week")
                    item.starttime = timingObj.getString("starttime")
                    item.endtime = timingObj.getString("endtime")
                    item.starttime1 = timingObj.getString("starttime1")
                    item.endtime1 = timingObj.getString("endtime1")
                    item.is_open = timingObj.getString("is_open")
                    timings.add(item)

                }
                setView()
            }
        }else if (type==Utility.reservationsCreateByStore){
            if (isData) {
                Utility.customSuccessToast(this, result.getString(Utility.key.message))
                finish()
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        }
    }
    private fun getIndoorOutdoor(indoor: Int, outdoor: Int): Int {
        if (indoor == 0 && outdoor == 0){
            return 0
        }else if (indoor == 1 && outdoor == 1){
            return 0
        }else if (indoor == 1 && outdoor == 0){
            return 1
        }else if (indoor == 0 && outdoor == 1){
            return 2
        }else{
            return 0
        }
    }

    fun setView() {
     
        layoutManager1 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerDateList = findViewById(R.id.recyclerDateList)
        recyclerTimeList = findViewById(R.id.recyclerTimeList)
        txtTime = findViewById(R.id.txtTime)

        val txtDate = findViewById<TextView>(R.id.txtDate)
        when {
            cal.get(Calendar.MONTH) + 1 == 1 -> {
                txtDate.text = getString(R.string.calendar_jan) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 2 -> {
                txtDate.text = getString(R.string.calendar_Feb) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 3 -> {
                txtDate.text = getString(R.string.calendar_Mar) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 4 -> {
                txtDate.text = getString(R.string.calendar_Apr) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 5 -> {
                txtDate.text = getString(R.string.calendar_May) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 6 -> {
                txtDate.text = getString(R.string.calendar_Jun) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 7 -> {
                txtDate.text = getString(R.string.calendar_May) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 8 -> {
                txtDate.text = getString(R.string.calendar_Aug) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 9 -> {
                txtDate.text = getString(R.string.calendar_Sep) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 10 -> {
                txtDate.text = getString(R.string.calendar_Oct) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 11 -> {
                txtDate.text = getString(R.string.calendar_Nov) + " " + cal.get(Calendar.YEAR)
            }
            cal.get(Calendar.MONTH) + 1 == 12 -> {
                txtDate.text = getString(R.string.calendar_Dec) + " " + cal.get(Calendar.YEAR)
            }
        }
        printDatesInMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, "auto")

        val editMessage = findViewById<EditText>(R.id.editMessage)
        val editName = findViewById<EditText>(R.id.editName)
        val editPhone = findViewById<EditText>(R.id.editPhone)
        val txtChar = findViewById<TextView>(R.id.txtChar)
        val dateErr = findViewById<TextView>(R.id.dateErr)
        val timeErr = findViewById<TextView>(R.id.timeErr)
        val locErr = findViewById<TextView>(R.id.locErr)
        val fullNameErr = findViewById<TextView>(R.id.fullNameErr)
        val phoneErr = findViewById<TextView>(R.id.phoneErr)
        val nestedView = findViewById<NestedScrollView>(R.id.nestedView)
        editName.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editName, fullNameErr, false, "")
        }
        editPhone.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editPhone, phoneErr, false, "")
        }

        editMessage.doOnTextChanged { text, start, before, count ->
            txtChar.text = "${text.toString().length}/300"
        }
        editTableNo.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editTableNo, tablenoErr, false, "")
        }


        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        setArrayType(spinnerType, tableArr)

        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item2, loctionArr)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                locErr.showOrGone(false)
                selectedLocationId = locations[position].id
                selectedLocation = locations[position].address
                if (locInOutDoorArrList[position] == 0){
                    tableArr.clear()
                    tableArr.add(getString(R.string.indoor))
                    tableArr.add(getString(R.string.outdoor))
                }else if (locInOutDoorArrList[position] == 1){
                    tableArr.clear()
                    tableArr.add(getString(R.string.indoor))
                }else if (locInOutDoorArrList[position] == 2){
                    tableArr.clear()
                    tableArr.add(getString(R.string.outdoor))
                }
                setArrayType(spinnerType, tableArr)
                if (locInOutDoorArrList[position]==0){
                    spinnerType.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        val spinnerPeople = findViewById<Spinner>(R.id.spinnerPeople)
        val adapter2 = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item2, peopleArr)
        spinnerPeople.adapter = adapter2
        spinnerPeople.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPeople = peopleArr[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }


        val spinnerAgeGroup = findViewById<Spinner>(R.id.spinnerAgeGroup)
        val adapterAgeGroup = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item2, ageArr)
        spinnerAgeGroup.adapter = adapterAgeGroup
        spinnerAgeGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(ageArr[position] == getString(R.string.select)){
                    selectedAge = ""

                }else{
                    selectedAge = ageArr[position]

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }




        val btnYes = findViewById<Button>(R.id.btnConfirmReserve)

        Utility.animationClick(btnYes).setOnClickListener {
            var selectedDate = ""
            var selectedTime = ""
//            for (i in 0 until items.size) {
//                if (items[i].getClicked()) {
//                    selectedDate = items[i].getFull_Date()
//                    dateErr.showOrGone(false)
//                }
//            }
            for (i in 0 until dateMonthAdapter.times.size) {
                if (dateMonthAdapter.times[i].isSelected) {
                    selectedDate = SimpleDateFormat(
                        Utility.api_date_format,
                        Locale.ENGLISH
                    ).format(times[i].fullDate)

                    selectedTime = Utility.formatdatetime(dateMonthAdapter.times[i].time,Utility.time_format,Utility.api_time)!!
                    timeErr.showOrGone(false)
                    dateErr.showOrGone(false)
                }
            }

            //validation for table reservation
            if (selectedDate == "") {
                //Utility.normal_toast(this, "Select your reservation date")
                dateErr.showOrGone(true)
                dateErr.text = getString(R.string.select_reservation_date)
                nestedView.fullScroll(ScrollView.FOCUS_UP)
            } else if (selectedTime == "") {
                //Utility.normal_toast(this, "Select your reservation time")
                timeErr.showOrGone(true)
                timeErr.text = getString(R.string.select_reservation_time)
                nestedView.fullScroll(ScrollView.FOCUS_UP)
            } else if (selectedLocationId.isBlank()) {
                //Utility.normal_toast(this, "Select restaurant location")
                locErr.showOrGone(true)
                locErr.text = getString(R.string.select_restaurant_location)
                nestedView.fullScroll(ScrollView.FOCUS_UP)
            }else if (selectedAge.isBlank()) {
                Utility.normal_toast(this, getString(R.string.select_age_group))
            }else if (editTableNo.text.toString().isBlank()) {
                //Utility.normal_toast(this, "Enter your mobile number")
                editTableNo.requestFocus()
                Utility.showGoneErrorView(editTableNo, tablenoErr, true, getString(R.string.enter_table_number))
            } else if (editName.text.toString().isBlank()) {
                //Utility.normal_toast(this, "Enter your name")
                editName.requestFocus()
                Utility.showGoneErrorView(editName, fullNameErr, true, getString(R.string.msg_name_valid_sub))
            } else if (editPhone.text.toString().isBlank()) {
                //Utility.normal_toast(this, "Enter your mobile number")
                editPhone.requestFocus()
                Utility.showGoneErrorView(editPhone, phoneErr, true, getString(R.string.enter_mobile_number))
            } else {
                createReservationApi(
                    selectedDate,
                    selectedTime,
                    editName.text.toString(),
                    editPhone.text.toString(),
                    selectedLocationId,
                    selectedPeople,
                    selectedTable,
                    editMessage.text.toString(),
                    selectedLocation,
                    selectedAge,
                    editTableNo.text.toString(),
                )
            }

        }

        txtDate.setOnClickListener {
            val builder = MonthPickerDialog.Builder(
                this,
                MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
                    Log.d("TAG", "selectedMonth : $selectedMonth selectedYear : $selectedYear")

                    when {
                        selectedMonth + 1 == 1 -> {
                            txtDate.text = "${getString(R.string.calendar_jan)} $selectedYear"
                        }
                        selectedMonth + 1 == 2 -> {
                            txtDate.text = "${getString(R.string.calendar_Feb)} $selectedYear"
                        }
                        selectedMonth + 1 == 3 -> {
                            txtDate.text = "${getString(R.string.calendar_Mar)} $selectedYear"
                        }
                        selectedMonth + 1 == 4 -> {
                            txtDate.text = "${getString(R.string.calendar_Apr)} $selectedYear"
                        }
                        selectedMonth + 1 == 5 -> {
                            txtDate.text = "${getString(R.string.calendar_May)} $selectedYear"
                        }
                        selectedMonth + 1 == 6 -> {
                            txtDate.text = "${getString(R.string.calendar_Jun)} $selectedYear"
                        }
                        selectedMonth + 1 == 7 -> {
                            txtDate.text = "${getString(R.string.calendar_Jul)} $selectedYear"
                        }
                        selectedMonth + 1 == 8 -> {
                            txtDate.text = "${getString(R.string.calendar_Aug)} $selectedYear"
                        }
                        selectedMonth + 1 == 9 -> {
                            txtDate.text = "${getString(R.string.calendar_Sep)} $selectedYear"
                        }
                        selectedMonth + 1 == 10 -> {
                            txtDate.text = "${getString(R.string.calendar_Oct)} $selectedYear"
                        }
                        selectedMonth + 1 == 11 -> {
                            txtDate.text = "${getString(R.string.calendar_Nov)} $selectedYear"
                        }
                        selectedMonth + 1 == 12 -> {
                            txtDate.text = "${getString(R.string.calendar_Dec)} $selectedYear"
                        }
                    }
                    if (selectedYear == cal.get(Calendar.YEAR) && selectedMonth == cal.get(Calendar.MONTH)) {
                        //auto selected month days listing
                        printDatesInMonth(selectedYear, selectedMonth + 1, "auto")
                    } else {
                        //manually selected month days listing
                        printDatesInMonth(selectedYear, selectedMonth + 1, "manual")
                    }

                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)
            )
            builder.setActivatedMonth(cal.get(Calendar.MONTH))
                .setMinYear(cal.get(Calendar.YEAR))
                .setActivatedYear(cal.get(Calendar.YEAR))
                //.setMaxYear(cal.get(Calendar.YEAR))
                .setMinMonth(cal.get(Calendar.MONTH))
                .setTitle(getString(R.string.select_month))
                .setMonthRange(cal.get(Calendar.MONTH), Calendar.DECEMBER)

                .setOnMonthChangedListener { selectedMonth ->
                    Log.d("TAG", "Selected month : $selectedMonth")
                    // Toast.makeText(MainActivity.this, " Selected month : " + selectedMonth, Toast.LENGTH_SHORT).show();
                }
                .setOnYearChangedListener { selectedYear ->
                    Log.d("TAG", "Selected year : $selectedYear")
                    // Toast.makeText(MainActivity.this, " Selected year : " + selectedYear, Toast.LENGTH_SHORT).show();
                }
                .build()
                .show()
        }

    }
    fun printDatesInMonth(year: Int, month: Int, type: String) {

        var Scrolled_pos = 0
        var isToday = false

        val fmt = SimpleDateFormat("EEE-dd",Locale.ENGLISH)
        val f = SimpleDateFormat(Utility.api_date_format,Locale.ENGLISH)
        val cal = Calendar.getInstance()
        cal.clear()
        cal[year, month - 1] = 1
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        items.clear()
        val c = Calendar.getInstance().time
        val formattedDate = f.format(c)
        for (i in 0 until daysInMonth) {
            println(fmt.format(cal.time))
            val data = DayDateData()
            val data_array = fmt.format(cal.time).split("-")
            val data_array_full_date = f.format(cal.time)
            if (type == "auto") {
                if (data_array_full_date == formattedDate) {
                    data.setDay(data_array[0])
                    data.setDate(data_array[1])
                    data.setFull_Date(data_array_full_date)
                    for (j in 0 until timings.size) {
                        if (data_array[0] == "Mon") {
                            if (timings[0].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Tue") {
                            if (timings[1].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Wed") {
                            if (timings[2].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Thu") {
                            if (timings[3].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Fri") {
                            if (timings[4].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Sat") {
                            if (timings[5].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Sun") {
                            if (timings[6].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        }

                    }

                    Scrolled_pos = i
                    isToday = true
                } else {
                    if (isToday) {
                        data.setDay(data_array[0])
                        data.setDate(data_array[1])
                        data.setFull_Date(data_array_full_date)
                        data.setClicked(false)
                        for (j in 0 until timings.size) {
                            if (data_array[0] == "Mon") {
                                if (timings[0].is_open == "0") {
                                    data.setisAvailable(false)
                                } else {
                                    data.setisAvailable(true)
                                }
                            } else if (data_array[0] == "Tue") {
                                if (timings[1].is_open == "0") {
                                    data.setisAvailable(false)
                                } else {
                                    data.setisAvailable(true)
                                }
                            } else if (data_array[0] == "Wed") {
                                if (timings[2].is_open == "0") {
                                    data.setisAvailable(false)
                                } else {
                                    data.setisAvailable(true)
                                }
                            } else if (data_array[0] == "Thu") {
                                if (timings[3].is_open == "0") {
                                    data.setisAvailable(false)
                                } else {
                                    data.setisAvailable(true)
                                }
                            } else if (data_array[0] == "Fri") {
                                if (timings[4].is_open == "0") {
                                    data.setisAvailable(false)
                                } else {
                                    data.setisAvailable(true)
                                }
                            } else if (data_array[0] == "Sat") {
                                if (timings[5].is_open == "0") {
                                    data.setisAvailable(false)
                                } else {
                                    data.setisAvailable(true)
                                }
                            } else if (data_array[0] == "Sun") {
                                if (timings[6].is_open == "0") {
                                    data.setisAvailable(false)
                                } else {
                                    data.setisAvailable(true)
                                }
                            }
                        }
                    }
                }
                if (isToday)
                    items.add(data)
            }
            if (type == "manual") {
                data.setDay(data_array[0])
                data.setDate(data_array[1])
                data.setFull_Date(data_array_full_date)
                if (i == 0) {
                    for (j in 0 until timings.size) {
                        if (data_array[0] == "Mon") {
                            if (timings[0].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Tue") {
                            if (timings[1].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Wed") {
                            if (timings[2].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Thu") {
                            if (timings[3].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Fri") {
                            if (timings[4].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Sat") {
                            if (timings[5].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        } else if (data_array[0] == "Sun") {
                            if (timings[6].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                                data.setClicked(true)

                            }
                        }

                    }

                } else {
                    data.setClicked(false)
                    for (j in 0 until timings.size) {
                        if (data_array[0] == "Mon") {
                            if (timings[0].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                            }
                        } else if (data_array[0] == "Tue") {
                            if (timings[1].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                            }
                        } else if (data_array[0] == "Wed") {
                            if (timings[2].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                            }
                        } else if (data_array[0] == "Thu") {
                            if (timings[3].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                            }
                        } else if (data_array[0] == "Fri") {
                            if (timings[4].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                            }
                        } else if (data_array[0] == "Sat") {
                            if (timings[5].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                            }
                        } else if (data_array[0] == "Sun") {
                            if (timings[6].is_open == "0") {
                                data.setisAvailable(false)
                            } else {
                                data.setisAvailable(true)
                            }
                        }
                    }

                }



                items.add(data)
            }

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
//        recyclerDateList.setHasFixedSize(true)
        dateMonthAdapter = DateMonthViewAdapter(this, items, timings, recyclerTimeList, txtTime,times)
        recyclerDateList.layoutManager = layoutManager1
        recyclerDateList.adapter = dateMonthAdapter
        //recyclerDateList.scrollToPosition(Scrolled_pos)
    }
    private fun setArrayType(spinnerType: Spinner?, tableArr: ArrayList<String>) {
        val adapter3 = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item2, tableArr)
        spinnerType!!.adapter = adapter3
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (tableArr[position] == getString(R.string.indoor)) {
                    selectedTable = "1"
                } else if (tableArr[position] == getString(R.string.outdoor)) {
                    selectedTable = "2"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
    }
    private fun createReservationApi(
        selectedDate: String,
        selectedTime: String,
        editName: String,
        editPhone: String,
        selectedLocationId: String,
        selectedPeople: String,
        selectedTable: String,
        editMessage: String,
        selectedLocation: String,
        selectedAge: String,
        selectedTableNo: String,

    ) {

        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.store_id,Utility.getForm(this, Utility.key.store_id)!!)
        mBuilder.add(Utility.key.contact_name,editName)
        mBuilder.add(Utility.key.contact_mobile,editPhone)
        mBuilder.add(Utility.key.total_people,selectedPeople)
        mBuilder.add(Utility.key.resdate, selectedDate)
        mBuilder.add(Utility.key.restime, selectedTime)
        mBuilder.add(Utility.key.location_id,selectedLocationId)
        mBuilder.add(Utility.key.location,selectedLocation)
        mBuilder.add(Utility.key.preferred_table,selectedTable)
        mBuilder.add(Utility.key.extra_notes,editMessage)
        mBuilder.add(Utility.key.age_group,selectedAge)
        mBuilder.add(Utility.key.table_no,selectedTableNo)
        CallApi.callAPi(mBuilder, ApiController.api.reservationsCreateByStore, this, Utility.reservationsCreateByStore, true, Utility.POST, true)
    }

}