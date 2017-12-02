package com.example.notebookpc.sparkcar

import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity


internal fun TestingActivity.selectNavigationItem(item: MenuItem) {
    when (item.itemId) {
    //sends back to main activity
        R.id.id_home -> {
            startActivity<TestingActivity>()
        }
        R.id.id_messagse -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, messagesFragment)
                    .commit()
            supportActionBar!!.title = "Messages Page"

        }
        R.id.id_settings -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, settingsFragment)
                    .commit()
            supportActionBar!!.title = "Settings Page"
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
    }
}