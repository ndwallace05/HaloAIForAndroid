package xyz.haloai.haloai_android_productivity.workers.workerFactory

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import xyz.haloai.haloai_android_productivity.workers.EmailCheckWorker
import xyz.haloai.haloai_android_productivity.workers.SuggestedTasksWorker

class SuggestedTasksWorkerFactory: WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return if (workerClassName == EmailCheckWorker::class.java.name) {
            SuggestedTasksWorker(appContext, workerParameters)
        } else {
            null
        }
    }
}