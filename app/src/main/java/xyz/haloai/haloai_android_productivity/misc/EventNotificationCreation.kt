package xyz.haloai.haloai_android_productivity.misc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.MainActivity
import xyz.haloai.haloai_android_productivity.R
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.util.Calendar

// Receiver to create notifications for events and reminders
class EventNotificationCreation: BroadcastReceiver(), KoinComponent {


    private val scope = CoroutineScope(Dispatchers.IO) // Coroutine scope for database operations
    private val context = getKoin().get<Context>()
    private val scheduleDbViewModel: ScheduleDbViewModel by inject { parametersOf(context) }

    override fun onReceive(context: Context, intent: Intent) {

        createNotification(context, intent)
    }

    private fun createNotification(context: Context, intent: Intent) {
        // Extract details from the intent
        val reminderType = intent.getStringExtra("reminderType") ?: "HALOAI_Event"
        val alarmHelper by lazy { AlarmHelper(context) }
        if (reminderType == "HALOAI_reminderCheck") // Every morning at 9AM, notify user of today's tasks
        {
            scope.launch {
                // Fetch today's todos and create one notification to redirect to MainActivity
                val todayBeginningTime = Calendar.getInstance()
                todayBeginningTime.set(Calendar.HOUR_OF_DAY, 0)
                todayBeginningTime.set(Calendar.MINUTE, 0)
                todayBeginningTime.set(Calendar.SECOND, 0)
                val todayEODTime = Calendar.getInstance()
                todayEODTime.set(Calendar.HOUR_OF_DAY, 23)
                todayEODTime.set(Calendar.MINUTE, 59)
                todayEODTime.set(Calendar.SECOND, 59)
                val tasksForToday = scheduleDbViewModel.getTasksBetween(todayBeginningTime.time, todayEODTime.time)
                if (tasksForToday.isNotEmpty()) {
                    val numTasks = tasksForToday.size
                    val notifTitle = "You have $numTasks tasks today!"
                    val notifText = tasksForToday.joinToString("\n") { it.title }
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channelId = "xyz.haloai.productivity.eventNotifications"
                    val channelName = "Halo AI Task Notifications"
                    val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(channel)
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val notificationBuilder = NotificationCompat.Builder(context, channelId)
                        .setContentTitle(notifTitle)
                        .setContentText(notifText)
                        .setSmallIcon(R.mipmap.haloai_logo_foreground)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                    notificationManager.notify(0, notificationBuilder.build())
                }
            }
        }

        else if (reminderType == "HALOAI_Event") {
            scope.launch {// Regular event notification
                val startTime = intent.getStringExtra("startTime") ?: "now"
                val eventId = intent.getStringExtra("eventId") ?: "0"
                val event = scheduleDbViewModel.getEventById(eventId.toLong())
                if (startTime != DateFormat.format("HH:mm", event.startTime)
                        .toString()
                ) // Self-correcting notification
                {
                    alarmHelper.scheduleNotificationReminder(context, event)
                    return@launch
                }
                var notifTitle = "Starting soon!"
                if (intent.getStringExtra("startsIn") != null) {
                    notifTitle = "Starting in ${intent.getStringExtra("startsIn")}!"
                }
                var notifText = ""
                val endTime = event.endTime
                val eventName = event.title
                val location = event.location ?: ""
                if (endTime == null) {
                    notifText = "${eventName}: $startTime\n$location"
                } else {
                    notifText = "${eventName}: $startTime - $endTime\n$location"
                }

                // Create a notification
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "xyz.haloai.productivity.eventNotifications"
                val channelName = "Halo AI Event Notifications"

                val channel =
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)

                // Create intent to launch MainActivity when notification is clicked
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setContentTitle(notifTitle)
                    .setContentText(notifText)
                    .setSmallIcon(R.mipmap.haloai_logo_foreground)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)

                val notifId = eventId.toInt()
                notificationManager.notify(notifId, notificationBuilder.build())
            }
        }
    }
}



