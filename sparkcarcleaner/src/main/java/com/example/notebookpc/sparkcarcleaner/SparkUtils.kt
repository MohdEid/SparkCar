package com.example.notebookpc.sparkcarcleaner

import android.support.v4.app.Fragment
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


internal fun MainActivity.selectNavigationItem(item: MenuItem) {
    when (item.itemId) {
    //sends back to main activity
        R.id.id_home -> {
            replaceFragment(mapFragment)
        }
        R.id.id_messages -> {
            replaceFragment(messagesFragment)
        }
        R.id.id_profile -> {
            replaceFragment(profileFragment)
        }
        R.id.id_about -> {
            replaceFragment(aboutFragment)
        }
        R.id.id_share -> {
            replaceFragment(shareFragment)
        }
    //sign out
        R.id.id_sign_out -> {
            AuthUI.getInstance().signOut(this)
            longToast("Logging out successfully")
        }
        R.id.id_sign_in -> {
            val signInIntent = AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(
                            listOf(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                                    AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                    AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                    .build()
            startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN)
        }
    }
}

fun MainActivity.replaceFragment(fragment: Fragment) {
    val currentFragment =
            supportFragmentManager.findFragmentById(R.id.main_container)
    if (currentFragment == fragment) return
    supportFragmentManager.popBackStack()
    val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
    if (fragment != mapFragment) {
        transaction.addToBackStack(null)
    }
    transaction.commit()
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
