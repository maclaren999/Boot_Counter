package com.bytebuddies.bootcounter

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bytebuddies.bootcounter.data.database.BootDatabase
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
    }
}