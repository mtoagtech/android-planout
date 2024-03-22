package com.planout.activities


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.planout.R
import com.planout.adapters.*
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import com.planout.constant.Utility.checkStringNullOrEmpty
import com.planout.constant.Utility.showOrGone
import com.planout.models.*
import com.planout.retrofit.DataInterface
import com.whiteelephant.monthpicker.MonthPickerDialog
import kotlinx.android.synthetic.main.activity_business_details.*
import okhttp3.FormBody
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class BusinessDetailsActivity : AppCompatActivity(), ApiResponse, OnMapReadyCallback,
    DataInterface {
    var isRemoveFav = ""
    val items: ArrayList<DayDateData> = ArrayList()
    val locations: ArrayList<StoreLocationData> = ArrayList()

    lateinit var layoutManager: LinearLayoutManager
    lateinit var layoutManager1: LinearLayoutManager
    lateinit var tagsAdapter: TagsViewAdapter
    lateinit var alsoLikeAdapter: BeachViewAdapter
    lateinit var upcomingEventAdapter: UpcomingEventViewAdapter
    lateinit var locateAdapter: AddressLocateViewAdapter
    lateinit var photoAdapter: PhotosViewAdapter
    var tags: ArrayList<TagData> = ArrayList()

    val recordsList: ArrayList<StoreModel> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    var resultLauncherFavorite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val ID = data!!.getStringExtra("ID")!!
                val isRemoveFav = data.getStringExtra("isRemoveFav")
                if (isRemoveFav == "true") {
                    for (i in 0 until recordsList.size) {
                        if (recordsList[i].id == ID) {
                            recordsList[i].is_favorite = "false"
                            alsoLikeAdapter.notifyDataSetChanged()
                            break
                        }
                    }
                } else if (isRemoveFav == "false") {
                    for (i in 0 until recordsList.size) {
                        if (recordsList[i].id == ID) {
                            recordsList[i].is_favorite = "true"
                            alsoLikeAdapter.notifyDataSetChanged()
                            break
                        }
                    }
                }

            }
        }


    val timings: ArrayList<TimingData> = ArrayList()
    val times = ArrayList<TimeData>()

    var strStoreId = ""
    var latLng: LatLng? = null
    var selectedLocationId = ""
    var selectedLocation = ""
    var selectedPeople = ""
    var selectedTable = ""
    var selectedAge = ""
    var selectedTableNo = ""
    var arrTags: String = ""
    var arrIndus: String = ""
    var firstTagName = ""
    var firstIndusName = ""
    var jsonObj = JsonObject()
    var isOpened = false

    lateinit var dateMonthAdapter: DateMonthViewAdapter
    lateinit var recyclerDateList: RecyclerView
    lateinit var recyclerTimeList: RecyclerView
    lateinit var txtTime: TextView
    private var arrPhotos: ArrayList<StoreMediaData> = ArrayList()
    var storeList: ArrayList<StoreModel> = ArrayList()
    val loctionArr = ArrayList<String>()
    val locInOutDoorArrList = ArrayList<Int>()
    val peopleArr = ArrayList<String>()
    val tableArr = ArrayList<String>()
    val ageArr = ArrayList<String>()
    val tableNoArr = ArrayList<String>()
    val cal: Calendar = Calendar.getInstance()

    companion object {
        var is_favorite = ""
        var storeId = ""
        fun setFavView(id: String, name: String, status: String) {
            if (storeId == id) {
                is_favorite = status
                BusinessDetailsActivity().addSetData(id, name, is_favorite)
            }
        }
    }

    override fun addSetData(id: String, name: String, status: String) {
        setView(status)
    }

    fun setView(isFav: String) {
        if (isFav == "true") {
            imgFavorites.setImageResource(R.drawable.ic_icon_fav_true)
        } else {
            imgFavorites.setImageResource(R.drawable.ic_icon_fav_day_night)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_details)

        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        strStoreId = intent.getStringExtra(Utility.key.id)!!
        storeId = intent.getStringExtra(Utility.key.id)!!
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


        clickView()
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

        tableNoArr.add("AB1")
        tableNoArr.add("AB2")
        tableNoArr.add("AB3")
        tableNoArr.add("AB4")
        tableNoArr.add("AB5")
        //call api for store details
        storeDetailApi()
    }

    private fun storeDetailApi() {
        dataAria.showOrGone(false)
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.stores + "/${strStoreId}"
        CallApi.callAPi(mBuilder, API, this, Utility.storeDetail, true, Utility.GET, true)
    }

    fun alertDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.working_days_hrs_popup_view_new)
        val dialogButton: TextView = dialog.findViewById(R.id.txtClose) as TextView
        val txtMonVal: TextView = dialog.findViewById(R.id.txtMonVal)
        val txtTueVal: TextView = dialog.findViewById(R.id.txtTueVal)
        val txtWedVal: TextView = dialog.findViewById(R.id.txtWedVal)
        val txtThuVal: TextView = dialog.findViewById(R.id.txtThuVal)
        val txtFriVal: TextView = dialog.findViewById(R.id.txtFriVal)
        val txtSatVal: TextView = dialog.findViewById(R.id.txtSatVal)
        val txtSunVal: TextView = dialog.findViewById(R.id.txtSunVal)
        for (i in 0 until timings.size) {
            when (i) {
                0 -> {
                    setTimingData(timings[i], txtMonVal)
                }

                1 -> {
                    setTimingData(timings[i], txtTueVal)
                }

                2 -> {
                    setTimingData(timings[i], txtWedVal)
                }

                3 -> {
                    setTimingData(timings[i], txtThuVal)
                }

                4 -> {
                    setTimingData(timings[i], txtFriVal)
                }

                5 -> {
                    setTimingData(timings[i], txtSatVal)
                }

                6 -> {
                    setTimingData(timings[i], txtSunVal)
                }
            }
        }
        dialogButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setTimingData(timingData: TimingData, txtView: TextView) {
        val startTime = Utility.formatdatetime(
            timingData.starttime,
            Utility.time_format_hms,
            Utility.time_format
        )
        val endTime =
            Utility.formatdatetime(timingData.endtime, Utility.time_format_hms, Utility.time_format)
        if (timingData.starttime1 == "null" || timingData.endtime1 == "null") {
            if (timingData.is_open == "0") { //close for full day
                txtView.text = "Closed"
            } else { //set open-close timing
                txtView.text = "$startTime to $endTime"
            }

        } else {
            val startTime1 = Utility.formatdatetime(
                timingData.starttime1,
                Utility.time_format_hms,
                Utility.time_format
            )
            val endTime1 = Utility.formatdatetime(
                timingData.endtime1,
                Utility.time_format_hms,
                Utility.time_format
            )
            if (timingData.is_open == "0") { //close for full day
                txtView.text = "Closed"
            } else { //set open-close timing
                txtView.text = "$startTime to $endTime\n$startTime1 to $endTime1"
            }

        }

    }

    override fun onBackPressed() {
        val id = intent.getStringExtra(Utility.key.id)!!
        val intent = Intent()
        intent.putExtra("isRemoveFav", isRemoveFav)
        intent.putExtra("ID", id)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun clickView() {
        alsoLikeSeeAll.setOnClickListener {
            startActivity(
                Intent(this, VisitorSearchActivity::class.java)
                    .putExtra("Title", "")
                    .putExtra("jsonVal", jsonObj.toString())
            )
        }
        btnReserveTable.setOnClickListener {
            if (Utility.isLoginCheck(this)) {
                //bottom dialog for table reservation
                openReserveTableDialog()
            }
        }
        txtDateTimeVal.setOnClickListener {
            //alert popup for working days/hrs
            alertDialog()
        }
        imgBack.setOnClickListener {
            onBackPressed()
        }
        btnCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CALL_PHONE),
                    5
                )
            } else {
                val callNumber = contactNum.text.toString()
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$callNumber"))
                startActivity(intent)
            }
        }
    }


    fun openReserveTableDialog() {
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_reserve_table_view_popup, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        layoutManager1 = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerDateList = dialogView.findViewById(R.id.recyclerDateList)
        recyclerTimeList = dialogView.findViewById(R.id.recyclerTimeList)
        txtTime = dialogView.findViewById(R.id.txtTime)

        val txtDate = dialogView.findViewById<TextView>(R.id.txtDate)
        when {
            cal.get(Calendar.MONTH) + 1 == 1 -> {
                txtDate.text = "Jan" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 2 -> {
                txtDate.text = "Feb" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 3 -> {
                txtDate.text = "Mar" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 4 -> {
                txtDate.text = "Apr" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 5 -> {
                txtDate.text = "May" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 6 -> {
                txtDate.text = "Jun" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 7 -> {
                txtDate.text = "Jul" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 8 -> {
                txtDate.text = "Aug" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 9 -> {
                txtDate.text = "Sep" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 10 -> {
                txtDate.text = "Oct" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 11 -> {
                txtDate.text = "Nov" + " " + cal.get(Calendar.YEAR)
            }

            cal.get(Calendar.MONTH) + 1 == 12 -> {
                txtDate.text = "Dec" + " " + cal.get(Calendar.YEAR)
            }
        }
        printDatesInMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, "auto")

        val editMessage = dialogView.findViewById<EditText>(R.id.editMessage)
        val editName = dialogView.findViewById<EditText>(R.id.editName)
        val editPhone = dialogView.findViewById<EditText>(R.id.editPhone)
        val editTableNo = dialogView.findViewById<EditText>(R.id.editTableNo)
        val txtChar = dialogView.findViewById<TextView>(R.id.txtChar)
        val dateErr = dialogView.findViewById<TextView>(R.id.dateErr)
        val timeErr = dialogView.findViewById<TextView>(R.id.timeErr)
        val locErr = dialogView.findViewById<TextView>(R.id.locErr)
        val tablenoErr = dialogView.findViewById<TextView>(R.id.tablenoErr)
        val fullNameErr = dialogView.findViewById<TextView>(R.id.fullNameErr)
        val phoneErr = dialogView.findViewById<TextView>(R.id.phoneErr)
        val nestedView = dialogView.findViewById<NestedScrollView>(R.id.nestedView)
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

        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerType)
        setArrayType(spinnerType, tableArr)

        val spinner = dialogView.findViewById<Spinner>(R.id.spinner)
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
                if (locInOutDoorArrList[position] == 0) {
                    tableArr.clear()
                    tableArr.add(getString(R.string.indoor))
                    tableArr.add(getString(R.string.outdoor))
                } else if (locInOutDoorArrList[position] == 1) {
                    tableArr.clear()
                    tableArr.add(getString(R.string.indoor))
                } else if (locInOutDoorArrList[position] == 2) {
                    tableArr.clear()
                    tableArr.add(getString(R.string.outdoor))
                }
                setArrayType(spinnerType, tableArr)
                if (locInOutDoorArrList[position] == 0) {
                    spinnerType.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        val spinnerPeople = dialogView.findViewById<Spinner>(R.id.spinnerPeople)
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


        val spinnerAgeGroup = dialogView.findViewById<Spinner>(R.id.spinnerAgeGroup)
        val adapterAgeGroup = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item2, ageArr)
        spinnerAgeGroup.adapter = adapterAgeGroup
        spinnerAgeGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedAge = if(ageArr[position] == getString(R.string.select)){
                    ""

                }else{
                    ageArr[position]

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }





        val btnYes = dialogView.findViewById<Button>(R.id.btnConfirmReserve)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)

        Utility.animationClick(btnYes).setOnClickListener {
            var selectedDate = ""
            var selectedTime = ""
//            for (i in 0 until items.size) {
//                if (items[i].getClicked()) {
//                    dateErr.showOrGone(false)
//                }
//            }
            for (i in 0 until dateMonthAdapter.times.size) {
                if (dateMonthAdapter.times[i].isSelected) {

                    selectedDate = SimpleDateFormat(
                        Utility.api_date_format,
                        Locale.ENGLISH
                    ).format(times[i].fullDate)

                    selectedTime = Utility.formatdatetime(
                        dateMonthAdapter.times[i].time,
                        Utility.time_format,
                        Utility.api_time
                    )!!
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
            } else if (editName.text.toString().isBlank()) {
                //Utility.normal_toast(this, "Enter your name")
                editName.requestFocus()
                Utility.showGoneErrorView(editName, fullNameErr, true, getString(R.string.msg_name_valid_sub))
            } else if (editPhone.text.toString().isBlank()) {
                //Utility.normal_toast(this, "Enter your mobile number")
                editPhone.requestFocus()
                Utility.showGoneErrorView(editPhone, phoneErr, true, getString(R.string.enter_mobile_number))
            }else {
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
                    editTableNo.text.toString()
                )
                serviceInfo_dialog.dismiss()
            }

        }
        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }

        txtDate.setOnClickListener {
            val builder = MonthPickerDialog.Builder(
                this,
                MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
                    Log.d("TAG", "selectedMonth : $selectedMonth selectedYear : $selectedYear")

                    when {
                        selectedMonth + 1 == 1 -> {
                            txtDate.text = "Jan $selectedYear"
                        }

                        selectedMonth + 1 == 2 -> {
                            txtDate.text = "Feb $selectedYear"
                        }

                        selectedMonth + 1 == 3 -> {
                            txtDate.text = "Mar $selectedYear"
                        }

                        selectedMonth + 1 == 4 -> {
                            txtDate.text = "Apr $selectedYear"
                        }

                        selectedMonth + 1 == 5 -> {
                            txtDate.text = "May $selectedYear"
                        }

                        selectedMonth + 1 == 6 -> {
                            txtDate.text = "Jun $selectedYear"
                        }

                        selectedMonth + 1 == 7 -> {
                            txtDate.text = "Jul $selectedYear"
                        }

                        selectedMonth + 1 == 8 -> {
                            txtDate.text = "Aug $selectedYear"
                        }

                        selectedMonth + 1 == 9 -> {
                            txtDate.text = "Sep $selectedYear"
                        }

                        selectedMonth + 1 == 10 -> {
                            txtDate.text = "Oct $selectedYear"
                        }

                        selectedMonth + 1 == 11 -> {
                            txtDate.text = "Nov $selectedYear"
                        }

                        selectedMonth + 1 == 12 -> {
                            txtDate.text = "Dec $selectedYear"
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

        serviceInfo_dialog.show()
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
        mBuilder.add(Utility.key.store_id, intent.getStringExtra(Utility.key.id)!!)
        mBuilder.add(Utility.key.contact_name, editName)
        mBuilder.add(Utility.key.contact_mobile, editPhone)
        mBuilder.add(Utility.key.total_people, selectedPeople)
        mBuilder.add(Utility.key.resdate, selectedDate)
        mBuilder.add(Utility.key.restime, selectedTime)
        mBuilder.add(Utility.key.location_id, selectedLocationId)
        mBuilder.add(Utility.key.location, selectedLocation)
        mBuilder.add(Utility.key.preferred_table, selectedTable)
        mBuilder.add(Utility.key.extra_notes, editMessage)
        mBuilder.add(Utility.key.age_group,selectedAge)
        mBuilder.add(Utility.key.table_no,selectedTableNo)

        CallApi.callAPi(
            mBuilder,
            ApiController.api.reservations,
            this,
            Utility.reservations,
            true,
            Utility.POST,
            true
        )
    }

    fun printDatesInMonth(year: Int, month: Int, type: String) {
        var Scrolled_pos = 0
        var isToday = false
        val fmt = SimpleDateFormat("EEE-dd", Locale.ENGLISH)
        val f = SimpleDateFormat(Utility.api_date_format, Locale.ENGLISH)
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
            Log.d("aslasjflasfj :- ",data_array.toString())
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
        dateMonthAdapter =
            DateMonthViewAdapter(this, items, timings, recyclerTimeList, txtTime, times)
        recyclerDateList.layoutManager = layoutManager1
        recyclerDateList.adapter = dateMonthAdapter
        //recyclerDateList.scrollToPosition(Scrolled_pos)
    }


    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.storeDetail) {
            if (isData) {
                dataAria.showOrGone(true)

                val data = result.getJSONObject(Utility.key.data)
                Log.d("TAG", "onTaskComplete: $data")
                val id = data.getString(Utility.key.id)
                val store_name = data.getString(Utility.key.store_name)
                val store_image = data.getString(Utility.key.store_image)
                val cover_image = data.getString(Utility.key.cover_image)
                val is_open = data.getString(Utility.key.is_open)
                val starttime = data.getString(Utility.key.starttime)
                val endtime = data.getString(Utility.key.endtime)
                val starttime1 = data.getString(Utility.key.starttime1)
                val endtime1 = data.getString(Utility.key.endtime1)
                val email = data.getString(Utility.key.email)
                val name = data.getString(Utility.key.name)
                val mobile = data.getString(Utility.key.mobile)
                val telephone = data.getString(Utility.key.telephone)
                val fax = data.getString(Utility.key.fax)
                val extra_notes = data.getString(Utility.key.extra_notes)
                val default_location_id = data.getString(Utility.key.default_location_id)
                val reservation_status = data.getInt(Utility.key.reservation_status)

                Log.d("jajslkfjasjf","asfljasfljfsa :- "+starttime)
                Log.d("jajslkfjasjf","asfljasfljfsa :- "+starttime1)
                Log.d("jajslkfjasjf","asfljasfljfsa :- "+endtime)
                Log.d("jajslkfjasjf","asfljasfljfsa :- "+endtime1)

                if (reservation_status == 1) {
                    btnReserveClose.showOrGone(false)
                    btnReserveTable.showOrGone(true)
                } else {
                    btnReserveClose.showOrGone(true)
                    btnReserveTable.showOrGone(false)
                }

                //set favourite
                is_favorite = data.getString(Utility.key.is_favorite)
                if (is_favorite == "true") {
                    imgFavorites.setImageResource(R.drawable.ic_icon_fav_true)
                } else {
                    imgFavorites.setImageResource(R.drawable.ic_icon_fav_day_night)
                }
                imgFavorites.setOnClickListener {
                    if (Utility.isLoginCheck(this)) {
                        if (is_favorite == "true") {
                            HomeVisitorActivity.myObject(this, storeId, store_name, "true")
                            VisitorSearchActivity.setSearchFavView(storeId, store_name, "true")
                            removeFavApi(id)
                        } else {
                            HomeVisitorActivity.myObject(this, storeId, store_name, "false")
                            VisitorSearchActivity.setSearchFavView(storeId, store_name, "false")
                            addFavApi(id)
                        }
                    }
                }

                Utility.SetImageSimple(this, cover_image, imgCoverPhoto)
                Utility.SetImageSimple(this, store_image, profile_image)
                txtTitle.text = store_name
                contactNum.text = mobile
                if(checkStringNullOrEmpty(extra_notes) == ""){
                    txtExtraNote.showOrGone(false)
                    txtExtraNotetxt.showOrGone(false)
                }else{
                    txtExtraNote.showOrGone(true)
                    txtExtraNotetxt.showOrGone(true)
                    txtExtraNotetxt.text = extra_notes

                }

                /*val sdf1: DateFormat = SimpleDateFormat("HH:mm:ss")
                if (is_open == "1") {
                    val c = Calendar.getInstance()
                    txtStatus.text = "Open"
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_open))
                    if (cal.get(Calendar.HOUR_OF_DAY) < starttime.split(":")[0].toInt()){
                        txtStatus.text = "Close"
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                        txtDateTimeVal.text =
                            "Open at " + Utility.formatdatetime(starttime, "HH:mm:ss", Utility.time_format)
                    }else if (cal.get(Calendar.HOUR_OF_DAY) < endtime.split(":")[0].toInt()){
                        txtStatus.text = "Open"
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_open))
                        txtDateTimeVal.text =
                            "Close at " + Utility.formatdatetime(endtime, "HH:mm:ss", Utility.time_format)
                    }else if (cal.get(Calendar.HOUR_OF_DAY) < starttime1.split(":")[0].toInt()){
                        txtStatus.text = "Close"
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                        txtDateTimeVal.text =
                            "Open at " + Utility.formatdatetime(starttime1, "HH:mm:ss", Utility.time_format)
                    }else if (cal.get(Calendar.HOUR_OF_DAY) < endtime1.split(":")[0].toInt()){
                        txtStatus.text = "Open"
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_open))
                        txtDateTimeVal.text =
                            "Close at " + Utility.formatdatetime(endtime1, "HH:mm:ss", Utility.time_format)
                    }else{
                        txtStatus.text = "Close"
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                        txtDateTimeVal.text = "Closed"
                    }
                } else if (is_open == "0") {
                    txtStatus.text = "Close"
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                    *//*txtDateTimeVal.text =
                        "Open at " + Utility.formatdatetime(starttime, "HH:mm:ss", Utility.time_format)*//*
                    txtDateTimeVal.text = "Closed"

                } else {
                    txtStatus.text = "Close"
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                    txtDateTimeVal.text = "Closed"
                }*/


                ////////arrays//////////
                ////////////////media data/////////////////////
                val mediaArray = data.getJSONArray(Utility.key.media)
                if (mediaArray.length() <= 0) {
                    photoTitle.visibility = View.GONE
                    photoSeeAll.visibility = View.GONE
                    linSelectPhoto.visibility = View.GONE
                    recyclerPhoto.visibility = View.GONE
                }else{
                    photoTitle.visibility = View.VISIBLE
                    photoSeeAll.visibility = View.VISIBLE
                    linSelectPhoto.visibility = View.GONE
                    recyclerPhoto.visibility = View.VISIBLE
                }

                if (mediaArray.length() <= 0 && checkStringNullOrEmpty(extra_notes) == "") {
                    constPhoto.showOrGone(false)
                }else{
                    constPhoto.showOrGone(true)

                }
                val media: ArrayList<StoreMediaData> = ArrayList()
                for (i in 0 until mediaArray.length()) {
                    val dataObj = mediaArray.getJSONObject(i)
                    val item = StoreMediaData()
                    item.id = dataObj.getString(Utility.key.id)
                    item.media_url = dataObj.getString(Utility.key.media_url)
                    item.isUrl = false
                    item.imageBitmap = null
                    media.add(item)
                }
//                recyclerPhoto.setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                photoAdapter = PhotosViewAdapter(this, media, "view")
                recyclerPhoto.layoutManager = layoutManager
                recyclerPhoto.adapter = photoAdapter
                Utility.animationClick(photoSeeAll).setOnClickListener {
                    startActivity(
                        Intent(this, VisitorPhotoGalleryActivity::class.java)
                            .putExtra("private_list", Gson().toJson(media))
                            .putExtra(Utility.key.isFrom, "seeAll") //storeID
                            .putExtra(Utility.key.itemposition, "0") //storeID
                    )
                }

                /////////////////location data////////////////////////
                val locationsArray = data.getJSONArray(Utility.key.locations)
                locations.clear()
                var arrLoc = ArrayList<StoreLocationData>()
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
                        latLng = LatLng(
                            locationdataObj.getString(Utility.key.latitude).toDouble(),
                            locationdataObj.getString(Utility.key.longitude).toDouble()
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
//                recyclerAddress.setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                locateAdapter = AddressLocateViewAdapter(this, locations, mMap)
                recyclerAddress.layoutManager = layoutManager
                recyclerAddress.adapter = locateAdapter

                /////////////timing data///////////////////////////////
                var isWeekFirstOpen = ""
                var isOpened = false
                var isOpenAfterClose = false
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
                    val dayWeek = cal.get(Calendar.DAY_OF_WEEK)
                    if (isDayWeek(dayWeek) == timingObj.getString("day_of_week").toInt()) {
                        isOpened = (timingObj.getString("is_open").toInt() == 1)
                    }
                }


                if (isOpened && (is_open == "1")) {


                        val c = Calendar.getInstance()

                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        val currentDate = sdf.format(Date())
                        val sdf1: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH)

                        val strcurrentDate = sdf1.format(Date())
                        val currentDateFinal: Date = sdf1.parse("$strcurrentDate")!!


                        val starttimedateCheck: Date = sdf1.parse("$currentDate $starttime")!!
                        var endtimedate: Date = sdf1.parse("$currentDate $endtime")!!



                        Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+currentDateFinal)
                        Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+starttimedateCheck)
                        Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+endtimedate)


                        if (starttimedateCheck.after(endtimedate)) {
                            c.add(Calendar.DATE, 1);
                            val df = SimpleDateFormat("dd-MM-yyyy")
                            val formattedDate = sdf.format(c.time)
                            endtimedate = sdf1.parse("$formattedDate $endtime")!!
                        }


                    var isOnShift2 = false
                    var starttimedateCheck1 = Date()
                    var endtimedate1 = Date()

                    if(starttime1 != "null" && endtime1 != "null"){
                             isOnShift2 = true
                             starttimedateCheck1 = sdf1.parse("$currentDate $starttime1")!!
                             endtimedate1 = sdf1.parse("$currentDate $endtime1")!!

                        if (starttimedateCheck1.after(endtimedate1)) {
                            val c2 = Calendar.getInstance()
                            c2.add(Calendar.DATE, 1);
                            val df = SimpleDateFormat("dd-MM-yyyy")
                            val formattedDate = sdf.format(c2.time)
                            endtimedate1 = sdf1.parse("$formattedDate $endtime1")!!
                        }

                    }



                    if(currentDateFinal < starttimedateCheck){
                        txtStatus.text = getString(R.string.close)
                            txtStatus.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.status_red
                                )
                            )
                            txtDateTimeVal.text =
                                getString(R.string.open_at) + Utility.formatdatetime(
                                    starttime,
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )
                    }
                    else if ((currentDateFinal > starttimedateCheck) && (currentDateFinal < endtimedate)) {
                        txtStatus.text = getString(R.string.open)
                            txtStatus.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.status_open
                                )
                            )
                            txtDateTimeVal.text =
                                getString(R.string.close_at) + Utility.formatdatetime(
                                    endtime,
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )
                    }
                    else if (isOnShift2){
                        if ((currentDateFinal > endtimedate) && (currentDateFinal < starttimedateCheck1)) {
                            txtStatus.text = getString(R.string.close)
                            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                            txtDateTimeVal.text =
                                getString(R.string.open_at) + Utility.formatdatetime(
                                    starttime1,
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )
                        }
                        else if ((currentDateFinal > starttimedateCheck1) && (currentDateFinal < endtimedate1)) {
                            txtStatus.text = getString(R.string.open)
                            txtStatus.setTextColor(
                                ContextCompat.getColor(
                                    this,
                                    R.color.status_open
                                )
                            )
                            txtDateTimeVal.text =
                                getString(R.string.close_at) + Utility.formatdatetime(
                                    endtime1,
                                    Utility.time_format_hms,
                                    Utility.time_format
                                )

                        }
                        else if (currentDateFinal > endtimedate1) {
                            txtStatus.text = getString(R.string.close)
                            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                            txtDateTimeVal.text = getString(R.string.closed)
                        }
                    }
                    else {
                        txtStatus.text = getString(R.string.close)
                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                        txtDateTimeVal.text = getString(R.string.closed)

                    }

                }
                else {
                    txtStatus.text = getString(R.string.close)
                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
                    txtDateTimeVal.text = getString(R.string.closed)
                }



//                if (isOpened) { //check today is open or not | true->open false->closed
//                    if (is_open == "1") {
//
//                        val c = Calendar.getInstance()
//
//                        val sdf = SimpleDateFormat("yyyy-MM-dd")
//                        val currentDate = sdf.format(Date())
//                        val sdf1: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH)
//
//                        val strcurrentDate = sdf1.format(Date())
//                        val currentDateFinal: Date = sdf1.parse("$strcurrentDate")!!
//
//
//                        val starttimedateCheck: Date = sdf1.parse("$currentDate $starttime")!!
//                        var endtimedate: Date = sdf1.parse("$currentDate $endtime")!!
//
//
//                        val starttimedateCheck1: Date = sdf1.parse("$currentDate $starttime1")!!
//                        var endtimedate1: Date = sdf1.parse("$currentDate $endtime1")!!
//
//                        Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+currentDateFinal)
//                        Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+starttimedateCheck)
//                        Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+endtimedate)
//
//
//                        if (starttimedateCheck.after(endtimedate)) {
//                            c.add(Calendar.DATE, 1);
//                            //val sdf1: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH)
//
//                            val df = SimpleDateFormat("dd-MM-yyyy")
//                            val formattedDate = sdf.format(c.time)
//
//                            Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+"$formattedDate")
//
//
//                            val endTimeFormate: Date = sdf1.parse("$formattedDate $endtime")!!
//
//                            Log.d("aljsljasjlfas :- ","alsjljfalsjf :- "+"$endTimeFormate")
//
//
//                            //endtimedate = sdf1.parse("$currentDate $endtime")!!
//                        }
//
//
//                        txtStatus.text = "Open"
//                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_open))
//
//
//                        if(currentDateFinal < endtimedate){
//                            txtStatus.text = "Open"
//                            txtStatus.setTextColor(
//                                ContextCompat.getColor(
//                                    this,
//                                    R.color.status_open
//                                )
//                            )
//                            txtDateTimeVal.text =
//                                "Close at " + Utility.formatdatetime(
//                                    endtime,
//                                    Utility.time_format_hms,
//                                    Utility.time_format
//                                )
//                        }
//                        else if(endtime1 != "null" && currentDateFinal < endtimedate1){
//                            txtStatus.text = "Open"
//                            txtStatus.setTextColor(
//                                ContextCompat.getColor(
//                                    this,
//                                    R.color.status_open
//                                )
//                            )
//                            txtDateTimeVal.text =
//                                "Close at " + Utility.formatdatetime(
//                                    endtime1,
//                                    Utility.time_format_hms,
//                                    Utility.time_format
//                                )
//                        }
//                        else{
//                            txtStatus.text = "Close"
//                            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
//                            txtDateTimeVal.text = "Closed"
//                        }
//
//
//
////                        if (cal.get(Calendar.HOUR_OF_DAY) < endtime.split(":")[0].toInt()) {
////                            txtStatus.text = "Open"
////                            txtStatus.setTextColor(
////                                ContextCompat.getColor(
////                                    this,
////                                    R.color.status_open
////                                )
////                            )
////                            txtDateTimeVal.text =
////                                "Close at " + Utility.formatdatetime(
////                                    endtime,
////                                    Utility.time_format_hms,
////                                    Utility.time_format
////                                )
////                        }
////                        else if (endtime1 != "null" && cal.get(Calendar.HOUR_OF_DAY) < endtime1.split(
////                                ":"
////                            )[0].toInt()
////                        ) {
////                            txtStatus.text = "Open"
////                            txtStatus.setTextColor(
////                                ContextCompat.getColor(
////                                    this,
////                                    R.color.status_open
////                                )
////                            )
////                            txtDateTimeVal.text =
////                                "Close at " + Utility.formatdatetime(
////                                    endtime1,
////                                    Utility.time_format_hms,
////                                    Utility.time_format
////                                )
////                        } else {
////                            txtStatus.text = "Close"
////                            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
////                            txtDateTimeVal.text = "Closed"
////                        }
//                    }
//                    else if (is_open == "0") { //check currently is open or not | 1->open 0->closed
//                        txtStatus.text = "Close"
//                        txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
//                        if (cal.get(Calendar.HOUR_OF_DAY) < starttime.split(":")[0].toInt()) {
//                            txtStatus.text = "Close"
//                            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
//                            txtDateTimeVal.text =
//                                "Open at " + Utility.formatdatetime(
//                                    starttime,
//                                    Utility.time_format_hms,
//                                    Utility.time_format
//                                )
//                        } else if (starttime1 != "null" && cal.get(Calendar.HOUR_OF_DAY) < starttime1.split(
//                                ":"
//                            )[0].toInt()
//                        ) {
//                            txtStatus.text = "Close"
//                            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
//                            txtDateTimeVal.text =
//                                "Open at " + Utility.formatdatetime(
//                                    starttime1,
//                                    Utility.time_format_hms,
//                                    Utility.time_format
//                                )
//                        } else {
//                            txtStatus.text = "Close"
//                            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
//                            txtDateTimeVal.text = "Closed"
//                        }
//                    }
//                } else { //check today is open or not | true->open false->closed
//                    txtStatus.text = "Close"
//                    txtStatus.setTextColor(ContextCompat.getColor(this, R.color.status_red))
//                    txtDateTimeVal.text = "Closed"
//                }

                //////////////////events data////////////////////////
                val eventsArray = data.getJSONArray(Utility.key.events)
                if (eventsArray.length() <= 0) {
                    constUpcomingEvent.visibility = View.GONE
                }
                val events: ArrayList<UpcomingEventData> = ArrayList()
                for (u in 0 until eventsArray.length()) {
                    val upcoming_eventsObj = eventsArray.getJSONObject(u)
                    val upcomingEventData = UpcomingEventData()
                    upcomingEventData.id = upcoming_eventsObj.getString("id")
                    upcomingEventData.store_id = upcoming_eventsObj.getString("store_id")
                    upcomingEventData.location_id = upcoming_eventsObj.getString("location_id")
                    upcomingEventData.event_title = upcoming_eventsObj.getString("event_title")
                    upcomingEventData.description = upcoming_eventsObj.getString("description")
                    upcomingEventData.event_date = upcoming_eventsObj.getString("event_date")
                    upcomingEventData.starttime = upcoming_eventsObj.getString("starttime")
                    upcomingEventData.endtime = upcoming_eventsObj.getString("endtime")
                    upcomingEventData.event_image = upcoming_eventsObj.getString("event_image")
                    upcomingEventData.store_image = upcoming_eventsObj.getString("store_image")
                    upcomingEventData.location = upcoming_eventsObj.getJSONObject("location")
                    events.add(upcomingEventData)

                }
//                recyclerUpcomingEvent.setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                upcomingEventAdapter = UpcomingEventViewAdapter(this, events)
                recyclerUpcomingEvent.layoutManager = layoutManager
                recyclerUpcomingEvent.adapter = upcomingEventAdapter

                /////////////////tags data//////////////////////

                val tagsArray = data.getJSONArray(Utility.key.tags)
                val tags: ArrayList<TagData> = ArrayList()
                for (j in 0 until tagsArray.length()) {
                    val tagsObj = tagsArray.getJSONObject(j)
                    val tagData = TagData()
                    tagData.id = tagsObj.getString("id")
                    tagData.tag_name = tagsObj.getString("tag_name")
                    tags.add(tagData)
                    val tag = tagsObj.getString("tag_name")
                    if (j == 0) {
                        txtSubTitle.text = tag
                    } else {
                        txtSubTitle.text = txtSubTitle.text.toString() + " / " + tag
                    }
                    if (firstTagName.isEmpty()) {
                        firstTagName = tagsObj.getString("tag_name")
                    }
                    if (arrTags.isEmpty()) {
                        arrTags = tagsObj.getString("id")
                    } else {
                        arrTags = "$arrTags,${tagsObj.getString("id")}"
                    }
                }


                /////////////industries data//////////////////////
                val industriesArray = data.getJSONArray(Utility.key.industries)
                val industries: ArrayList<IndustriesData> = ArrayList()
                for (i in 0 until industriesArray.length()) {
                    val idIndus = industriesArray.getJSONObject(i).getString(Utility.key.id)
                    val indus =
                        industriesArray.getJSONObject(i).getString(Utility.key.industry_name)
                    if (firstIndusName.isEmpty()) {
                        firstIndusName = indus
                    }
                    if (arrIndus.isEmpty()) {
                        arrIndus = idIndus
                    } else {
                        arrIndus = "$arrIndus,${idIndus}"
                    }
                    val dataObj = IndustriesData()
                    dataObj.id = idIndus
                    dataObj.industry_name = indus
                    industries.add(dataObj)
                }
//                recyclerTags.setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                tagsAdapter = TagsViewAdapter(this, industries)
                recyclerTags.layoutManager = layoutManager
                recyclerTags.adapter = tagsAdapter


                recordsList.clear()
                val similar_storesArray = data.getJSONArray(Utility.key.similar_stores)
                if (similar_storesArray.length() <= 0) {
                    constAlsoLike.visibility = View.INVISIBLE
                    recyclerAlsoLike.visibility = View.GONE
                }
                for (r in 0 until similar_storesArray.length()) {
                    val storeModel = StoreModel()
                    val storeObj = similar_storesArray.getJSONObject(r)
                    storeModel.id = storeObj.getString("id")
                    storeModel.store_name = storeObj.getString("store_name")
                    storeModel.store_image = storeObj.getString("store_image")
                    storeModel.cover_image = storeObj.getString("cover_image")
                    storeModel.is_open = storeObj.getString("is_open")
                    storeModel.starttime = storeObj.getString("starttime")
                    storeModel.endtime = storeObj.getString("endtime")
                    storeModel.starttime1 = storeObj.getString("starttime1")
                    storeModel.endtime1 = storeObj.getString("endtime1")
                    storeModel.email = storeObj.getString("email")
                    storeModel.name = storeObj.getString("name")
                    storeModel.mobile = storeObj.getString("mobile")
                    storeModel.telephone = storeObj.getString("telephone")
                    storeModel.fax = storeObj.getString("fax")
                    storeModel.is_favorite = storeObj.getString("is_favorite")
                    val tagArray = storeObj.getJSONArray("tags")
                    val tagsList: ArrayList<TagData> = ArrayList()

                    for (j in 0 until tagArray.length()) {
                        val tagsObj = tagArray.getJSONObject(j)
                        val tagData = TagData()
                        tagData.id = tagsObj.getString("id")
                        tagData.tag_name = tagsObj.getString("tag_name")
                        tagsList.add(tagData)
                    }

                    storeModel.tags = tagsList

                    val industriesArraySimilar = storeObj.getJSONArray("industries")
                    val industriessList: ArrayList<IndustriesData> = ArrayList()
                    for (k in 0 until industriesArraySimilar.length()) {
                        val industriessObj = industriesArraySimilar.getJSONObject(k)
                        val industriesData = IndustriesData()
                        industriesData.id = industriessObj.getString("id")
                        industriesData.industry_name = industriessObj.getString("industry_name")
                        industriessList.add(industriesData)
                    }
                    storeModel.industries = industriessList

                    if (storeObj.getString("default_location") != "null") {
                        storeModel.default_location_id =
                            storeObj.getJSONObject("default_location").getString("id")

                        storeModel.default_location = storeObj.getJSONObject("default_location")
                    } else {
                        storeModel.default_location_id = "null"
                    }
                    recordsList.add(storeModel)
                }

//                recyclerAlsoLike.setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                alsoLikeAdapter = BeachViewAdapter(this, recordsList, resultLauncherFavorite)
                recyclerAlsoLike.layoutManager = layoutManager
                recyclerAlsoLike.adapter = alsoLikeAdapter

                //////////default location obj/////////

                jsonObj.addProperty("searchText", "")
                jsonObj.addProperty("indusId", arrIndus)
                jsonObj.addProperty("indusName", firstIndusName)
                jsonObj.addProperty("tagsId", arrTags)
                jsonObj.addProperty("tagsName", firstTagName)

            }
        } else if (type == Utility.favoritesRemoveDetail) {
            if (isData) {
                is_favorite = "false"
                isRemoveFav = "true"
                imgFavorites.setImageResource(R.drawable.ic_icon_fav_day_night)
                //Utility.customSuccessToast(this, result.getString(Utility.key.message))
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        } else if (type == Utility.favoritesAddDetail) {
            if (isData) {
                is_favorite = "true"
                isRemoveFav = "false"
                imgFavorites.setImageResource(R.drawable.ic_icon_fav_true)
                //Utility.customSuccessToast(this, result.getString(Utility.key.message))
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        } else if (type == Utility.favoritesRemove) {
            if (isData) {
                //Utility.customSuccessToast(this, result.getString(Utility.key.message))
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        } else if (type == Utility.favoritesAdd) {
            if (isData) {
                //Utility.customSuccessToast(this, result.getString(Utility.key.message))
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        } else if (type == Utility.reservations) {
            if (isData) {
                Utility.customSuccessToast(this, result.getString(Utility.key.message))
                startActivity(
                    Intent(this, ReserveTableStatusActivity::class.java)
                        .putExtra(Utility.key.isFrom, "booking")
                        .putExtra(Utility.key.data, result.getString(Utility.key.data))
                        .putExtra(
                            Utility.key.id,
                            result.getJSONObject(Utility.key.data).getString(Utility.key.id)
                        )
                )
                finish()
            } else {
                Utility.customErrorToast(this, result.getString(Utility.key.message))
            }
        }
    }

    private fun getIndoorOutdoor(indoor: Int, outdoor: Int): Int {
        if (indoor == 0 && outdoor == 0) {
            return 0
        } else if (indoor == 1 && outdoor == 1) {
            return 0
        } else if (indoor == 1 && outdoor == 0) {
            return 1
        } else if (indoor == 0 && outdoor == 1) {
            return 2
        } else {
            return 0
        }
    }

    private fun isDayWeek(dayWeek: Int): Int {
        when (dayWeek) {
            2 -> {
                return 1
            }

            3 -> {
                return 2
            }

            4 -> {
                return 3
            }

            5 -> {
                return 4
            }

            6 -> {
                return 5
            }

            7 -> {
                return 6
            }

            1 -> {
                return 7
            }
        }
        return 1
    }

    private fun addFavApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            "${ApiController.api.favorites}/$id",
            this,
            Utility.favoritesAddDetail,
            true,
            Utility.POST,
            true
        )

    }

    private fun removeFavApi(id: String) {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            "${ApiController.api.favorites}/$id",
            this,
            Utility.favoritesRemoveDetail,
            true,
            Utility.DELETE,
            true
        )

    }

    private var mMap: GoogleMap? = null
    override fun onMapReady(map: GoogleMap) {
        mMap = map
//        // Add some marker to the map, and add a data object to each marker.
//        val marker = MarkerOptions().position(LatLng(latD, longtD))
//        map.addMarker(marker)
        mMap!!.uiSettings.isZoomControlsEnabled = false
        mMap!!.uiSettings.isZoomGesturesEnabled = false
        mMap!!.uiSettings.isScrollGesturesEnabled = false
        try {
            if (mMap != null && latLng != null) {
                mMap!!.clear()
                val marker = MarkerOptions().position(latLng!!)
                mMap!!.addMarker(marker)
                val center = CameraUpdateFactory.newLatLng(latLng!!)
                val zoom =
                    CameraUpdateFactory.newLatLngZoom(latLng!!, 15f)
                mMap!!.animateCamera(center)
                mMap!!.animateCamera(zoom)
            }
            locateAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
