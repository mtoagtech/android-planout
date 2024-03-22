package com.planout.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.planout.R
import com.planout.adapters.*
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.models.*
import com.planout.retrofit.DataInterface
import kotlinx.android.synthetic.main.activity_visitor_search.*
import kotlinx.android.synthetic.main.activity_visitor_search.editSearch
import kotlinx.android.synthetic.main.activity_visitor_search.imgBack
import kotlinx.android.synthetic.main.activity_visitor_search.linNoMore
import kotlinx.android.synthetic.main.activity_visitor_search.loadView
import okhttp3.FormBody
import org.json.JSONArray
import org.json.JSONObject


class VisitorSearchActivity : AppCompatActivity(), ApiResponse, DataInterface {

    val citiesItems:ArrayList<CityModel> = ArrayList()
    val industriesItems:ArrayList<IndustriesData> = ArrayList()
    val tagsItems:ArrayList<TagData> = ArrayList()
    private var pageCount = 1
    private var lstCurrent = 0
    private var isLoading = false
    private var lastSearch = ""
    private lateinit var handler: Handler
    val savedSearchItems: ArrayList<SavedSearchData> = ArrayList()
    var isComeOut = false
    var isRecent = false
    var recordListSearch: ArrayList<StoreModel> = ArrayList()
    val checkedcitiesArray:ArrayList<Int> = ArrayList()
    val checkedindustriesArray:ArrayList<Int> = ArrayList()
    val checkedtagsArray:ArrayList<Int> = ArrayList()

    lateinit var beachViewAdapter: SeeMoreViewAdapter
    lateinit var linearLayoutManager: LinearLayoutManager

