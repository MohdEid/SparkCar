package com.example.notebookpc.sparkcarcleaner.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notebookpc.sparkcarcleaner.R
import com.example.notebookpc.sparkcarcommon.data.Customer
import com.example.notebookpc.sparkcarcommon.data.Orders
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.joda.time.format.DateTimeFormat

internal class PendingOrdersAdapter(private val orders: List<Orders>) : RecyclerView.Adapter<PendingOrdersViewHolder>() {
    override fun onBindViewHolder(holder: PendingOrdersViewHolder?, position: Int) {
        holder?.bind(orders[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PendingOrdersViewHolder {
        val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.order_list_item, parent, false)
        return PendingOrdersViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}

internal class PendingOrdersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val customerNameTextView: TextView = itemView.findViewById(R.id.customerNameTextView)
    val customerMobileTextView: TextView = itemView.findViewById(R.id.customerMobileTextView)
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    val carNameTextView: TextView = itemView.findViewById(R.id.carModelTextView)
    val carPlateTextView: TextView = itemView.findViewById(R.id.carPlateTextView)

    private var listener: ValueEventListener? = null

    fun bind(orders: Orders) {

        val reference = FirebaseDatabase.getInstance().getReference("/customers/${orders.customerId}")
        if (listener != null) {
            reference.removeEventListener(listener)
        }
        listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val customer = Customer.newCustomer(snapshot)

                customerNameTextView.text = customer.name
                customerMobileTextView.text = customer.mobile
                dateTextView.text = orders.date.toString(DateTimeFormat.mediumDateTime())
                carNameTextView.text = orders.car.name
                carPlateTextView.text = orders.car.carPlate
            }
        }
        reference.addListenerForSingleValueEvent(listener)
    }

}