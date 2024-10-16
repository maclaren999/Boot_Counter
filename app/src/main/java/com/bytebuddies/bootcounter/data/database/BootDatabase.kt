package com.bytebuddies.bootcounter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bytebuddies.bootcounter.model.BootEvent

@Database(entities = [BootEvent::class], version = 1)
abstract class BootDatabase : RoomDatabase() {

    abstract fun bootEventDao(): BootEventDao

    companion object {
        @Volatile
        private var INSTANCE: BootDatabase? = null

        fun getDatabase(context: Context): BootDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BootDatabase::class.java,
                    "boot_counter_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}