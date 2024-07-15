package xyz.haloai.haloai_android_productivity.workers.workerFactory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import xyz.haloai.haloai_android_productivity.workers.CalendarUpdateWorker

class CalendarUpdateWorkerFactory: WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == CalendarUpdateWorker::class.java.name) {
            CalendarUpdateWorker(appContext, workerParameters)
        } else {
            null
        }
    }
}