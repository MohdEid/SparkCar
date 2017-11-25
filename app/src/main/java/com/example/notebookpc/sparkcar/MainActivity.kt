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
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
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

class MainActivity : AppCompatActivity(),
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
    private lateinit var lastLocation: Location
    private var currentLocationMarker: Marker? = null
    private val cleanersMarkers = mutableMapOf<Id, Marker>()

    private lateinit var fused: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
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
            debug("current location: $latLng")
        }
    }


    private lateinit var dialog: Dialog

    //Navigation Drawer Initialization
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolBar: Toolbar
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView


    //fragments Initializing
    private val messagesFragment = MessagesFragment.newInstance()
    private val settingsFragment = SettingsFragment.newInstance()
    private val shareFragment = ShareFragment.newInstance()
    private val profileFragment = ProfileFragment.newInstance()
    private val carsFragment = CarsFragment.newInstance()
    private val aboutFragment = AboutFragment.newInstance()
    private val favoritesFragment = FavoritesFragment.newInstance()
    private val locationFragment = LocationFragment.newInstance()

    //    private val item: MenuItem = (R.id.id_sign_out) as MenuItem

    //Firebase Initialization
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().reference.child("cleaners")

    private val cleaners: MutableList<Cleaner> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        //TODO fix visibility of sign out in the menu
//        if (client.isConnected) {
//
//            item.isVisible = false
//            this.invalidateOptionsMenu()
//        } else {
//            item.isVisible = true
//            this.invalidateOptionsMenu()
//        }

        //instantiating NavigationDrawer
        drawerLayout = findViewById(R.id.drawer_layout)
        toolBar = findViewById(R.id.toolbar)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.drawer_opened, R.string.drawer_closed)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

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
                .snippet("Mobile: ${cleaner.mobile} \nRating: ${cleaner.rating}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        marker.tag = CleanerTag(cleaner.name, "Mobile: ${cleaner.mobile}", rating = cleaner.rating)
        cleanersMarkers.put(cleaner.id, marker)

    }

    private fun moveCleaner(cleaner: Cleaner) {
        cleaners.removeAll { it.id == cleaner.id }
        cleaners.add(cleaner)
        val marker1 = cleanersMarkers[cleaner.id]
        marker1?.apply {
            position = cleaner.location
            title = cleaner.name

            //TODO replace Mobile number with distance, and Rating to numerical
            snippet = "Mobile: ${cleaner.mobile} \nRating: ${cleaner.rating}"

            tag = CleanerTag(cleaner.name, "Mobile: ${cleaner.mobile}", rating = cleaner.rating)
            if (isInfoWindowShown) {
                hideInfoWindow()
                showInfoWindow()
            }
        }
    }


    //calling variables of the cleaners database
    private data class Cleaner(
            val id: Id = "",
            val location: LatLng,
            val mobile: String = "",
            val name: String = "",
            val rating: Float = 0.0f
    ) {
        companion object {
            fun newCleaner(dataSnapshot: DataSnapshot?): Cleaner {
                val snapshot = dataSnapshot ?: throw AssertionError("Null child added to database")
                val id = snapshot.child("id").getValue(Id::class.java) ?: throw AssertionError("child not expected to be null")
                val lat = snapshot.child("location/lat").getValue(Double::class.java) ?: throw AssertionError("child not expected to be null")
                val lon = snapshot.child("location/lon").getValue(Double::class.java) ?: throw AssertionError("child not expected to be null")
                val location = LatLng(lat, lon)
                val name = snapshot.child("name").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
                val mobile = snapshot.child("mobile").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
                val rating = snapshot.child("rating").getValue(Float::class.java) ?: throw AssertionError("child not expected to be null")

                return Cleaner(id = id, name = name, location = location, mobile = mobile, rating = rating)
            }
        }
    }

    data class CleanerTag(val title: String,
                          val message: String,
                          val rating: Float)

    //TODO check if its possible to move this to different class, to be called again
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        drawerLayout.closeDrawers()

        when (item.itemId) {
        //sends back to main activity
            R.id.id_home -> {
                startActivity<TestingActivity>()
            }
            R.id.id_messagse -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, messagesFragment)
                        .commit()
                supportActionBar!!.title = "Messages Page"

            }
            R.id.id_settings -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, settingsFragment)
                        .commit()
                supportActionBar!!.title = "Settings Page"
            }
            R.id.id_profile -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, profileFragment)
                        .commit()
                supportActionBar!!.title = "Profile Page"
            }
            R.id.id_favorite_cleaner -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, favoritesFragment)
                        .commit()
                supportActionBar!!.title = "Favorite Cleaner Page"
            }
            R.id.id_location -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, locationFragment)
                        .commit()
                supportActionBar!!.title = "Location Page"
            }
            R.id.id_car -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, carsFragment)
                        .commit()
                supportActionBar!!.title = "Cars Page"
            }
            R.id.id_about -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, aboutFragment)
                        .commit()
                supportActionBar!!.title = "About Page"
            }
            R.id.id_share -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, shareFragment)
                        .commit()
                supportActionBar!!.title = "Share Page"
            }
        //TODO need to make this visible only if the user is signed in
        //sign out
            R.id.id_sign_out -> {
                AuthUI.getInstance().signOut(this)
                longToast("Logging out successfully")
            }
        }
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

        //TODO add custom marker and save it to database
        /*val options = MarkerOptions()
                .title("Custom Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .position( LatLng())
                        map.addMarker()*/

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
        ref.addChildEventListener(cleanersListener)

        map.setOnInfoWindowClickListener {
            alert {
                //                val dataSnapShot:DataSnapshot?
//                val cleaner =Cleaner.newCleaner(dataSnapShot)
                //TODO add cleaner name to the title
                title = "This is a cleaner"
                customView {
                    include<ConstraintLayout>(R.layout.custom_dialog)
//                    txtMobileNumber.text= cleaners[2].toString()
                }
                positiveButton("Request Cleaner") {

                    val firebaseAuth = FirebaseAuth.getInstance()
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
    }


    private fun checkLocationSetting() {
        val locationSettingsBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val task = LocationServices
                .getSettingsClient(this@MainActivity)
                .checkLocationSettings(locationSettingsBuilder.build())
        task.addOnFailureListener {
            when ((task.exception as ApiException).statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED -> {
                    (task.exception as ResolvableApiException)
                            .startResolutionForResult(this@MainActivity, TestingActivity.REQUEST_CODE_LOCATION_SETTINGS)
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
            TestingActivity.REQUEST_CODE_LOCATION_SETTINGS -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
        }
    }
}