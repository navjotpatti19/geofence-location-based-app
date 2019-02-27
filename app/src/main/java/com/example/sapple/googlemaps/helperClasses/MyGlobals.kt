package com.example.sapple.googlemaps.helperClasses

import android.app.Activity
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.EditText
import java.util.*

class MyGlobals {

    private val c = Calendar.getInstance()
    private var mHour = c.get(Calendar.HOUR_OF_DAY)
    private var mMinute = c.get(Calendar.MINUTE)

    fun timePicker(context: Context, editText: EditText) {
        val timePickerDialog = TimePickerDialog(context,
                TimePickerDialog.OnTimeSetListener {
                    _, hourOfDay, minute ->
                    editText.setText(String.format("%02d:%02d", hourOfDay, minute))
                    mHour = hourOfDay
                    mMinute = minute
                }, mHour, mMinute, false)
        timePickerDialog.show()
    }
    private fun isLocationEnabled(activity: Activity): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE)
                as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun checkLocation(activity: Activity): Boolean {
        if(!isLocationEnabled(activity)) {
            MyGlobals.locationEnableAlert(activity)
        }
        return isLocationEnabled(activity)
    }

    companion object {
        var activityName = "AddReminderActivity"

        private fun locationEnableAlert(activity: Activity) {
            val dialog = AlertDialog.Builder(activity)
            dialog.create()
            dialog.setCancelable(false)
            dialog.setTitle("Enable Location")
                    .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                            "use this app")
                    .setPositiveButton("Location Settings") { _, _ ->
                        val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        activity.startActivity(myIntent)

                    }
            dialog.show()
        }
    }
}