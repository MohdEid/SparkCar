package com.example.notebookpc.sparkcar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.favorite_cleaner_list_item.*

class OrdersActivity : AppCompatActivity() {

    //TODO make initial text for Spinners

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        val cleaner = intent.extras.get("cleaner") as TestingActivity.CleanerTag
        txtName.text = cleaner.title

    }
}
