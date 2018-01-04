package com.example.notebookpc.sparkcar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import com.example.notebookpc.sparkcarcommon.data.Car
import com.example.notebookpc.sparkcarcommon.data.Orders
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_confirm_orders.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast


class ConfirmOrderActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_orders)

        val cleaner = intent.extras.get("cleaner") as? MainActivity.CleanerTag ?: throw  IllegalStateException("Intent must have cleaner extra")
        cleanerName.text = cleaner.title
        CustomerHolder.customer.observe(this, android.arch.lifecycle.Observer { currentCustomer ->
            val locationAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, currentCustomer?.favoriteLocations?.map { it.name })
            locationSpinner.adapter = locationAdapter
            val carAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, currentCustomer?.cars?.map { "${it.color} ${it.name},\t Plate#: ${it.carPlate}" })
            carSpinner.adapter = carAdapter

        })

        //TODO create a limit for how many order per user
        //TODO add a time option for order to be executed
        confirmButton.onClick {

            val orderReference = FirebaseDatabase.getInstance().getReference("/orders")
            val locationName = locationSpinner.selectedItem.toString()
            val locations = CustomerHolder.customer.value?.favoriteLocations ?: throw IllegalStateException()
            lateinit var currentLocation: LatLng
            locations
                    .filter { it.name == locationName }
                    .forEach { currentLocation = it.location }
            val car = carSpinner.selectedItem.toString()
            val carDetail = CustomerHolder.customer.value?.cars ?: throw  IllegalStateException()

            //TODO CarInfo isn't initialized properly
            var carInfo: Car? = null
            carDetail
                    .filter { it.toString() == car }
                    .forEach { carInfo = it }

            val customerId = CustomerHolder.customer.value?.id ?: throw IllegalStateException()
            val order = Orders(
                    cleanerId = cleaner.id,
                    location = currentLocation,
                    car = carInfo!!,
                    customerId = customerId,
                    orderId = null,
                    status = Orders.STATUS_INCOMPLETE,
                    pictureUrl = null
            )
            orderReference.push().setValue(order.toMap()).addOnCompleteListener {

                if (it.isSuccessful) {
                    toast("Request added to the database successfully")
                    finish()
                } else {
                    toast("something went wrong")
                }
            }

        }
    }
}

