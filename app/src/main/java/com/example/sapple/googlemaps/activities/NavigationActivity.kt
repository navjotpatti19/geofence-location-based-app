package com.example.sapple.googlemaps.activities

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.WindowManager
import android.widget.TextView

import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.entities.ReminderData
import com.example.sapple.googlemaps.helperClasses.Utils
import com.example.sapple.googlemaps.interfaces.ConnectivityReceiverListener
import com.example.sapple.googlemaps.receivers.ConnectivityReceiver
import java.util.*

abstract class NavigationActivity : AppCompatActivity(), View.OnClickListener,
         ConnectivityReceiverListener {
    fun getRepository() = (application as ReminderApp).getRepository()

    private var connectivityManager : ConnectivityReceiver? = null

    //callback will be called when there is a change
    override fun onNetworkConnectionChanged(boolean: Boolean) {
        showMessage(boolean)
    }

    private lateinit var drawer: DrawerLayout
    lateinit var toolbar: Toolbar
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        setUpView()
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onStart() {
        super.onStart()
        connectivityManager = ConnectivityReceiver()
        registerReceiver(connectivityManager, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private fun setUpView() {
        findViewById<View>(R.id.ll_headerLayout).setOnClickListener(this)
        drawer = findViewById(R.id.drawer_layout)
        val tvShowReminder = findViewById<TextView>(R.id.tvShowReminder)
        val tvAddReminder = findViewById<TextView>(R.id.tvAddReminder)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        tvShowReminder.setOnClickListener(this)
        tvAddReminder.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        drawer.closeDrawer(GravityCompat.START)
        val intent: Intent
        when (v.id) {
            R.id.tvShowReminder -> {
                intent = Intent(this, ReminderListActivity::class.java)
                startActivity(intent)
            }

            R.id.tvAddReminder -> {
                intent = Intent(this, GoogleMapActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    private fun showMessage(isConnected: Boolean) {
        if(!isConnected) {
            val msg = "No Internet Connection"
            snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            snackbar?.show()
        } else{
            snackbar?.dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connectivityManager)
    }
}
