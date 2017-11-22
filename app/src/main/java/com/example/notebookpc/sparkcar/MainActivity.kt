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
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class MainActivity : AppCompatActivity(),
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
        private val REQUEST_LOCATION_CODE = 99
        private val REQUEST_CODE_LOCATION_SETTINGS: Int = 100
    }

    //GoogleMaps Initialization
    private lateinit var map: GoogleMap
    private lateinit var client: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var lastLocation: Location
    private var currentLocationMarker: Marker? = null
    private var cleanerLocationMarker: Marker? = null

    private lateinit var fused: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback


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

    private val cleaners: MutableList<Cleaners> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        initCleanersList()

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

    private fun initCleanersList() {
        val cleanersListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot!!.children.mapNotNullTo(cleaners) {
                    it.getValue<Cleaners>(Cleaners::class.java)

                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(location: LocationResult?) {
                            super.onLocationResult(location)
                            lastLocation = location?.lastLocation ?: return

                            currentLocationMarker?.remove()

                            val latLng = LatLng(cleaners[2].location.latitude, cleaners[2].location.longitude)

                            val markerOptions = MarkerOptions()
                            markerOptions.position(latLng)
                            markerOptions.title(cleaners[1].id)
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                            cleanerLocationMarker = map.addMarker(markerOptions)

                        }
                    }
                    return
                }
            }
        }
    }

    //calling variables of the cleaners database
    private data class Cleaners(
            val id: String = "",
            val location: Location,
            val mobile: String = "",
            val name: String = "",
            val Rating: String = ""
    )

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    //TODO need to make sure the user is signed in before showing the menu option
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.id_sign_out ->{
                signOutButton.setOnClickListener {
                    AuthUI.getInstance().signOut(this)
                    Toast.makeText(this,"Log-out succefully",Toast.LENGTH_LONG).show()
                }
            }
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        drawerLayout.closeDrawers()

        when (item.itemId) {
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
                supportActionBar!!.title = "Favorite Cleaners Page"
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
            R.id.id_sign_out -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, shareFragment)
                        .commit()
                supportActionBar!!.title = "Share Page"
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
            else -> Toast.makeText(this, "Can't connect to mapping services", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this, "something went wrong", Toast.LENGTH_LONG).show()
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