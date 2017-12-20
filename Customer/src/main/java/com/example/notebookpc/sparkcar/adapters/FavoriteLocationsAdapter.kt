package com.example.notebookpc.sparkcar.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notebookpc.sparkcarcommon.data.FavoriteLocation

internal class FavoriteLocationsAdapter(private val favoritesList: List<FavoriteLocation>) : RecyclerView.Adapter<FavouriteLocationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FavouriteLocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return FavouriteLocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteLocationViewHolder, position: Int) {
        holder.bind(favoritesList[position])
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }
}

class FavouriteLocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(location: FavoriteLocation) {
        val textView = itemView as TextView
        textView.text = location.name
    }
}
