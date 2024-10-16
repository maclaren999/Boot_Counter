package com.bytebuddies.bootcounter.data.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.bytebuddies.bootcounter.model.BootEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.Date
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.bytebuddies.bootcounter.data.database.BootDatabase
import com.bytebuddies.bootcounter.data.worker.NotificationWorker
import com.bytebuddies.bootcounter.data.worker.Scheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                BootDatabase::class.java, "boot_counter_database"
            ).build()

            CoroutineScope(Dispatchers.IO).launch {
                db.bootEventDao().insert(BootEvent(timestamp = Date().time))
            }

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "BootCounterNotification",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )

            // Schedule the repeating task
            Scheduler.scheduleRepeatingTask(context)

            // Optional: Check if a notification was present before reboot and restore it
            // This part can be implemented based on specific requirements
        }
    }
}