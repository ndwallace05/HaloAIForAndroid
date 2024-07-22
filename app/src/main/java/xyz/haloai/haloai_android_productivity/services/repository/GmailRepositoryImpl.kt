package xyz.haloai.haloai_android_productivity.services.repository

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel
import xyz.haloai.haloai_android_productivity.services.GmailService
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MiscInfoDbViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ProductivityFeedViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

interface GmailRepository {
    suspend fun getCalendarIdsForGoogleAccount(context: Context, emailId: String): MutableList<Pair<String, String>>

    suspend fun getEventsFromEmail(context: Context, emailId: String, calendarIds: List<String>): List<Event>

    suspend fun updateScheduleDbForEmail(context: Context, emailId: String, calendarIds: List<String>)

    suspend fun checkAndProcessNewEmails()

}
class GmailRepositoryImpl(private val gmailApiService: GmailService) :
    GmailRepository, KoinComponent {

    private val context: Context = getKoin().get()
    private val scheduleDbViewModel: ScheduleDbViewModel by inject { parametersOf (context) }
    private val miscInfoDbViewModel: MiscInfoDbViewModel by inject()
    private val productivityFeedViewModel: ProductivityFeedViewModel by inject()
    private val emailDbViewModel: EmailDbViewModel by inject()
    private val openAIViewModel: OpenAIViewModel by inject()
    private val emailCheckerMutex = Mutex()

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

    override suspend fun updateScheduleDbForEmail(context: Context, emailId: String, calendarIds: List<String>) {
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
            Log.d("GmailRepositoryImpl", "Error in updateScheduleDbForEmail: ${e.message}")
            Log.d("GmailRepositoryImpl", "Error in updateScheduleDbForEmail: ${e.stackTrace}")
        }
    }

    override suspend fun checkAndProcessNewEmails() {
        emailCheckerMutex.withLock {
            try {
                // First, get last processed date from misc info db (Format: "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                val lastProcessedDate = miscInfoDbViewModel.get("lastProcessedDate_Gmail") as String?
                val dateToUse: Date
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
                if (lastProcessedDate == null) {
                    // Insert yesterday's date as last processed date
                    dateToUse = Calendar.getInstance().time
                    dateToUse.time -= 24 * 60 * 60 * 1000
                    // miscInfoDbViewModel.updateOrCreate("lastProcessedDate_Gmail", dateFormatter.format(dateToUse))
                    // If no last processed date, get all emails from the beginning
                    // and process them
                } else {
                    dateToUse = dateFormatter.parse(lastProcessedDate)!!
                }
                // Get all gmail email IDs
                val allGmailIds = emailDbViewModel.getGoogleAccountsAdded()

                // Get all emails from the last processed date
                for (emailId in allGmailIds) {
                    val emails = gmailApiService.getEmailsAfterDate(
                        context = context,
                        emailId = emailId,
                        date = dateToUse
                    )
                    val conversationThreadIdsProcessed = mutableListOf<String>()
                    if (emails != null) {
                        for (email in emails) {
                            // Get thread ID
                            val threadId = email.threadId
                            if (conversationThreadIdsProcessed.contains(threadId)) {
                                continue
                            }
                            conversationThreadIdsProcessed.add(threadId)
                            // Get all emails in the thread
                            val conversationThread = gmailApiService.getEmailsInThread(context, emailId, threadId)
                            if (conversationThread == null) {
                                continue
                            }
                            val emailBody = gmailApiService.getLast3EmailsBody(conversationThread, emailId)
                            val subject = email.payload.headers.find { it.name == "Subject" }!!.value
                            val sender = email.payload.headers.find { it.name == "From" }!!.value


                            productivityFeedViewModel.processEmailContent(
                                emailId = emailId,
                                emailType = enumEmailType.GMAIL,
                                emailSubject = subject,
                                emailSnippet = email.snippet,
                                emailSender = sender,
                                emailBody = emailBody
                            )
                            // Process the email
                            // Find out from a small model (GPT-3.5) if the email has any of the
                            // following: tasks, newsletters, need to reply (Just get a yes or no response)

                            /*var initialPromptText = "You are a helpful assistant, whose job is to help the user be productive. \n" +
                                "\n" +
                                "Given an email received by the user, perform the following task: \n" +
                                "1. Respond with \"Yes\" if any of the following is true: \n" +
                                "- the email mentions a task that the user needs to perform (like sending a file, sending a calendar invite, doing some work, etc.)\n" +
                                "- the email is a newsletter\n" +
                                "- the email requires the user to respond to it\n" +
                                "2. Respond with \"No\" if you think this email does not require the user to do anything (they can just read it and not do anything if they so choose). \n" +
                                "\n" +
                                "Only respond with \"Yes\" or \"No\", no other text or formatting."*/
                        }
                    }
                }
                // Update last processed date in misc info db as current date
                miscInfoDbViewModel.updateOrCreate(
                    "lastProcessedDate_Gmail",
                    dateFormatter.format(Calendar.getInstance().time)
                )
                delay(1000) // Sleep for 1 second
            } catch (e: Exception) {
                Log.d("GmailRepositoryImpl", "Error in checkAndProcessNewEmails: ${e.message}")
                Log.d("GmailRepositoryImpl", "Error in checkAndProcessNewEmails: ${e.stackTrace}")
            }
        }
    }
}