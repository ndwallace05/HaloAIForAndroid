package xyz.haloai.haloai_android_productivity.services.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.microsoft.identity.client.IAuthenticationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel
import xyz.haloai.haloai_android_productivity.services.MicrosoftGraphService
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resumeWithException

interface MicrosoftGraphRepository {
    suspend fun getCalendarIdsForMicrosoftAccount(context: Context, coroutineScope: CoroutineScope): MutableList<Pair<String, String>>

    suspend fun updateScheduleDbForEmail(emailId: String, context: Context, coroutineScope: CoroutineScope, startDate:
    Date? = null, endDate: Date? = null)

}

class MicrosoftGraphRepositoryImplementation(private val microsoftGraphService: MicrosoftGraphService) : MicrosoftGraphRepository, KoinComponent {

    // This is a placeholder for the results from the callback
    private val _resultsFromCalIdCallback = MutableLiveData<MutableList<Pair<String, String>>>()
    val resultsFromCalIdCallback: LiveData<MutableList<Pair<String, String>>> get() = _resultsFromCalIdCallback
    private val _resultsFromEventsCallback = MutableLiveData<JSONObject>()
    val resultsFromEventsCallback: LiveData<JSONObject> get() =
        _resultsFromEventsCallback

    val context = getKoin().get<Context>()

