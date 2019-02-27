package com.example.sapple.googlemaps.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.R.id.clContent
import com.example.sapple.googlemaps.activities.AddReminderActivity
import com.example.sapple.googlemaps.activities.ReminderListActivity
import com.example.sapple.googlemaps.database.MyDbHelper
import com.example.sapple.googlemaps.entities.ReminderData
import com.example.sapple.googlemaps.helperClasses.MyGlobals
import com.example.sapple.googlemaps.helperClasses.Utils
import kotlinx.android.synthetic.main.activity_reminder_list.*
import kotlinx.android.synthetic.main.list_reminders.view.*
import java.io.Serializable

class ReminderListAdapter (private val items: ArrayList<ReminderData>, private val context: Context):
        RecyclerView.Adapter<ReminderListAdapter.ViewHolderReminderList>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderReminderList {
        return ViewHolderReminderList(LayoutInflater.from(context).inflate(R.layout.list_reminders,
                parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolderReminderList, position: Int) {
        holder.tvItem.text = items[position].reminderName
        holder.switch.isChecked = items[position].switchCheck!!

        holder.editButton.visibility = View.VISIBLE
        holder.editButton.setOnClickListener {
            MyGlobals.activityName = "UpdateReminderActivity"
            val intent = Intent(context, AddReminderActivity::class.java)
            intent.putExtra("reminderItems",items[position] as Serializable)
            context.startActivity(intent)
        }
        holder.removeButton.visibility = View.VISIBLE
        holder.removeButton.setOnClickListener {
            // delete row from database
            popUp(position)
        }

        holder.switch.visibility = View.VISIBLE
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            items[position].switchCheck = isChecked
            Thread {
                MyDbHelper.getInstance(context)?.reminderDao()?.updateReminder(items[position])
            }.start()
        }
    }

    inner class ViewHolderReminderList(view: View): RecyclerView.ViewHolder(view) {
        val tvItem = view.tv_itemName!!
        val removeButton = view.remove_button!!
        val editButton = view.edit_button!!
        val switch = view.switch_button!!

    }

    private fun popUp(position: Int) {
        val alertDialog = AlertDialog.Builder(this.context).create()
        alertDialog.setTitle(this.context.getString(R.string.app_name))
        alertDialog.setMessage(this.context.getString(R.string.delete_alert))
        alertDialog.setCancelable(false)
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes"
        ) { _, _ ->
            Thread {
                MyDbHelper.getInstance(context)?.reminderDao()?.deleteReminder(items[position])
                Utils().cancelNotificationReceiver(context, items[position])
                (context as ReminderListActivity).runOnUiThread {
                    removeReminder(items[position])
                    items.removeAt(position)
                    notifyDataSetChanged()
                    if(items.isEmpty()) {
                        (context).tvAddReminder.visibility = View.VISIBLE
                    }
                }
            }.start()
        }

        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No"
        ) { _, _ ->
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun removeReminder(reminder: ReminderData) {
        (context as ReminderListActivity).getRepository().remove(
                reminder,
                success = {
                    Toast.makeText(context, R.string.reminder_removed_success, Toast.LENGTH_LONG).show()
//                    Snackbar.make(clContent, R.string.reminder_removed_success,
//                            Snackbar
//                            .LENGTH_LONG).show()
                },
                failure = {
//                    Snackbar.make(clCon, it, Snackbar.LENGTH_LONG).show()
                })
    }
}

