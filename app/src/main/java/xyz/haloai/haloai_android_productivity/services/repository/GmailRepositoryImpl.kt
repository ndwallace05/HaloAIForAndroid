package xyz.haloai.haloai_android_productivity.services.repository

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.calendar.model.Event
import org.koin.core.component.KoinComponent
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.services.GmailService
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.util.Calendar
import java.util.Date

interface GmailRepository {
    suspend fun getCalendarIdsForGoogleAccount(context: Context, emailId: String): MutableList<Pair<String, String>>

    suspend fun getEventsFromEmail(context: Context, emailId: String, calendarIds: List<String>): List<Event>

    suspend fun updateScheduleDbForEmail(scheduleDbViewModel: ScheduleDbViewModel, context:
    Context, emailId: String,
                                         calendarIds: List<String>)
}
class GmailRepositoryImpl(private val gmailApiService: GmailService) :
    GmailRepository, KoinComponent {

    override suspend fun getCalendarIdsForGoogleAccount(context: Context, emailId: String): MutableList<Pair<String, String>> {
        var calIds = mutableListOf<Pair<String, String>>()
        try {
            calIds = gmailApiService.getCalendarIdsForGoogleAccount(context, emailId)!!
        } catch (e: UserRecoverableAuthIOException) {
            // Let the UI layer handle this exception
            throw e
        } catch (e: Exception) {
            // Handle other exceptions (network errors, etc.)
            e.printStackTrace()
            throw e
        }
        return calIds
    }

    override suspend fun getEventsFromEmail(context: Context, emailId: String, calendarIds: List<String>):
            List<Event> {
        var events = listOf<Event>()
        try {
            events = gmailApiService.getEventsFromEmail(context, emailId, calendarIds)!!
        } catch (e: UserRecoverableAuthIOException) {
            // Let the UI layer handle this exception
            throw e
        } catch (e: Exception) {
            // Handle other exceptions (network errors, etc.)
            e.printStackTrace()
            throw e
        }
        return events
    }

    override suspend fun updateScheduleDbForEmail(scheduleDbViewModel: ScheduleDbViewModel, context: Context,
                                                  emailId:
    String, calendarIds: List<String>) {
        try {
            val events = gmailApiService.getEventsFromEmail(context, emailId, calendarIds)!!
            // Clear all events for this emailId
            scheduleDbViewModel.deleteEventsNotInList(context, emailId, events.map { it.id })
            var type: enumEventType = enumEventType.CALENDAR_EVENT
            for (event in events) {
                type = enumEventType.CALENDAR_EVENT
                var startTime: Date? = Date()
                if (event.start.dateTime != null) {
                    startTime!!.time = event.start.dateTime.value // UTC Normalized time
                }
                else if (event.start.date != null) {
                    startTime!!.time = event.start.date.value
                    // Offset by timezone to get the correct time
                    startTime.time -= Calendar.getInstance().timeZone.rawOffset
                    type = enumEventType.SCHEDULED_TASK
                }
                else {
                    startTime = null
                    type = enumEventType.UNSCHEDULED_TASK
                }
                var endTime: Date? = Date()
                if (event.end.dateTime != null) {
                    endTime!!.time = event.end.dateTime.value // UTC Normalized time
                }
                else {
                    endTime = startTime // If end time is not present, set it to start time, and mark it as a task.
                    if (startTime == null) {
                        type = enumEventType.UNSCHEDULED_TASK
                    }
                    else {
                        type = enumEventType.SCHEDULED_TASK
                    }
                }

                val nullFieldsList = mutableListOf<String>()
                if (event.description == null) {
                    nullFieldsList.add("description")
                }
                if (event.location == null) {
                    nullFieldsList.add("location")
                }
                if (event.attendees == null) {
                    nullFieldsList.add("attendees")
                }
                if (startTime == null) {
                    nullFieldsList.add("startTime")
                }
                if (endTime == null) {
                    nullFieldsList.add("endTime")
                }

                scheduleDbViewModel.insertOrUpdate(
                    context = context,
                    type = type,
                    title = event.summary,
                    description = event.description,
                    startTime = startTime,
                    endTime = endTime,
                    location = event.location,
                    attendees = event.attendees?.map { it.email },
                    sourceEmailId = emailId,
                    eventIdFromCal = event.id,
                    fieldsToSetAsNull = nullFieldsList
                )
            }
        } catch (e: UserRecoverableAuthIOException) {
            // Let the UI layer handle this exception
            throw e
        } catch (e: Exception) {
            // Handle other exceptions (network errors, etc.)
            e.printStackTrace()
            throw e
        }
    }
}