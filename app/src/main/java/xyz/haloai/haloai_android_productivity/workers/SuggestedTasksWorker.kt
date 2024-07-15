package xyz.haloai.haloai_android_productivity.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MiscInfoDbViewModel

class SuggestedTasksWorker (
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams), KoinComponent {
    val miscInfoDbViewModel: MiscInfoDbViewModel by inject()

    override fun doWork(): Result {
        // TODO: Lookup misc info db, see if suggested tasks have been generated, if not, generate.

        return Result.success()
    }
}