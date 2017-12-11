package com.example.notebookpc.sparkcar

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.v4.app.FragmentActivity
import com.example.notebookpc.sparkcar.data.Car
import com.example.notebookpc.sparkcar.data.Customer
import com.example.notebookpc.sparkcar.data.FavoriteLocation
import com.example.notebookpc.sparkcar.data.Id
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.AnkoLogger

internal object CustomerHolder : AnkoLogger {

    private val _customer = MutableLiveData<Customer?>()
    val customer: LiveData<Customer?> = _customer
    var isInProgress = false
    var latestRequest: Task<Void?> = Tasks.call { null }


    private var databaseReference: DatabaseReference? = null
    private val listener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            if (snapshot == null || snapshot.value == null) {
                _customer.value = null
            } else {
                _customer.value = Customer.newCustomer(snapshot)
            }
            isInProgress = false
        }
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                val uid = firebaseAuth.currentUser!!.uid
                databaseReference = FirebaseDatabase.getInstance().getReference("/customers/$uid")
                isInProgress = true
                databaseReference!!.addValueEventListener(listener)
            } else {
                databaseReference?.removeEventListener(listener)
                databaseReference = null
                _customer.value = null
            }
        }
    }

    fun signOut(activity: FragmentActivity) {
        AuthUI.getInstance().signOut(activity)
        _customer.value = null
    }

    fun updateCustomer(customer: Customer): Task<Void?> {
        if (this.customer.value != null) {
            assert(this.customer.value!!.id == customer.id)
        }
        return FirebaseDatabase.getInstance().getReference("/customers/" + customer.id).setValue(customer.toMap())
    }

    fun updateCustomer(customer: Customer, onComplete: (Task<Void?>) -> Unit) {
        val task = updateCustomer(customer)
        onComplete(task)
    }

    fun addFavoriteLocation(location: FavoriteLocation) {
        val id = customer.value!!.id
        val locationReference = FirebaseDatabase.getInstance().getReference("/customers/$id/favorite_locations")
        locationReference.push().setValue(location.toMap())
    }

    fun addFavoriteCleaner(cleanerId: Id): Task<Void?> {
        val uid = customer.value!!.id
        val locationReference = FirebaseDatabase.getInstance().getReference("/customers/$uid/favorite_cleaners")
        return locationReference.push().setValue(cleanerId)
    }

    fun addCar(car: Car): Task<Void> {
        val id = customer.value!!.id
        val carRefrence = FirebaseDatabase.getInstance().getReference("/customers/$id/cars_list")
        return carRefrence.push().setValue(car.toMap())
    }
}