    val emailDbViewModel: EmailDbViewModel by inject()
    val scheduleDbViewModel: ScheduleDbViewModel by inject { parametersOf(context, false) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCalendarIdsForMicrosoftAccount(context: Context, coroutineScope: CoroutineScope): MutableList<Pair<String, String>> {
        return suspendCancellableCoroutine<MutableList<Pair<String, String>>> { continuation ->
            // Authenticate user, then get calendar IDs
            coroutineScope.launch {
                authenticateMicrosoftAccountToFetchCalendarIds(context, coroutineScope)
            }

            // Observe LiveData for results
            val observer = object : Observer<MutableList<Pair<String, String>>> {
                override fun onChanged(results: MutableList<Pair<String, String>>) {
                    results.let {
                        _resultsFromCalIdCallback.removeObserver(this)
                        continuation.resume(it) {
                            continuation.resumeWithException(it)
                        }
                    }
                }
            }

            _resultsFromCalIdCallback.observeForever(observer)

            continuation.invokeOnCancellation {
                _resultsFromCalIdCallback.removeObserver(observer)
            }
        }
    }

    private suspend fun authenticateMicrosoftAccountToFetchCalendarIds(context: Context, coroutineScope: CoroutineScope) {
        microsoftGraphService.addMicrosoftAccount(context, this::getCalendarIdsCallback, coroutineScope)
    }

    private suspend fun authenticateMicrosoftAccountToFetchEvents(context: Context, coroutineScope:
    CoroutineScope, emailId: String, startDate: Date?, endDate: Date?) {
        microsoftGraphService.authenticateAccountForEventsFetch(context, emailId, this::getEventsCallback,
            coroutineScope, startDate, endDate)
    }

    private suspend fun getCalendarIdsCallback(result: Result<IAuthenticationResult>) {
        // Handle the result
        if (result.isSuccess) {
            // Get the calendar IDs
            val calIdsWithNames = microsoftGraphService.callGraphAPIToFetchAllCalendarIDs(result.getOrNull())
            val emailId = result.getOrNull()?.account?.username
            // Add an entry in the calIdsWithNames list for the email ID
            calIdsWithNames.add(Pair("<EMAILID>", emailId!!))
            _resultsFromCalIdCallback.postValue(calIdsWithNames)
        } else {
            // Handle the error
            throw result.exceptionOrNull()!!
        }
    }

    private suspend fun getEventsFromEmail(emailId: String, context: Context, coroutineScope:
    CoroutineScope, startDate: Date?, endDate: Date?): JSONObject  {
        return suspendCancellableCoroutine<JSONObject> { continuation ->
            // Authenticate user, then get calendar IDs
            coroutineScope.launch {
                authenticateMicrosoftAccountToFetchEvents(context, coroutineScope, emailId, startDate, endDate)
            }

            // Observe LiveData for results
            val observer = object : Observer<JSONObject> {
                override fun onChanged(results: JSONObject) {
                    results.let {
                        _resultsFromEventsCallback.removeObserver(this)
                        continuation.resume(it) {
                            continuation.resumeWithException(it)
                        }
                    }
                }
            }

            _resultsFromEventsCallback.observeForever(observer)

            continuation.invokeOnCancellation {
                _resultsFromEventsCallback.removeObserver(observer)
            }
        }
    }

    override suspend fun updateScheduleDbForEmail(emailId: String, context: Context,
                                                  coroutineScope: CoroutineScope, startDate:
                                                  Date?, endDate: Date?) {
        val events = getEventsFromEmail(emailId, context, coroutineScope, startDate, endDate)
        val allEventIds = mutableListOf<String>()
        // Parse JSON to get events
        for (i in 0 until events.getJSONArray("value").length()) {
            val event = events.getJSONArray("value").getJSONObject(i)
            allEventIds.add(event.getString("id"))
        }
        // Clear all events for this emailId
        scheduleDbViewModel.deleteEventsNotInList(context, emailId, allEventIds)
        // Update the schedule DB
        for (i in 0 until events.getJSONArray("value").length()) {
            val event = events.getJSONArray("value").getJSONObject(i)
            val startTime = Date()
            var type = enumEventType.CALENDAR_EVENT
            if (event.getJSONObject("start").has("dateTime")) {
                // Parse the date time string to a long, format is "2024-05-06T16:00:00.0000000"
                val dateTimeString = event.getJSONObject("start").getString("dateTime")
                val dateTimeStringWithoutTimeZone = dateTimeString.substring(0, dateTimeString.length - 1)
                startTime.time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateTimeStringWithoutTimeZone)!!.time
                startTime.time += TimeZone.getDefault().getOffset(startTime.time)
            } else {
                startTime.time = event.getJSONObject("start").getString("date").toLong()
                type = enumEventType.SCHEDULED_TASK
            }
            val endTime = Date()
            if (event.getJSONObject("end").has("dateTime")) {
                val dateTimeString = event.getJSONObject("end").getString("dateTime")
                val dateTimeStringWithoutTimeZone = dateTimeString.substring(0, dateTimeString.length - 1)
                endTime.time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateTimeStringWithoutTimeZone)!!.time
                endTime.time += TimeZone.getDefault().getOffset(endTime.time)
                // endTime.time = event.getJSONObject("end").getString("dateTime").toLong()
            } else {
                endTime.time = event.getJSONObject("end").getString("date").toLong()
                type = enumEventType.SCHEDULED_TASK
            }
            val attendees = event.getJSONArray("attendees")
            val attendeeEmails = mutableListOf<String>()
            for (j in 0 until attendees.length()) {
                attendeeEmails.add(attendees.getJSONObject(j).getString("emailAddress"))
            }
            /*var html = event.getString("body").replace("\n", "<br>")
            html = html.replace("\r", "<br>")
            html = html.replace("\t", "")*/

            /*// Parse the HTML
            val doc = Ksoup.parse(html)

            val innerDoc = doc.select("<body>") // JSON object
            val innerHtml = JSONObject(innerDoc.toString())
            println("Inner HTML: $innerHtml")
            val innerHTML_content = innerHtml.getString("content")
            println("Inner HTML content: $innerHTML_content")
            val parsedInnerHTML = Ksoup.parse(innerHTML_content)

            // Get all anchor tags
            val links = parsedInnerHTML.select("a")
            for (link in links) {
                val href = link.attr("href")
                println("Link: $href")
            }*/

            scheduleDbViewModel.insertOrUpdate(
                context = context,
                type = type,
                eventIdFromCal = event.getString("id"),
                sourceEmailId = emailId,
                title = event.getString("subject"),
                description = event.getString("bodyPreview"),
                location = event.getJSONObject("location").getString("displayName"),
                startTime = startTime,
                endTime = endTime,
                attendees = attendeeEmails
            )
        }
    }

    suspend fun getEventsCallback(result: Result<IAuthenticationResult>, startDate: Date?, endDate: Date?) {
        // Handle the result
        if (result.isSuccess) {
            // Get the calendar IDs
            val calendarIds = emailDbViewModel.getCalendarIdsForEmail(result.getOrNull()?.account?.username!!)
            var allEventsJson: JSONObject? = null
            for (calId in calendarIds) {
                val eventsJson = microsoftGraphService.callGraphAPIToFetchAllEvents(result
                    .getOrNull(), calId, startDate, endDate)
                if (allEventsJson == null) {
                    allEventsJson = eventsJson
                } else {
                    var allEventsArray = allEventsJson!!.getJSONArray("value")
                    var newEventsArray = eventsJson.getJSONArray("value")
                    for (i in 0 until newEventsArray.length()) {
                        allEventsArray.put(newEventsArray.getJSONObject(i))
                    }
                    allEventsJson!!.put("value", allEventsArray)
                }
            }
            _resultsFromEventsCallback.postValue(allEventsJson!!)
        } else {
            // Handle the error
            throw result.exceptionOrNull()!!
        }
    }
}