package com.example.sapple.googlemaps.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.adapters.ReminderListItemAdapter
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.entities.ReminderData
import com.example.sapple.googlemaps.helperClasses.MyGlobals
import com.example.sapple.googlemaps.helperClasses.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_add_reminder.*

class AddReminderActivity : NavigationActivity(), View.OnClickListener {

    private var itemsList = arrayListOf<String>()
    private var latitude: String? = null
    private var longitude: String? = null
    private var locationName: String? = null
    private val myGlobals = MyGlobals()
    private var dbHelper: MyDbHelper? = null
    var reminderData = ReminderData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reminder)

        //get an instance of database
        dbHelper = MyDbHelper.getInstance(this@AddReminderActivity)
        if(intent.extras != null) {
            fab.setImageDrawable(resources.getDrawable(R.drawable.add_button, this.theme))
            if(MyGlobals.activityName == "AddReminderActivity") {
                latitude = intent.getStringExtra("LAT")
                longitude = intent.getStringExtra("LNG")
                locationName = intent.getStringExtra("LOCATION")
                etLat.setText(latitude)
                etLng.setText(longitude)
                etLocName.setText(locationName)
            } else {
                fab.setImageDrawable(resources.getDrawable(R.drawable.update_button, this.theme))
                reminderData = intent.extras.get("reminderItems") as ReminderData
                etReminderName.setText(reminderData.reminderName)
                etStartTime.setText(reminderData.startTime)
                etEndTime.setText(reminderData.endTime)
                etLat.setText(reminderData.latitude)
                etLng.setText(reminderData.longitude)
                etLocName.setText(reminderData.locationName)
                etDistance.setText(reminderData.distance.toString())

                when {
                    reminderData.reminderType!! == 0 -> rb_onlyOnce.isChecked = true  // 0 is for only once
                    reminderData.reminderType!! == 1 -> rb_oncePerDay.isChecked = true  // 1 is for once per day
                    reminderData.reminderType!! == 2 -> rb_everytime.isChecked = true  // 2 is for every time
                }

                if(reminderData.inOrOutFlag!! == 0) {
                    rb_in.isChecked = true
                } else {
                    rb_out.isChecked = true
                }
                itemsList.clear()
                itemsList = Gson().fromJson(reminderData.listItems,
                        object : TypeToken<List<String>>() {}.type) as ArrayList<String>
            }
        }

        listeners()
        setUpRecyclerView(rv, this@AddReminderActivity)
        setUpAdapter()
    }

    private fun setUpRecyclerView(recyclerView: RecyclerView, activity: Activity) {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun setUpAdapter() {
        val adapter = ReminderListItemAdapter(itemsList, this@AddReminderActivity)
        rv.adapter = null
        rv.adapter = adapter
    }

    private fun listeners() {
        etStartTime.setOnClickListener(this)
        etEndTime.setOnClickListener(this)
        btnAddItem.setOnClickListener(this)
        fab.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when(v.id) {
            R.id.etStartTime -> myGlobals.timePicker(this@AddReminderActivity, etStartTime)

            R.id.etEndTime -> myGlobals.timePicker(this@AddReminderActivity, etEndTime)

            R.id.fab -> {
                if(mandatoryFieldsFilled()) {
                    if(etDistance.text.toString().toInt() in 50..10000) {
                        insertOrUpdateData()

                        if(MyGlobals.activityName == "AddReminderActivity") {
                            popUp(resources.getString(R.string.add_reminder))
                        } else {
                            popUp(resources.getString(R.string.reminder_update))
                        }
                    } else {
                        Toast.makeText(this@AddReminderActivity, "Distance should be " +
                                "in between 50 metres to 10000 metres", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@AddReminderActivity, " Fill all fields first",
                            Toast.LENGTH_LONG).show()
                }
            }

            R.id.btnAddItem -> {
                if(!TextUtils.isEmpty(etAddItem.text.toString())) {
                    itemsList.add(etAddItem.text.toString())
                    etAddItem.text.clear()
                    rv.adapter.notifyDataSetChanged()
                    Toast.makeText(this@AddReminderActivity, "Item Added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AddReminderActivity, "Empty item cannot be added", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // method to insert the data in the room database
    private fun insertOrUpdateData() {
        Thread {
            setData()
            if(MyGlobals.activityName == "AddReminderActivity") {
                dbHelper?.reminderDao()?.insertReminderData(reminderData)
                val lastReminder = dbHelper?.reminderDao()?.getLastReminder()

                Utils().notificationReceiver(this@AddReminderActivity, lastReminder!!)
                addReminder(lastReminder)
            } else {
                dbHelper?.reminderDao()?.updateReminder(reminderData)
                Utils().notificationReceiver(this@AddReminderActivity, reminderData)
                addReminder(reminderData)
            }
        }.start()
    }

    private fun addReminder(reminder: ReminderData) {

        getRepository().add(reminder,
                success = {
                    Toast.makeText(this, "Reminder Added", Toast.LENGTH_SHORT).show()
                },
                failure = {
                    Snackbar.make(rl, it, Snackbar.LENGTH_LONG).show()
                })
    }

    //method to set the data in objects
    private fun setData() {
        reminderData.reminderName = etReminderName.text.toString()
        reminderData.startTime = etStartTime.text.toString()
        reminderData.endTime = etEndTime.text.toString()
        reminderData.latitude = etLat.text.toString()
        reminderData.longitude = etLng.text.toString()
        reminderData.distance = etDistance.text.toString().toInt()
        reminderData.locationName = etLocName.text.toString()
        reminderData.listItems = Gson().toJson(itemsList)

        reminderData.notificationReady = false
        if(rb_in.isChecked) {
            reminderData.inOrOutFlag = 0
        } else {
            reminderData.inOrOutFlag = 1
        }

        when {
            rb_onlyOnce.isChecked -> reminderData.reminderType = 0
            rb_oncePerDay.isChecked -> reminderData.reminderType = 1
            rb_everytime.isChecked ->  {
                reminderData.reminderType = 2
                reminderData.flag = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MyDbHelper.destroyInstance()
    }

    private fun mandatoryFieldsFilled(): Boolean {
        return when {
            etReminderName.text.toString() == "" -> false
            etStartTime.text.toString() == "" -> false
            etStartTime.text.toString() == "" -> false
            TextUtils.isEmpty(etDistance.text.toString()) -> false
            else -> true
        }
    }

    private fun popUp(msg: String) {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(getString(R.string.app_name))
        alertDialog.setMessage(msg)
        alertDialog.setCancelable(false)
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK"
        ) { _, _ ->
            finish()
        }
        alertDialog.show()
    }
}
