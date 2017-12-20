package com.example.notebookpc.sparkcar.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notebookpc.sparkcar.R
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.example.notebookpc.sparkcarcommon.data.Orders
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.joda.time.format.DateTimeFormat

internal class OrdersListAdapter(private val orders: List<Orders>) : RecyclerView.Adapter<OrdersListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): OrdersListViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.customer_orders_recycler_view, parent, false)
        return OrdersListViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdersListViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}

class OrdersListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val cleanerNameTextView: TextView = itemView.findViewById(R.id.cleanerNameTextView)
    private val cleanerMobileTextView: TextView = itemView.findViewById(R.id.cleanerMobileTextView)
    private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    private val carNameTextView: TextView = itemView.findViewById(R.id.carModelTextView)
    private val carPlateTextView: TextView = itemView.findViewById(R.id.carPlateTextView)

    private var listener: ValueEventListener? = null

    fun bind(orders: Orders) {
        val reference = FirebaseDatabase.getInstance().getReference("/cleaners/${orders.cleanerId}")
        if (listener != null) {
            reference.removeEventListener(listener)
        }
        listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val cleaner = Cleaner.newCleaner(snapshot)

                cleanerNameTextView.text = cleaner.name
                cleanerMobileTextView.text = cleaner.mobile
                dateTextView.text = orders.date.toString(DateTimeFormat.mediumDateTime())
                carNameTextView.text = orders.car.name
                carPlateTextView.text = orders.car.carPlate
            }
        }
        reference.addListenerForSingleValueEvent(listener)
    }
}

