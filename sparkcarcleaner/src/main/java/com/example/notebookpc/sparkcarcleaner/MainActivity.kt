package com.example.notebookpc.sparkcarcleaner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.notebookpc.sparkcarcommon.data.Car
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.example.notebookpc.sparkcarcommon.data.Id
import com.example.notebookpc.sparkcarcommon.data.Orders
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
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener,
        ProfileFragment.OnFragmentInteractionListener,
        AboutFragment.OnFragmentInteractionListener,
        ShareFragment.OnFragmentInteractionListener,
        MessagesFragment.OnFragmentInteractionListener,
        PendingOrderFragment.OnFragmentInteractionListener {


    companion object {
        private val LOG_TAG: String = MainActivity::class.java.simpleName
        val REQUEST_LOCATION_CODE = 101
        val RC_LOCATION_SETTINGS: Int = 102
        var RC_SIGN_IN: Int = 125
        private const val RC_SIGN_UP = 126
    }

    internal val messagesFragment = MessagesFragment.newInstance()
    internal val shareFragment = ShareFragment.newInstance()
    internal val profileFragment = ProfileFragment.newInstance()
    internal val aboutFragment = AboutFragment.newInstance()
    internal val pendingFragment = PendingOrderFragment.newInstance()

    internal val mapFragment = SupportMapFragment()

    private lateinit var dialog: Dialog

    //Navigation Drawer Initialization
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView


    private lateinit var map: GoogleMap
    private val cleanersReference = FirebaseDatabase.getInstance().getReference("/cleaners")
    private val ordersReference = FirebaseDatabase.getInstance().getReference("/orders")
    private val orders: MutableList<Orders> = mutableListOf()
    private val ordersMarkers = mutableMapOf<Id, Marker>()
    private var currentLocationMarker: Marker? = null
    private lateinit var client: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private lateinit var fused: FusedLocationProviderClient
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult?) {
            super.onLocationResult(location)

            currentLocationMarker?.remove()

            val lastLocation = lastLocation ?: return
            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)

            val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("home")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            currentLocationMarker = map.addMarker(markerOptions)
            val currentCleaner = CleanerHolder.cleaner.value
            if (currentCleaner != null) {
                CleanerHolder.updateCleaner(currentCleaner.copy(location = latLng))
            }
        }
    }

    private val firebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)


        supportFragmentManager.beginTransaction().
                replace(R.id.main_container, mapFragment)
                .commit()
        mapFragment.getMapAsync(this)


        var observer: Observer<Cleaner?>? = null
        observer = Observer { cleaner ->
            if (cleaner == null && firebaseAuth.currentUser != null)
                CleanerHolder.signOut(this)
            CleanerHolder.cleaner.removeObserver(observer!!)
        }
        CleanerHolder.cleaner.observe(this, observer)

        //instantiating NavigationDrawer
        drawerLayout = findViewById(R.id.drawer_layout)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, null, R.string.drawer_opened, R.string.drawer_closed)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        //TODO need to fill Customer name and Mobile number in the Navigation Header
        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)


        fused = LocationServices.getFusedLocationProviderClient(this)

        //requests the location of user
        locationRequest = LocationRequest()

        locationRequest.interval = 5000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (servicesOk()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission()
            }
            buildGoogleApiClient()
        } else {
            Toast.makeText(this, "Map is not Connected", Toast.LENGTH_LONG).show()
        }

        firebaseAuth.addAuthStateListener {
            updateSignOutItem()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                //adds cleaners markers from database to the map
                val ordersListener = object : ChildEventListener {
                    override fun onCancelled(dataSnapshot: DatabaseError?) {
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot?, p1: String?) {
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                        val orders = Orders.newOrder(dataSnapshot)
                        moveOrder(orders)
                    }

                    override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                        Log.d(LOG_TAG, "order snapshot: $dataSnapshot")
                        val orders = Orders.newOrder(dataSnapshot)
                        addOrder(orders)
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val orders = Orders.newOrder(dataSnapshot)
                        removeOrder(orders)
                    }
                }
                ordersReference.orderByChild("cleaner_id").equalTo(currentUser.uid).addChildEventListener(ordersListener)
            }
        }
    }

    private fun updateSignOutItem() {
        val signOutItem = navigationView.menu.findItem(R.id.id_sign_out)
        val messagesItem = navigationView.menu.findItem(R.id.id_messages)
        val profileItem = navigationView.menu.findItem(R.id.id_profile)
        val pendingItem = navigationView.menu.findItem(R.id.pending_orders)
        val signInItem = navigationView.menu.findItem(R.id.id_sign_in)

        if (firebaseAuth.currentUser != null) {
            signOutItem.isVisible = true
            messagesItem.isVisible = true
            profileItem.isVisible = true
            pendingItem.isVisible = true
            signInItem.isVisible = false
            this.invalidateOptionsMenu()
        } else {
            signInItem.isVisible = true
            signOutItem.isVisible = false
            messagesItem.isVisible = false
            profileItem.isVisible = false
            pendingItem.isVisible = false
            this.invalidateOptionsMenu()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        drawerLayout.closeDrawers()

        selectNavigationItem(item)
        return true
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(value = Manifest.permission.ACCESS_FINE_LOCATION)
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        fused.lastLocation.addOnSuccessListener {
            lastLocation = it ?: return@addOnSuccessListener
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation!!.latitude, lastLocation!!.longitude), 13f))
        }

        fused.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    /**
     * Removes cleaner by id
     */
    private fun removeOrder(order: Orders) {
        orders.removeAll { it.orderId == order.orderId }
        ordersMarkers[order.orderId]?.remove() ?: throw AssertionError()
        ordersMarkers.remove(order.orderId)
    }

    private fun createSnippet(marker: Marker): String {
        val orderTag = marker.tag as? OrderTag ?: return ""
        return " " + orderTag.id
    }

    /**
     * This function tracks the location of order when they move*/
    private fun moveOrder(order: Orders) {
        orders.removeAll { it.orderId == order.orderId }
        orders.add(order)
        val marker1 = ordersMarkers[order.orderId]
        marker1?.apply {
            position = order.location
            title = order.orderId

            snippet = createSnippet(this)

            val id = order.orderId ?: throw IllegalStateException()
            tag = OrderTag(message = "OrderId: ${order.orderId}",
                    status = order.status, id = id, car = order.car, customerId = order.customerId, title = order.cleanerId)
            if (isInfoWindowShown) {
                hideInfoWindow()
                showInfoWindow()
            }
        }
    }

    data class OrderTag(val title: Id,
                        val id: Id,
                        val car: Car,
                        val message: String,
                        val customerId: Id,
                        val status: String) : Serializable

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
        map.uiSettings.isZoomControlsEnabled = true

        //sets listener on the window of marker of a cleaner
        map.setOnInfoWindowClickListener { marker: Marker? ->
            alert {
                val orderTag = marker?.tag as? OrderTag ?: return@alert

                customView {
                    val view = include<View>(R.layout.custom_dialog)


                    val isAvailableTextView = view.find<TextView>(R.id.txtActive)
                    isAvailableTextView.text = if (orderTag.status == "Finished") "Finished" else "Not Finished"

                }
                positiveButton("Confirm Order") {

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
                        startActivity<CustomerOrderActivity>("order" to orderTag)
                        CleanerHolder.updateOrderStatus(orderTag.id, Orders.STATUS_INPROGRESS)
                    }
                }
                negativeButton("Cancel") {}

            }.show()
        }

    }

    //adds order marker to the map
    private fun addOrder(order: Orders) {
        orders.add(order)
        val marker = map.addMarker(MarkerOptions()
                .position(order.location)
                .title(order.orderId)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
        marker.snippet = createSnippet(marker)
        val title = order.customerId
        marker.tag = OrderTag(title = title, message = createSnippet(marker), car = order.car,
                customerId = order.customerId, status = order.status, id = order.orderId!!)
        ordersMarkers.put(title, marker)

    }

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

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
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

    //instantiate google map
    @Synchronized private fun buildGoogleApiClient() {
        client = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        client.connect()
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

        when (requestCode) {
            RC_LOCATION_SETTINGS -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
            RC_SIGN_IN -> {

                if (resultCode == Activity.RESULT_OK) {
                    toast("Log in successful")

                    val uid = firebaseAuth.currentUser?.uid ?: throw AssertionError()
                    cleanersReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {
                            toast("There is an error")
                        }

                        override fun onDataChange(snapshot: DataSnapshot?) {
                            if (snapshot == null || snapshot.value == null) {
                                startActivityForResult<SignUpActivity>(RC_SIGN_UP, "id" to uid)
                            }
                        }
                    })
                } else {
                    toast("Log in failed")
                }
            }

            RC_SIGN_UP -> {
                if (resultCode == Activity.RESULT_OK) {
                    longToast("Thank you for signing up")
                } else {
                    longToast("Sign up failed")
                    CleanerHolder.signOut(this)
                }
            }

            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
