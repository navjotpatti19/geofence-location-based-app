package com.example.sapple.googlemaps.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.helperClasses.Utils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val ERROR_DIALOG_REQUEST = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(isServiceOk()) {
            initMap()
        }
        flagResetReceiver()
    }

    private fun initMap() {
        val intent = Intent(this@MainActivity, GoogleMapActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isServiceOk() : Boolean {
        Log.d(TAG, "isServiceOk: checking google service version")
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)

        when {
            available == ConnectionResult.SUCCESS -> {
                //everything is working fine
                Log.d(TAG, "isServiceOk: Google Play service is working")
                return true
            }

            GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
                Log.d(TAG, "isServiceOk: An error occurred, but we can fix it")
                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
                dialog.show()
            }
            else -> Toast.makeText(this@MainActivity, "You can't make map request", Toast.LENGTH_LONG).show()
        }
        return false
    }

    private fun flagResetReceiver() {
        Utils().setFlagResetReceiver(this@MainActivity)
    }
}
