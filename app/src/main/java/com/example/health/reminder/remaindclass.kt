package com.example.health.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class ReminderActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
        setContent {
            MaterialTheme(
                colors = lightColors(
                    primary = Color(0xFFFF4C5D),
                    background = Color.White,
                    surface = Color.White
                )
            ) {
                ReminderScreen()
            }
        }
    }
}

data class Reminder(
    val title: String,
    val description: String,
    val time: String,
    val duration: String = "",
    val completed: Boolean = false
)

data class Rem(val title: String, val description: String, val time: Long)

@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
@Composable
fun ReminderScreen() {
    val context = LocalContext.current
    var reminders by remember { mutableStateOf(loadReminders(context)) }
    var showAddReminder by remember { mutableStateOf(false) }
    val backgroundColor = Color(0xFFF5F6FA)
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        DateSelector { date ->
            selectedDate = date
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Today's Reminder",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Filter reminders for the selected date
            val filteredReminders = reminders.filter { reminder ->
                isSameDate(reminder.time, selectedDate)
            }

            if (filteredReminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reminders for this day", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(filteredReminders) { reminder ->
                        ReminderItem(reminder, onDelete = {
                            deleteReminder(context, reminder)
                            reminders = loadReminders(context)
                        })
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showAddReminder = true },
                backgroundColor = Color(0xFFFF4C5D),
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (showAddReminder) {
        AddReminderScreen(
            onDismiss = { showAddReminder = false },
            onSave = { title, description, time ->
                val newReminder = Reminder(title, description, time, completed = false)
                saveReminderToLocalStorage(context, "$title|$description|$time||false")
                reminders = loadReminders(context)
                showAddReminder = false

                val rem = Rem(title, description, parseReminderTime(time))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setReminder(context, rem)
                }
            }
        )
    }
}

// Helper function to check if reminder date matches selected date
private fun isSameDate(timeString: String, selectedDate: Calendar): Boolean {
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return try {
        val reminderDate = format.parse(timeString.substringBefore(" at "))
        val reminderCalendar = Calendar.getInstance().apply { time = reminderDate }
        reminderCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                reminderCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                reminderCalendar.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)
    } catch (e: Exception) {
        false
    }
}

@Composable
fun DateSelector(onDateSelected: (Calendar) -> Unit) {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val days = remember {
        val daysData = mutableListOf<Triple<Int, String, Calendar>>()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        for (i in 0 until 7) {
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time).uppercase().take(3)
            val date = calendar.clone() as Calendar
            daysData.add(Triple(day, dayOfWeek, date))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        daysData
    }

    var selectedDay by remember { mutableStateOf(today) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { (day, dayOfWeek, date) ->
            DayItem(
                day = day,
                dayOfWeek = dayOfWeek,
                isSelected = day == selectedDay,
                monthStatus = if (date.get(Calendar.MONTH) == currentMonth && date.get(Calendar.YEAR) == currentYear) "" else "other",
                onClick = {
                    selectedDay = day
                    onDateSelected(date)
                }
            )
        }
    }
}

