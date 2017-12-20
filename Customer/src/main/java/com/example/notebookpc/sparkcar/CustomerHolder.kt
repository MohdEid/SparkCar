package com.example.notebookpc.sparkcar

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.example.notebookpc.sparkcarcommon.data.*
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

internal object CustomerHolder {

    private val LOG_TAG: String? = CustomerHolder::class.java.simpleName

    private val _customer = MutableLiveData<Customer?>()
    val customer: LiveData<Customer?> = _customer

    //orders live Data
    private val ordersList = mutableListOf<Orders>()
    private val ordersLiveData = MutableLiveData<List<Orders>>()
    val orders: LiveData<List<Orders>> = ordersLiveData

    //messages live Data
    private val messagesList = mutableListOf<Messages>()
    private val messagesLiveData = MutableLiveData<List<Messages>>()
    val messages: LiveData<List<Messages>> = messagesLiveData

    //Database references
    private var ordersDatabaseReference: Query? = null
    private var databaseReference: DatabaseReference? = null
    private var messagesDatabaseReference: Query? = null

    //Listeners
    private val customerListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
        }

        override fun onDataChange(snapshot: DataSnapshot?) {
            if (snapshot == null || snapshot.value == null) {
                _customer.value = null
            } else {
                _customer.value = Customer.newCustomer(snapshot)
            }
        }
    }
    private val ordersListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError?) {

        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {

        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            val snapshot = p0 ?: return
            val newOrder = Orders.newOrder(snapshot)
            ordersList.removeAll { it.orderId == newOrder.orderId }
            ordersList.add(newOrder)
            ordersLiveData.value = ordersList
        }

        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            val snapshot = p0 ?: return
            val order = Orders.newOrder(snapshot)
            ordersList.add(order)
            ordersLiveData.value = ordersList
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
            val snapshot = p0 ?: return
            val order = Orders.newOrder(snapshot)
            ordersList.remove(order)
            ordersLiveData.value = ordersList
        }
    }

    private val messagesListener = object : ChildEventListener {
        override fun onCancelled(p0: DatabaseError?) {

        }

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
        }

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            val snapshot = p0 ?: return
            val newMessage = Messages.newMessage(snapshot)
            messagesList.removeAll { it.id == newMessage.id }
            messagesList.add(newMessage)
            messagesLiveData.value = messagesList
        }

        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
            Log.d(LOG_TAG, "Snapshot: $p0")
            val snapshot = p0 ?: return
            val message = Messages.newMessage(snapshot)
            messagesList.add(message)
            messagesLiveData.value = messagesList
        }

        override fun onChildRemoved(p0: DataSnapshot?) {
            val snapshot = p0 ?: return
            val message = Messages.newMessage(snapshot)
            messagesList.remove(message)
            messagesLiveData.value = messagesList
        }
    }


    init {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                val uid = firebaseAuth.currentUser!!.uid
                databaseReference = FirebaseDatabase.getInstance().getReference("/customers/$uid")
                databaseReference!!.addValueEventListener(customerListener)

                ordersDatabaseReference = FirebaseDatabase.getInstance().getReference("/orders").orderByChild("customer_id").equalTo(uid)
                ordersDatabaseReference!!.addChildEventListener(ordersListener)

                messagesDatabaseReference = FirebaseDatabase.getInstance().getReference("/messages").orderByChild("customer_id").equalTo(uid)
                messagesDatabaseReference!!.addChildEventListener(messagesListener)
            } else {
                databaseReference?.removeEventListener(customerListener)
                databaseReference = null
                _customer.value = null

                ordersLiveData.value = mutableListOf()
                ordersDatabaseReference?.removeEventListener(ordersListener)
                ordersDatabaseReference = null
                ordersList.clear()

                messagesLiveData.value = mutableListOf()
                messagesDatabaseReference?.removeEventListener(messagesListener)
                messagesDatabaseReference = null
                messagesList.clear()
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

    fun updateCar(car: Car): Task<Void> {
        val id = customer.value!!.id
        assert(car.id != null)
        val carRefrence = FirebaseDatabase.getInstance().getReference("/customers/$id/cars_list/${car.id}")
        return carRefrence.setValue(car.toMap())
    }

    fun deleteCar(carId: Id): Task<Void> {
        val id = customer.value!!.id
        val carRefrence = FirebaseDatabase.getInstance().getReference("/customers/$id/cars_list/$carId")
        return carRefrence.removeValue()
    }

}