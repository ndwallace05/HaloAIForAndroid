package xyz.haloai.haloai_android_productivity.services.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.microsoft.identity.client.IAuthenticationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel
import xyz.haloai.haloai_android_productivity.services.MicrosoftGraphService
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MiscInfoDbViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ProductivityFeedViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resumeWithException

interface MicrosoftGraphRepository {
    suspend fun getCalendarIdsForMicrosoftAccount(context: Context, coroutineScope: CoroutineScope): MutableList<Pair<String, String>>

    suspend fun updateScheduleDbForEmail(emailId: String, context: Context, coroutineScope: CoroutineScope, startDate:
    Date? = null, endDate: Date? = null)

    suspend fun checkAndProcessNewEmails()

}

class MicrosoftGraphRepositoryImplementation(private val microsoftGraphService: MicrosoftGraphService) : MicrosoftGraphRepository, KoinComponent {

    // LiveData for results of the API calls

    // For the calendar IDs
    private val _resultsFromCalIdCallback = MutableLiveData<MutableList<Pair<String, String>>>()
    val resultsFromCalIdCallback: LiveData<MutableList<Pair<String, String>>> get() = _resultsFromCalIdCallback

    // For the events
    private val _resultsFromEventsCallback = MutableLiveData<JSONObject>()
    val resultsFromEventsCallback: LiveData<JSONObject> get() = _resultsFromEventsCallback

    // For the emails
    private val _resultsFromEmailsCallback = MutableLiveData<JSONObject>()
    val resultsFromEmailsCallback: LiveData<JSONObject> get() = _resultsFromEmailsCallback

    // For the conversation thread
    private val _resultsFromConversationThreadCallback = MutableLiveData<JSONObject>()
    val resultsFromConversationThreadCallback: LiveData<JSONObject> get() = _resultsFromConversationThreadCallback

    val context = getKoin().get<Context>()

    private val emailDbViewModel: EmailDbViewModel by inject()
    val scheduleDbViewModel: ScheduleDbViewModel by inject { parametersOf(context) }
    private val miscInfoDbViewModel: MiscInfoDbViewModel by inject()
    private val productivityFeedViewModel: ProductivityFeedViewModel by inject()

    private val emailCheckerMutex: Mutex = Mutex()

    private val liveDataMap = ConcurrentHashMap<String, MutableLiveData<JSONObject>>()

