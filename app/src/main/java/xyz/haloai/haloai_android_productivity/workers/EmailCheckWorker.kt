package xyz.haloai.haloai_android_productivity.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.ui.viewmodel.GmailViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MicrosoftGraphViewModel

class EmailCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams), KoinComponent {

    private val gmailViewModel: GmailViewModel by inject()
    private val microsoftViewModel: MicrosoftGraphViewModel by inject()
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun doWork(): Result {
        return runBlocking {
            try {
                gmailViewModel.checkForNewEmails()
                microsoftViewModel.checkForNewEmails()
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}