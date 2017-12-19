package com.example.notebookpc.sparkcarcommon.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

data class Orders(
        val orderId: Id?,
        val cleanerId: Id,
        val customerId: Id,
        val location: LatLng,
        val car: Car,
        val status: String,
        val pictureUrl: String? = null,
        val date: DateTime = DateTime(DateTimeZone.getDefault())
) {
    companion object {

        val STATUS_COMPLETE = "Completed"
        val STATUS_INCOMPLETE = "Not Finished"
        val STATUS_INPROGRESS = "In-Progress"

        fun newOrder(orderSnapshot: DataSnapshot): Orders {

            val orderId = orderSnapshot.key
            val cleanerId = orderSnapshot.child("cleaner_id").getValue(Id::class.java) ?: throw IllegalStateException()
            val customerId = orderSnapshot.child("customer_id").getValue(Id::class.java) ?: throw IllegalStateException()
            val locationSnapshot = orderSnapshot.child("location") ?: throw IllegalStateException()
            val lat = locationSnapshot.child("lat").getValue(Double::class.java) ?: throw IllegalStateException()
            val lon = locationSnapshot.child("lon").getValue(Double::class.java) ?: throw IllegalStateException()
            val status = orderSnapshot.child("status").getValue(String::class.java) ?: throw IllegalStateException()
            val location = LatLng(lat, lon)
            val car = Car.newCar(orderSnapshot.child("car"))
            val dateString = orderSnapshot.child("order_date").getValue(String::class.java) ?: throw IllegalStateException()
            val pictureUrl = orderSnapshot.child("picture_url").getValue(String::class.java)
            val date = DateTime.parse(dateString).withZone(DateTimeZone.getDefault())

            return Orders(orderId, cleanerId, customerId, location, car, status, pictureUrl, date)
        }
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map["cleaner_id"] = cleanerId
        map["customer_id"] = customerId
        map["location"] = mapOf("lat" to location.latitude, "lon" to location.longitude)
        map["car"] = car.toMap()
        map["status"] = status
        map["order_date"] = date.withZone(DateTimeZone.UTC).toString()
        if (pictureUrl != null) {
            map["picture_url"] = pictureUrl
        }
        return map
    }
}