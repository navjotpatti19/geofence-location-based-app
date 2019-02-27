package com.example.sapple.googlemaps.activities

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.WindowManager
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.adapters.ReminderListItemAdapter
import com.example.sapple.googlemaps.entities.ReminderData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_alarm_screen.*
import android.content.Intent
import android.media.Ringtone
import android.view.KeyEvent

class AlarmScreen : AppCompatActivity() {
    private lateinit var vibrator: Vibrator
    private lateinit var reminderData: ReminderData
    private var listItems = ArrayList<String>()
    private lateinit var ringtone: Ringtone

    override fun onAttachedToWindow() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onAttachedToWindow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_screen)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        if(intent.extras != null) {
            reminderData = intent.extras.get("reminderData") as ReminderData
            listItems.clear()
            listItems = Gson().fromJson(reminderData.listItems,
                    object : TypeToken<List<String>>() {}.type) as ArrayList<String>
        }

        tvReminderName.text = reminderData.reminderName.toString()

        setUpRecyclerView(rv_alarmItems, this@AlarmScreen)
        val adapter = ReminderListItemAdapter(listItems, this@AlarmScreen)
        rv_alarmItems.adapter = null
        rv_alarmItems.adapter = adapter

        clickListener()
        createAlarm()
    }

    private fun setUpRecyclerView(rv_alarmItems: RecyclerView, activity: Activity) {
        rv_alarmItems.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        rv_alarmItems.layoutManager = layoutManager
        rv_alarmItems.itemAnimator = DefaultItemAnimator()
    }

    private fun clickListener() {
        btn_alarmDismiss.setOnClickListener {
            ringtone.stop()
            vibrator.cancel()
            finish()
        }
    }

    private fun createAlarm() {
        try {
            val alert = RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(this, alert)
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = longArrayOf(0, 500, 1000)
            ringtone.play()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                vibrator.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE))
                vibrator.vibrate(pattern, 0)
            } else {
                vibrator.vibrate(pattern,0)
            }
        } catch (e:Exception) {
            Log.e("AlarmException :", "${e.printStackTrace()}")
        }
    }

    override fun onBackPressed() {
    }
}
