package com.example.notebookpc.sparkcar.data

import com.google.android.gms.maps.model.LatLng


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
}