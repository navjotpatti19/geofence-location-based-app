package com.example.sapple.googlemaps.adapters

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.sapple.googlemaps.R
import com.example.sapple.googlemaps.activities.AlarmScreen
import kotlinx.android.synthetic.main.list_reminders.view.*

class ReminderListItemAdapter(private val reminderListItem: ArrayList<String>, private val context: Context):
        RecyclerView.Adapter<ReminderListItemAdapter.ViewHolderReminderListItem>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderReminderListItem {
        return ViewHolderReminderListItem(LayoutInflater.from(context).inflate(R.layout.list_reminders,
                parent, false))
    }

    override fun getItemCount(): Int {
        return reminderListItem.size
    }

    override fun onBindViewHolder(holder: ViewHolderReminderListItem, position: Int) {
        holder.tvItem.text = reminderListItem[position]
        if(context is AlarmScreen) {
            holder.removeButton.visibility = View.GONE
        }
        holder.removeButton.setOnClickListener {
            popUp(position)
        }
    }

    inner class ViewHolderReminderListItem(view: View): RecyclerView.ViewHolder(view) {
        val tvItem = view.tv_itemName!!
        val removeButton = view.remove_button!!
    }

    private fun popUp(position: Int) {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(context.getString(R.string.app_name))
        alertDialog.setMessage(context.getString(R.string.delete_alert))
        alertDialog.setCancelable(false)
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes"
        ) { _, _ ->
            reminderListItem.removeAt(position)
            notifyDataSetChanged()
        }

        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No"
        ) { _, _ ->
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}
