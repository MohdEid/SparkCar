package com.example.notebookpc.sparkcar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.example.notebookpc.sparkcarcommon.data.Customer
import com.example.notebookpc.sparkcarcommon.data.Id
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
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
import java.io.Serializable

class MainActivity : AppCompatActivity(),
        AnkoLogger,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        MessagesFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        FavoriteCleanersFragment.OnFragmentInteractionListener,
        FavoriteLocationsFragment.OnFragmentInteractionListener,
        CarsFragment.OnFragmentInteractionListener,
        AboutFragment.OnFragmentInteractionListener,
        ShareFragment.OnFragmentInteractionListener,
        FragmentManager.OnBackStackChangedListener,
        OrdersListFragment.OnFragmentInteractionListener {

    companion object {
        val REQUEST_LOCATION_CODE = 99
        val RC_LOCATION_SETTINGS: Int = 100
        var RC_SIGN_IN: Int = 123
        private const val RC_SIGN_UP = 124

    }

    internal val messagesFragment = MessagesFragment.newInstance()
    internal val shareFragment = ShareFragment.newInstance()
    internal val profileFragment = ProfileFragment.newInstance()
    internal val carsFragment = CarsFragment.newInstance()
    internal val aboutFragment = AboutFragment.newInstance()
    internal val favoriteCleanersFragment: Fragment = FavoriteCleanersFragment.newInstance()
    internal val locationFragment = FavoriteLocationsFragment.newInstance()
    internal val ordersListFragment = OrdersListFragment.newInstance()
    internal val mapFragment = SupportMapFragment.newInstance()

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
        }
    }


    private lateinit var dialog: Dialog

    //Navigation Drawer Initialization
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView


    //Firebase Initialization
    private val cleanersReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("cleaners")
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val customersReference = FirebaseDatabase.getInstance().getReference("/customers")


    private val cleaners: MutableList<Cleaner> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        supportFragmentManager.addOnBackStackChangedListener(this)

        var observer: Observer<Customer?>? = null
        observer = Observer { customer ->
            if (customer == null && firebaseAuth.currentUser != null)
                CustomerHolder.signOut(this)
            CustomerHolder.customer.removeObserver(observer!!)
        }
        CustomerHolder.customer.observe(this, observer)


        //instantiating NavigationDrawer
        drawerLayout = findViewById(R.id.drawer_layout)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, null, R.string.drawer_opened, R.string.drawer_closed)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        //TODO need to fill Customer name and Mobile number in the Navigation Header
        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        fused = LocationServices.getFusedLocationProviderClient(this)

        //adds map fragment to main activity

        supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, mapFragment, "Home")
                .commit()
        mapFragment.getMapAsync(this)

        //checks if build version of sdk is recent
        if (servicesOk()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission()
            }
            buildGoogleApiClient()
        } else {
            Toast.makeText(this, "Map is not Connected", Toast.LENGTH_LONG).show()
        }

        //requests the location of user
        locationRequest = LocationRequest()

        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        CustomerHolder.customer.observe(this, Observer {
            updateSignOutItem()
        })
    }

    //updates the visibility of sign out in the menu when the user is signed in or not
    private fun updateSignOutItem() {
        val signOutItem = navigationView.menu.findItem(R.id.id_sign_out)
        val messagesItem = navigationView.menu.findItem(R.id.id_messages)
        val profileItem = navigationView.menu.findItem(R.id.id_profile)
        val locationItem = navigationView.menu.findItem(R.id.id_location)
        val favoriteCleanerItem = navigationView.menu.findItem(R.id.id_favorite_cleaner)
        val favoriteCarsItem = navigationView.menu.findItem(R.id.id_car)
        val signInItem = navigationView.menu.findItem(R.id.id_sign_in)



        if (CustomerHolder.customer.value != null) {
            signOutItem.isVisible = true
            messagesItem.isVisible = true
            profileItem.isVisible = true
            favoriteCleanerItem.isVisible = true
            locationItem.isVisible = true
            favoriteCarsItem.isVisible = true
            signInItem.isVisible = false
            this.invalidateOptionsMenu()
        } else {
            signOutItem.isVisible = false
            messagesItem.isVisible = false
            profileItem.isVisible = false
            favoriteCleanerItem.isVisible = false
            locationItem.isVisible = false
            favoriteCarsItem.isVisible = false
            signInItem.isVisible = true
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

    //adds cleaner marker to the map
    private fun addCleaner(cleaner: Cleaner) {
        cleaners.add(cleaner)
        val marker = map.addMarker(MarkerOptions()
                .position(cleaner.location)
                .title(cleaner.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        marker.snippet = createSnippet(marker)
        marker.tag = CleanerTag(title = cleaner.name, message = createSnippet(marker), rating = cleaner.rating,
                mobile = cleaner.mobile, isAvailable = cleaner.isAvailable, id = cleaner.id)
        cleanersMarkers.put(cleaner.id, marker)

    }

    /**
     * This function tracks the location of cleaner when they move*/
    private fun moveCleaner(cleaner: Cleaner) {
        cleaners.removeAll { it.id == cleaner.id }
        cleaners.add(cleaner)
        val marker1 = cleanersMarkers[cleaner.id]
        marker1?.apply {
            position = cleaner.location
            title = cleaner.name

            snippet = createSnippet(this)

            tag = CleanerTag(cleaner.name, message = "Mobile: ${cleaner.mobile}",
                    rating = cleaner.rating, isAvailable = cleaner.isAvailable, mobile = cleaner.mobile, id = cleaner.id)
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
                          val id: Id,
                          val message: String,
                          val mobile: String,
                          val rating: Float,
                          val isAvailable: Boolean) : Serializable

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        drawerLayout.closeDrawers()

        selectNavigationItem(item)
        return true
    }

    override fun onBackStackChanged() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_container)
        when (currentFragment) {
            mapFragment -> {
                supportActionBar!!.title = "SparkCar"
                drawerLayout.dispatchSetSelected(false)
            }
            messagesFragment -> {
                supportActionBar!!.title = "Messages Page"
            }
            profileFragment -> {
                supportActionBar!!.title = "Profile Page"
            }
            favoriteCleanersFragment -> {
                supportActionBar!!.title = "Favorite Cleaner Page"
            }
            locationFragment -> {
                supportActionBar!!.title = "Location Page"
            }
            carsFragment -> {
                supportActionBar!!.title = "Cars Page"
            }
            aboutFragment -> {
                supportActionBar!!.title = "About Page"
            }
            shareFragment -> {
                supportActionBar!!.title = "Share Page"
            }
        }
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

        checkLocationSetting(RC_LOCATION_SETTINGS)

        buildGoogleApiClient()
        if (checkLocationPermission()) {
            enableMyLocation()
        }

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

        //sets listener on the window of marker of a cleaner
        map.setOnInfoWindowClickListener { marker: Marker? ->
            alert {
                val cleanerTag = marker?.tag as? CleanerTag ?: return@alert
                title = cleanerTag.title
                customView {
                    val view = include<View>(R.layout.custom_dialog)

                    val mobileTextView = view.find<TextView>(R.id.txtMobile)
                    mobileTextView.text = getString(R.string.display_mobile_number, cleanerTag.mobile)

                    val isAvailableTextView = view.find<TextView>(R.id.txtActive)
                    isAvailableTextView.text = if (cleanerTag.isAvailable) "Available" else "Not available"

                    val ratingBar = view.find<RatingBar>(R.id.ratingBar)
                    ratingBar.rating = cleanerTag.rating
                }
                positiveButton("Request Cleaner") {

                    //verifies if there is a user signed in or not
                    if (firebaseAuth.currentUser == null) {
                        //starts an intent to sign in or sign up
                        val signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                                listOf(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                .build()
                        startActivityForResult(signInIntent, RC_SIGN_IN)
                    } else {
                        //loads the activity when the user is present
                        startActivity<ConfirmOrderActivity>("cleaner" to cleanerTag)
                    }
                }
                negativeButton("Cancel") {}
                neutralPressed("favorite") {
                    val task = CustomerHolder.addFavoriteCleaner(cleanerTag.id)
                    task.addOnCompleteListener {
                        toast("Added successfully")
                    }
                }
            }.show()
        }
        map.setOnMarkerClickListener { marker ->
            marker.snippet = createSnippet(marker)
            false
        }
    }


    //validates if Location Services is enabled in the device or not


    //shows your current location
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

    //checks if the device has recent google play services or not
    private fun servicesOk(): Boolean {

        val isAvailable: Int = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        when {
            isAvailable == ConnectionResult.SUCCESS -> return true
            GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable) -> {
                dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, isAvailable, MainActivity.REQUEST_LOCATION_CODE)
                dialog.show()
            }
            else -> longToast("Can't connect to mapping services")
        }
        return false
    }

    //instantiate google map
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
        //checks if permissions granted to access GPS or not
        when (requestCode) {
            MainActivity.REQUEST_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        buildGoogleApiClient()
                        enableMyLocation()
                    }
                } else {
                    longToast("Something went wrong")
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
                    MainActivity.REQUEST_LOCATION_CODE
            )
            false
        } else
            true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val providers = FirebaseAuth.getInstance().currentUser?.providers
        info { "Providers :" + providers }
        when (requestCode) {
            RC_LOCATION_SETTINGS -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
            RC_SIGN_IN -> {

                if (resultCode == Activity.RESULT_OK) {
                    toast("Log in successful")

                    if (!firebaseAuth.currentUser!!.isEmailVerified) {
                        firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                val snackbar = Snackbar.make(drawerLayout, "Verify your email then sign in again", Snackbar.LENGTH_INDEFINITE)
                                snackbar.setAction("Dismiss") {
                                    snackbar.dismiss()
                                }
                                snackbar.show()
                                CustomerHolder.signOut(this)
                            }
                        }
                    } else {
                        val uid = firebaseAuth.currentUser?.uid ?: throw AssertionError()
                        customersReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {
                                toast("There is an error")
                            }

                            override fun onDataChange(snapshot: DataSnapshot?) {
                                if (snapshot == null || snapshot.value == null) {
                                    startActivityForResult<SignUpActivity>(RC_SIGN_UP, "id" to uid)
                                }
                            }
                        })
                    }
                } else {
                    toast("Log in failed")
                }
            }

            RC_SIGN_UP -> {
                if (resultCode == Activity.RESULT_OK) {
                    longToast("Thank you for signing up")
                } else {
                    longToast("Sign up failed")
                    CustomerHolder.signOut(this)
                }
            }

            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}