@Composable
fun DayItem(day: Int, dayOfWeek: String, isSelected: Boolean, monthStatus: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(48.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = dayOfWeek,
            fontSize = 12.sp,
            color = if (isSelected) Color.Gray else Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .size(36.dp)
                .background(
                    color = if (isSelected) Color(0xFFFF4C5D) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Text(
                text = day.toString(),
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun ReminderItem(reminder: Reminder, onDelete: () -> Unit) {
    val cardColor = Color.White
    val timeColor = Color(0xFF7E7E7E)
    val accentColor = Color(0xFF00BCD4)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 2.dp,
        backgroundColor = cardColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Time",
                        tint = timeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reminder.time,
                        fontSize = 12.sp,
                        color = timeColor
                    )

                    if (reminder.duration.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Duration",
                            tint = timeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reminder.duration,
                            fontSize = 12.sp,
                            color = timeColor
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, CircleShape)
                        .clip(CircleShape)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Complete",
                        tint = if (reminder.completed) Color.Green else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (reminder.title.contains("Ayurveda", ignoreCase = true)) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(accentColor, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun AddReminderScreen(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf(calendar.timeInMillis) }
    var selectedTimeString by remember { mutableStateOf("08:00 AM") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(selectedDate))

    val context = LocalContext.current

    if (showDatePicker) {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        DisposableEffect(Unit) {
            datePickerDialog.show()
            onDispose {
                datePickerDialog.dismiss()
            }
        }
    }

    if (showTimePicker) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                selectedTimeString = timeFormat.format(cal.time)
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )

        DisposableEffect(Unit) {
            timePickerDialog.show()
            onDispose {
                timePickerDialog.dismiss()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add Reminder") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedDate,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEEEEEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Select Date",
                                tint = Color(0xFFFF4C5D),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedTimeString,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEEEEEE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Select Time",
                                tint = Color(0xFFFF4C5D),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quick Options",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickTimeButton("Morning", "08:00 AM") {
                        selectedTimeString = it
                    }

                    QuickTimeButton("Afternoon", "02:00 PM") {
                        selectedTimeString = it
                    }

                    QuickTimeButton("Evening", "08:00 PM") {
                        selectedTimeString = it
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        val fullDateTime = "$formattedDate at $selectedTimeString"
                        onSave(title, description, fullDateTime)
                    } else {
                        Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF4C5D))
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickTimeButton(label: String, time: String, onSelect: (String) -> Unit) {
    Button(
        onClick = { onSelect(time) },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFFEEEEEE),
            contentColor = Color(0xFF333333)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

private fun parseReminderTime(timeString: String): Long {
    val format = SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.getDefault())
    return try {
        val date = format.parse(timeString)
        date?.time ?: System.currentTimeMillis() + 3600000
    } catch (e: Exception) {
        System.currentTimeMillis() + 3600000
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private fun setReminder(context: Context, reminder: Rem) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("reminder_title", reminder.title)
        putExtra("reminder_description", reminder.description)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.title.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.time, pendingIntent)
}

@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
private fun saveReminderToLocalStorage(context: Context, reminderData: String) {
    val sharedPreferences = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val reminders = sharedPreferences.getStringSet("reminder_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    reminders.add(reminderData)
    editor.putStringSet("reminder_list", reminders)
    editor.apply()
}

private fun loadReminders(context: Context): List<Reminder> {
    val sharedPreferences = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
    val reminderSet = sharedPreferences.getStringSet("reminder_list", mutableSetOf()) ?: mutableSetOf()

    return reminderSet.map { reminderData ->
        val parts = reminderData.split("|")
        if (parts.size >= 3) {
            val title = parts[0]
            val description = parts[1]
            val time = parts[2]
            val duration = if (parts.size > 3) parts[3] else ""
            val completed = if (parts.size > 4) parts[4].toBoolean() else false
            Reminder(title, description, time, duration, completed)
        } else {
            Reminder(parts[0], "", "", "", false)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.GINGERBREAD)
private fun deleteReminder(context: Context, reminder: Reminder) {
    val sharedPreferences = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
    val reminderSet = sharedPreferences.getStringSet("reminder_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

    val toRemove = reminderSet.find { it.contains(reminder.title) }
    if (toRemove != null) {
        reminderSet.remove(toRemove)
        sharedPreferences.edit().putStringSet("reminder_list", reminderSet).apply()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("reminder_title") ?: "Reminder"
        val description = intent.getStringExtra("reminder_description") ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminders_channel",
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "reminders_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(title.hashCode(), notification)

        updateReminderCompletion(context, title)
    }

    @RequiresApi(Build.VERSION_CODES.GINGERBREAD)
    private fun updateReminderCompletion(context: Context, title: String) {
        val sharedPreferences = context.getSharedPreferences("reminders", Context.MODE_PRIVATE)
        val reminderSet = sharedPreferences.getStringSet("reminder_list", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val updatedSet = reminderSet.map { reminderData ->
            val parts = reminderData.split("|")
            if (parts[0] == title) {
                val description = parts.getOrElse(1) { "" }
                val time = parts.getOrElse(2) { "" }
                val duration = parts.getOrElse(3) { "" }
                "$title|$description|$time|$duration|true"
            } else {
                reminderData
            }
        }.toSet()

        sharedPreferences.edit().putStringSet("reminder_list", updatedSet).apply()
    }
}