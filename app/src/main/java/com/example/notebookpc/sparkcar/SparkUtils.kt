package com.example.notebookpc.sparkcar

import android.support.v4.app.FragmentActivity
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity


internal fun TestingActivity.selectNavigationItem(item: MenuItem) {
    when (item.itemId) {
    //sends back to main activity
        R.id.id_home -> {
            startActivity<TestingActivity>()
        }
        R.id.id_messages -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, messagesFragment)
                    .commit()
            supportActionBar!!.title = "Messages Page"

        }
        R.id.id_profile -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, profileFragment)
                    .commit()
            supportActionBar!!.title = "Profile Page"
        }
        R.id.id_favorite_cleaner -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, favoriteCleanersFragment)
                    .commit()
            supportActionBar!!.title = "Favorite Cleaner Page"
        }
        R.id.id_location -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, locationFragment)
                    .commit()
            supportActionBar!!.title = "Location Page"
        }
        R.id.id_car -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, carsFragment)
                    .commit()
            supportActionBar!!.title = "Cars Page"
        }
        R.id.id_about -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, aboutFragment)
                    .commit()
            supportActionBar!!.title = "About Page"
        }
        R.id.id_share -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, shareFragment)
                    .commit()
            supportActionBar!!.title = "Share Page"
        }
    //sign out
        R.id.id_sign_out -> {
            AuthUI.getInstance().signOut(this)
            longToast("Logging out successfully")
        }
        R.id.id_sign_in -> {
            val signInIntent = AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                    listOf(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                            AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                    .build()
            startActivityForResult(signInIntent, TestingActivity.RC_SIGN_IN)
        }
    }
}

fun FragmentActivity.checkLocationSetting(requestCode: Int) {
    val locationSettingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(LocationRequest().apply {
                interval = 5000
                fastestInterval = 5000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            })
    launch(CommonPool) {
        val task = LocationServices
                .getSettingsClient(this@checkLocationSetting)
                .checkLocationSettings(locationSettingsBuilder.build())
        Tasks.await(task)
        if (!task.isSuccessful) {
            when ((task.exception as ApiException).statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED -> {
                    (task.exception as ResolvableApiException)
                            .startResolutionForResult(this@checkLocationSetting, requestCode)
                }
            }
        }
    }
}