    var resultLauncherFavorite =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val ID = data!!.getStringExtra("ID")!!
                val isRemoveFav=data.getStringExtra("isRemoveFav")
                //setView(isRemoveFav!!, ID)
            }
        }

    fun setView(isRemoveFav:String, ID:String){
        if (isRemoveFav=="true"){
            for (i in 0 until recordListSearch.size){
                if (recordListSearch[i].id==ID){
                    recordListSearch[i].is_favorite = "false"
                    beachViewAdapter.notifyItemChanged(i)
                    break
                }
            }
        }else if (isRemoveFav=="false"){
            for (i in 0 until recordListSearch.size){
                if (recordListSearch[i].id==ID){
                    recordListSearch[i].is_favorite = "true"
                    beachViewAdapter.notifyItemChanged(i)
                    break
                }
            }
        }
    }

    override fun addSetData(id: String, name: String, status: String) {
        setView(status, id)
    }

    companion object{
        var idList : ArrayList<String> = ArrayList()
        var idListStatus : ArrayList<String> = ArrayList()
        fun setSearchFavView(id:String, name:String, status:String){
            //VisitorSearchActivity().addSetData(id, name, status)
            idList.add(id)
            idListStatus.add(status)
        }
    }

    override fun onResume() {
        super.onResume()
        if (idList.size>0){
            Utility.show_progress(this)
        }
        for (i in 0 until idList.size){
            setView(idListStatus[i], idList[i])
            if (i== idList.size-1){
                idList.clear()
                idListStatus.clear()
                Utility.hide_progress(this)
            }
        }
    }

    var filterApiCall=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor_search)
        handler = Handler()
        //savedSearchApi()
        clickView()

        //searching to get listing from json | #saved search selection  #store detail show more
        if (intent.hasExtra("jsonVal")){
            constSaveBtn.showOrGone(false)
            isComeOut = true
            getForSearch(intent.getStringExtra("Title"), intent.getStringExtra("jsonVal"))
        }else{
            //open soft-input keyboard
            val inputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(
                constTop.applicationWindowToken, InputMethodManager.SHOW_FORCED, 0)
            editSearch.requestFocus()
        }
        //call api for recent search listing
        showRecentSearch()
    }

    private fun showRecentSearch() {
        try {
            val savedList: ArrayList<SavedSearchData>
            val type = object : TypeToken<List<SavedSearchData?>?>() {}.type
            val items = Utility.getForm(this, Utility.key.recentJson)
            savedList = Gson().fromJson(items, type)
            savedSearchItems.clear()
            savedSearchItems.addAll(savedList)
            Log.d("TAG", "onCreate: $savedList")
            chipGrpRecent.removeAllViews()
            //show chips for recent search listing
            showRecentChipsView()
        }catch (e:Exception){e.printStackTrace()}
    }

    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun getForSearch(title: String?, jsonVal: String?) {
        val jsonObj = JSONObject(jsonVal)
        if (jsonObj.has("searchText")){
            editSearch.setText(jsonObj.getString("searchText"))
        }
        if (jsonObj.has("cityId")) {
            if (jsonObj.getString("cityId") != "" && jsonObj.getString("cityId") != "[]") {
                val arrCity =
                    jsonObj.getString("cityId").replace("[", "").replace("]", "").split(",")
                if (arrCity.isNotEmpty()) {
                    for (element in arrCity) {
                        checkedcitiesArray.add(element.toInt())
                    }
                    val added = if (checkedcitiesArray.size-1 <= 0){
                        ""
                    }else{
                        if (jsonObj.getString("cityName").contains("+")) {
                            ""
                        }else{
                            " +"+(checkedcitiesArray.size-1)
                        }
                    }
                    selectedCityTxt.text = jsonObj.getString("cityName")+added
                    selectedCityTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                    selectedCityTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))
                    selectedCityTxt.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.app_green)
                }
            }
        }
        if (jsonObj.has("indusId")) {
            if (jsonObj.getString("indusId") != "" && jsonObj.getString("indusId") != "[]") {
                val arrIndus =
                    jsonObj.getString("indusId").replace("[", "").replace("]", "").split(",")
                if (arrIndus.isNotEmpty()) {
                    for (element in arrIndus) {
                        checkedindustriesArray.add(element.toInt())
                    }
                    val added = if (checkedindustriesArray.size-1 <= 0){
                        ""
                    }else{
                        if (jsonObj.getString("indusName").contains("+")) {
                            ""
                        }else{
                            " +"+(checkedindustriesArray.size-1)
                        }
                    }
                    selectedIndustryTxt.text = jsonObj.getString("indusName")+added
                    selectedIndustryTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                    selectedIndustryTxt.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.app_green
                        )
                    )
                    selectedIndustryTxt.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.app_green)
                }
            }
        }
        if (jsonObj.has("tagsId")) {
            if (jsonObj.getString("tagsId") != "" && jsonObj.getString("tagsId") != "[]") {
                val arrTags =
                    jsonObj.getString("tagsId").replace("[", "").replace("]", "").split(",")
                if (arrTags.isNotEmpty()) {
                    for (element in arrTags) {
                        checkedtagsArray.add(element.toInt())
                    }
                    val added = if (checkedtagsArray.size-1 <= 0){
                        ""
                    }else{
                        if (jsonObj.getString("tagsName").contains("+")) {
                            ""
                        }else{
                            " +" + (checkedtagsArray.size - 1)
                        }
                    }
                    selectedTagsTxt.text = jsonObj.getString("tagsName")+added
                    selectedTagsTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                    selectedTagsTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))
                    selectedTagsTxt.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.app_green)
                }
            }
        }
        searchStoreApi(editSearch.text.toString(),checkedcitiesArray,checkedindustriesArray,checkedtagsArray)
    }

    private fun savedSearchApi() {
        linRecentSearch.showOrGone(false)

        val mBuilder = FormBody.Builder()
        CallApi.callAPi(
            mBuilder,
            ApiController.api.searches,
            this,
            Utility.searches,
            true,
            Utility.GET,
            true
        )

    }

    private fun clickView() {
        editSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utility.hideSoftKeyboard(this)
                val text=editSearch.text.toString()
                if (text.isEmpty()){
                    Utility.customErrorToast(this, getString(R.string.search_about_restaurant_club_bar))
                }else {
                    pageCount = 1
                    searchStoreApi(
                        text,
                        checkedcitiesArray,
                        checkedindustriesArray,
                        checkedtagsArray
                    )
                }
                true
            } else false
        }
        editSearch.doOnTextChanged { text, start, before, count ->
            if (text!!.length>=3) {
                imgCross.showOrGone(true)
            } else {
                imgCross.showOrGone(false)
            }
            constSaveBtn.showOrGone(false)
            /*if (lastSearch == text.toString() && !isSavedSearch(text.toString())){
                constSaveBtn.showOrGone(true)
            }else if (lastSearch == text.toString() && isSavedSearch(text.toString())){
                constSaveBtn.showOrGone(false)
            }else{
                constSaveBtn.showOrGone(false)
            }*/
        }

        imgCross.setOnClickListener {
            editSearch.setText("")
            linRecentSearch.visibility = View.VISIBLE
            linSaveListing.visibility = View.GONE

            selectedCityTxt.text="City"
            selectedCityTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
            selectedCityTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

            for (drawable in selectedCityTxt.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(selectedCityTxt.context, R.color.black_white),
                            PorterDuff.Mode.SRC_IN
                        )
                }
            }
            selectedIndustryTxt.text="Category"
            selectedIndustryTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
            selectedIndustryTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

            for (drawable in selectedIndustryTxt.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(selectedIndustryTxt.context, R.color.black_white),
                            PorterDuff.Mode.SRC_IN
                        )
                }
            }
            selectedTagsTxt.text="Tags"
            selectedTagsTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
            selectedTagsTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

            for (drawable in selectedTagsTxt.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(selectedTagsTxt.context, R.color.black_white),
                            PorterDuff.Mode.SRC_IN
                        )
                }
            }
            checkedcitiesArray.clear()
            checkedindustriesArray.clear()
            checkedtagsArray.clear()
            //open soft-input keyboard
            val inputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(
                constTop.applicationWindowToken, InputMethodManager.SHOW_FORCED, 0)
            editSearch.requestFocus()
        }

        linSaveSearch.setOnClickListener {
            if (Utility.isLoginCheck(this)) {
                saveSearchApi(editSearch.text.toString())
            }
            /*handler.postDelayed(Runnable {
                linSaveSearch.visibility = View.VISIBLE
                linSavedSearch.visibility = View.GONE
            }, 5000)*/ // saved search label remove after 5 sec
        }

        txtViewSaved.setOnClickListener {
            startActivity(Intent(this, SavedSearchActivity::class.java))
            //linSaveSearch.visibility = View.VISIBLE
            //linSavedSearch.visibility = View.GONE
        }

        imgFilter.setOnClickListener {
            if (!filterApiCall){
                //call api for filter searching
                searchFilterApi()
            }else{
                //dialog for filter searching
                openFilterDialog("all")
            }
        }

        selectedCityTxt.setOnClickListener {
            if (!filterApiCall){
                //call api for filter searching
                searchFilterApi()
            }else{
                //dialog for filter searching
                openFilterDialog(Utility.key.cities)
            }
        }

        selectedIndustryTxt.setOnClickListener {
            if (!filterApiCall){
                //call api for filter searching
                searchFilterApi()
            }else{
                //dialog for filter searching
                openFilterDialog(Utility.key.industries)
            }
        }
        selectedTagsTxt.setOnClickListener {
            if (!filterApiCall){
                //call api for filter searching
                searchFilterApi()
            }else{
                //dialog for filter searching
                openFilterDialog(Utility.key.tags)
            }
        }

        imgBack.setOnClickListener {
            onBackPressed()
        }

        recyclerViewSearch.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            val total = recyclerViewSearch.layoutManager!!.itemCount
            val currentLastItem: Int = linearLayoutManager.findLastVisibleItemPosition()
            if (currentLastItem == total - 1 && isLoading && lstCurrent<=currentLastItem) {
                lstCurrent = currentLastItem
                loadView.showOrGone(true)
                pageCount += 1
                val text=editSearch.text.toString()
                //call api for searching store listing
                searchStoreApi(
                    text,
                    checkedcitiesArray,
                    checkedindustriesArray,
                    checkedtagsArray
                )
            }
        }

    }

    private fun isSavedSearch(searchTxt: String): Boolean {
        for (i in 0 until savedSearchItems.size){
            if (searchTxt == savedSearchItems[i].search_title){
                return true
            }
        }
        return false
    }

    override fun onStop() {
        super.onStop()
        if (handler!=null) {
            handler.removeCallbacksAndMessages(null)
        }
    }

    private fun searchFilterApi() {
        val mBuilder = FormBody.Builder()
        CallApi.callAPi(mBuilder, ApiController.api.searchfilters, this, Utility.searchfilters, true, Utility.GET, true)
    }

    private fun searchStoreApi(
        text: String,
        checkedcitiesArray: ArrayList<Int>,
        checkedindustriesArray: ArrayList<Int>,
        checkedtagsArray: ArrayList<Int>
    ) {
        /*val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.page, pageCount.toString())
        mBuilder.add(Utility.key.industries,Gson().toJson(checkedindustriesArray))
        mBuilder.add(Utility.key.tags,Gson().toJson(checkedtagsArray))
        mBuilder.add(Utility.key.cities,Gson().toJson(checkedcitiesArray))
        mBuilder.add(Utility.key.searchkey,text)*/

        val json = JSONObject()
        json.put(Utility.key.page,pageCount)
        json.put(Utility.key.industries, JSONArray(checkedindustriesArray))
        json.put(Utility.key.tags, JSONArray(checkedtagsArray))
        json.put(Utility.key.cities, JSONArray(checkedcitiesArray))
        json.put(Utility.key.searchkey,text)
        CallApi.callAPiJson(json, ApiController.api.stores, this, Utility.stores, pageCount==1, Utility.POST, true)
        //constSaveBtn.showOrGone(editSearch.text.toString().isNotEmpty())
        lastSearch = editSearch.text.toString()
    }

    private fun saveSearchApi(toString: String) {
        //Log.d("TAG", "saveSearchApi: ${getFilterJson()}")
        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.search_title, getSearchTitle(toString))
        mBuilder.add(Utility.key.search_data, getFilterJson())
        CallApi.callAPi(
            mBuilder,
            ApiController.api.searches,
            this,
            Utility.createSearches,
            true,
            Utility.POST,
            true
        )
    }

    private fun getSearchTitle(toString: String): String {
        var strVal = ""
        if (toString.isEmpty()){
            for (i in 0 until citiesItems.size){
                if (citiesItems[i].isChecked){
                    strVal = citiesItems[i].cityName
                }
            }
            for (i in 0 until industriesItems.size){
                if (industriesItems[i].isChecked){
                    if (strVal.isEmpty())
                    strVal = industriesItems[i].industry_name
                }
            }
            for (i in 0 until tagsItems.size){
                if (tagsItems[i].isChecked){
                    if (strVal.isEmpty())
                    strVal = tagsItems[i].tag_name
                }
            }
        }else{
            strVal = toString
        }
        return strVal
    }

    private fun getFilterJson(): String {
        val jsonStr = JsonObject()
        var cityArr: String = ""
        var indusArr: String = ""
        var tagsArr: String = ""
        for (i in 0 until checkedcitiesArray.size){
            if (cityArr.isEmpty()){
                cityArr = checkedcitiesArray[i].toString()
            }else{
                cityArr = "$cityArr,${checkedcitiesArray[i]}"
            }
        }
        for (i in 0 until checkedindustriesArray.size){
            if (indusArr.isEmpty()){
                indusArr = checkedindustriesArray[i].toString()
            }else{
                indusArr = "$indusArr,${checkedindustriesArray[i]}"
            }
        }
        for (i in 0 until checkedtagsArray.size){
            if (tagsArr.isEmpty()){
                tagsArr = checkedtagsArray[i].toString()
            }else{
                tagsArr = "$tagsArr,${checkedtagsArray[i]}"
            }
        }
        jsonStr.addProperty("searchText", editSearch.text.toString())
        jsonStr.addProperty("cityId", cityArr)
        jsonStr.addProperty("cityName", selectedCityTxt.text.toString().split(" +")[0])
        jsonStr.addProperty("indusId", indusArr)
        jsonStr.addProperty("indusName", selectedIndustryTxt.text.toString().split(" +")[0])
        jsonStr.addProperty("tagsId", tagsArr)
        jsonStr.addProperty("tagsName", selectedTagsTxt.text.toString().split(" +")[0])
        return jsonStr.toString()
    }

    private fun addChipToGroup(str: String, chipGrp: ChipGroup) {
        val chip = Chip(this)

        chip.text = str
        chip.setTextAppearance(R.style.chipText)
        chip.chipBackgroundColor = ColorStateList.valueOf(getColor(R.color.gray_F8_48))
        chip.chipStrokeColor = ColorStateList.valueOf(getColor(R.color.transparent))
        chip.chipStrokeWidth = 2f
        chip.chipCornerRadius = 40f
        chip.setPadding(40, 30, 40, 30)
        chipGrp.chipSpacingVertical = 2
        chipGrp.chipSpacingHorizontal = 10
        chipGrp.addView(chip as View)
        chip.setOnClickListener {
            isRecent = true
            val txtName = chip.text.toString()
            editSearch.setText(txtName)
            getSetFilterJson(txtName)
        }
    }

    private fun getSetFilterJson(txtName: String) {
        if (txtName.isNotEmpty()){
            for (i in 0 until savedSearchItems.size){
                if (txtName == savedSearchItems[i].search_title){
                    if (savedSearchItems[i].search_data.contains("{")) {
                        checkedcitiesArray.clear()
                        checkedindustriesArray.clear()
                        checkedtagsArray.clear()
                        val jsonObj = JSONObject(savedSearchItems[i].search_data)
                        if (jsonObj.getString("cityId")!="" && jsonObj.getString("cityId")!="[]"){
                            val arrCity = jsonObj.getString("cityId").replace("[","").replace("]","").split(",")
                            for (j in 0 until arrCity.size){
                                checkedcitiesArray.add(arrCity[j].toInt())
                            }
                            val added = if (checkedcitiesArray.size-1 <= 0){
                                ""
                            }else{
                                if (jsonObj.getString("cityName").contains("+")) {
                                    ""
                                }else{
                                    " +" + (checkedcitiesArray.size - 1)
                                }
                            }
                            selectedCityTxt.text = jsonObj.getString("cityName")+added
                            selectedCityTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                            selectedCityTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))
                            for (drawable in selectedCityTxt.compoundDrawables) {
                                if (drawable != null) {
                                    drawable.colorFilter =
                                        PorterDuffColorFilter(
                                            ContextCompat.getColor(selectedCityTxt.context, R.color.app_green),
                                            PorterDuff.Mode.SRC_IN
                                        )
                                }
                            }
                        }
                        if (jsonObj.getString("indusId")!="" && jsonObj.getString("indusId")!="[]"){
                            val arrIndus = jsonObj.getString("indusId").replace("[","").replace("]","").split(",")
                            for (k in 0 until arrIndus.size){
                                checkedindustriesArray.add(arrIndus[k].toInt())
                            }
                            val added = if (checkedindustriesArray.size-1 <= 0){
                                ""
                            }else{
                                if (jsonObj.getString("indusName").contains("+")) {
                                    ""
                                }else{
                                    " +" + (checkedindustriesArray.size - 1)
                                }
                            }
                            selectedIndustryTxt.text = jsonObj.getString("indusName")+added
                            selectedIndustryTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                            selectedIndustryTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))

                            for (drawable in selectedIndustryTxt.compoundDrawables) {
                                if (drawable != null) {
                                    drawable.colorFilter =
                                        PorterDuffColorFilter(
                                            ContextCompat.getColor(selectedIndustryTxt.context, R.color.app_green),
                                            PorterDuff.Mode.SRC_IN
                                        )
                                }
                            }
                        }

                        if (jsonObj.getString("tagsId")!="" && jsonObj.getString("tagsId")!="[]"){
                            val arrTags = jsonObj.getString("tagsId").replace("[","").replace("]","").split(",")
                            for (l in 0 until arrTags.size){
                                checkedtagsArray.add(arrTags[l].toInt())
                            }
                            val added = if (checkedtagsArray.size-1 <= 0){
                                ""
                            }else{
                                if (jsonObj.getString("tagsName").contains("+")) {
                                    ""
                                }else{
                                    " +" + (checkedtagsArray.size - 1)
                                }
                            }
                            selectedTagsTxt.text = jsonObj.getString("tagsName")+added
                            selectedTagsTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                            selectedTagsTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))

                            for (drawable in selectedTagsTxt.compoundDrawables) {
                                if (drawable != null) {
                                    drawable.colorFilter =
                                        PorterDuffColorFilter(
                                            ContextCompat.getColor(selectedTagsTxt.context, R.color.app_green),
                                            PorterDuff.Mode.SRC_IN
                                        )
                                }
                            }
                        }
                    }
                }
            }
            searchStoreApi(txtName, checkedcitiesArray, checkedindustriesArray, checkedtagsArray)
        }
    }

    private fun openFilterDialog(type: String) {
        val serviceInfo_dialog = BottomSheetDialog(
            this,
            R.style.AppBottomSheetDialogTheme
        )
        serviceInfo_dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.bottom_filter_view_popup, null)
        serviceInfo_dialog.setContentView(dialogView)
        serviceInfo_dialog.setCancelable(true)

        val btnSearch = dialogView.findViewById<Button>(R.id.btnSearch)
        val txtClear = dialogView.findViewById<TextView>(R.id.txtClear)
        val imgTop = dialogView.findViewById<ImageView>(R.id.imgTop)
        val recyclerCityView = dialogView.findViewById<RecyclerView>(R.id.recyclerCityView)
        val recyclerIndustryView = dialogView.findViewById<RecyclerView>(R.id.recyclerIndustryView)
        val recyclerTagsView = dialogView.findViewById<RecyclerView>(R.id.recyclerTagsView)
        val cityTitle = dialogView.findViewById<TextView>(R.id.cityTitle)
        val industriesTitle = dialogView.findViewById<TextView>(R.id.industriesTitle)
        val tagsTitle = dialogView.findViewById<TextView>(R.id.tagsTitle)

        if (type==Utility.key.cities){
            cityTitle.showOrGone(true)
            recyclerCityView.showOrGone(true)
            industriesTitle.showOrGone(false)
            recyclerIndustryView.showOrGone(false)
            tagsTitle.showOrGone(false)
            recyclerTagsView.showOrGone(false)
        }else if (type==Utility.key.industries){
            cityTitle.showOrGone(false)
            recyclerCityView.showOrGone(false)
            industriesTitle.showOrGone(true)
            recyclerIndustryView.showOrGone(true)
            tagsTitle.showOrGone(false)
            recyclerTagsView.showOrGone(false)
        }else if (type==Utility.key.tags){
            cityTitle.showOrGone(false)
            recyclerCityView.showOrGone(false)
            industriesTitle.showOrGone(false)
            recyclerIndustryView.showOrGone(false)
            tagsTitle.showOrGone(true)
            recyclerTagsView.showOrGone(true)
        }else {
            cityTitle.showOrGone(true)
            recyclerCityView.showOrGone(true)
            industriesTitle.showOrGone(true)
            recyclerIndustryView.showOrGone(true)
            tagsTitle.showOrGone(true)
            recyclerTagsView.showOrGone(true)
        }

        val cityfilterViewAdapter = CityFilterViewAdapter(this@VisitorSearchActivity,citiesItems)
        val industryfilterViewAdapter = IndustriesFilterViewAdapter(this@VisitorSearchActivity,industriesItems)
        val tagsfilterViewAdapter = TagsFilterViewAdapter(this@VisitorSearchActivity,tagsItems)

        val gridLayoutManager = GridLayoutManager(this@VisitorSearchActivity, 2)
        val gridLayoutManager2 = GridLayoutManager(this@VisitorSearchActivity, 2)
        val gridLayoutManager3 = GridLayoutManager(this@VisitorSearchActivity, 2)
        // for city
