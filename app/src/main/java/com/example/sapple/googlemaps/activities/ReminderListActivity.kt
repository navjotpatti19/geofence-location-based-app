package com.example.sapple.googlemaps.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.adapters.ReminderListAdapter
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.entities.ReminderData
import com.example.sapple.googlemaps.helperClasses.MyGlobals
import kotlinx.android.synthetic.main.activity_reminder_list.*

class ReminderListActivity : NavigationActivity() {

    private var dbHelper: MyDbHelper? = null
    private val reminderList = arrayListOf<String>()
    private var allData = arrayListOf<ReminderData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_list)
        clickListeners()
    }

    override fun onStart() {
        super.onStart()
        MyGlobals().checkLocation(this)
    }

    private fun clickListeners() {
        fab.setOnClickListener {
            val intent = Intent(this@ReminderListActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        dbHelper = MyDbHelper.getInstance(this@ReminderListActivity)
        getAndBindData()

        if(allData.isEmpty()) {
            tvAddReminder.visibility = View.VISIBLE
        }
    }

    private fun getAndBindData() {
        Thread {
            allData  = dbHelper?.reminderDao()?.getReminderData() as ArrayList<ReminderData>
            runOnUiThread {
                if (!allData.isEmpty()) {
                    tvAddReminder.visibility = View.GONE
                    for (values in allData) {
                        reminderList.add(values.reminderName!!)
                    }
                    setUpRecyclerView(recyclerView, this@ReminderListActivity)
                    val adapter = ReminderListAdapter(allData, this@ReminderListActivity)
                    recyclerView.adapter = null
                    recyclerView.adapter = adapter
                } else {
                    tvAddReminder.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun setUpRecyclerView(recyclerView: RecyclerView, activity: Activity) {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    override fun onDestroy() {
        super.onDestroy()
        MyDbHelper.destroyInstance()
    }
}
