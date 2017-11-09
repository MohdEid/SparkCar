package com.example.notebookpc.sparkcar

import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem

class DrawerPage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MessagesFragment.OnFragmentInteractionListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var toolBar: Toolbar
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var navigationView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_page)

        setContentView(R.layout.activity_drawer_page)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolBar = findViewById(R.id.toolbar)

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.drawer_opened, R.string.drawer_closed)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.id_home -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment())
                        .commit()
                supportActionBar!!.title = "Home Page"
            }
            R.id.id_car -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, CarsFragment())
                        .commit()
                supportActionBar!!.title = "Cars Page"
            }

            R.id.id_messagse -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MessagesFragment())
                        .commit()
                supportActionBar!!.title = "Messages Page"
            }

            R.id.id_share -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ShareFragment())
                        .commit()
                supportActionBar!!.title = "Share Page"
            }

            R.id.id_favorite_cleaner -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, FavoritesFragment())
                        .commit()
                supportActionBar!!.title = "Favorite Cleaners Page"
            }

            R.id.id_profile -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, ProfileFragment())
                        .commit()
                supportActionBar!!.title = "Profile Page"
            }
            R.id.id_about -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, AboutFragment())
                        .commit()
                supportActionBar!!.title = "About Page"
            }

            R.id.id_settings -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, SettingsFragment())
                        .commit()
                supportActionBar!!.title = "Settings Page"
            }
        }
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

}
