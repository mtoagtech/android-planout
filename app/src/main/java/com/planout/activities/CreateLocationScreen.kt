package com.planout.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.planout.R
import com.planout.api_calling.ApiController
import com.planout.api_calling.ApiResponse
import com.planout.api_calling.CallApi
import com.planout.constant.Utility
import com.planout.constant.Utility.showOrGone
import com.planout.constant.Utility.showOrInvisible
import kotlinx.android.synthetic.main.activity_create_location_screen.*
import okhttp3.FormBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class CreateLocationScreen : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLoadedCallback, ApiResponse,
    Listener {
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    var mCurrLocationMarker: Marker? = null
    private lateinit var mMap: GoogleMap
    var state = ""
    var country = ""
    var subLocality = ""
    var cityName = ""
    var isPermission = false
    var editManually = false
    var editGPS = false

    var result: PendingResult<LocationSettingsResult>? = null
    val REQUEST_LOCATION = 199
    var is_Form=""
    var is_ManualClick=false

    var easyWayLocation: EasyWayLocation? = null

    var location_get = false

    var cityID = ""

    var latitude:Double = 0.0
    var longitude:Double = 0.0
    var editLatitude:Double = 0.0
    var editLongitude:Double = 0.0
    var StoreId=""
    var locationId=""
    var isEdit=false

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_location_screen)

        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        is_Form=intent.getStringExtra(Utility.key.isFrom)!!
        Utility.show_progress(this)
        Utility.animationClick(imgBackHeader).setOnClickListener {
            onBackPressed()
        }

        if (intent.getStringExtra(Utility.key.isFrom)==Utility.key.add){
            StoreId=intent.getStringExtra(Utility.key.store_id)!!
        }else if (intent.getStringExtra(Utility.key.isFrom)==Utility.key.edit){
            val dataObj=JSONObject(intent.getStringExtra(Utility.key.details)!!)
            locationId=dataObj.getString(Utility.key.id)
            val store_id=dataObj.getString(Utility.key.store_id)
            val city_id=dataObj.getString(Utility.key.city_id)
            val city_name=dataObj.getString(Utility.key.city_name)
            val area=dataObj.getString(Utility.key.area)
            val address=dataObj.getString(Utility.key.address)
            val address1=dataObj.getString(Utility.key.address1)
            val postal_code=dataObj.getString(Utility.key.postal_code)
            val latitudeGet=dataObj.getString(Utility.key.latitude)
            val addressType = dataObj.getString(Utility.key.address_type)
            editLatitude= dataObj.getString(Utility.key.latitude).toDouble()
            val longitudeGet=dataObj.getString(Utility.key.longitude)
            editLongitude=dataObj.getString(Utility.key.longitude).toDouble()
            val table_indoor=dataObj.getString(Utility.key.table_indoor)
            val table_outdoor=dataObj.getString(Utility.key.table_outdoor)
            editManually = addressType == "2"
            editGPS = addressType == "1"
            if (addressType == "1"){
                editLocation.text=address
                radBtnGPS.isChecked = true
                radBtnManual.isChecked = false
                setRadioGPS()
            }else{
                editAddress.setText(address1)
                editArea.setText(area)
                editLat.setText(latitudeGet)
                editLong.setText(longitudeGet)
                radBtnGPS.isChecked = false
                radBtnManual.isChecked = true
                setRadioManual()
            }
            editPostal.setText(postal_code)
            txtSelCity.text = city_name
            latitude=latitudeGet.toDouble()
            longitude=longitudeGet.toDouble()
            cityID=city_id
            StoreId=store_id

            txtIndoor.isChecked = table_indoor=="1"
            txtOutdoor.isChecked = table_outdoor=="1"

        }
        //select or unselect indoor
        txtIndoor.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                typeErr.showOrGone(false)
                typeErr.text = ""
                txtIndoor.setTextColor(ContextCompat.getColor(this, R.color.app_green))
            }else{
                txtIndoor.setTextColor(ContextCompat.getColor(this, R.color.gray_5B_FF))
            }
        }
        //select or unselect outdoor
        txtOutdoor.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                typeErr.showOrGone(false)
                typeErr.text = ""
                txtOutdoor.setTextColor(ContextCompat.getColor(this, R.color.app_green))
            }else{
                txtOutdoor.setTextColor(ContextCompat.getColor(this, R.color.gray_5B_FF))
            }
        }

        var strLat : String
        var strLong : String
        editLat.doOnTextChanged { text, start, before, count ->
            strLat = text.toString()
            strLong = editLong.text.toString()
            if (strLat.isNotEmpty() && strLong.isNotEmpty()){
                setdata(LatLng(strLat.toDouble(), strLong.toDouble()))
            }
        }
        editLong.doOnTextChanged { text, start, before, count ->
            strLat = editLat.text.toString()
            strLong = text.toString()
            if (strLat.isNotEmpty() && strLong.isNotEmpty()){
                setdata(LatLng(strLat.toDouble(), strLong.toDouble()))
            }
        }

        //GPS or Manual Location
        radBtnGPS.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                setRadioGPS()
            }else{
                is_ManualClick = true
                radBtnManual.isChecked = true
            }
        }
        radBtnManual.setOnCheckedChangeListener { compoundButton, b ->
            if (b){
                is_ManualClick = true
                setRadioManual()
            }else{
                radBtnGPS.isChecked = true
            }
        }

        easyWayLocation = EasyWayLocation(this, false, false, this)


        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.api_key))
        }
        editLocation.setOnClickListener {
            // return after the user has made a selection.
            val field = listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )

            // Start the autocomplete intent.
            val intent = Autocomplete
                .IntentBuilder(AutocompleteActivityMode.OVERLAY, field)
                .build(this@CreateLocationScreen)
            //start activity result
            startActivityForResult(intent, 102)

        }

        //set map set first
        setFirstMapView()

        Utility.animationClick(txtSelCity).setOnClickListener {
            //launch activity for cities
            resultLauncher.launch(Intent(this, CityListScreen::class.java))
        }

        editLocation.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editLocation, locationErr, false, "")
        }
        editAddress.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editAddress, addressErr, false, "")
        }
        editArea.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editArea, areaErr, false, "")
        }
        editPostal.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editPostal, cityPostalErr, false, "")
        }
        editLat.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editLat, areaLatErr, false, "")
        }
        editLong.doOnTextChanged { text, start, before, count ->
            Utility.showGoneErrorView(editLong, areaLongErr, false, "")
        }

        Utility.animationClick(saveLocation).setOnClickListener {
            //validation for save location
            if (radBtnGPS.isChecked){
                if (editLocation.text.toString() == "") {
                    //Utility.customErrorToast(this, "Select your location on map first")
                    Utility.showGoneErrorView(editLocation, locationErr, true, "Select your location on map first")
                }else if (cityID == "") {
                    //Utility.customErrorToast(this, "Select your city")
                    cityPostalErr.showOrGone(true)
                    cityPostalErr.text = getString(R.string.select_your_city)
                }else if (!txtIndoor.isChecked && !txtOutdoor.isChecked) {
                    //Utility.customErrorToast(this, "Enter your table type")
                    typeErr.showOrGone(true)
                    typeErr.text = getString(R.string.enter_table_type)
                }else{
                    //call api for save location
                    addLocationApi()
                }
            }else{
                if (editAddress.text.toString() == "") {
                    //Utility.customErrorToast(this, "Enter your address")
                    Utility.showGoneErrorView(editAddress, addressErr, true, getString(R.string.enter_address))
                }else if (editAddress.text.toString().length<3) {
                    //Utility.customErrorToast(this, "The address must be at least 3 characters.")
                    Utility.showGoneErrorView(editAddress, addressErr, true, getString(R.string.enter_address_validation))
                }else if (editArea.text.toString() == "") {
                    //Utility.customErrorToast(this, "Enter your area")
                    Utility.showGoneErrorView(editArea, areaErr, true, getString(R.string.enter_area))
                }else if (cityID == "") {
                    //Utility.customErrorToast(this, "Select your city")
                    cityPostalErr.showOrGone(true)
                    cityPostalErr.text = getString(R.string.select_your_city)
                }else if (editPostal.text.toString() == "") {
                    //Utility.customErrorToast(this, "Enter your postal code")
                    Utility.showGoneErrorView(editPostal, cityPostalErr, true, getString(R.string.enter_postal_code))
                }else if (!txtIndoor.isChecked && !txtOutdoor.isChecked) {
                    //Utility.customErrorToast(this, "Enter your table type")
                    typeErr.showOrGone(true)
                    typeErr.text = getString(R.string.enter_table_type)
                }else if (editLat.text.toString().trim()=="") {
                    //Utility.customErrorToast(this, "Enter your table type")
                    areaLatErr.showOrGone(true)
                    areaLatErr.text = getString(R.string.enter_latitude)
                }else if (editLong.text.toString().trim()=="") {
                    //Utility.customErrorToast(this, "Enter your table type")
                    areaLongErr.showOrGone(true)
                    areaLongErr.text = getString(R.string.enter_longitude)
                }else{
                    try {
                        latitude = editLat.text.toString().trim().toDouble()

                    }catch (e:Exception){
//                        latitude = 0.0
                        e.printStackTrace()
                    }
                    try {
                        longitude = editLong.text.toString().trim().toDouble()

                    }catch (e:Exception){
//                        longitude = 0.0
                        e.printStackTrace()
                    }
                    //call api for save location
                    addLocationApi()
                }
            }
        }
    }

    private fun setRadioManual() {
        radBtnGPS.isChecked = false
        currentLocation.showOrGone(false)
        editLocation.showOrGone(false)
        locationErr.showOrGone(false)
        editAddress.showOrGone(true)
        addressErr.showOrGone(false)
        editArea.showOrGone(true)
        areaErr.showOrGone(false)
        editLat.showOrGone(true)
        areaLatErr.showOrGone(false)
        editLong.showOrGone(true)
        areaLongErr.showOrGone(false)
        editPostal.showOrInvisible(true)
        txtSelCity.showOrGone(true)
        cityPostalErr.showOrGone(false)
        if (!editManually) {
            editLocation.text = ""
            editAddress.setText("")
            editArea.setText("")
            editLat.setText("")
            editLong.setText("")
            editPostal.setText("")
        }else{
            if (!editLat.text.toString().isNullOrEmpty() && !editLong.text.toString().isNullOrEmpty() && is_ManualClick) {
                setdata(
                    LatLng(
                        editLat.text.toString().trim().toDouble(),
                        editLong.text.toString().trim().toDouble()
                    )
                )
                is_ManualClick = false
            }
        }
        try {
            if (mMap!=null) {
                mMap.clear()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun setRadioGPS() {
        radBtnManual.isChecked = false
        currentLocation.showOrGone(true)
        editLocation.showOrGone(true)
        locationErr.showOrGone(false)
        editAddress.showOrGone(false)
        addressErr.showOrGone(false)
        editArea.showOrGone(false)
        areaErr.showOrGone(false)
        editLat.showOrGone(false)
        areaLatErr.showOrGone(false)
        editLong.showOrGone(false)
        areaLongErr.showOrGone(false)
        editPostal.showOrInvisible(false)
        txtSelCity.showOrGone(true)
        cityPostalErr.showOrGone(false)
        if (!editManually) {
            editAddress.setText("")
            editArea.setText("")
            editLat.setText("")
            editLong.setText("")
            editPostal.setText("")
        }
        //set current location
    }

    @SuppressLint("ResourceType")
    private fun setFirstMapView() {
        //check location required permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isPermission = checkLocationPermission()
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val mapView = mapFragment.view
        if (mapView?.findViewById<View?>(1) != null
        ) {
            val locationButton =
                (mapView.findViewById<View>(1).parent as View).findViewById<View>(2)
            // and next place it, on bottom right (as Google Maps app)
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 30, 30)

        }
        Utility.animationClick(currentLocation).setOnClickListener {

            mapFragment.getMapAsync(this)
            editLocation.text = ""
            location_get = false
            is_Form=Utility.key.add
            easyWayLocation!!.startLocation()

        }

    }

    private fun addLocationApi() {

        val mBuilder = FormBody.Builder()
        mBuilder.add(Utility.key.store_id,StoreId)
        mBuilder.add(Utility.key.address,editLocation.text.toString())
        mBuilder.add(Utility.key.address1,editAddress.text.toString())
        mBuilder.add(Utility.key.latitude,latitude.toString())
        mBuilder.add(Utility.key.longitude,longitude.toString())
        mBuilder.add(Utility.key.city_id,cityID)
        mBuilder.add(Utility.key.area,editArea.text.toString())
        mBuilder.add(Utility.key.postal_code,editPostal.text.toString())
        if (radBtnGPS.isChecked) {
            mBuilder.add(Utility.key.address_type, "1")//GPS
        }else{
            mBuilder.add(Utility.key.address_type, "2")//Manually
        }
        if (txtIndoor.isChecked){
            mBuilder.add(Utility.key.table_indoor,"1")
        }else{
            mBuilder.add(Utility.key.table_indoor,"0")
        }
        if (txtOutdoor.isChecked){
            mBuilder.add(Utility.key.table_outdoor,"1")
        }else{
            mBuilder.add(Utility.key.table_outdoor,"0")
        }
        if (intent.getStringExtra(Utility.key.isFrom)==Utility.key.add){
            CallApi.callAPi(mBuilder, ApiController.api.store_locations, this, Utility.store_locations, true, Utility.POST, true)
        }else{
            CallApi.callAPi(mBuilder, ApiController.api.store_locations+"/"+locationId, this, Utility.store_locationsEdit, true, Utility.PUT, true)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 102) {
            when (resultCode) {
                RESULT_OK -> {
                    //When success initialize place
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    setdata(place.latLng)
                    //set address on edittext
                    editLocation.text = place.address
                    editPostal.setText(place.name)
                    val address_comp = place.addressComponents
                    locationErr.showOrGone(false)
                    locationErr.text = ""
                    try {
                        val addresses: List<Address>
                        val geocoder = Geocoder(this, Locale.getDefault())
                        try {
                            addresses = geocoder.getFromLocation(
                                place.latLng!!.latitude,
                                place.latLng!!.longitude,
                                1
                            )!! // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            val address1 =
                                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            val address2 =
                                addresses[0].getAddressLine(1) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            val city = addresses[0].locality
                            val state = addresses[0].adminArea
                            val country = addresses[0].countryName
                            val postalCode = addresses[0].postalCode
                            Log.e("Address1: ", "" + address1)
                            Log.e("Address2: ", "" + address2)
                            Log.e("AddressCity: ", "" + city)
                            Log.e("AddressState: ", "" + state)
                            Log.e("AddressCountry: ", "" + country)
                            Log.e("AddressPostal: ", "" + postalCode)
                            Log.e("AddressLatitude: ", "" + place.latLng.latitude)
                            Log.e("AddressLongitude: ", "" + place.latLng.longitude)
                            editAddress.setText(address1)
                            editArea.setText(address1)
                            if (!postalCode.isNullOrEmpty()){
                                editPostal.setText(postalCode)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        //setMarker(latLng);
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    //Log.i(TAG, status.getStatusMessage());
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        } else if (requestCode == REQUEST_LOCATION) {
            when (resultCode) {
                RESULT_OK -> {}
                RESULT_CANCELED -> {
                    // The user was asked to change settings, but chose not to
                    Utility.customErrorToast(
                        this@CreateLocationScreen,
                        getString(R.string.location_not_enabled)
                    )
                }
                else -> {}
            }
        } else if (requestCode == EasyWayLocation.LOCATION_SETTING_REQUEST_CODE) {
            easyWayLocation!!.onActivityResult(resultCode)
        }
    }

    override fun onResume() {
        super.onResume()
        easyWayLocation!!.startLocation()
    }

    override fun onPause() {
        super.onPause()
        easyWayLocation!!.endUpdates()
    }

    private fun setdata(latLng: LatLng?) {
        mMap.clear()

       longitude = latLng!!.longitude
       latitude = latLng.latitude
        val geocoder = Geocoder(
            applicationContext,
            Locale.getDefault()
        )
        try {
            getCompleteAddressString(latitude, longitude, latLng)

        } catch (e: IOException) {
            e.printStackTrace()
        }

        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                Utility.getBitmap(
                    this,
                    R.drawable.ic_location_flag
                )!!
            )
        )
        mCurrLocationMarker = mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f), 1000, null);

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap;
        if (isPermission) {
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.setOnMapLoadedCallback(this);
//        mMap.uiSettings.isZoomControlsEnabled = true
            mMap.uiSettings.isZoomGesturesEnabled = true
//        mMap.uiSettings.isCompassEnabled = true
            //Initialize Google Play Services
            mMap.uiSettings.isMyLocationButtonEnabled = false
            mMap.isMyLocationEnabled = true
            currentLocation.showOrGone(radBtnGPS.isChecked)
        }

        mMap.setOnMyLocationButtonClickListener {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            editLocation.setText("")
            location_get = false
            easyWayLocation!!.startLocation()
            true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
            }
        } else {
            mMap.isMyLocationEnabled = true
        }
    }

    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {}


    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    finish()
                }
                return
            }
        }
    }

    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double, latLng: LatLng) {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = java.lang.StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()

                Utility.hide_progress(this)
                editLocation.setText(strAdd)

                state = ""
                country = ""
                subLocality = ""
                cityName = ""
                cityName = addresses[0].locality
                state = addresses[0].adminArea
                country = addresses[0].countryName
                if (addresses[0].subLocality != null) {
                    subLocality = addresses[0].subLocality
                }

//                Utility.animationClick(use_location).setOnClickListener {
//                    if (isdeliver){
//                        val data = Intent()
//                        data.putExtra("pincode", addresses[0].postalCode)
//                        data.putExtra("address", strAdd)
//                        data.putExtra("cityName", cityName)
//                        data.putExtra("state", state)
//                        data.putExtra("country", country)
//                        data.putExtra("subLocality", subLocality)
//                        data.putExtra("latitude", LATITUDE.toString())
//                        data.putExtra("longitude", LONGITUDE.toString())
//                        setResult(RESULT_OK, data)
//                        Animatoo.animateSlideRight(this@CreateLocationScreen)
//                        finish()
//                    }
//                }


                Log.d(
                    "Location", "" + latLng + "," + subLocality + "," + cityName + "," + state
                            + "," + country
                )
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapLoaded() {
        Utility.hide_progress(this)
    }

    override fun onTaskComplete(result: JSONObject, type: String, isData: Boolean) {
        if (type==Utility.store_locations){
            if (isData){
                val intent = Intent()
                setResult(Activity.RESULT_OK, intent)
                finish()
                Utility.customSuccessToast(this, Utility.checkStringNullOrNot(result, Utility.key.message))
            }else{
                Utility.customErrorToast(this, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }else if (type==Utility.store_locationsEdit){
            if (isData){
                isEdit=true
                Utility.customSuccessToast(this, Utility.checkStringNullOrNot(result, Utility.key.message))
                onBackPressed()
            }else{
                Utility.customErrorToast(this, Utility.checkStringNullOrNot(result, Utility.key.message))
            }
        }

    }

    override fun onBackPressed() {
        if (isEdit){
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }else{
            setResult(Activity.RESULT_CANCELED)
            finish()
            Animatoo.animateSlideRight(this@CreateLocationScreen)
        }

    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {
        if (!location_get) {
            if (is_Form == Utility.key.edit) {
                setdata(LatLng(latitude, longitude))
            } else {
                val data = StringBuilder()
                data.append(location!!.latitude)
                data.append(" , ")
                data.append(location.longitude)
                setdata(LatLng(location.latitude, location.longitude))
                location_get = true
                if (intent.getStringExtra(Utility.key.isFrom)==Utility.key.edit){
                    is_Form=Utility.key.edit
                }
            }
        }
    }

    override fun locationCancelled() {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                cityID = data!!.getStringExtra("CityID")!!
                txtSelCity.text = data.getStringExtra("CityName")
                cityPostalErr.showOrGone(false)
                cityPostalErr.text = ""
            }
        }

}