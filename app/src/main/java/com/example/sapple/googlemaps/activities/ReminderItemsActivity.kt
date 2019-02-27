package com.example.sapple.googlemaps.activities

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.adapters.ReminderListItemAdapter
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.entities.ReminderData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_reminder_items.*

class ReminderItemsActivity : AppCompatActivity() {

    private var listItems = ArrayList<String>()
    private var reminderData = ReminderData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_items)

        if(intent.extras != null) {
            reminderData = intent.extras.get("reminderItems") as ReminderData
            listItems.clear()
            listItems = Gson().fromJson(reminderData.listItems,
                    object : TypeToken<List<String>>() {}.type) as ArrayList<String>
        }

        clickListeners()
        setUpRecyclerView(recyclerView, this@ReminderItemsActivity)
        val adapter = ReminderListItemAdapter(listItems, this@ReminderItemsActivity)
        recyclerView.adapter = null
        recyclerView.adapter = adapter
    }

    private fun setUpRecyclerView(recyclerView: RecyclerView, activity: Activity) {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun clickListeners() {
        fabButton.setOnClickListener {
            //showAlertDialog()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MyDbHelper.destroyInstance()
    }
}
