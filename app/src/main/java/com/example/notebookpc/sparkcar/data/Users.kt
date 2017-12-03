package com.example.notebookpc.sparkcar.data

import com.google.firebase.database.DataSnapshot

/**
 * Created by Mahdi on 2017-12-03.
 */
public class Users(val id: String,
                   val name: String,
                   val mobile: String,
                   val email: String) {
    companion object {
        fun fromSnapshot(snapshot: DataSnapshot): Users? {
            val id = snapshot.key
            val name = snapshot.child("name").getValue(String::class.java) ?: return null
            val mobile = snapshot.child("mobile").getValue(String::class.java) ?: return null
            val email = snapshot.child("email").getValue(String::class.java) ?: return null

            return Users(id, name, mobile, email)
        }
    }

    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map["name"] = name
        map["mobile"] = mobile
        map["email"] = email

        return map
    }
}