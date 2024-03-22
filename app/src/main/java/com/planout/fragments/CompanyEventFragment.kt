package com.planout.fragments

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.planout.R
import com.planout.activities.HomeCompanyActivity
import com.planout.activities.NotificationActivity
import com.planout.adapters.DateMonthViewAdapter2
import com.planout.adapters.ViewPagerAdapter
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.api_calling.CallFileApi
import com.planout.constant.CustomTimePicker
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.DayDateData
import com.planout.models.TimingData
import com.whiteelephant.monthpicker.MonthPickerDialog
import kotlinx.android.synthetic.main.fragment_company_event.view.*
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CompanyEventFragment(val activityBase: HomeCompanyActivity) : Fragment(), ApiResponse {

    private val GALLERYIMAGE = 124
    private var profile_image_file: File? = null
    var bitmapSelect: Bitmap? = null
    var imagePath = ""
    lateinit var imgAddErr : TextView
    var timeSelectFor = ""
    lateinit var rootView: View
    private var arrFragList : ArrayList<Fragment> = ArrayList()

    private val tabsArray = arrayOf(
        "Upcoming",
        "Past"
    )

    lateinit var handler: Handler
    var isClick = false
    lateinit var upcomingFrag: EventUpcomingFragment
    lateinit var pastFrag: EventPastFragment
    val cal: Calendar = Calendar.getInstance()
    val items: ArrayList<DayDateData> = ArrayList()
    val timings: ArrayList<TimingData> = ArrayList()
    lateinit var recyclerDateList: RecyclerView
    lateinit var dateMonthAdapter: DateMonthViewAdapter2
    lateinit var layoutManager1: LinearLayoutManager
    val loctionArr = ArrayList<String>()
    val loctionArrId = ArrayList<String>()
    lateinit var constImgChoose: ConstraintLayout
    lateinit var constImgView: ConstraintLayout
    lateinit var mediaImage: ImageView

    var selectedLocationId = ""
    var selectedLocation = ""

    // Edit Event
    var isEdit = false
    var eventId = ""
    var eventStoreId = ""
    var eventTitle = ""
    var selectedDateEdit = 0
    var eventMonthYear = ""
    var eventDateTime = ""
    var eventStartTime = ""
    var eventEndTime = ""
    var eventLocationId = ""
    var eventLocation = ""
    var eventImageUrl = ""
    var eventDescription = ""
    var isOpenAddEdit = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_company_event, container, false)

        handler = Handler()
        viewData()
        clickView()
        return rootView
    }

    private fun clickView() {
        if(Utility.getForm(activityBase, Utility.key.is_owner) == "1"){
            Utility.animationClick(rootView.addEvent).setOnClickListener {
                if (!isClick) {
                    locationListApi(false)
                }
                clickForFew()
            }
        }
        else{
            rootView.addEvent.visibility = View.GONE
        }
        rootView.imgNotify.setOnClickListener {
            startActivity(Intent(activityBase, NotificationActivity::class.java))
        }
    }

    fun locationListApi(b: Boolean) {
        isEdit = b
        val mBuilder = FormBody.Builder()
        val API = ApiController.api.store_locations + "?${Utility.key.store_id}=${Utility.getForm(activityBase,Utility.key.store_id)}"
        CallApi.callAPi(mBuilder, API, activityBase, Utility.store_locations, true, Utility.GET, true)
    }

    fun onBackPressed() {
        if (isOpenAddEdit){
            isOpenAddEdit = false
        }
    }

    private fun viewData() {
        upcomingFrag = EventUpcomingFragment(this,activityBase)
        pastFrag = EventPastFragment(this,activityBase)
        arrFragList.clear()
        arrFragList.add(upcomingFrag)
        arrFragList.add(pastFrag)
        setFragmentsView()
    }

    private fun setFragmentsView() {
        val tabsArray = arrayOf(
            getString(R.string.upcoming),
            getString(R.string.past),
        )
        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle, arrFragList)
        rootView.pager.adapter = adapter
        rootView.pager.isUserInputEnabled = false
        TabLayoutMediator(rootView.tab_layout, rootView.pager) { tab, position ->
            tab.text = tabsArray[position]
        }.attach()
        rootView.tab_layout.getTabAt(0)!!.select()
    }

    override fun onResume() {
        super.onResume()
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
        try {
            handler.removeCallbacksAndMessages(null)
        }catch (e:Exception){e.printStackTrace()}
    }

    fun clickForFew(){
        isClick = true
        handler.postDelayed(Runnable { isClick = false }, 1500)
    }

    fun openCreateEditEventDialog(isEdit: Boolean) {
        val serviceInfo_dialog = BottomSheetDialog(
            activityBase,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(activityBase)
            .inflate(R.layout.bottom_create_edit_event_view_popup, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)
        isOpenAddEdit = true
        val txtTitleDialog = dialogView.findViewById<TextView>(R.id.txtTitleDialog)
        val txtDate = dialogView.findViewById<TextView>(R.id.txtDate)
        val txtStartTime = dialogView.findViewById<TextView>(R.id.txtStartTime)
        val txtEndTime = dialogView.findViewById<TextView>(R.id.txtEndTime)
        val txtChar = dialogView.findViewById<TextView>(R.id.txtChar)
        val txtSelectDoc = dialogView.findViewById<TextView>(R.id.txtSelectDoc)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner)
        constImgChoose = dialogView.findViewById<ConstraintLayout>(R.id.constImgChoose)
        constImgView = dialogView.findViewById<ConstraintLayout>(R.id.constImgView)
        val editEventTitle = dialogView.findViewById<EditText>(R.id.editEventTitle)
        val editMessage = dialogView.findViewById<EditText>(R.id.editMessage)
        recyclerDateList = dialogView.findViewById<RecyclerView>(R.id.recyclerDateList)
        val btnEvent = dialogView.findViewById<Button>(R.id.btnEvent)
        mediaImage = dialogView.findViewById<ImageView>(R.id.mediaImage)
        val imgDelete = dialogView.findViewById<ImageView>(R.id.imgDelete)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        val nestedView = dialogView.findViewById<NestedScrollView>(R.id.nestedView)
        val eventTitleErr = dialogView.findViewById<TextView>(R.id.eventTitleErr)
        val dateErr = dialogView.findViewById<TextView>(R.id.dateErr)
        val timeErr = dialogView.findViewById<TextView>(R.id.timeErr)
        val locErr = dialogView.findViewById<TextView>(R.id.locErr)
        imgAddErr = dialogView.findViewById<TextView>(R.id.imgAddErr)
        val msgErr = dialogView.findViewById<TextView>(R.id.msgErr)
        val linMsg = dialogView.findViewById<LinearLayout>(R.id.linMsg)
        layoutManager1 = LinearLayoutManager(activityBase, LinearLayoutManager.HORIZONTAL, false)

        if (isEdit){
            txtTitleDialog.text = getString(R.string.edit_event)
            btnEvent.text = getString(R.string.update_event)
            editEventTitle.setText(eventTitle)
            txtStartTime.text = eventStartTime
            txtEndTime.text = eventEndTime
            editMessage.setText(eventDescription)
            txtChar.text = "${eventDescription.length}/300"
            imagePath = eventImageUrl
            profile_image_file = null
            bitmapSelect = null
            Utility.SetImageSimple(activityBase, eventImageUrl, mediaImage)
            constImgChoose.showOrGone(false)
            constImgView.showOrGone(true)
            activityBase.eventStartTime = eventStartTime
            activityBase.eventEndTime = eventEndTime
            selectedDateEdit = eventDateTime.split("-")[2].toInt()
            //printDatesInMonth(eventDateTime.split("-")[0].toInt(), eventDateTime.split("-")[1].toInt(), "manual")
            selectYearMonth(txtDate, eventDateTime.split("-")[1].toInt()-1, eventDateTime.split("-")[0].toInt(), txtStartTime, txtEndTime,isEdit)
        } else {
            when {
                cal.get(Calendar.MONTH) + 1 == 1 -> {
                    txtDate.text = "${getString(R.string.calendar_jan)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 2 -> {
                    txtDate.text = "${getString(R.string.calendar_Feb)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 3 -> {
                    txtDate.text = "${getString(R.string.calendar_Mar)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 4 -> {
                    txtDate.text = "${getString(R.string.calendar_Apr)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 5 -> {
                    txtDate.text = "${getString(R.string.calendar_May)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 6 -> {
                    txtDate.text = "${getString(R.string.calendar_Jun)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 7 -> {
                    txtDate.text = "${getString(R.string.calendar_Jul)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 8 -> {
                    txtDate.text = "${getString(R.string.calendar_Aug)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 9 -> {
                    txtDate.text = "${getString(R.string.calendar_Sep)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 10 -> {
                    txtDate.text = "${getString(R.string.calendar_Oct)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 11 -> {
                    txtDate.text = "${getString(R.string.calendar_Nov)}" + " " + cal.get(Calendar.YEAR)
                }
                cal.get(Calendar.MONTH) + 1 == 12 -> {
                    txtDate.text = "${getString(R.string.calendar_Dec)}" + " " + cal.get(Calendar.YEAR)
                }
            }
            printDatesInMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, "auto", txtStartTime, txtEndTime,isEdit)
        }

        txtSelectDoc.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (PermissionChecker.checkSelfPermission(
                        activityBase,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED!!
                ) {
                    requestPermissions(
                        arrayOf<String>(Manifest.permission.READ_MEDIA_IMAGES),
                        1
                    )
                } else {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, GALLERYIMAGE)
                }

            }else{
                if (PermissionChecker.checkSelfPermission(
                        activityBase,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED!!
                ) {
                    requestPermissions(
                        arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1
                    )
                } else if (PermissionChecker.checkSelfPermission(
                        activityBase,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED!!
                ) {
                    requestPermissions(
                        arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        2
                    )
                } else {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, GALLERYIMAGE)
                }
            }
        }

        imgDelete.setOnClickListener {
            constImgChoose.showOrGone(true)
            constImgView.showOrGone(false)
            profile_image_file = null
            bitmapSelect = null
            imagePath = ""
        }

        txtStartTime.setOnClickListener {
            if (dateMonthAdapter.selectedDate.isEmpty()){
                //Utility.normal_toast(activityBase, "Please select date at first")
                dateErr.showOrGone(true)
                dateErr.text = getString(R.string.date_validation)
            }else {
                dateErr.showOrGone(false)
                timeSelectFor = "start"
                if (dateMonthAdapter.selectedDate == Utility.current_date(Utility.api_date_format)) {
                    getTime(txtStartTime, txtStartTime, txtEndTime, true, false, true)
                } else {
                    getTime(txtStartTime, txtStartTime, txtEndTime, false, false, true)
                }
            }
        }

        txtEndTime.setOnClickListener {
            if (txtStartTime.text == "00:00"){
                timeErr.showOrGone(true)
                timeErr.text = "Select start time at first"
                //Utility.normal_toast(activityBase, "Select start time at first")
            }else{
                timeErr.showOrGone(false)
                timeSelectFor = "end"
                getTime(txtEndTime, txtStartTime, txtEndTime, false, false, false)
            }
        }


        val adapter = ArrayAdapter(activityBase, R.layout.simple_spinner_dropdown_item2,
            loctionArr
        )
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                locErr.showOrGone(false)
                selectedLocation = loctionArr[position]
                selectedLocationId = loctionArrId[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        if (isEdit) {
            for (i in 0 until loctionArrId.size){
                if (eventLocationId == loctionArrId[i]){
                    spinner.setSelection(i)
                    selectedLocation = loctionArr[i]
                    selectedLocationId = loctionArrId[i]
                }
            }
            for (i in 0 until items.size) {
                if (items[i].getDate() == eventDateTime.split("-")[2]){
                    items[i].setClicked(true)
                }else{
                    items[i].setClicked(false)
                }
                dateMonthAdapter.notifyDataSetChanged()
            }
        }

        editMessage.doOnTextChanged { text, start, before, count ->
            txtChar.text = "${text.toString().length}/300"
            Utility.showGoneErrorView(linMsg, msgErr, false, "")
        }

        editEventTitle.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editEventTitle, eventTitleErr, false, "")
        }

        Utility.animationClick(btnEvent).setOnClickListener {
            var selectedDate = ""
            var selectedTime = ""
            for (i in 0 until items.size) {
                if (items[i].getClicked()) {
                    dateErr.showOrGone(false)
                    selectedDate = items[i].getFull_Date()
                }
            }

            if (editEventTitle.text.toString()==""){
                //Utility.normal_toast(activityBase,"Enter your event title")
                editEventTitle.requestFocus()
                Utility.showGoneErrorView(editEventTitle, eventTitleErr, true, getString(R.string.event_title_validation))
                nestedView.fullScroll(ScrollView.FOCUS_UP)
            }else if (selectedDate==""){
                //Utility.normal_toast(activityBase,"Select your event date")
                dateErr.showOrGone(true)
                dateErr.text = getString(R.string.event_date_validation)
            }else if (txtStartTime.text.toString()=="00:00"){
                //Utility.normal_toast(activityBase,"Select your event start and end time")
                timeErr.showOrGone(true)
                timeErr.text = getString(R.string.event_start_end_time_validation)
            }else if (txtEndTime.text.toString()=="00:00"){
                //Utility.normal_toast(activityBase,"Select your event end time")
                timeErr.showOrGone(true)
                timeErr.text = getString(R.string.event_time_validation)
            }else if (selectedLocation==""){
                //Utility.normal_toast(activityBase,"Select your event location")
                locErr.showOrGone(true)
                locErr.text = getString(R.string.event_location_validation)
            }else if (profile_image_file==null && imagePath.isEmpty()){
                //Utility.normal_toast(activityBase,"Select your event image")
                imgAddErr.showOrGone(true)
                imgAddErr.text = getString(R.string.event_image_validation)
            }else if (editMessage.text.toString()==""){
                //Utility.normal_toast(activityBase,"Enter your event description")
                editMessage.requestFocus()
                Utility.showGoneErrorView(linMsg, msgErr, true, getString(R.string.enter_event_description))
            }else{
                isOpenAddEdit = false
                serviceInfo_dialog.dismiss()
                addEventApi(editEventTitle.text.toString(),
                    selectedDate,
                    txtStartTime.text.toString(),
                    txtEndTime.text.toString(),
                    selectedLocationId,
                    profile_image_file,
                    editMessage.text.toString()
                )
            }
        }

        Utility.animationClick(imgTop).setOnClickListener {
            isOpenAddEdit = false
            serviceInfo_dialog.dismiss()
        }

        txtDate.setOnClickListener {
            val builder = MonthPickerDialog.Builder(
                activityBase,
                { selectedMonth, selectedYear ->
                    Log.d("TAG", "selectedMonth : $selectedMonth selectedYear : $selectedYear")
                    selectYearMonth(txtDate, selectedMonth, selectedYear,txtStartTime, txtEndTime,isEdit)
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

    private fun selectYearMonth(
        txtDate: TextView,
        selectedMonth: Int,
        selectedYear: Int,
        txtStartTime: TextView,
        txtEndTime: TextView,
        isEditE: Boolean
    ) {
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
            printDatesInMonth(selectedYear, selectedMonth + 1, "auto", txtStartTime, txtEndTime,
                isEditE
            )
        } else {
            printDatesInMonth(selectedYear, selectedMonth + 1, "manual", txtStartTime, txtEndTime,
                isEditE
            )
        }
    }

    private fun addEventApi(editEventTitle: String,
                            selectedDate: String,
                            txtStartTime: String,
                            txtEndTime: String,
                            selectedLocationId: String,
                            profileImageFile: File?,
                            editMessage: String) {

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        requestBody.addFormDataPart(Utility.key.store_id,Utility.getForm(activityBase,Utility.key.store_id)!!)
        requestBody.addFormDataPart(Utility.key.location_id,selectedLocationId)
        requestBody.addFormDataPart(Utility.key.event_title,editEventTitle)
        requestBody.addFormDataPart(Utility.key.description,editMessage)
        requestBody.addFormDataPart(Utility.key.event_date,selectedDate)
        requestBody.addFormDataPart(Utility.key.starttime,Utility.formatdatetime(txtStartTime,Utility.time_format,Utility.api_time)!!)
        requestBody.addFormDataPart(Utility.key.endtime,Utility.formatdatetime(txtEndTime,Utility.time_format,Utility.api_time)!!)
        if (profile_image_file != null){
            requestBody.addFormDataPart(
                Utility.key.event_image,
                profileImageFile!!.name,
                profileImageFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }
        if (isEdit){
            CallFileApi.callAPi(
                requestBody,
                ApiController.api.events_update+"/$eventId",
                activityBase,
                Utility.eventsUpdate,
                true,
                Utility.POST,
                true
            )
        }else {
            CallFileApi.callAPi(
                requestBody,
                ApiController.api.events,
                activityBase,
                Utility.eventsAdd,
                true,
                Utility.POST,
                true
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            try {
                if (requestCode == GALLERYIMAGE) {
                    val selectedImageUri = data!!.data
                    imagePath = Utility.getPath(selectedImageUri, activityBase)!!
                    bitmapSelect =
                        MediaStore.Images.Media.getBitmap(activityBase.contentResolver, selectedImageUri)
                    try {
                        val exif = ExifInterface(imagePath)
                        val rotation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        val matrix = Matrix()
                        matrix.postRotate(exifToDegrees(rotation).toFloat())
                        bitmapSelect = Bitmap.createBitmap(
                            bitmapSelect!!,
                            0,
                            0,
                            bitmapSelect!!.width,
                            bitmapSelect!!.height,
                            matrix,
                            true
                        )
                    }catch (e: Exception){e.printStackTrace()}
                    profile_image_file = Utility.bitmapToFile(bitmapSelect!!, activityBase)
                    Utility.SetImageSimple(activityBase, imagePath, mediaImage)
                    constImgChoose.showOrGone(false)
                    constImgView.showOrGone(true)
                    //gone error msg view
                    imgAddErr.showOrGone(false)
                    imgAddErr.text = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Utility.customErrorToast(
                    activityBase,
                    getString(R.string.use_another_image)
                )
            }
        }
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }

    fun getTime(setView: TextView, textView: TextView, endView: TextView, isToday: Boolean, setMin: Boolean, setMax: Boolean){
        // setView -> in which set data
        // textView -> first time widget
        // endView -> second time widget

        val cal = Calendar.getInstance()
        val lis= TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            val timing = SimpleDateFormat(Utility.time_format,Locale.ENGLISH).format(cal.time).replace("am", "AM")
                .replace("pm", "PM")
            setView.text = timing.toString()
            if (timeSelectFor == "start"){
                eventStartTime = timing
                if (Utility.current_date(Utility.api_date_format) != dateMonthAdapter.selectedDate)
                activityBase.eventStartTime = timing
            }else if (timeSelectFor == "end"){
                eventEndTime = timing
                if (Utility.current_date(Utility.api_date_format) != dateMonthAdapter.selectedDate)
                activityBase.eventEndTime = timing
            }
            //Utility.errorEditTextWithAnim(ll_time, cardview_time, false, this)
        }
        val customTimePicker : CustomTimePicker
        if (setView.text.toString()=="00:00"){
            customTimePicker = CustomTimePicker(activityBase, lis, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)
            if (isToday){ //isToday->true |  set current time
                customTimePicker.setMin(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        }else{
            val time= timeCoversion12to24(setView.text.toString())
            val hour= time!!.split(":")[0].toInt()
            val min= time.split(":")[1].toInt()
            customTimePicker = CustomTimePicker(activityBase, lis, hour, min,false)
            if (isToday){ //isToday->true |  set current time
                customTimePicker.setMin(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
            }
        }

        if (!isToday) { //isToday->false |  set full day time
            if (setMin) { //setMin->true |  set minimum time,to get from first time
                val time = timeCoversion12to24(textView.text.toString())
                val hour = time!!.split(":")[0].toInt()
                val min = time.split(":")[1].toInt()
                customTimePicker.setMin(hour, min)
            }
        }

        if (setMax) { //setMax->true |  set maximum first time,to get from end time
            if (endView.text.toString() != "00:00") {
                val time = timeCoversion12to24(endView.text.toString())
                val hour = time!!.split(":")[0].toInt()
                val min = time.split(":")[1].toInt()
                customTimePicker.setMax(hour, min)
            }
        }
        customTimePicker.show()
    }

    @Throws(ParseException::class)
    fun timeCoversion12to24(twelveHoursTime: String?): String? {

        //Date/time pattern of input date (12 Hours format - hh used for 12 hours)
        val df: DateFormat = SimpleDateFormat(Utility.time_format,Locale.ENGLISH)

        //Date/time pattern of desired output date (24 Hours format HH - Used for 24 hours)
        val outputformat: DateFormat = SimpleDateFormat(Utility.api_time,Locale.ENGLISH)
        var date: Date? = null
        var output: String? = null

        //Returns Date object
        date = df.parse(twelveHoursTime!!)

        //old date format to new date format
        output = outputformat.format(date!!)
        println(output)
        return output
    }

    fun printDatesInMonth(
        year: Int,
        month: Int,
        type: String,
        txtStartTime: TextView,
        txtEndTime: TextView,
        isEditE: Boolean
    ) {

        var Scrolled_pos = 0
        var isToday = false

        val fmt = SimpleDateFormat("EEE-dd",Locale.ENGLISH)
        val f = SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH)
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
        dateMonthAdapter = DateMonthViewAdapter2(activityBase, items, timings, txtStartTime, txtEndTime,isEditE,eventStartTime,eventEndTime)
        recyclerDateList.layoutManager = layoutManager1
        recyclerDateList.adapter = dateMonthAdapter
        //recyclerDateList.scrollToPosition(Scrolled_pos)
    }


    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.eventsPast){
            pastFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.eventsUpcoming){
            upcomingFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.eventsDelete){
            upcomingFrag.onTaskComplete(result,type,isData)
        }else if (type==Utility.eventsAdd || type==Utility.eventsUpdate){
            if (isData){
                isOpenAddEdit = false
                //edit or create event response |  0->upcoming events  1->past events
                if (rootView.tab_layout.selectedTabPosition==0){
                    upcomingFrag.onTaskComplete(result,type,isData)
                }else{
                    pastFrag.onTaskComplete(result,type,isData)
                }
                Utility.customSuccessToast(activityBase,result.getString(Utility.key.message))

            }else{
                Utility.customErrorToast(activityBase,result.getString(Utility.key.message))

            }
        }else if (type==Utility.store_locations){
            if (isData){
                val dataArray=result.getJSONArray(Utility.key.data)
                if (dataArray.length()>0){
                    loctionArr.clear()
                    loctionArrId.clear()
                    for (i in 0 until dataArray.length()){
                        val dataObj=dataArray.getJSONObject(i)
                        loctionArr.add(dataObj.getString(Utility.key.address))
                        loctionArrId.add(dataObj.getString(Utility.key.id))
                    }
                    //dialog for create or edit events
                    openCreateEditEventDialog(isEdit)
                }else{
                    Utility.customErrorToast(activityBase,"No location is added, please add a location and try again")
                }
            }
        }
    }
}