package com.example.notebookpc.sparkcar

import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity


internal fun AppCompatActivity.selectNavigationItem(item: MenuItem) {
    when (item.itemId) {
    //sends back to main activity
        R.id.id_home -> {
            startActivity<TestingActivity>()
        }
        R.id.id_messagse -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.messagesFragment)
                    .commit()
            supportActionBar!!.title = "Messages Page"

        }
        R.id.id_settings -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.settingsFragment)
                    .commit()
            supportActionBar!!.title = "Settings Page"
        }
        R.id.id_profile -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.profileFragment)
                    .commit()
            supportActionBar!!.title = "Profile Page"
        }
        R.id.id_favorite_cleaner -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.favoritesFragment)
                    .commit()
            supportActionBar!!.title = "Favorite Cleaner Page"
        }
        R.id.id_location -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.locationFragment)
                    .commit()
            supportActionBar!!.title = "Location Page"
        }
        R.id.id_car -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.carsFragment)
                    .commit()
            supportActionBar!!.title = "Cars Page"
        }
        R.id.id_about -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.aboutFragment)
                    .commit()
            supportActionBar!!.title = "About Page"
        }
        R.id.id_share -> {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, Fragments.shareFragment)
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