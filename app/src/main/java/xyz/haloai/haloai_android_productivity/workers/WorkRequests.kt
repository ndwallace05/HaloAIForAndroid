package xyz.haloai.haloai_android_productivity.workers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun Context.scheduleEmailCheckWork() {
    val emailCheckWorkRequest = PeriodicWorkRequestBuilder<EmailCheckWorker>(30, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "EmailCheckWork",
        ExistingPeriodicWorkPolicy.KEEP,
        emailCheckWorkRequest
    )
}

fun Context.scheduleCalendarUpdateWork() {
    val calendarUpdateWorkRequest = PeriodicWorkRequestBuilder<CalendarUpdateWorker>(1, TimeUnit.HOURS)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "CalendarUpdateWork",
        ExistingPeriodicWorkPolicy.KEEP,
        calendarUpdateWorkRequest
    )
}

fun Context.scheduleSuggestedTasksWork() {
    val suggestedTasksWorkRequest = PeriodicWorkRequestBuilder<SuggestedTasksWorker>(6, TimeUnit.HOURS)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "SuggestedTasksWork",
        ExistingPeriodicWorkPolicy.KEEP,
        suggestedTasksWorkRequest
    )
}