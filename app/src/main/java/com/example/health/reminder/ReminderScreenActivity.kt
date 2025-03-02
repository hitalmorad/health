package com.example.health.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
//import com.example.health.com.example.health.reminder.ReminderReceiver
import java.util.*

class ReminderScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderSetupUI()
        }
    }

    @Composable
    fun ReminderSetupUI() {
        val context = this
        var reminderText by remember { mutableStateOf("") }
        var selectedTime by remember { mutableStateOf("") }
        var reminderTimeInMillis by remember { mutableStateOf(0L) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            TextField(
                value = reminderText,
                onValueChange = { reminderText = it },
                label = { Text("Enter reminder") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val calendar = Calendar.getInstance()
                DatePickerDialog(context, { _, year, month, day ->
                    TimePickerDialog(context, { _, hour, minute ->
                        calendar.set(year, month, day, hour, minute, 0)
                        reminderTimeInMillis = calendar.timeInMillis
                        selectedTime = "$day/${month + 1}/$year $hour:$minute"
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }) {
                Text(text = "Pick Date & Time")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Selected Time: $selectedTime")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (reminderText.isNotEmpty() && reminderTimeInMillis > 0) {
                    setReminder(context, reminderText, reminderTimeInMillis)
                    Toast.makeText(context, "Reminder Set!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(context, "Enter reminder & select time!", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text(text = "Set Reminder")
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setReminder(context: Context, reminderText: String, reminderTimeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_title", reminderText)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent)

        saveReminderToLocalStorage(context, reminderText)
    }

    private fun saveReminderToLocalStorage(context: Context, reminderText: String) {
        val sharedPreferences = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val reminders = sharedPreferences.getStringSet("reminder_list", mutableSetOf()) ?: mutableSetOf()

        reminders.add(reminderText)
        editor.putStringSet("reminder_list", reminders)
        editor.apply()
    }
}
