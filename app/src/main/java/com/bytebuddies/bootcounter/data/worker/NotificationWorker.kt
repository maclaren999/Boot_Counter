package com.bytebuddies.bootcounter.data.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bytebuddies.bootcounter.data.database.BootDatabase
import com.bytebuddies.bootcounter.notification.NotificationUtils
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationWorker(val applicationContext: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = BootDatabase.getDatabase(applicationContext)
        val bootEventDao = db.bootEventDao()

        val notificationBody: String = runBlocking {
            val bootEvents = bootEventDao.getAllBootEvents()
            when {
                bootEvents.isEmpty() -> "No boots detected"
                bootEvents.size == 1 -> {
                    val lastBoot = bootEvents.first()
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    "The boot was detected = ${dateFormat.format(Date(lastBoot.timestamp))}"
                }
                else -> {
                    val lastBoot = bootEvents[0]
                    val secondLastBoot = bootEvents[1]
                    val timeDelta = lastBoot.timestamp - secondLastBoot.timestamp
                    val hours = TimeUnit.MILLISECONDS.toHours(timeDelta)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDelta) % 60
                    "Last boots time delta = $hours hours $minutes minutes"
                }
            }
        }

        NotificationUtils.showNotification(applicationContext, "Boot Event", notificationBody)
        return Result.success()
    }
}