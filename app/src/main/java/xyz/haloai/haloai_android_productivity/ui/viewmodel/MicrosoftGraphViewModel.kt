package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import xyz.haloai.haloai_android_productivity.services.repository.MicrosoftGraphRepository
import java.util.Date

class MicrosoftGraphViewModel(private val microsoftGraphRepository: MicrosoftGraphRepository) : ViewModel() {

    suspend fun getCalendarIdsForMicrosoftAccount(context: Context, coroutineScope: CoroutineScope): MutableList<Pair<String, String>> {
        return microsoftGraphRepository.getCalendarIdsForMicrosoftAccount(context, coroutineScope)
    }

    suspend fun updateScheduleDbForEmail(emailId: String, context: Context, coroutineScope: CoroutineScope, startDate: Date?, endDate: Date?) {
        return microsoftGraphRepository.updateScheduleDbForEmail(emailId, context, coroutineScope, startDate, endDate)
    }

    suspend fun checkForNewEmails() {
        // Add logic to check and process new emails
        microsoftGraphRepository.checkAndProcessNewEmails()
        // Example: repository.checkAndProcessNewEmails()
    }

}