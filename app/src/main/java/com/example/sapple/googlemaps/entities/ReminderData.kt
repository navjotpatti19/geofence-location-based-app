package com.example.sapple.googlemaps.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "reminderData")
data class ReminderData (
        @PrimaryKey(autoGenerate = true) var primaryId: Long?,
        @ColumnInfo(name = "flag") var flag: Boolean?,
        @ColumnInfo(name = "reminderName") var reminderName: String?,
        @ColumnInfo(name = "startTime") var startTime: String?,
        @ColumnInfo(name = "endTime") var endTime: String?,
        @ColumnInfo(name = "latitude") var latitude: String?,
        @ColumnInfo(name = "longitude") var longitude: String?,
        @ColumnInfo(name = "distance") var distance: Int?,
        @ColumnInfo(name = "locationName") var locationName: String?,
        @ColumnInfo(name = "identifier") var identifier: Int?,
        @ColumnInfo(name = "inOrOut") var inOrOutFlag: Int?,
        @ColumnInfo(name = "reminderType") var reminderType: Int?,
        @ColumnInfo(name = "notificationReady") var notificationReady: Boolean?,
        @ColumnInfo(name = "switchCheck") var switchCheck: Boolean?,
        @ColumnInfo(name = "listItem") var listItems: String?): Serializable {
            constructor():this(null,false, "", "", "",
                    "", "", 0, "", 0, 0,
                    0, false, true, "")
        }
