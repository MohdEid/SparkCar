package com.example.notebookpc.sparkcar.adapters

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notebookpc.sparkcar.R
import com.example.notebookpc.sparkcar.data.Cleaner
import com.example.notebookpc.sparkcar.data.Id
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

internal class FavoriteCleanersAdapter(private val favoritesList: List<Id>) : RecyclerView.Adapter<CleanerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CleanerViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.favorite_cleaner_list_item, parent, false)
        return CleanerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CleanerViewHolder, position: Int) {
        holder.bind(favoritesList[position])
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }
}

internal class CleanerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView = itemView.findViewById<TextView>(R.id.txtName)
    private val ratingTextView = itemView.findViewById<TextView>(R.id.txtRating)
    private val availabilityTextView = itemView.findViewById<TextView>(R.id.txtAvailable)
    private var listener: ValueEventListener? = null

    fun bind(cleanerId: Id) {
        nameTextView.text = ""
        ratingTextView.text = ""
        availabilityTextView.text = ""

        val reference = FirebaseDatabase.getInstance().getReference("/cleaners").orderByChild("id").equalTo(cleanerId)
        if (listener != null) {
            reference.removeEventListener(listener)
        }
        listener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            @SuppressLint("StringFormatInvalid")
            override fun onDataChange(p0: DataSnapshot?) {
                for (snapshot in p0?.children ?: throw AssertionError()) {
                    val cleaner = Cleaner.newCleaner(snapshot)
                    nameTextView.text = nameTextView.context.getString(R.string.favorite_cleaner_list_item_name, cleaner.name)
                    val rating = cleaner.rating
                    if (rating < 0) {
                        ratingTextView.text = nameTextView.context.getString(R.string.favorite_cleaner_unlist_item_rating)

                    } else {
                        ratingTextView.text = nameTextView.context.getString(R.string.favorite_cleaner_list_item_rating, rating)
                    }
                    availabilityTextView.text = nameTextView.context.getString(R.string.favorite_cleaner_list_item_availability, cleaner.isAvailable)
                }
            }
        }
        reference.addListenerForSingleValueEvent(listener)
    }
}
