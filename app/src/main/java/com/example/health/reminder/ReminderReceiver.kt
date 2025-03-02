package com.example.health.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.health.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val reminderText = intent?.getStringExtra("reminder_title") ?: "You have a reminder!"

        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.cons)
            .setContentTitle("Reminder")
            .setContentText(reminderText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
        saveNotification(context, reminderText)
    }
    private fun saveNotification(context: Context, reminderText: String) {
        val sharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val notifications = sharedPreferences.getStringSet("notification_list", mutableSetOf()) ?: mutableSetOf()

        notifications.add(reminderText)
        editor.putStringSet("notification_list", notifications)
        editor.apply()
    }
}
