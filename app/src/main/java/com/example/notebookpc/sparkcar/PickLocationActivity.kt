package com.example.notebookpc.sparkcar

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import com.example.notebookpc.sparkcar.data.FavoriteLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_pick_location.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class PickLocationActivity : AppCompatActivity(), OnMapReadyCallback, AnkoLogger {
    companion object {
        private val LOG_TAG: String = PickLocationActivity::class.java.simpleName
        private val REQUEST_CODE_LOCATION_PERMISSION: Int = 10
        private val REQUEST_CODE_LOCATION_SETTINGS: Int = 11
    }

    private lateinit var favoritesList: List<Id>
    private lateinit var map: GoogleMap


    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var destination: LatLng? = null
    private lateinit var destinationMarker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        addLocation.onClick {
            val favoriteLocation = destination
            if (favoriteLocation == null) {
                toast(getString(R.string.please_long_click_location))
                return@onClick
            }

            alert {
                lateinit var nameEditText: EditText
                customView {
                    linearLayout {
                        orientation = LinearLayout.HORIZONTAL

                        textView {
                            text = "Enter location name: "
                        }
                        nameEditText = editText {}
                    }
                }
                okButton {
                    val uid = CustomerHolder.customer?.id ?: throw IllegalStateException()
                    val locationReference = FirebaseDatabase.getInstance().getReference("/customers/$uid/favorite_locations")

                    locationReference.push().setValue(FavoriteLocation(nameEditText.text.toString(), favoriteLocation))
                    finish()
                }

            }.show()
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        destinationMarker = map.addMarker(MarkerOptions()
                .position(map.cameraPosition.target)
                .visible(false)
                .draggable(true)
                .title(getString(R.string.delivery_point))
        )

        with(map.uiSettings) {
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
            setAllGesturesEnabled(true)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            requestLocationPermission(REQUEST_CODE_LOCATION_PERMISSION)
        }

        checkLocationSetting(REQUEST_CODE_LOCATION_SETTINGS)

        toast(getString(R.string.set_pickup_location))
        map.setOnMapLongClickListener { latLng: LatLng ->
            setDestination(latLng)
        }
    }

    private fun setDestination(latLng: LatLng) {
        destination = latLng
        destinationMarker.apply {
            isVisible = true
            position = latLng
        }
        addLocation.isEnabled = true
    }


    private fun requestLocationPermission(requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this).apply {
                setTitle("Location Permission")
                setMessage("This app needs to access your location in order to set your position on the map.")
                setPositiveButton("Accept") { _, _ ->
                    ActivityCompat.requestPermissions(this@PickLocationActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
                }
                setNegativeButton("Reject") { _, _ ->
                }
            }.show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_LOCATION_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // redundant check to eliminate warning
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        enableMyLocation()
                        FirebaseAuth.getInstance().currentUser?.uid
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_LOCATION_SETTINGS -> {
                Log.d(LOG_TAG, "returned from updating location settings")
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        launch(CommonPool) {
            val task = fusedLocationClient.lastLocation
            Tasks.await(task)
            val lastLocation: Location? = if (task.isSuccessful) task.result else null
            Log.d(LOG_TAG, "lastLocation: $lastLocation")
            lastLocation?.apply {
                kotlinx.coroutines.experimental.run(UI) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 13f))
                }
            }
        }
    }
}
