package com.example.sapple.googlemaps.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.sapple.googlemaps.helperClasses.Utils
import com.example.sapple.googlemaps.services.FlagResetJobService

class DbFlagResetReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if ("android.intent.action.BOOT_COMPLETED" == intent?.action) {
            Utils().setFlagResetReceiver(context!!)
        }
        FlagResetJobService.enqueueWork(context!!, intent!!)
    }
}