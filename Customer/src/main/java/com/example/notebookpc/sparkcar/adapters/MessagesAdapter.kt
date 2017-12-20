package com.example.notebookpc.sparkcar.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notebookpc.sparkcar.R
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.example.notebookpc.sparkcarcommon.data.Messages
import com.google.firebase.database.*
import org.joda.time.format.DateTimeFormat

internal class MessagesAdapter(private val messages: List<Messages>) : RecyclerView.Adapter<MessagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.messages_recycler_view, parent, false)
        return MessagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}

class MessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    lateinit var cleanerReference: DatabaseReference
    val fromCleanerTextView: TextView = itemView.findViewById(R.id.fromCleanerTextView)
    val titleFromCleanerTextView: TextView = itemView.findViewById(R.id.titleFromCleanerTextView)
    val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)


    private var cleanerListener: ValueEventListener? = null
    fun bind(messages: Messages) {

        if (cleanerListener != null) {
            cleanerReference.removeEventListener(cleanerListener)
        }
        cleanerReference = FirebaseDatabase.getInstance().getReference("/cleaners/${messages.cleanerId}")
        cleanerListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                val cleaner = Cleaner.newCleaner(snapshot)
                fromCleanerTextView.text = cleaner.name

            }
        }
        cleanerReference.addListenerForSingleValueEvent(cleanerListener)

        titleFromCleanerTextView.text = messages.title
        dateTextView.text = messages.date.toString(DateTimeFormat.mediumDateTime())
    }
}
