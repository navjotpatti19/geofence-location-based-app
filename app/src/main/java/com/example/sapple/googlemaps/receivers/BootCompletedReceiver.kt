package com.example.sapple.googlemaps.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.sapple.googlemaps.activities.ReminderApp
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.helperClasses.Utils

class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val mApplication = context?.applicationContext
        if ("android.intent.action.BOOT_COMPLETED" == intent?.action) {
            Thread {
                val reminderList = MyDbHelper.getInstance(context!!)?.reminderDao()?.
                        getReminderData()
                for(reminder in reminderList!!) {
                    Utils().notificationReceiver(context, reminder)
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