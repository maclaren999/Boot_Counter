package com.bytebuddies.bootcounter.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bytebuddies.bootcounter.R

object NotificationUtils {

    private const val CHANNEL_ID = "boot_event_channel"
    private const val CHANNEL_NAME = "Boot Event Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications about boot events"

    fun createNotificationChannel(context: Context) {
        // Only create the channel on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationUtils", "Creating notification channel")
        }
    }

    fun showNotification(context: Context, notificationBody: String) {
        // Ensure the channel is created before showing the notification
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_24) // Replace with your app's icon
            .setContentTitle("Boot Counter")
            .setContentText(notificationBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(1, builder.build())
    }
}