package com.example.notebookpc.sparkcar

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.widget.Toast
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

class MainActivity : FragmentActivity(),OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private lateinit var map:GoogleMap
    private lateinit var client:GoogleApiClient
    private lateinit var locationReuqest:LocationRequest
    private lateinit var lastLocation: Location
    private lateinit var currentLocation:Marker

    private lateinit var geoDataClient:LocationServices

    private lateinit var dialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

       /* if(servicesOk()) {
            setContentView(R.layout.activity_maps)
            if (initMap()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkLocationPermission()
                }
                buildGoogleApiClient()
            }
            else{
                Toast.makeText(this,"Map is not Connected",Toast.LENGTH_LONG).show()
            }
        }
        else{
            setContentView(R.layout.activity_main)
        }*/
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {


        map =googleMap
        buildGoogleApiClient()
        map.isMyLocationEnabled = true
    }

    fun servicesOk():Boolean{

        var isAvailable:Int

        isAvailable=GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (isAvailable== ConnectionResult.SUCCESS){
            return true
        }else if (GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable)){
            dialog=GoogleApiAvailability.getInstance().getErrorDialog(this,isAvailable, REQUEST_LOCATION_CODE)
        dialog.show()
        }
        else{
            Toast.makeText(this,"Can't connect to mapping services",Toast.LENGTH_LONG).show()
        }
        return false
    }

    @SuppressLint("MissingPermission")
    @Synchronized protected fun buildGoogleApiClient(){
        client = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener (this)
                .addApi(LocationServices.API)
                .build()
        client.connect()
        map.isMyLocationEnabled = true
    }

    fun initMap():Boolean{
        if(map == null){
            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
        return (map != null)
    }


    override fun onConnected(bundle: Bundle?) {
        locationReuqest = LocationRequest()

        locationReuqest.interval = 1000
        locationReuqest.fastestInterval = 1000
        locationReuqest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY


            //LocationServices.FusedLocationApi.removeLocationUpdates(client, locationReuqest, this)
            val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.removeLocationUpdates(LocationCallback())

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location

        if(currentLocation !=null){
            currentLocation.remove()
        }

        val latLng = LatLng(location.latitude,location.longitude)

        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("home")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        currentLocation= map.addMarker(markerOptions)
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        map.animateCamera(CameraUpdateFactory.zoomBy(10f))

        LocationServices.FusedLocationApi.removeLocationUpdates(client,this)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode){
            REQUEST_LOCATION_CODE ->{
                if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                        if(client !=null){
                            buildGoogleApiClient()
                        }
                        map.isMyLocationEnabled=true
                    }
                }else
                {
                    Toast.makeText(this,"something went wrong", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }
    fun checkLocationPermission():Boolean{
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_LOCATION_CODE)
            }else
            {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),REQUEST_LOCATION_CODE)
            }
            return false
        }else
            return true
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    companion object {
        val REQUEST_LOCATION_CODE = 99
    }
}


private fun FusedLocationProviderApi.removeLocationUpdates(client: GoogleApiClient, mainActivity: MainActivity) {}

private fun FusedLocationProviderApi.removeLocationUpdates(client: GoogleApiClient?, locationReuqest: LocationRequest, mainActivity: MainActivity) {}
