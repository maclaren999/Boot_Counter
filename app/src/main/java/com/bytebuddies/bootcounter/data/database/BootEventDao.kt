package com.bytebuddies.bootcounter.data.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bytebuddies.bootcounter.model.BootEvent

@Dao
interface BootEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bootEvent: BootEvent)

    @Query("SELECT * FROM boot_events ORDER BY timestamp DESC")
    suspend fun getAllBootEvents(): List<BootEvent>

    @Query("SELECT * FROM boot_events ORDER BY timestamp DESC LIMIT 2")
    suspend fun getLastTwoBootEvents(): List<BootEvent>

    @Query("SELECT DATE(timestamp / 1000, 'unixepoch') as date, COUNT(*) as count FROM boot_events GROUP BY date")
    suspend fun getBootEventsGroupedByDate(): List<BootEventCount>
}

data class BootEventCount(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "count") val count: Int
)