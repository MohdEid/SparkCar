package com.example.notebookpc.sparkcarcommon.data

import com.google.firebase.database.DataSnapshot

data class Customer(
        val id: Id,
        val mobile: String,
        val name: String,
        val email: String,
        val favoriteLocations: List<FavoriteLocation>,
        val favoriteCleaners: List<Id>,
        val cars: List<Car>
) {
    companion object {
        fun newCustomer(dataSnapshot: DataSnapshot?): Customer {
            val snapshot = dataSnapshot ?: throw AssertionError("Null child added to database")
            val id = snapshot.key
            val name = snapshot.child("name").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val mobile = snapshot.child("mobile").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val email = snapshot.child("email").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val favoriteCleanersIterable = snapshot.child("favorite_cleaners").children ?: throw AssertionError("child not expected to be null")
            val favoriteLocationsIterable = snapshot.child("favorite_locations").children ?: throw AssertionError("child not expected to be null")
            val favoriteCleaners = mutableListOf<Id>()
            val favoriteLocations = favoriteLocationsIterable.map { FavoriteLocation.newLocation(it) }
            val carsSnapshot = snapshot.child("cars_list") ?: throw AssertionError("child is not expected to be null")
            val cars = carsSnapshot.children.map { Car.newCar(it) }
            for (cleanerSnapshot in favoriteCleanersIterable) {
                val cleanerId = cleanerSnapshot.getValue(Id::class.java) ?: throw AssertionError("child not expected to be null")

                favoriteCleaners.add(cleanerId)
            }

            return Customer(id = id, name = name, mobile = mobile, email = email, favoriteLocations = favoriteLocations, favoriteCleaners = favoriteCleaners, cars = cars)
        }
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map["name"] = name
        map["mobile"] = mobile
        map["email"] = email
        map["favorite_locations"] = favoriteLocations.map { it.toMap() }
        map["favorite_cleaners"] = favoriteCleaners
        map["cars_list"] = cars.map { it.toMap() }

        return map
    }
}