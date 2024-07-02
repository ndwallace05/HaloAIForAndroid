package xyz.haloai.haloai_android_productivity.misc

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import xyz.haloai.haloai_android_productivity.data.local.entities.ScheduleEntry
import java.util.Calendar
import java.util.Date

class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    fun scheduleNotificationReminder(context: Context, event: ScheduleEntry) {

        if (event.startTime != null) {
            this.setAlarmForEventReminder(event.id, event.startTime!!)
        }
    }

    fun updateNotificationReminder(context: Context, event: ScheduleEntry, oldStartTime: Date, oldEventId: Long) {

        // Delete the old notification
        deleteAlarmForEventReminder(oldEventId, oldStartTime)

        // Schedule the new notification
        scheduleNotificationReminder(context, event)
    }

    fun deleteAlarmForEventReminder(eventId: Long, eventStartTime: Date) {
        val intent = Intent(context, EventNotificationCreation::class.java)
        intent.putExtra("reminderType", "HALOAI_Event")
        intent.putExtra("startTime", DateFormat.format("HH:mm", eventStartTime).toString())
        intent.putExtra("eventId", eventId.toString())

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun setAlarmForEventReminder(eventId: Long, eventStartTime: Date) {
        val intent = Intent(context, EventNotificationCreation::class.java)
        // Format start time as "HH:mm"
        intent.putExtra("reminderType", "HALOAI_Event")
        intent.putExtra("startTime", DateFormat.format("HH:mm", eventStartTime).toString())
        intent.putExtra("eventId", eventId.toString())

        // Set the time for the notification to be 15 minutes before the event
        val notificationTime = Calendar.getInstance()
        notificationTime.time = eventStartTime
        notificationTime.add(Calendar.MINUTE, -15)

        // Use the hashCode as Uid is long, and the alarm manager requires an int. This distributes the alarms across the int space.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setExactAlarm(notificationTime.timeInMillis, pendingIntent)
    }

    private fun setExactAlarm(eventTimeInMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleExactAlarm(eventTimeInMillis, pendingIntent)
            } else {
                requestExactAlarmPermission()
            }
        } else {
            scheduleExactAlarm(eventTimeInMillis, pendingIntent)
        }
    }

    private fun scheduleExactAlarm(eventTimeInMillis: Long, pendingIntent: PendingIntent) {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            eventTimeInMillis,
            pendingIntent
        )
    }

    private fun requestExactAlarmPermission() {
        if (context is AppCompatActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context as AppCompatActivity).startActivityForResult(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM),
                    REQUEST_CODE_SCHEDULE_EXACT_ALARM
                )
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SCHEDULE_EXACT_ALARM = 1001
    }

}