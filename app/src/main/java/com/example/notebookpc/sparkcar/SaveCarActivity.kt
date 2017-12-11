package com.example.notebookpc.sparkcar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.notebookpc.sparkcar.data.Car
import kotlinx.android.synthetic.main.activity_save_car.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class SaveCarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_car)

        addCarButton.onClick {
            val name = nameEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your car model")
                    return@onClick
                }
                it
            }

            val plate = carPlateEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your plate")
                    return@onClick
                }
                it
            }

            val color = colorEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your car color")
                    return@onClick
                }
                it
            }

            CustomerHolder.addCar(Car(name, plate, color)).addOnCompleteListener {
                if (it.isSuccessful) {
                    toast("Car added successfully")
                    finish()
                } else {
                    toast("Failed to add car")
                }
            }
        }
    }
}
