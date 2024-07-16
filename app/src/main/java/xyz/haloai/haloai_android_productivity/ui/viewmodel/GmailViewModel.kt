package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.api.services.calendar.model.Event
import xyz.haloai.haloai_android_productivity.services.repository.GmailRepository

class GmailViewModel(private val gmailRepository: GmailRepository) : ViewModel() {

    // Launch authorization flow
    suspend fun getCalendarIdsForGoogleAccount(context: Context, emailId: String): MutableList<Pair<String, String>> {
        return gmailRepository.getCalendarIdsForGoogleAccount(context, emailId)
    }

    suspend fun getEventsFromEmail(context: Context, emailId: String, calendarIds: List<String>):
            List<Event> {
        return gmailRepository.getEventsFromEmail(context, emailId, calendarIds)
    }

    suspend fun updateScheduleDbForEmail(context: Context, emailId: String, calendarIds: List<String>) {
        return gmailRepository.updateScheduleDbForEmail(context, emailId, calendarIds)
    }

    suspend fun checkForNewEmails() {
        gmailRepository.checkAndProcessNewEmails()
    }

}

