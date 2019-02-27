package com.example.sapple.googlemaps.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.example.sapple.googlemaps.entities.ReminderData

@Database(entities = [ReminderData::class], version = 3)
abstract class MyDbHelper: RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        private var INSTANCE: MyDbHelper ?= null
        private const  val DATABASE_NAME = "ReminderApp.db"

        fun getInstance(context: Context): MyDbHelper ? {
            if(INSTANCE == null) {
                synchronized(MyDbHelper::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            MyDbHelper::class.java, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
//                            .addMigrations(MIGRATION_1_2)
                            .build()
                }
            }
            return INSTANCE
        }

        /*val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
            }
        }*/

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}