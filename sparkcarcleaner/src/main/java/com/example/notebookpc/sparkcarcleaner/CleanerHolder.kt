package com.example.notebookpc.sparkcarcleaner

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.v4.app.FragmentActivity
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.example.notebookpc.sparkcarcommon.data.Id
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.AnkoLogger


internal object CleanerHolder : AnkoLogger {
    private val cleanerLiveData = MutableLiveData<Cleaner?>()
    val cleaner: LiveData<Cleaner?> = cleanerLiveData

    private var databaseReference: DatabaseReference? = null
    private val listener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.value == null) {
                cleanerLiveData.value = null
            } else {
                cleanerLiveData.value = Cleaner.newCleaner(snapshot)
            }
        }
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                val uid = firebaseAuth.currentUser!!.uid
                databaseReference = FirebaseDatabase.getInstance().getReference("/cleaners/$uid")
                databaseReference!!.addValueEventListener(listener)
            } else {
                databaseReference?.removeEventListener(listener)
                databaseReference = null
                cleanerLiveData.value = null
            }
        }
    }

    internal fun updateCleaner(cleaner: Cleaner): Task<Void?> {
        if (this.cleaner.value != null) {
            assert(this.cleaner.value!!.id == cleaner.id)
        }
        return FirebaseDatabase.getInstance().getReference("/cleaners/" + cleaner.id).setValue(cleaner.toMap())
    }

    fun updateCleaner(cleaner: Cleaner, onComplete: (Task<Void?>) -> Unit) {
        val task = updateCleaner(cleaner)
        onComplete(task)
    }

    fun updateOrderStatus(orderId: Id, newStatus: String): Task<Void?> {
        return FirebaseDatabase.getInstance().getReference("orders/$orderId/status").setValue(newStatus)
    }

    fun signOut(activity: FragmentActivity) {
        AuthUI.getInstance().signOut(activity)
    }
}