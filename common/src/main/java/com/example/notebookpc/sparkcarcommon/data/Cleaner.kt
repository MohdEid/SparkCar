package com.example.notebookpc.sparkcarcommon.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot

data class Cleaner(
        val id: Id,
        val location: LatLng,
        val mobile: String,
        val name: String,
        val email: String,
        val rating: Float = 0.0f,
        val isAvailable: Boolean
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
            val email = snapshot.child("email").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val available = snapshot.child("is_available").getValue(Boolean::class.java) ?: false

            return Cleaner(id = id, name = name, location = location, mobile = mobile, rating = rating, email = email, isAvailable = available)
        }
    }
}