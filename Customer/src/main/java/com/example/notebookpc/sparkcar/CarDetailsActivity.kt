package com.example.notebookpc.sparkcar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.notebookpc.sparkcarcommon.data.Car
import kotlinx.android.synthetic.main.activity_car_details.*
import org.jetbrains.anko.toast

class CarDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_details)

        val car = intent.extras["car"] as Car

        nameEditText.setText(car.name)
        carPlateEditText.setText(car.carPlate)
        colorEditText.setText(car.color)

        updateButton.setOnClickListener {
            val carName = nameEditText.text.toString()
            val carPlate = carPlateEditText.text.toString()
            val color = colorEditText.text.toString()
            val newCar = car.copy(name = carName, carPlate = carPlate, color = color)
            CustomerHolder.updateCar(newCar).addOnCompleteListener {
                if (it.isSuccessful) {
                    toast("Success")
                    finish()
                } else {
                    toast("Error: ${it.exception?.message}")
                }
            }
        }

        deleteButton.setOnClickListener {
            CustomerHolder.deleteCar(car.id!!).addOnCompleteListener {
                if (it.isSuccessful) {
                    toast("Success")
                    finish()
                } else {
                    toast("Error: ${it.exception?.message}")
                }
            }
        }
    }
}
