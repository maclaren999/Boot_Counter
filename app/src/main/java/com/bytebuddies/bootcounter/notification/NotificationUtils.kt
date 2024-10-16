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
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.bytebuddies.bootcounter.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

object NotificationUtils {

    private const val CHANNEL_ID = "boot_event_channel"
    private const val CHANNEL_NAME = "Boot Event Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications about boot events"
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private val NOTIFICATION_ACTIVE_KEY = booleanPreferencesKey("notification_active")

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

    suspend fun showNotification(context: Context, notificationBody: String) {
        // Ensure the channel is created before showing the notification
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Boot Counter")
            .setContentText(notificationBody)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(1, builder.build())

        // Store the flag indicating the notification is active
        context.dataStore.edit { settings ->
            settings[NOTIFICATION_ACTIVE_KEY] = true
        }
    }

    suspend fun clearNotificationFlag(context: Context) {
        context.dataStore.edit { settings ->
            settings[NOTIFICATION_ACTIVE_KEY] = false
        }
    }

    fun isNotificationActive(context: Context): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[NOTIFICATION_ACTIVE_KEY] ?: false
            }
    }
}