package xyz.haloai.haloai_android_productivity.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun Context.scheduleEmailCheckWork() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val emailCheckWorkRequest = PeriodicWorkRequestBuilder<EmailCheckWorker>(30, TimeUnit
        .MINUTES).setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "EmailCheckWork",
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        emailCheckWorkRequest
    )
}

fun Context.scheduleCalendarUpdateWork() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val calendarUpdateWorkRequest = PeriodicWorkRequestBuilder<CalendarUpdateWorker>(1, TimeUnit.HOURS).setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "CalendarUpdateWork",
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        calendarUpdateWorkRequest
    )
}

fun Context.scheduleSuggestedTasksWork() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()


    val suggestedTasksWorkRequest = PeriodicWorkRequestBuilder<SuggestedTasksWorker>(6, TimeUnit.HOURS).setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "SuggestedTasksWork",
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        suggestedTasksWorkRequest
    )
}