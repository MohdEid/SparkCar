package com.example.notebookpc.sparkcarcleaner

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.v4.app.FragmentActivity
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.example.notebookpc.sparkcarcommon.data.Id
import com.example.notebookpc.sparkcarcommon.data.Messages
import com.example.notebookpc.sparkcarcommon.data.Orders
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.jetbrains.anko.AnkoLogger


internal object CleanerHolder : AnkoLogger {
    //cleaner LiveData
    private val cleanerLiveData = MutableLiveData<Cleaner?>()
    val cleaner: LiveData<Cleaner?> = cleanerLiveData

    //orders live Data
    private val ordersList = mutableListOf<Orders>()
    private val ordersLiveData = MutableLiveData<List<Orders>>()
    val orders: LiveData<List<Orders>> = ordersLiveData

    //messages live Data
    private val messagesList = mutableListOf<Messages>()
    private val messagesLiveData = MutableLiveData<List<Messages>>()
    val messages: LiveData<List<Messages>> = messagesLiveData

    //Database references
    private var cleanerDatabaseReference: DatabaseReference? = null
    private var ordersDatabaseReference: Query? = null
    private var messagesDatabaseReference: Query? = null

    //Listeners
    private val cleanerListener = object : ValueEventListener {
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
                cleanerDatabaseReference = FirebaseDatabase.getInstance().getReference("/cleaners/$uid")
                cleanerDatabaseReference!!.addValueEventListener(cleanerListener)

                ordersDatabaseReference = FirebaseDatabase.getInstance().getReference("/orders").orderByChild("cleaner_id").equalTo(uid)
                ordersDatabaseReference!!.addChildEventListener(ordersListener)

                messagesDatabaseReference = FirebaseDatabase.getInstance().getReference("/messages").orderByChild("cleaner_id").equalTo(uid)
                messagesDatabaseReference!!.addChildEventListener(messagesListener)

            } else {
                cleanerDatabaseReference?.removeEventListener(cleanerListener)
                cleanerDatabaseReference = null
                cleanerLiveData.value = null

                ordersDatabaseReference?.removeEventListener(ordersListener)
                ordersDatabaseReference = null
                ordersList.clear()

                messagesLiveData.value = mutableListOf()
                messagesDatabaseReference?.removeEventListener(messagesListener)
                messagesDatabaseReference = null
                messagesList.clear()
                messagesLiveData.value = mutableListOf()
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