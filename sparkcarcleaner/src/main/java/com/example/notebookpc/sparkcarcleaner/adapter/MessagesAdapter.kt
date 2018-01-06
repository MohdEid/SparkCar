package com.example.notebookpc.sparkcarcleaner.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notebookpc.sparkcarcleaner.R
import com.example.notebookpc.sparkcarcommon.data.Customer
import com.example.notebookpc.sparkcarcommon.data.Messages
import com.google.firebase.database.*
import org.joda.time.format.DateTimeFormat

internal class MessagesAdapter(private val messages: List<Messages>) : RecyclerView.Adapter<MessagesViewHolder>() {
    override fun onBindViewHolder(holder: MessagesViewHolder?, position: Int) {
        holder?.bind(messages[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessagesViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.messages_recycler_view, parent, false)
        return MessagesViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}

internal class MessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    // TODO add title to itemView layout
    lateinit var customerReference: DatabaseReference
    val fromCustomerTextView: TextView = itemView.findViewById(R.id.fromCustomerTextView)
    val titleFromCustomerTextView: TextView = itemView.findViewById(R.id.titleFromCustomerTextView)
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)


    private var customerListener: ValueEventListener? = null
    fun bind(messages: Messages) {

        if (customerListener != null) {
            customerReference.removeEventListener(customerListener)
        }
        customerReference = FirebaseDatabase.getInstance().getReference("/customers/${messages.customerId}")
        customerListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val customer = Customer.newCustomer(snapshot)
                fromCustomerTextView.text = customer.name

            }
        }
        customerReference.addListenerForSingleValueEvent(customerListener)

        titleFromCustomerTextView.text = messages.content
        dateTextView.text = messages.date.toString(DateTimeFormat.mediumDateTime())
    }
}
