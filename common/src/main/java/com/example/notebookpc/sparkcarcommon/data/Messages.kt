package com.example.notebookpc.sparkcarcommon.data

import com.google.firebase.database.DataSnapshot
import org.joda.time.DateTime
import org.joda.time.DateTimeZone


data class Messages(
        val id: Id?,
        val title: String,
        val customerId: Id,
        val cleanerId: Id,
        val content: String,
        val date: DateTime = DateTime(DateTimeZone.getDefault())) {
    companion object {
        fun newMessage(dataSnapshot: DataSnapshot?): Messages {
            val snapshot = dataSnapshot ?: throw AssertionError("Null child added to database")
            val id = snapshot.key ?: throw AssertionError("child not expected to be null")
            val title = snapshot.child("title").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val customerId = snapshot.child("customer_id").getValue(Id::class.java) ?: throw AssertionError("child not expected to be null")
            val cleanerId = snapshot.child("cleaner_id").getValue(Id::class.java) ?: throw AssertionError("child not expected to be null")
            val content = snapshot.child("content").getValue(String::class.java) ?: throw AssertionError("child not expected to be null")
            val dateString = dataSnapshot.child("message_date").getValue(String::class.java) ?: throw IllegalStateException()
            val date = DateTime.parse(dateString).withZone(DateTimeZone.getDefault())
            return Messages(id = id, title = title, customerId = customerId, cleanerId = cleanerId, content = content, date = date)
        }
    }


    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()

        map["title"] = title
        map["customer_id"] = customerId
        map["cleaner_id"] = cleanerId
        map["content"] = content
        map["message_date"] = date.withZone(DateTimeZone.UTC).toString()
        return map
    }

}