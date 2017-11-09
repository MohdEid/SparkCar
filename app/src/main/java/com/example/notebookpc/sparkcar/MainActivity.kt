package com.example.notebookpc.sparkcar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    companion object {
        val LOG_TAG = MainActivity::class.java.simpleName
        private val REQUEST_LOCATION_CODE = 99
        private val REQUEST_CODE_LOCATION_SETTINGS: Int = 100
    }

    private lateinit var map: GoogleMap
    private lateinit var client: GoogleApiClient
    private lateinit var locationReuqest: LocationRequest
    private lateinit var lastLocation: Location
    private var currentLocationMarker: Marker? = null

    private lateinit var fused: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var geoDataClient: LocationServices

    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fused = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /* if(servicesOk()) {
             if (initMap()) {
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                     checkLocationPermission()
                 }
                 buildGoogleApiClient()
             }
             else{
                 Toast.makeText(this,"Map is not Connected",Toast.LENGTH_LONG).show()
             }
         }*/

        locationReuqest = LocationRequest()

        locationReuqest.interval = 5000
        locationReuqest.fastestInterval = 5000
        locationReuqest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(location: LocationResult?) {
                super.onLocationResult(location)
                lastLocation = location?.lastLocation ?: return

                currentLocationMarker?.remove()


                val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)

                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("home")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                currentLocationMarker = map.addMarker(markerOptions)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        fused.removeLocationUpdates(locationCallback)
    }

    //    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        checkLocationSetting()
        buildGoogleApiClient()
        if (checkLocationPermission()) {
            enableMyLocation()
        }

    }

    private fun checkLocationSetting() {
        val locationSettingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationReuqest)
        val task = LocationServices
                .getSettingsClient(this@MainActivity)
                .checkLocationSettings(locationSettingsBuilder.build())
        task.addOnFailureListener {
            when ((task.exception as ApiException).statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED -> {
                    (task.exception as ResolvableApiException)
                            .startResolutionForResult(this@MainActivity, REQUEST_CODE_LOCATION_SETTINGS)
                }
            }
        }

    }

    @RequiresPermission(value = Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        fused.lastLocation.addOnSuccessListener {
            val location = it ?: return@addOnSuccessListener
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13f))
        }

        fused.requestLocationUpdates(locationReuqest, locationCallback, null)
    }

    private fun servicesOk(): Boolean {

        val isAvailable: Int = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable)) {
            dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, isAvailable, REQUEST_LOCATION_CODE)
            dialog.show()
        } else {
            Toast.makeText(this, "Can't connect to mapping services", Toast.LENGTH_LONG).show()
        }
        return false
    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        client = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        client.connect()
    }

    override fun onConnected(bundle: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        buildGoogleApiClient()

                        enableMyLocation()
                    }
                } else {
                    Toast.makeText(this, "something went wrong", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
            return false
        } else
            return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_LOCATION_SETTINGS -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
        }
    }
}