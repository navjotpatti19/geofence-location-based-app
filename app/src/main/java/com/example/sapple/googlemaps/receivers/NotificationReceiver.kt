package com.example.sapple.googlemaps.receivers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.activities.ReminderItemsActivity
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.entities.ReminderData
import java.text.SimpleDateFormat
import java.util.*
import android.net.ConnectivityManager

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected) {

        } else {
            Thread {
                val reminder = MyDbHelper.getInstance(context)?.reminderDao()?.getSingleReminder(intent?.action!!)
                if (reminder != null) {
                    val endTime = reminder.endTime
                    val startTime = reminder.startTime
                    val eTime = SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(endTime)
                    val sTime = SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(startTime)
                    if (sTime.time <= currentTime()!!.time && eTime.time > currentTime()!!.time
                            && !reminder.flag!!) {
                        createNotification(context, reminder)
                    }
                }
            }.start()
        }
    }

    private fun currentTime(): Date? {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minutes = c.get(Calendar.MINUTE)
        return SimpleDateFormat("HH:mm", Locale.ENGLISH).parse("$hour:$minutes")
    }

    private fun createNotification(context: Context?, reminderData: ReminderData) {
        lateinit var notification: Notification
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* Create notification channel , but only on 26+
            because the NotificationChannel is new and not in the support library */
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = context.getColor(R.color.green)
            notificationChannel.description = "Hello Notification"
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notifyIntent = Intent(context, ReminderItemsActivity::class.java)
        val title = context.getString(R.string.app_name)
        val message = context.getString(R.string.notification_msg)
        notifyIntent.putExtra("reminderItems", reminderData)
        notifyIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent = PendingIntent.getActivity(context,
                0, notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val res = context.resources
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             notification = Notification.Builder(context, CHANNEL_ID)
                    //set the intent that will fire when the user will tap on the notification
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.add_button)
                    .setLargeIcon(BitmapFactory.decodeResource(res,R.mipmap.ic_launcher))
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(Notification.BigTextStyle().bigText(message))
                    .setSound(uri)
                    .setVibrate(longArrayOf(500, 1000))
                    .setOnlyAlertOnce(true)
                    .setContentText(message).build()
        } else {
            notification = Notification.Builder(context)
                    //set the intent that will fire when the user will tap on the notification
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.add_button)
                    .setLargeIcon(BitmapFactory.decodeResource(res,R.mipmap.ic_launcher))
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(Notification.BigTextStyle().bigText(message))
                    .setSound(uri)
                    .setVibrate(longArrayOf(500, 1000))
                    .setOnlyAlertOnce(true)
                    .setContentText(message).build()
        }
        notificationManager.notify(((Date().time / 1000L) % Integer.MAX_VALUE).toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "ChannelId"
        const val CHANNEL_NAME = "Sample Notification"
    }
}