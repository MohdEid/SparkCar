package com.example.notebookpc.sparkcar.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.notebookpc.sparkcar.CarDetailsActivity
import com.example.notebookpc.sparkcarcommon.data.Car
import org.jetbrains.anko.startActivity

internal class FavoriteCarsAdapter(private val favoritesList: List<Car>) : RecyclerView.Adapter<FavouriteCarsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FavouriteCarsViewHolder {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return FavouriteCarsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouriteCarsViewHolder, position: Int) {
        holder.bind(favoritesList[position])
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }
}

class FavouriteCarsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    lateinit var car: Car

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        p0?.context?.startActivity<CarDetailsActivity>("car" to car)
    }

    fun bind(car: Car) {
        this.car = car
        val textView = itemView as TextView
        textView.text = car.name
    }
}
