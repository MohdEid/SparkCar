package com.example.notebookpc.sparkcarcleaner

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.notebookpc.sparkcarcommon.data.Cleaner
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val currentUser = FirebaseAuth.getInstance().currentUser ?: throw AssertionError()


        if (!currentUser.displayName.isNullOrBlank()) {
            nameEditText.setText(currentUser.displayName)
        }
        if (!currentUser.email.isNullOrBlank()) {
            emailEditText.setText(currentUser.email)
        }
        if (!currentUser.phoneNumber.isNullOrBlank()) {
            mobileEditText.setText(currentUser.phoneNumber)
        }


        //TODO send email verification before executing the function
        //TODO do expression check before signing up
        submitButton.onClick {
            val name = nameEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your name")
                    return@onClick
                }
                it
            }

            val mobile = mobileEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your mobile number")
                    return@onClick
                }
                it
            }

            val email = emailEditText.text.toString().let {
                if (it.isBlank()) {
                    toast("Please enter your email")
                    return@onClick
                }
                it
            }

            val uid = intent.extras.getString("id")
            val cleaner = Cleaner(
                    id = uid,
                    name = name,
                    email = email,
                    mobile = mobile,
                    location = LatLng(0.0, 0.0),
                    rating = 1f,
                    isAvailable = true
            )
            CleanerHolder.updateCleaner(cleaner) { task ->
                task.addOnCompleteListener {
                    if (it.isSuccessful) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        toast("Exception occurred: " + it.exception?.message)
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            }
        }
    }
}