//        recyclerCityView.setHasFixedSize(true)
        recyclerCityView.layoutManager = gridLayoutManager
        recyclerCityView.adapter = cityfilterViewAdapter
        // for industry
//        recyclerIndustryView.setHasFixedSize(true)
        recyclerIndustryView.layoutManager = gridLayoutManager2
        recyclerIndustryView.adapter = industryfilterViewAdapter
        // for tags
//        recyclerTagsView.setHasFixedSize(true)
        recyclerTagsView.layoutManager = gridLayoutManager3
        recyclerTagsView.adapter = tagsfilterViewAdapter

        Utility.animationClick(btnSearch).setOnClickListener {
            checkedcitiesArray.clear()
            checkedindustriesArray.clear()
            checkedtagsArray.clear()
            var selectedCity=""
            var selectedIndustry=""
            var selectedTag=""
            for (i in 0 until citiesItems.size){
                if (citiesItems[i].isChecked){
                    if (selectedCity.isEmpty()) {
                        selectedCity = citiesItems[i].cityName
                    }
                    checkedcitiesArray.add(citiesItems[i].cityID.toInt())
                }
            }

            for (i in 0 until industriesItems.size){
                if (industriesItems[i].isChecked){
                    if (selectedIndustry.isEmpty()) {
                        selectedIndustry = industriesItems[i].industry_name
                    }
                    checkedindustriesArray.add(industriesItems[i].id.toInt())
                }
            }

            for (i in 0 until tagsItems.size){
                if (tagsItems[i].isChecked){
                    if (selectedTag.isEmpty()) {
                        selectedTag = tagsItems[i].tag_name
                    }
                    checkedtagsArray.add(tagsItems[i].id.toInt())
                }
            }

            if (checkedcitiesArray.size==1){
                selectedCityTxt.text=selectedCity
                selectedCityTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                selectedCityTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))
                for (drawable in selectedCityTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedCityTxt.context, R.color.app_green),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }

            }else if (checkedcitiesArray.size>1){
                selectedCityTxt.text=selectedCity+" +"+(checkedcitiesArray.size-1).toString()
                selectedCityTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                selectedCityTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))

                for (drawable in selectedCityTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedCityTxt.context, R.color.app_green),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }
            }else{
                selectedCityTxt.text="City"
                selectedCityTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
                selectedCityTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

                for (drawable in selectedCityTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedCityTxt.context, R.color.black_white),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }


            }

            if (checkedindustriesArray.size==1){
                selectedIndustryTxt.text=selectedIndustry
                selectedIndustryTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                selectedIndustryTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))

                for (drawable in selectedIndustryTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedIndustryTxt.context, R.color.app_green),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }


            }else if (checkedindustriesArray.size>1){
                selectedIndustryTxt.text=selectedIndustry+" +"+(checkedindustriesArray.size-1).toString()
                selectedIndustryTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                selectedIndustryTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))

                for (drawable in selectedIndustryTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedIndustryTxt.context, R.color.app_green),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }


            }else{
                selectedIndustryTxt.text="Category"
                selectedIndustryTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
                selectedIndustryTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

                for (drawable in selectedIndustryTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedIndustryTxt.context, R.color.black_white),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }


            }

            if (checkedtagsArray.size==1){
                selectedTagsTxt.text=selectedTag
                selectedTagsTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                selectedTagsTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))

                for (drawable in selectedTagsTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedTagsTxt.context, R.color.app_green),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }


            }else if (checkedtagsArray.size>1){
                selectedTagsTxt.text=selectedTag+" +"+(checkedtagsArray.size-1).toString()
                selectedTagsTxt.setBackgroundResource(R.drawable.bg_round_edit_green_drawable)
                selectedTagsTxt.setTextColor(ContextCompat.getColor(this, R.color.app_green))

                for (drawable in selectedTagsTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedTagsTxt.context, R.color.app_green),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }


            }else{
                selectedTagsTxt.text="Tags"
                selectedTagsTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
                selectedTagsTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

                for (drawable in selectedTagsTxt.compoundDrawables) {
                    if (drawable != null) {
                        drawable.colorFilter =
                            PorterDuffColorFilter(
                                ContextCompat.getColor(selectedTagsTxt.context, R.color.black_white),
                                PorterDuff.Mode.SRC_IN
                            )
                    }
                }

            }
            //editSearch.setText("")
            searchStoreApi(editSearch.text.toString(),checkedcitiesArray,checkedindustriesArray,checkedtagsArray)

            serviceInfo_dialog.dismiss()
        }

        Utility.animationClick(txtClear).setOnClickListener {
            checkedcitiesArray.clear()
            checkedindustriesArray.clear()
            checkedtagsArray.clear()

            for (i in 0 until citiesItems.size){
                citiesItems[i].isChecked=false
            }
            for (i in 0 until industriesItems.size){
                industriesItems[i].isChecked=false
            }
            for (i in 0 until tagsItems.size){
                tagsItems[i].isChecked=false
            }

            for (drawable in selectedTagsTxt.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(selectedTagsTxt.context, R.color.black_white),
                            PorterDuff.Mode.SRC_IN
                        )
                }
            }

            for (drawable in selectedIndustryTxt.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(selectedIndustryTxt.context, R.color.black_white),
                            PorterDuff.Mode.SRC_IN
                        )
                }
            }

            for (drawable in selectedCityTxt.compoundDrawables) {
                if (drawable != null) {
                    drawable.colorFilter =
                        PorterDuffColorFilter(
                            ContextCompat.getColor(selectedCityTxt.context, R.color.black_white),
                            PorterDuff.Mode.SRC_IN
                        )
                }
            }

            selectedTagsTxt.text="Tags"
            selectedTagsTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
            selectedTagsTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

            selectedIndustryTxt.text="Category"
            selectedIndustryTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
            selectedIndustryTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))

            selectedCityTxt.text="City"
            selectedCityTxt.setBackgroundResource(R.drawable.bg_round_edit_unselect_drawable)
            selectedCityTxt.setTextColor(ContextCompat.getColor(this, R.color.black_white))


            serviceInfo_dialog.dismiss()
        }

        Utility.animationClick(imgTop).setOnClickListener {
            serviceInfo_dialog.dismiss()
        }

        serviceInfo_dialog.show()

    }

    private fun showRecentChipsView(){
        try {
            for (i in 0 until savedSearchItems.size){
                addChipToGroup(savedSearchItems[i].search_title, chipGrpRecent)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type == Utility.searches) {
            if (isData) {
                savedSearchItems.clear()
                val data = result.getJSONArray(Utility.key.data)
                if (data.length() != 0) {
                    linRecentSearch.showOrGone(true)
                    for (i in 0 until data.length()) {
                        val dataObj = data.getJSONObject(i)
                        addChipToGroup(dataObj.getString("search_title"), chipGrpRecent)
                        val model = SavedSearchData()
                        model.id = dataObj.getString("id")
                        model.user_id = dataObj.getString("user_id")
                        model.search_title = dataObj.getString("search_title")
                        model.search_data = dataObj.getString("search_data")
                        model.created_at = dataObj.getString("created_at")
                        model.updated_at = dataObj.getString("updated_at")
                        savedSearchItems.add(model)
                    }
                }else{
                    linRecentSearch.showOrGone(false)
                }
            }
        }else if (type==Utility.createSearches){
            if (isData){
                Utility.customSuccessToast(this,result.getString(Utility.key.message))
                linSaveSearch.visibility = View.GONE
                linSavedSearch.visibility = View.VISIBLE
            }else{
                Utility.customErrorToast(this,result.getString(Utility.key.message))
            }

        }else if (type==Utility.stores){
            if (isData){
                val dataObj=result.getJSONObject(Utility.key.data)
                val records=dataObj.getJSONArray("records")
                pageCount = dataObj.getInt("current_page")
                isLoading = records.length()==20
                val currPage= Utility.checkStringNullOrNot(dataObj, Utility.key.current_page)
                if(currPage.toInt() == 1){
                    recordListSearch.clear()
                }
                if(currPage.toInt() == 1 && records.length()>0 && !isRecent){
                    setRecentSearch(editSearch.text.toString(), getFilterJson())
                }
                isRecent = false
                for (r in 0 until records.length()){
                    val storeModel= StoreModel()
                    val storeObj=records.getJSONObject(r)
                    storeModel.id=storeObj.getString("id")
                    storeModel.store_name=storeObj.getString("store_name")
                    storeModel.store_image=storeObj.getString("store_image")
                    storeModel.cover_image=storeObj.getString("cover_image")
                    storeModel.is_open=storeObj.getString("is_open")
                    storeModel.starttime=storeObj.getString("starttime")
                    storeModel.endtime=storeObj.getString("endtime")
                    storeModel.starttime1=storeObj.getString("starttime1")
                    storeModel.endtime1=storeObj.getString("endtime1")
                    storeModel.email=storeObj.getString("email")
                    storeModel.name=storeObj.getString("name")
                    storeModel.mobile=storeObj.getString("mobile")
                    storeModel.telephone=storeObj.getString("telephone")
                    storeModel.fax=storeObj.getString("fax")
                    storeModel.is_favorite=storeObj.getString("is_favorite")
//                        storeModel.timings
//                        storeModel.events
                    val tagArray=storeObj.getJSONArray("tags")
                    val tagsList: ArrayList<TagData> = ArrayList()

                    for (j in 0 until tagArray.length()){
                        val tagsObj=tagArray.getJSONObject(j)
                        val tagData= TagData()
                        tagData.id=tagsObj.getString("id")
                        tagData.tag_name=tagsObj.getString("tag_name")
                        tagsList.add(tagData)
                    }

                    storeModel.tags=tagsList

                    val industriesArray=storeObj.getJSONArray("industries")
                    val industriessList: ArrayList<IndustriesData> = ArrayList()
                    for (k in 0 until industriesArray.length()){
                        val industriessObj=industriesArray.getJSONObject(k)
                        val industriesData= IndustriesData()
                        industriesData.id=industriessObj.getString("id")
                        industriesData.industry_name=industriessObj.getString("industry_name")
                        industriessList.add(industriesData)
                    }
                    storeModel.industries=industriessList

                    if (storeObj.getString("default_location")!="null"){
                        storeModel.default_location_id=storeObj.getJSONObject("default_location").getString("id")

                        storeModel.default_location=storeObj.getJSONObject("default_location")
                    }else{
                        storeModel.default_location_id="null"
                    }
                    recordListSearch.add(storeModel)
                }

                if (pageCount == 1) {
//                    recyclerViewSearch.setHasFixedSize(true)
                    linearLayoutManager =
                        LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                    beachViewAdapter = SeeMoreViewAdapter(this, recordListSearch, resultLauncherFavorite)
                    recyclerViewSearch.layoutManager = linearLayoutManager
                    recyclerViewSearch.adapter = beachViewAdapter
                    linRecentSearch.visibility = View.GONE
                    linSaveListing.visibility = View.VISIBLE
                }else{
                    loadView.showOrGone(false)
                    beachViewAdapter.notifyDataSetChanged()
                }
                if (recordListSearch.size == 0){
                    constSaveBtn.showOrGone(false)
                    nestedView.showOrGone(false)
                    linNoMore.showOrGone(true)
                }else{
                    if (isComeOut) {
                        /*if (editSearch.text.isNotEmpty()) {
                            constSaveBtn.showOrGone(true)
                            linSaveSearch.showOrGone(true)
                            linSavedSearch.showOrGone(false)
                        } else {
                            constSaveBtn.showOrGone(false)
                        }*/
                        constSaveBtn.showOrGone(false)
                        linSaveSearch.showOrGone(true)
                        linSavedSearch.showOrGone(false)
                        isComeOut = false
                    }else{
                        constSaveBtn.showOrGone(true)
                        linSaveSearch.showOrGone(true)
                        linSavedSearch.showOrGone(false)
                    }
                    nestedView.showOrGone(true)
                    linNoMore.showOrGone(false)
                }
            }else{
                loadView.showOrGone(false)
            }
        }else if (type==Utility.searchfilters){
            if (isData){
                filterApiCall=true
                val data =result.getJSONObject(Utility.key.data)
                val citiesArray =data.getJSONArray(Utility.key.cities)
                val industriesArray =data.getJSONArray(Utility.key.industries)
                val tagsArray =data.getJSONArray(Utility.key.tags)

                for (i in 0 until citiesArray.length()){
                    val item = CityModel()
                    val citiesObj=citiesArray.getJSONObject(i)
                    item.cityID=citiesObj.getString(Utility.key.id)
                    item.cityName=citiesObj.getString(Utility.key.city_name)
                    item.isChecked = checkedcitiesArray.contains(citiesObj.getString(Utility.key.id).toInt())
                    citiesItems.add(item)
                }


                for (i in 0 until industriesArray.length()){
                    val item = IndustriesData()
                    val industriesObj=industriesArray.getJSONObject(i)
                    item.id=industriesObj.getString(Utility.key.id)
                    item.industry_name=industriesObj.getString(Utility.key.industry_name)
                    item.isChecked = checkedindustriesArray.contains(industriesObj.getString(Utility.key.id).toInt())
                    industriesItems.add(item)
                }


                for (i in 0 until tagsArray.length()){
                    val item = TagData()
                    val tagsObj=tagsArray.getJSONObject(i)
                    item.id=tagsObj.getString(Utility.key.id)
                    item.tag_name=tagsObj.getString(Utility.key.tag_name)
                    item.isChecked = checkedtagsArray.contains(tagsObj.getString(Utility.key.id).toInt())
                    tagsItems.add(item)
                }
                openFilterDialog("all")

            }
        }
    }

    private fun setRecentSearch(searchTxt: String, filterJson: String) {
        try {
            if (searchTxt.isNotEmpty()) {
                if (!isSavedSearch(searchTxt)) {
                    if (savedSearchItems.size<10) {
                        val model = SavedSearchData()
                        model.search_title = searchTxt
                        model.search_data = filterJson
                        savedSearchItems.add(model)
                        var hash = HashMap<String, String>()
                        hash.put(Utility.key.recentJson, Gson().toJson(savedSearchItems))
                        Utility.saveForm(hash, this)
                    }else{
                        savedSearchItems.removeAt(0)
                        val model = SavedSearchData()
                        model.search_title = searchTxt
                        model.search_data = filterJson
                        savedSearchItems.add(model)
                        var hash = HashMap<String, String>()
                        hash.put(Utility.key.recentJson, Gson().toJson(savedSearchItems))
                        Utility.saveForm(hash, this)
                    }
                }else{
                    for (i in 0 until savedSearchItems.size){
                        if (searchTxt == savedSearchItems[i].search_title){
                            savedSearchItems[i].search_data = filterJson
                        }
                        var hash = HashMap<String, String>()
                        hash.put(Utility.key.recentJson, Gson().toJson(savedSearchItems))
                        Utility.saveForm(hash, this)
                    }
                }
            }
            showRecentSearch()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}