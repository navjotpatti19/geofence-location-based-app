package com.example.sapple.googlemaps.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sapple.googlemaps.activities.AlarmScreen
import com.example.sapple.googlemaps.activities.GeofenceErrorMessages
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.entities.ReminderData
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.text.SimpleDateFormat
import java.util.*

class GeoFenceReceiver: BroadcastReceiver() {
    private lateinit var context: Context
    override fun onReceive(context: Context?, intent: Intent?) {
        this.context = context!!
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(context,
                    geofencingEvent.errorCode)
            Log.e("ReminderActivity", errorMessage)
            return
        }
        Thread { handleEvent(geofencingEvent) }.start()
    }

    private fun handleEvent(event: GeofencingEvent) {
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val dbReminderData = getFirstReminder(event.triggeringGeofences)

            if(dbReminderData != null) {
                val endTime = dbReminderData.endTime
                val startTime = dbReminderData.startTime
                val eTime = SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(endTime)
                val sTime = SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(startTime)
                if(dbReminderData.switchCheck!!) {
                    if (sTime.time <= currentTime()!!.time && eTime.time > currentTime()!!.time
                            && !dbReminderData.flag!!) {
                        if (dbReminderData.reminderType == 0 || dbReminderData.reminderType == 1) {
                            dbReminderData.flag = true
                            createAlarm(dbReminderData)
                        } else {
                            createAlarm(dbReminderData)
                        }
                        MyDbHelper.getInstance(context)?.reminderDao()?.
                                updateReminder(dbReminderData)
                    }
                }
            }
        }
    }

    private fun getFirstReminder(triggeringGeofences: List<Geofence>): ReminderData? {
        val firstGeofence = triggeringGeofences[0]
        return MyDbHelper.getInstance(context)?.reminderDao()?.
                getSingleReminder(firstGeofence.requestId)
    }

    private fun currentTime(): Date? {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minutes = c.get(Calendar.MINUTE)
        return SimpleDateFormat("HH:mm", Locale.ENGLISH).parse("$hour:$minutes")
    }

    private fun createAlarm(reminderData: ReminderData) {
        val intent = Intent(context, AlarmScreen::class.java)
        intent.putExtra("reminderData", reminderData)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}