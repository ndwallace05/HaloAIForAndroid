package xyz.haloai.haloai_android_productivity.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.util.Date

class CalendarUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams), KoinComponent {

    private val scheduleDbViewModel: ScheduleDbViewModel by inject { parametersOf (context) }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun doWork(): Result {
        val startDate = Date()
        val endDate = Date()
        // Start date is one week prior to today
        startDate.time = startDate.time - 7 * 24 * 60 * 60 * 1000
        // End date is two weeks after today
        endDate.time = endDate.time + 14 * 24 * 60 * 60 * 1000
        // Update the schedule database
        return runBlocking {
            try {
                scheduleDbViewModel.updateScheduleDb(context, coroutineScope, startDate, endDate)
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}