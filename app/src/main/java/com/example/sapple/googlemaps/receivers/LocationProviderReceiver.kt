package com.example.sapple.googlemaps.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sapple.googlemaps.activities.ReminderApp
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.helperClasses.Utils

class LocationProviderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val mApplication = context?.applicationContext
        if ("android.intent.action.PROVIDER_CHANGED" == intent?.action) {
            Thread {
                val reminderList = MyDbHelper.getInstance(context!!)?.reminderDao()?.
                        getReminderData()
                for(reminder in reminderList!!) {
                    (mApplication as ReminderApp).getRepository().add(reminder,
                            success = {
                                Log.d("Reboot Receiver: ", "1")
                            },
                            failure = {
                            })
                }
            }.start()
        }
    }
}