package com.example.sapple.googlemaps.database

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.example.sapple.googlemaps.entities.ReminderData

@Dao
interface ReminderDao {
    @Insert(onConflict = REPLACE)
    fun insertReminderData(reminderData: ReminderData)

    @Query("SELECT * from reminderData")
    fun getReminderData(): List<ReminderData>

    @Query("SELECT * from reminderData WHERE primaryId = :id")
    fun getSingleReminder(id: String): ReminderData

    @Delete
    fun deleteReminder(reminderData: ReminderData)

    @Update
    fun updateReminder(reminderData: ReminderData)

    @Query("SELECT * FROM reminderData ORDER BY primaryId DESC LIMIT 1")
    fun getLastReminder(): ReminderData
}