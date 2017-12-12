package com.example.notebookpc.sparkcar.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot


data class FavoriteLocation(
        val name: String,
        val location: LatLng
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map["name"] = name
        map["lat"] = location.latitude
        map["lon"] = location.longitude

        return map
    }

    companion object {
        fun newLocation(locationSnapshot: DataSnapshot): FavoriteLocation {
            val lat = locationSnapshot.child("lat").getValue(Double::class.java) ?: throw AssertionError("child not expected to be null")
            val lon = locationSnapshot.child("lon").getValue(Double::class.java) ?: throw AssertionError("child not expected to be null")
            val locationName = locationSnapshot.child("name").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            return FavoriteLocation(name = locationName, location = LatLng(lat, lon))
        }
    }
}