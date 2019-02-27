package com.example.sapple.googlemaps.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import com.example.sapple.googlemaps.activities.ReminderApp
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.helperClasses.Utils
import com.example.sapple.googlemaps.interfaces.ConnectivityReceiverListener

class ConnectivityReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context))
        }

        Thread {
             val reminderList = MyDbHelper.getInstance(context!!)?.reminderDao()?.
                    getReminderData()
            for(reminder in reminderList!!) {
                (context.applicationContext as ReminderApp).getRepository().add(reminder,
                        success = {
                            Log.d("Reboot Receiver: ", "1")
                        },
                        failure = {
                        })
            }
        }.start()
    }

    private fun isConnectedOrConnecting(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }
}