package com.example.notebookpc.sparkcar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.example.notebookpc.sparkcar.data.Cleaner
import com.example.notebookpc.sparkcar.data.Customer
import com.firebase.ui.auth.AuthUI
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.*

typealias Id = String

class TestingActivity : AppCompatActivity(),
        AnkoLogger,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        MessagesFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        FavoritesFragment.OnFragmentInteractionListener,
        LocationFragment.OnFragmentInteractionListener,
        CarsFragment.OnFragmentInteractionListener,
        AboutFragment.OnFragmentInteractionListener,
        ShareFragment.OnFragmentInteractionListener {

    companion object {
        val REQUEST_LOCATION_CODE = 99
        val REQUEST_CODE_LOCATION_SETTINGS: Int = 100
        var RC_SIGN_IN: Int = 123
    }

    //GoogleMaps Initialization
    private lateinit var map: GoogleMap
    private lateinit var client: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var currentLocationMarker: Marker? = null
    private val cleanersMarkers = mutableMapOf<Id, Marker>()

    private lateinit var fused: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult?) {
            super.onLocationResult(location)

            currentLocationMarker?.remove()

            val lastLocation = lastLocation ?: return
            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)

            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("home")
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            currentLocationMarker = map.addMarker(markerOptions)
            debug("current location: $latLng")
        }
    }


    private lateinit var dialog: Dialog

    //Navigation Drawer Initialization
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView


    //Firebase Initialization
    private val cleanersReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("cleaners")
    private val customersReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("customers")
    private val firebaseAuth = FirebaseAuth.getInstance()


    private val cleaners: MutableList<Cleaner> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        //instantiating NavigationDrawer
        drawerLayout = findViewById(R.id.drawer_layout)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, null, R.string.drawer_opened, R.string.drawer_closed)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        updateSignOutItem()

        fused = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = SupportMapFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, mapFragment)
                .commit()
        mapFragment.getMapAsync(this)

        if (servicesOk()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission()
            }
            buildGoogleApiClient()
        } else {
            Toast.makeText(this, "Map is not Connected", Toast.LENGTH_LONG).show()
        }

        locationRequest = LocationRequest()

        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        firebaseAuth.addAuthStateListener {
            updateSignOutItem()
        }
    }

    private fun updateSignOutItem() {
        val item = navigationView.menu.findItem(R.id.id_sign_out)
        if (firebaseAuth.currentUser != null) {
            item.isVisible = true
            this.invalidateOptionsMenu()
        } else {
            item.isVisible = false
            this.invalidateOptionsMenu()
        }
    }

    /**
     * Removes cleaner by id
     */
    private fun removeCleaner(cleaner: Cleaner) {
        cleaners.removeAll { it.id == cleaner.id }
        cleanersMarkers[cleaner.id]?.remove() ?: throw AssertionError()
        cleanersMarkers.remove(cleaner.id)

    }

    private fun addCleaner(cleaner: Cleaner) {
        cleaners.add(cleaner)
        val marker = map.addMarker(MarkerOptions()
                .position(cleaner.location)
                .title(cleaner.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        marker.snippet = createSnippet(marker)
        marker.tag = CleanerTag(cleaner.name, createSnippet(marker), rating = cleaner.rating)
        cleanersMarkers.put(cleaner.id, marker)

    }

    private fun moveCleaner(cleaner: Cleaner) {
        cleaners.removeAll { it.id == cleaner.id }
        cleaners.add(cleaner)
        val marker1 = cleanersMarkers[cleaner.id]
        marker1?.apply {
            position = cleaner.location
            title = cleaner.name

            snippet = createSnippet(this)

            tag = CleanerTag(cleaner.name, "Mobile: ${cleaner.mobile}", rating = cleaner.rating)
            if (isInfoWindowShown) {
                hideInfoWindow()
                showInfoWindow()
            }
        }
    }

    private fun createSnippet(marker: Marker): String {
        val lastLocation = lastLocation ?: return ""
        val cleanerTag = marker.tag as? CleanerTag ?: return ""
        val distance: Int = calculateDistance(marker.position, LatLng(lastLocation.latitude, lastLocation.longitude)).toInt()
        return "${distance / 1000}km    ${cleanerTag.rating.toInt()}/5"
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Float {
        val result = floatArrayOf(0.0f)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, result)
        return result[0]
    }

    data class CleanerTag(val title: String,
                          val message: String,
                          val rating: Float)


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        drawerLayout.closeDrawers()

        selectNavigationItem(item)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onFragmentInteraction(uri: Uri) = Unit

    override fun onStop() {
        super.onStop()
        fused.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true

        checkLocationSetting()
        buildGoogleApiClient()
        if (checkLocationPermission()) {
            enableMyLocation()
        }

        //add markers on the map


        //adds cleaners markers from database to the map
        val cleanersListener = object : ChildEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError?) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot?, p1: String?) {
                val cleaner = Cleaner.newCleaner(dataSnapshot)
                moveCleaner(cleaner)
            }

            override fun onChildAdded(snapshot: DataSnapshot?, p1: String?) {
                val cleaner = Cleaner.newCleaner(snapshot)
                addCleaner(cleaner)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                val cleaner = Cleaner.newCleaner(dataSnapshot)
                removeCleaner(cleaner)
            }
        }
        cleanersReference.addChildEventListener(cleanersListener)

        map.setOnInfoWindowClickListener { marker: Marker? ->
            alert {
                val cleanerTag = marker?.tag as? CleanerTag ?: return@alert
                title = cleanerTag.title
                customView {
                    include<ConstraintLayout>(R.layout.custom_dialog)
//                    txtMobileNumber.text= cleaners[2].toString()
                }
                positiveButton("Request Cleaner") {

                    if (firebaseAuth.currentUser == null) {
                        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                                listOf(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                .build()
                        startActivityForResult(signInIntent, RC_SIGN_IN)
                    } else {
                        startActivity<OrdersActivity>()
                    }
                }
                negativeButton("Cancel") {}
            }.show()
        }
        map.setOnMarkerClickListener { marker ->
            marker.snippet = createSnippet(marker)
            false
        }
    }


    private fun checkLocationSetting() {
        val locationSettingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val task = LocationServices
                .getSettingsClient(this@TestingActivity)
                .checkLocationSettings(locationSettingsBuilder.build())
        task.addOnFailureListener {
            when ((task.exception as ApiException).statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED -> {
                    (task.exception as ResolvableApiException)
                            .startResolutionForResult(this@TestingActivity, TestingActivity.REQUEST_CODE_LOCATION_SETTINGS)
                }
            }
        }

    }

    @RequiresPermission(value = Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        fused.lastLocation.addOnSuccessListener {
            lastLocation = it ?: return@addOnSuccessListener
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation!!.latitude, lastLocation!!.longitude), 13f))
        }

        fused.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun servicesOk(): Boolean {

        val isAvailable: Int = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        when {
            isAvailable == ConnectionResult.SUCCESS -> return true
            GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable) -> {
                dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, isAvailable, TestingActivity.REQUEST_LOCATION_CODE)
                dialog.show()
            }
            else -> longToast("Can't connect to mapping services")
        }
        return false
    }

    @Synchronized private fun buildGoogleApiClient() {
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
            TestingActivity.REQUEST_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        buildGoogleApiClient()

                        enableMyLocation()
                    }
                } else {
                    longToast("something went wrong")
                }
                return
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    TestingActivity.REQUEST_LOCATION_CODE
            )
            false
        } else
            true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_LOCATION_SETTINGS -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }

            RC_SIGN_IN -> {
                if (resultCode == RESULT_OK) {
                    // TODO get the id of the current user and check if the user is already in the database
                    val uid = "uid"
                    customersReference.orderByChild("id").equalTo(uid).limitToFirst(1).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {
                            toast("Error: ${p0.toString()}")
                            info("Customer not found in database")
                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            info("snapshot: $p0")
                            for (snapshot in p0?.children ?: return) {
                                val customer = Customer.newCustomer(snapshot)
                                info("Customer signed in: $customer")
                            }
                        }
                    })
                } else {
                    toast("Sign in failed. Please try again")
                }
            }
        }
    }
}