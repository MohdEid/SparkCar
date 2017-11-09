package com.example.notebookpc.sparkcar

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentTransaction
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem

class DrawerPage : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout:DrawerLayout
    lateinit var toolBar: Toolbar
    lateinit var actionBarDrawerToggle:ActionBarDrawerToggle
    lateinit var navigationView:NavigationView
    lateinit var fragmentTransaction: FragmentTransaction


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_page)

        setContentView(R.layout.activity_drawer_page)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolBar = findViewById(R.id.toolbar)

        actionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout, toolBar ,R.string.drawer_opened,R.string.drawer_closed)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)
        }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.id_home ->
            fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container,HomeFragment()).commit()->
                    supportActionBar!!.setTitle("Home Page")

            R.id.id_car ->
                        fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container,CarsFragment())
                    /*.addToBackStack("Home")*/
                    .commit()->
            supportActionBar!!.setTitle("Cars Page")
        //item.setChecked(true)
        //drawerLayout.closeDrawers()
        //
            R.id.id_messagse ->
                fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container,MessagesFragment())
                    /*.addToBackStack("Home")*/
                    .commit()->
                supportActionBar!!.setTitle("Messages Page")

            R.id.id_share ->
                fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container,ShareFragment())
                    /*.addToBackStack("Home")*/
                    .commit()->
                supportActionBar!!.setTitle("Share Page")

            R.id.id_favorite_cleaner ->
                fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container,FavoritesFragment())
                    /*.addToBackStack("Home")*/
                    .commit()->
                supportActionBar!!.setTitle("Favorite Cleaners Page")

            R.id.id_profile ->
                fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container,ProfileFragment())
                    /*.addToBackStack("Home")*/
                    .commit()->
                supportActionBar!!.setTitle("Profile Page")

            R.id.id_about->
                fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container,AboutFragment())
                    /*.addToBackStack("Home")*/
                    .commit()->
                supportActionBar!!.setTitle("About Page")

            R.id.id_settings ->
                fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.main_container, SettingsFragment())
                    /*.addToBackStack("Home")*/
                    .commit()->
                supportActionBar!!.setTitle("Settings Page")
        }
        return true
    }

     override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

}
