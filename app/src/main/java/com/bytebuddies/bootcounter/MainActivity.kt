package com.bytebuddies.bootcounter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bytebuddies.bootcounter.data.database.BootDatabase
import com.bytebuddies.bootcounter.data.worker.Scheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        checkNotificationPermission()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bootEventsTextView: TextView = findViewById(R.id.bootEventsTextView)
        val db = BootDatabase.getDatabase(applicationContext)
        val bootEventDao = db.bootEventDao()

        CoroutineScope(Dispatchers.IO).launch {
            val bootEvents = bootEventDao.getAllBootEvents()
            val bootEventsText = if (bootEvents.isEmpty()) {
                "No boots detected"
            } else {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                bootEvents.groupBy { dateFormat.format(it.timestamp) }
                    .map { (date, events) -> "$date - ${events.size}" }
                    .joinToString("\n")
            }

            withContext(Dispatchers.Main) {
                bootEventsTextView.text = bootEventsText
            }
        }

        // Schedule the repeating task
        Scheduler.scheduleRepeatingTask(applicationContext)
    }

    fun checkNotificationPermission() {
        val REQUEST_CODE_POST_NOTIFICATIONS = 1001
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }
    }
}