package com.example.sapple.googlemaps.services

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import com.example.sapple.googlemaps.database.MyDbHelper

class FlagResetJobService: JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        val remindersList = MyDbHelper.getInstance(this)?.reminderDao()?.getReminderData()
        if(remindersList != null) {
            for(data in remindersList) {
                if(data.reminderType == 1 || data.reminderType == 2) {
                    data.flag = false
                    MyDbHelper.getInstance(this)?.reminderDao()?.updateReminder(data)
                }
            }
        }
    }

    companion object {
        private const val JOB_ID = 1000
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, FlagResetJobService::class.java, JOB_ID, intent)
        }
    }
}