package com.example.notebookpc.sparkcar

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class LoginActivity : AppCompatActivity() {

    var RC_SIGN_IN:Int = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val firebaseAuth= FirebaseAuth.getInstance()
        val button =findViewById<Button>(R.id.signInButton)
        button.setOnClickListener {
            if(firebaseAuth.currentUser == null) {
                val signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                        listOf(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .build()
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
            else{
                Toast.makeText(this, "You're already signed in.", Toast.LENGTH_LONG).show()

            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode){

            RC_SIGN_IN -> {
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(this, "Log-In Success.",Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this, "Log-In Failed.",Toast.LENGTH_LONG).show()
                }

            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
