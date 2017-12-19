package com.example.notebookpc.sparkcarcleaner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.notebookpc.sparkcarcommon.data.Customer
import com.example.notebookpc.sparkcarcommon.data.Orders
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_customer_order.*
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CustomerOrderActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE: Int = 101
        private val LOGTAG: String = CustomerOrderActivity::class.java.simpleName
    }

    lateinit var currentPhotoPath: String
    var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_order)

        val orderTag = intent.extras.get("order") as MainActivity.OrderTag
        FirebaseDatabase.getInstance().getReference("/customers/${orderTag.customerId}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        val customer = Customer.newCustomer(snapshot)
                        customerName.text = getString(R.string.customer_name_s, customer.name)
                    }

                })
        orderStatus.text = getString(R.string.status_s, orderTag.status)

        imageView.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File

                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(this,
                            "com.example.notebookpc.sparkcarcleaner.fileprovider",
                            photoFile)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

        confirmButton.setOnClickListener {
            val uri = photoURI
            if (uri == null) {
                toast("Please add a picture first")
                return@setOnClickListener
            }
            FirebaseStorage.getInstance().getReference("/car_pictures/${uri.lastPathSegment}").putFile(uri).addOnCompleteListener {
                if (it.isSuccessful) {
                    val pictureUrl = it.result.downloadUrl.toString()
                    Log.d(LOGTAG, "order_id: ${orderTag.id}")
                    Log.d(LOGTAG, "orders: ${CleanerHolder.orders.value}")
                    val order = CleanerHolder.orders.value!!.first { it.orderId == orderTag.id }.copy(status = Orders.STATUS_COMPLETE, pictureUrl = pictureUrl)
                    FirebaseDatabase.getInstance().getReference("/orders/${orderTag.id}").setValue(order.toMap())
                    toast("Order completed successfully")
                    finish()
                } else {
                    toast("Error: ${it.exception?.message}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageView.setImageURI(photoURI)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }
}
