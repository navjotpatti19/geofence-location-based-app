package com.example.sapple.googlemaps.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.adapters.CustomInfoWindowAdapter
import com.example.sapple.googlemaps.helperClasses.MyGlobals
import com.example.sapple.googlemaps.helperClasses.Permissions
import com.example.sapple.googlemaps.helperClasses.PlaceAutoCompleteAdapter
import com.example.sapple.googlemaps.models.PlaceInfo
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_google_map.*
import java.io.IOException
import java.util.*

class GoogleMapActivity : NavigationActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener,
        TextView.OnEditorActionListener, GoogleApiClient.OnConnectionFailedListener {

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this@GoogleMapActivity, "Map search failed", Toast.LENGTH_LONG).show()
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event?.action == KeyEvent.ACTION_DOWN
                || event?.action == KeyEvent.KEYCODE_ENTER) {
            //execute the method for searching
            mMap.clear()
            geoLocate()
        }
        return false
    }

    override fun onMarkerDragEnd(marker: Marker?) {
        val position = marker?.position
        latitude = position?.latitude.toString()
        longitude = position?.longitude.toString()
        locationAddress(position!!)
    }

    override fun onMarkerDragStart(p0: Marker?) {
    }

    override fun onMarkerDrag(p0: Marker?) {
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        if(permissionObj.isPermissionGranted(this, permissionsList, LOCATION_PERMISSION_REQUEST_CODE)) {
            getDeviceLocation()
            if(ContextCompat.checkSelfPermission(applicationContext, FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(applicationContext, COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false

            listenersImplementation()
        }
    }

    // class variables
    private var mLocationPermissionGranted = false
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var etSearch: AutoCompleteTextView
    private lateinit var ivGps: ImageView
    private lateinit var ivMap: ImageView
    private lateinit var ivInfo: ImageView
    private lateinit var googleApiClient: GoogleApiClient
    private var latitude: String? = null
    private var longitude: String? = null
    private var locationName: String? = null
    private lateinit var placeAutoCompleteAdapter: PlaceAutoCompleteAdapter
    private var placeInfo: PlaceInfo? = null
    private lateinit var marker: Marker
    val permissionObj = Permissions()

    companion object {
        const val TAG = "GoogleMapActivity"
        const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        val permissionsList = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
        const val PLACE_PICKER_REQUEST = 1
        const val DEFAULT_ZOOM = 15f
        val latLngBounds = LatLngBounds(LatLng(-40.0, -168.0), LatLng(71.0, 136.0))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_map)
        etSearch = findViewById(R.id.et_search)
        ivGps = findViewById(R.id.iv_gps)
        ivMap = findViewById(R.id.iv_map)
        ivInfo = findViewById(R.id.iv_info)

        getLocationPermission()
    }

    private fun listenersImplementation() {
        googleApiClient = GoogleApiClient
                .Builder(this@GoogleMapActivity)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()

         placeAutoCompleteAdapter = PlaceAutoCompleteAdapter(this, googleApiClient,
                 latLngBounds, null)
        etSearch.setAdapter(placeAutoCompleteAdapter)
        etSearch.onItemClickListener = autoCompleteClickListener
        etSearch.setOnEditorActionListener(this@GoogleMapActivity)

        ivGps.setOnClickListener {
            Log.d(TAG, "onClick: Clicked GPS icon")
            mMap.clear()
            getDeviceLocation()
        }

        ivMap.setOnClickListener {
            Log.d(TAG, "onClick: Clicked Map icon")
            val builder = PlacePicker.IntentBuilder()
            try {
                startActivityForResult(builder.build(this@GoogleMapActivity), PLACE_PICKER_REQUEST)
            } catch (e: GooglePlayServicesRepairableException) {
                Log.e(TAG, "GooglePlayServicesRepairableException" + e.message)
            } catch (e: GooglePlayServicesNotAvailableException) {
                Log.e(TAG, "GooglePlayServicesNotAvailableException" + e.message)
            }
        }

        ivInfo.setOnClickListener {
            Log.d(TAG, "onClick: Clicked info icon")
            try {
                if(marker.isInfoWindowShown) {
                    marker.hideInfoWindow()
                } else {
                    marker.showInfoWindow()
                }
            } catch (e: Exception) {
                Log.e(TAG, "onclick: Null Pointer Exception ${e.message}")
            }
        }

        mMap.setOnMapClickListener {
            mMap.clear()
            latitude = it.latitude.toString()
            longitude = it.longitude.toString()
            val address = locationAddress(it)
            moveCamera(LatLng(it.latitude, it.longitude), DEFAULT_ZOOM, address)
        }

        fabButton.setOnClickListener {
            MyGlobals.activityName = "AddReminderActivity"
            val intent = Intent(this@GoogleMapActivity, AddReminderActivity::class.java)
            intent.putExtra("LAT", latitude)
            intent.putExtra("LNG", longitude)
            intent.putExtra("LOCATION", locationName)
            startActivity(intent)
            finish()
        }
    }

    private fun geoLocate() {
        Log.d(TAG, "geoLocate: geoLocating")
        val searchString = etSearch.text.toString()
        val geoCoder = Geocoder(this@GoogleMapActivity)
        var addressList = listOf<Address>()
        try {
            addressList = geoCoder.getFromLocationName(searchString, 1)
        } catch (e:IOException) {
            Log.e(TAG, "geoLocate: IOException : " + e.message)
        }
        if(addressList.isNotEmpty()) {
            val address = addressList[0]
            Log.d(TAG, "geoLocate: found a location" + address.toString())
            moveCamera(LatLng(address.latitude, address.longitude), DEFAULT_ZOOM, address.getAddressLine(0))
        }
    }

    private fun getLocationPermission() {
        if(permissionObj.isPermissionGranted(this, permissionsList, LOCATION_PERMISSION_REQUEST_CODE)) {
            initMap()
        }
    }

    private fun getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@GoogleMapActivity)
        try {
            if(permissionObj.isPermissionGranted(this, permissionsList, LOCATION_PERMISSION_REQUEST_CODE)) {
                val location = fusedLocationProviderClient.lastLocation
                location.addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.d(TAG, "Location Found")
                        if(it.result != null) {
                            val currentLocation = it.result as Location
                            moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude), DEFAULT_ZOOM, "My Location")
                        } else {
                            MyGlobals().checkLocation(this)
                        }
                    } else {
                        Log.d(TAG, "Current Location is null")
                        Toast.makeText(this@GoogleMapActivity, "Unable to get current location", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.d(TAG, "Security Exception: " + e.message)
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float,  title: String) {

        Log.d(TAG, "Move Camera: Moving the camera to Lat: " + latLng.latitude + ", Lng: " + latLng.longitude)
        latitude = latLng.latitude.toString()
        longitude = latLng.longitude.toString()
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this@GoogleMapActivity))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        locationAddress(latLng)

        //adding a marker on location
        marker = mMap.addMarker(MarkerOptions()
                .position(latLng)
//                .title(title)
                .snippet(title)
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
        mMap.setOnMarkerDragListener(this@GoogleMapActivity)
    }

    private fun moveCamera(latLng: LatLng, zoom: Float,placeInfo: PlaceInfo?) {
        Log.d(TAG, "Move Camera: Moving the camera to Lat: " + latLng.latitude + ", Lng: " + latLng.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        locationAddress(latLng)
        latitude = latLng.latitude.toString()
        longitude = latLng.longitude.toString()
        mMap.clear()
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this@GoogleMapActivity))
        if(placeInfo != null) {
            try {
                val snippet = "Address: ${placeInfo.address} \n Phone Number: ${placeInfo.phone} \n " +
                        "Website: ${placeInfo.websiteUri} \n Rating: ${placeInfo.rating}"

                //adding a marker on location
                marker = mMap.addMarker(
                        MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.name)
                        .draggable(true)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED)
                        )
                )
                mMap.setOnMarkerDragListener(this@GoogleMapActivity)
            } catch (e: NullPointerException) {
              Log.e(TAG, "MoveCamera: NullPointerException: ${e.message}")
            }
        } else {
            mMap.addMarker(MarkerOptions()
                    .position(latLng))
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@GoogleMapActivity)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        mLocationPermissionGranted = false
        when(requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                var isGranted = false
                if(grantResults.isNotEmpty()) {
                    for(i in permissions.indices) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isGranted = false
                            break
//                            mLocationPermissionGranted = false
                        } else {
                            isGranted = true
                        }
                    }

                    if(isGranted) {
                        //initialize the map
                        initMap()
                    } else {
                        permissionObj.shouldShowRequestPermissionRationale(this, permissionsList, requestCode)
                    }
                    /*mLocationPermissionGranted = true
                    //initialize the map
                    initMap()*/
                }
            }
        }
    }

    /** google places API autocomplete suggestions */
    private val autoCompleteClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        hideSoftKeyboard()
        val item = placeAutoCompleteAdapter.getItem(position)
        val placeId = item.placeId
        val placeResult = Places.GeoDataApi
                .getPlaceById(googleApiClient, placeId)
        placeResult.setResultCallback(updatePlaceDetailsCallback)
    }

    private val updatePlaceDetailsCallback = ResultCallback<PlaceBuffer> { places ->
        if(!places.status.isSuccess) {
            Log.d(TAG, "onResult: Place query did not complete successfully: ${places.status}")
            places.release()
            return@ResultCallback
        }

        val place = places.get(0)
        try {
            placeInfo = PlaceInfo(place.name.toString(), place.address.toString(),
                    place.phoneNumber.toString(), place.id, place.websiteUri, place.latLng,
                    place.rating, place.attributions.toString())
        } catch (e: NullPointerException) {
            Log.e(TAG, "onresult: Null Pointer Exception ${e.message}")
        }
        moveCamera(LatLng(place.viewport!!.center.latitude,
                place.viewport!!.center.longitude), DEFAULT_ZOOM, placeInfo)
        places.release()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                val placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, place.id)
                placeResult.setResultCallback(updatePlaceDetailsCallback)
            }
        }
    }

    private fun hideSoftKeyboard() {
        val keyboard = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        keyboard.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun locationAddress(latLng: LatLng): String {
            var addresses: List<Address> = listOf()
            val geoCoder = Geocoder(this@GoogleMapActivity, Locale.getDefault())
            try {
                addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if(!addresses.isEmpty()) {
                    val obj = addresses[0]
                    locationName = obj.getAddressLine(0)
                }
            } catch (e: IOException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        if(!addresses.isEmpty()) {
            return addresses[0].getAddressLine(0) + "\n" + addresses[0].locality + "\n" +
                    addresses[0].adminArea + "\n" + addresses[0].countryName + "\n" +
                    addresses[0].featureName
        }
        return ""
    }
}
