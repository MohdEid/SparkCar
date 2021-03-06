package com.example.notebookpc.sparkcarcommon.data

import com.google.firebase.database.DataSnapshot
import java.io.Serializable


data class Car(
        val id: Id?,
        val name: String,
        val carPlate: String,
        val color: String) : Serializable {

    companion object {
        fun newCar(snapshot: DataSnapshot): Car {
            val id = snapshot.key
            val name = snapshot.child("name").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val plate = snapshot.child("car_plate").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val color = snapshot.child("color").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            return Car(id, name, plate, color)
        }
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map["name"] = name
        map["car_plate"] = carPlate
        map["color"] = color

        return map
    }
}

   
