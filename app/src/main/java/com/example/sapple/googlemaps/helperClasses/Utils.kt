package com.example.sapple.googlemaps.helperClasses

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.sapple.googlemaps.entities.ReminderData
import com.example.sapple.googlemaps.receivers.DbFlagResetReceiver
import com.example.sapple.googlemaps.receivers.NotificationReceiver
import java.util.*

class Utils {

    //set notification
    fun notificationReceiver(context: Context, reminderData: ReminderData) {
        val time = reminderData.startTime?.split(":")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, time!![0].toInt())
        calendar.set(Calendar.MINUTE, time[1].toInt())
        calendar.set(Calendar.SECOND, 0)
        Utils().setNotificationReceiver(context, calendar, reminderData)
    }

    private fun setNotificationReceiver(activity: Context, calendar: Calendar, reminderData: ReminderData) {

        //alarm setting start
        val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(activity.applicationContext, NotificationReceiver::class.java)
        alarmIntent.action = reminderData.primaryId.toString()
        val pendingIntent = PendingIntent.getBroadcast(activity,
                 reminderData.primaryId!!.toInt(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis ,
                 AlarmManager.INTERVAL_DAY, pendingIntent)
        //end alarm settings
    }

    fun cancelNotificationReceiver(activity: Context, reminderData: ReminderData) {
        val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(activity.applicationContext, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(activity,
                reminderData.primaryId!!.toInt(), alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    fun setFlagResetReceiver(activity: Context) {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 0)

        //alarm setting start
        val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(activity.applicationContext, DbFlagResetReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(activity, 1, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis ,
                AlarmManager.INTERVAL_DAY, pendingIntent)
        //end alarm settings
    }
}