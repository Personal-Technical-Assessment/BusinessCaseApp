package com.gozem.test.businesscase

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.gozem.test.businesscase.databinding.ActivityHomeBinding
import com.gozem.test.businesscase.models.DataDriven
import com.gozem.test.businesscase.utils.Constants.ERROR_TOAST_TYPE
import com.gozem.test.businesscase.utils.Constants.INFO_TOAST_TYPE
import com.gozem.test.businesscase.utils.Constants.MAP_TYPE
import com.gozem.test.businesscase.utils.Constants.PERMISSIONS
import com.gozem.test.businesscase.utils.Constants.PERMISSION_ALL
import com.gozem.test.businesscase.utils.Constants.PROFILE_TYPE
import com.gozem.test.businesscase.utils.Utils.allPermissionsGranted
import com.gozem.test.businesscase.utils.Utils.displayToastMessage
import com.gozem.test.businesscase.utils.Utils.requestForPermissions
import com.gozem.test.businesscase.viewModels.MainViewModel
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {


    private val viewModel: MainViewModel by viewModels()
    private lateinit var mMap: GoogleMap
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mCurrLocationMarker: Marker? = null
    private var mLocationRequest: LocationRequest? = null
    private lateinit var binding: ActivityHomeBinding
    private var markerBitmapIcon: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified
        // when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        viewModel.buildPopUp(this)

        // Fetch data driven list
        viewModel.fetchDataDriven(this)

        // Initialize viewModel observer
        initObservable()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                    initView()
                } else if (!shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // If 'Don't ask again' option is checked
                    displayToastMessage(
                        getString(R.string.permission_do_not_ask_again_checked_message),
                        INFO_TOAST_TYPE
                    )
                } else {
                    requestForPermissions(this)
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        // Check permission state and initialize Google Play Services
        initView()
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (allPermissionsGranted(this, *PERMISSIONS)) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient!!,
                mLocationRequest!!,
                this
            )
        } else {
            requestForPermissions(this)
        }
    }

    override fun onConnectionSuspended(p0: Int) { }

    @SuppressLint("MissingPermission")
    override fun onLocationChanged(location: Location) {
        updateMapView(location, null)
    }

    override fun onConnectionFailed(p0: ConnectionResult) { }

    @SuppressLint("MissingPermission")
    private fun initView() {
        if (allPermissionsGranted(this, *PERMISSIONS)) {
            if (mGoogleApiClient == null) {
                buildGoogleApiClient()
            }
            mMap.isMyLocationEnabled = true
        } else {
            requestForPermissions(this)
        }
    }

    @Synchronized
    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient?.connect()
    }

    private fun initObservable() {
        viewModel.dataDrivenLiveData.observe(this, {
            if (it == null) {
                displayToastMessage(
                    getString(R.string.fetch_data_driven_failure_string),
                    ERROR_TOAST_TYPE)
            } else {
                updateUI(it)
            }
        })
        viewModel.dataValue.observe(this, {
            if (it != null) {
                tvData.text = it
            }
        })
    }

    private fun updateUI(dataDrivenList: List<DataDriven>) {
        for (dataDriven in dataDrivenList) {
            when (dataDriven.type) {
                PROFILE_TYPE -> {
                    // Display user profile picture
                    Glide
                        .with(this)
                        .load(dataDriven.content["image"].toString())
                        .placeholder(R.drawable.loading_spinner)
                        .into(userProfilePicture)

                    // Display user name
                    tvUserName.text = dataDriven.content["name"].toString()

                    // Display user email address
                    tvUserEmail.text = dataDriven.content["email"].toString()

                    // Change view visibility
                    cardProfile.visibility = VISIBLE
                }
                MAP_TYPE -> {
                    // Create location object
                    val location = Location("")
                    location.latitude = dataDriven.content["lat"] as Double
                    location.longitude = dataDriven.content["lng"] as Double

                    // Initialize marker icon
                    Glide.with(this)
                        .asBitmap()
                        .load(dataDriven.content["pine"].toString())
                        .into(object : CustomTarget<Bitmap>(){
                            override fun onResourceReady(resource: Bitmap,
                                                         transition:
                                                         Transition<in Bitmap>?) {
                                markerBitmapIcon = resource
                                // Update the map content
                                updateMapView(
                                    location,
                                    dataDriven.content["title"].toString()
                                )
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                // Update the map content
                                updateMapView(
                                    location,
                                    dataDriven.content["title"].toString()
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) { }
                        })
                }
                else -> {
                    // Display current user data value
                    tvData.text = dataDriven.content["value"].toString()
                    val socketUrl = dataDriven.content["source"].toString()
                    if (TextUtils.isEmpty(socketUrl)) {
                        displayToastMessage(
                            getString(R.string.socker_url_empty_string),
                            ERROR_TOAST_TYPE
                        )
                    } else {
                        // Start socket worker and listen for user data update
                        viewModel.initSocketIOClient(
                            this,
                            socketUrl
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateMapView(location: Location, title: String?) {
        if (allPermissionsGranted(this, *PERMISSIONS)) {
            requestForPermissions(this)
        } else {
            if (mCurrLocationMarker != null) {
                mCurrLocationMarker?.remove()
            }
            // Set marker location
            val latLng = LatLng(location.latitude, location.longitude)
            // Showing Current Location Marker on Map
            val markerOptions = MarkerOptions()
            // Set marker position
            markerOptions.position(latLng)
            // Set marker title
            if (!title.isNullOrEmpty() && !title.isNullOrBlank()) {
                markerOptions.title(title)
            }
            // Set marker icon
            if (markerBitmapIcon != null) {
                markerOptions.icon(
                    BitmapDescriptorFactory
                        .fromBitmap(markerBitmapIcon!!)
                )
            }

            mCurrLocationMarker = mMap.addMarker(markerOptions)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient!!,
                    this
                )
            }
        }
    }
}