    private fun getLiveData(conversationId: String): MutableLiveData<JSONObject> {
        return liveDataMap.getOrPut(conversationId) { MutableLiveData<JSONObject>() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getCalendarIdsForMicrosoftAccount(context: Context, coroutineScope: CoroutineScope): MutableList<Pair<String, String>> {
        return suspendCancellableCoroutine { continuation ->
            // Authenticate user, then get calendar IDs
            coroutineScope.launch {
                authenticateMicrosoftAccountToFetchCalendarIds(context, coroutineScope)
            }

            // Observe LiveData for results
            val observer = object : Observer<MutableList<Pair<String, String>>> {
                override fun onChanged(value: MutableList<Pair<String, String>>) {
                    value.let { pairs ->
                        _resultsFromCalIdCallback.removeObserver(this)
                        continuation.resume(pairs) {
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

    private suspend fun authenticateMicrosoftAccountToFetchEmails(context: Context, coroutineScope:
    CoroutineScope, emailId: String, date: Date) {
        microsoftGraphService.authenticateAccountForEmailsFetch(context, emailId, this::getEmailsCallback,
            coroutineScope, date)
    }

    private suspend fun authenticateMicrosoftAccountToFetchConversationThread(context: Context,
                                                                    coroutineScope:
    CoroutineScope, emailId: String, conversationId: String) {
        microsoftGraphService.authenticateAccountForConversationThreadFetch(context, emailId,
            this::getConversationThreadCallback,
            coroutineScope, conversationId)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getEventsFromEmail(emailId: String, context: Context, coroutineScope:
    CoroutineScope, startDate: Date?, endDate: Date?): JSONObject  {
        return suspendCancellableCoroutine { continuation ->
            // Authenticate user, then get calendar IDs
            coroutineScope.launch {
                authenticateMicrosoftAccountToFetchEvents(context, coroutineScope, emailId, startDate, endDate)
            }

            // Observe LiveData for results
            val observer = object : Observer<JSONObject> {
                override fun onChanged(value: JSONObject) {
                    _resultsFromEventsCallback.removeObserver(this)
                    if (continuation.isActive) {
                        continuation.resume(value) {
                            continuation.resumeWithException(it)
                        }
                    }
                }
            }

            coroutineScope.launch(Dispatchers.Main) {
                _resultsFromEventsCallback.observeForever(observer)
            }

            continuation.invokeOnCancellation {
                coroutineScope.launch(Dispatchers.Main) {
                    _resultsFromEventsCallback.removeObserver(observer)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getEmailsForEmail(emailId: String, context: Context, coroutineScope:
    CoroutineScope, dateToUse: Date): JSONObject  {

        return suspendCancellableCoroutine { continuation ->
            // Authenticate user, then get calendar IDs
            coroutineScope.launch {
                authenticateMicrosoftAccountToFetchEmails(context, coroutineScope, emailId, dateToUse)
            }

            // Observe LiveData for results
            val observer = object : Observer<JSONObject> {
                override fun onChanged(value: JSONObject) {
                    _resultsFromEmailsCallback.removeObserver(this)
                    if (continuation.isActive) {
                        continuation.resume(value) {
                            continuation.resumeWithException(it)
                        }
                    }
                }
            }

            _resultsFromEmailsCallback.observeForever(observer)

            continuation.invokeOnCancellation {
                coroutineScope.launch(Dispatchers.Main) {
                    _resultsFromEmailsCallback.removeObserver(observer)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getConversationThread(emailId: String, context: Context, coroutineScope:
    CoroutineScope, conversationId: String): JSONObject  {

        return suspendCancellableCoroutine { continuation ->
            val resultLiveData = getLiveData(conversationId)

            // Authenticate user, then get calendar IDs
            coroutineScope.launch {
                authenticateMicrosoftAccountToFetchConversationThread(context, coroutineScope,
                    emailId, conversationId)
            }

            // Observe LiveData for results
            val observer = object : Observer<JSONObject> {
                override fun onChanged(value: JSONObject) {
                    // _resultsFromConversationThreadCallback.removeObserver(this)
                    resultLiveData.removeObserver(this)
                    if (continuation.isActive) {
                        continuation.resume(value) {
                            continuation.resumeWithException(it)
                        }
                    }
                    liveDataMap.remove(conversationId) // Cleanup LiveData
                }
            }

            // coroutineScope.launch(Dispatchers.Main) {
                // _resultsFromConversationThreadCallback.observeForever(observer)
            resultLiveData.observeForever(observer)
            // }

            continuation.invokeOnCancellation {
                coroutineScope.launch(Dispatchers.Main) {
                    // _resultsFromConversationThreadCallback.removeObserver(observer)
                    resultLiveData.removeObserver(observer)
                    liveDataMap.remove(conversationId) // Cleanup LiveData
                }
            }
        }
    }

    override suspend fun updateScheduleDbForEmail(emailId: String, context: Context,
                                                  coroutineScope: CoroutineScope, startDate:
                                                  Date?, endDate: Date?) {
        try {
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
        catch (e: Exception) {
            Log.d("MicrosoftGraphRepository", "Error in updateScheduleDbForEmail: ${e.message}")
            Log.d("MicrosoftGraphRepository", "Error in updateScheduleDbForEmail: ${e.stackTrace}")
        }
    }

    private suspend fun getEventsCallback(result: Result<IAuthenticationResult>, startDate: Date?, endDate: Date?) {
        // Handle the result
        result.fold(
            onSuccess = { authResult ->
                authResult.account.username.let { username ->
                    try {
                        val calendarIds = emailDbViewModel.getCalendarIdsForEmail(username)
                        var allEventsJson: JSONObject? = null
                        for (calId in calendarIds) {
                            val eventsJson = microsoftGraphService.callGraphAPIToFetchAllEvents(authResult, calId, startDate, endDate)
                            if (allEventsJson == null) {
                                allEventsJson = eventsJson
                            } else {
                                val allEventsArray = allEventsJson.getJSONArray("value")
                                val newEventsArray = eventsJson.getJSONArray("value")
                                for (i in 0 until newEventsArray.length()) {
                                    allEventsArray.put(newEventsArray.getJSONObject(i))
                                }
                                allEventsJson.put("value", allEventsArray)
                            }
                        }
                        _resultsFromEventsCallback.postValue(allEventsJson!!)
                    } catch (e: Exception) {
                        Log.e("GraphAPI", "Error fetching events", e)
                        // Handle specific exceptions if necessary
                    }
                }
            },
            onFailure = { exception ->
                Log.e("GraphAPI", "Authentication failed", exception)
                // Handle different types of exceptions specifically if needed
            }
        )

        /*if (result.isSuccess) {
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
        }*/
    }

    private suspend fun getConversationThreadCallback(result: Result<IAuthenticationResult>,
                                                      conversationId: String) {
        // Handle the result
        result.fold(
            onSuccess = { authResult ->
                authResult.account.username.let {
                    try {
                        val allEmailsJSON = microsoftGraphService.callGraphAPIToFetchConversationThread(authResult, conversationId)
                        // _resultsFromConversationThreadCallback.postValue(allEmailsJSON)
                        getLiveData(conversationId).postValue(allEmailsJSON)
                    } catch (e: Exception) {
                        Log.e("GraphAPI", "Error fetching conversation thread", e)
                        // Handle specific exceptions if necessary
                    }
                }
            },
            onFailure = { exception ->
                Log.e("GraphAPI", "Authentication failed", exception)
                // Handle different types of exceptions specifically if needed
            }
        )
    }

    private suspend fun getEmailsCallback(result: Result<IAuthenticationResult>, date: Date) {
        // Handle the result
        result.fold(
            onSuccess = { authResult ->
                authResult.account.username.let {
                    try {
                        val allEmailsJSON = microsoftGraphService.callGraphAPIToFetchEmails(authResult, date)
                        _resultsFromEmailsCallback.postValue(allEmailsJSON)
                    } catch (e: Exception) {
                        Log.e("GraphAPI", "Error fetching emails", e)
                        // Handle specific exceptions if necessary
                    }
                }
            },
            onFailure = { exception ->
                Log.e("GraphAPI", "Authentication failed", exception)
                // Handle different types of exceptions specifically if needed
            }
        )

        /*if (result.isSuccess) {
            // Get the calendar IDs
            // val calendarIds = emailDbViewModel.getCalendarIdsForEmail(result.getOrNull()?.account?.username!!)
            var allEmailsJSON: JSONObject = microsoftGraphService.callGraphAPIToFetchEmails(result.getOrNull()!!, date)
            _resultsFromEmailsCallback.postValue(allEmailsJSON!!)
        } else {
            // Handle the error
            throw result.exceptionOrNull()!!
        }*/
    }

    override suspend fun checkAndProcessNewEmails() {
        emailCheckerMutex.withLock {
            try {
                val lastProcessedDate = miscInfoDbViewModel.get("lastProcessedDate_Microsoft") as String?
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
                val allMicrosoftEmailIds = emailDbViewModel.getEmailsOfType(enumEmailType.MICROSOFT)

                for (emailId in allMicrosoftEmailIds) {
                    val coroutineScope = CoroutineScope(Dispatchers.IO)

                    val emails = getEmailsForEmail(emailId.email, context, coroutineScope, dateToUse)

                    val conversationIdsProcessed = mutableListOf<String>()
                    val allEmails = emails.getJSONArray("value")
                    // Iterate through all emails
                    for (i in 0 until allEmails.length()) {
                        val email = allEmails.getJSONObject(i)
                        val conversationId = email.getString("conversationId")
                        if (conversationIdsProcessed.contains(conversationId)) {
                            continue
                        }
                        conversationIdsProcessed.add(conversationId)
                        val conversationThread = getConversationThread(emailId.email, context, coroutineScope, conversationId)

                        val emailSubject = email.getString("subject")
                        val emailBodyPreview = email.getString("bodyPreview")
                        val emailSender = email.getJSONObject("from").getJSONObject("emailAddress").getString("address")
                        val emailBody = microsoftGraphService.getLast3EmailsBody(conversationThread, emailId.email)
                        // val emailDate = email.getString("receivedDateTime")
                        // Date format: "2024-07-12T16:08:32Z"
                        // val emailDateParsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).parse(emailDate)
                        // val emailDateLong = emailDateParsed!!.time
                        /*val emailAttachments = email.getJSONArray("attachments")
                        val emailAttachmentNames = mutableListOf<String>()
                        for (j in 0 until emailAttachments.length()) {
                            emailAttachmentNames.add(emailAttachments.getJSONObject(j).getString("name"))
                        }*/
                        // Insert into the DB
                        productivityFeedViewModel.processEmailContent(
                            emailId = emailId.email,
                            emailType = enumEmailType.MICROSOFT,
                            emailSubject = emailSubject,
                            emailSnippet = emailBodyPreview,
                            emailSender = emailSender,
                            emailBody = emailBody,
                        )
                    }
                }
                miscInfoDbViewModel.updateOrCreate(
                    "lastProcessedDate_Microsoft",
                    dateFormatter.format(Calendar.getInstance().time)
                )
                // delay(1000)
            } catch (e: Exception) {
                Log.d("MicrosoftGraphRepository", "Error in checkAndProcessNewEmails: ${e.message}")
                Log.d(
                    "MicrosoftGraphRepository",
                    "Error in checkAndProcessNewEmails: ${e.stackTrace}"
                )
            }
        }
    }
}