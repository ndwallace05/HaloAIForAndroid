package xyz.haloai.haloai_android_productivity.workers.workerFactory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import xyz.haloai.haloai_android_productivity.workers.CalendarUpdateWorker
import xyz.haloai.haloai_android_productivity.workers.EmailCheckWorker

class CombinedWorkerFactory(
    private val emailCheckWorkerFactory: EmailCheckWorkerFactory,
    private val calendarUpdateWorkerFactory: CalendarUpdateWorkerFactory,
    private val suggestedTasksWorkerFactory: SuggestedTasksWorkerFactory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            EmailCheckWorker::class.java.name -> emailCheckWorkerFactory.createWorker(appContext, workerClassName, workerParameters)
            CalendarUpdateWorker::class.java.name -> calendarUpdateWorkerFactory.createWorker(appContext, workerClassName, workerParameters)
            SuggestedTasksWorkerFactory::class.java.name -> suggestedTasksWorkerFactory.createWorker(appContext, workerClassName, workerParameters)
            else -> null
        }
    }
}
