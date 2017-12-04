package com.example.notebookpc.sparkcar.data

import com.google.android.gms.maps.model.LatLng

/**
 * Created by Mahdi on 2017-12-04.
 */
data class FavoriteLocation(
        val name: String,
        val location: LatLng
)