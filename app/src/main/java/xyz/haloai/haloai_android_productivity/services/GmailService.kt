package xyz.haloai.haloai_android_productivity.services

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import com.fleeksoft.ksoup.Ksoup
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.Thread
import com.google.common.io.BaseEncoding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import xyz.haloai.haloai_android_productivity.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GmailService(private val context: Context) {
    private var maxEmailsPerAccount = 100
    suspend fun getCalendarIdsForGoogleAccount(context: Context, emailId: String): MutableList<Pair<String, String>>? {
        val accountManager = AccountManager.get(context)
        val selectedGoogleAccount = accountManager.getAccountsByType("com.google").find { it.name == emailId }

        return if (selectedGoogleAccount != null) {
            val authToken = getAuthToken(context, accountManager, selectedGoogleAccount)
            try {
                authToken?.let {
                    coroutineScope {
                        callGoogleAPIForCalendarIds(context, selectedGoogleAccount.name)
                    }
                }
            }
            catch (e: Exception) {
                throw e
            }
        } else {
            null
        }
    }

    private suspend fun getAuthToken(context: Context, accountManager: AccountManager, account: Account): String? {
        return suspendCoroutine { cont ->
            accountManager.getAuthToken(
                account,
                "oauth2:https://www.googleapis.com/auth/calendar " + GmailScopes.GMAIL_READONLY +
                        " " + CalendarScopes.CALENDAR_READONLY,
                null,
                true,
                { future ->
                    try {
                        val bundle = future.result
                        val authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN)
                        if (authToken != null) {
                            cont.resume(authToken)
                        } else {
                            cont.resumeWithException(Exception("Auth token is null"))
                        }
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                },
                null
            )
        }
    }

    private suspend fun callGoogleAPIForCalendarIds(context: Context, emailId: String): MutableList<Pair<String, String>>? {
        // Call the Google Calendar API to get the calendar IDs
        // Initialize GoogleAccountCredential using OAuth 2.0 scopes
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR_READONLY, GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = Account(emailId, "com.google")

        // Create a new authorized API client
        val client = com.google.api.services.calendar.Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(context.getString(R.string.app_name)).build()

        var calendarList: CalendarList

        // Fetch the list of calendars
        try{

            calendarList = withContext(Dispatchers.IO) {
                client.calendarList().list().setFields("items(id,summary)").execute()
            }
            val items = calendarList.items
            var calIdsWithNames = mutableListOf<Pair<String, String>>()

            // Extracting calendar IDs and names
            items.forEach { calendar ->
                calIdsWithNames.add(Pair(calendar.id, calendar.summary))
            }

            return calIdsWithNames
        }
        catch (e: UserRecoverableAuthIOException)
        {
            throw e
        }
    }

    suspend fun getEventsFromEmail(context: Context, emailId: String, calendarIds: List<String>):
            List<Event>? {
        val accountManager = AccountManager.get(context)
        val selectedGoogleAccount = accountManager.getAccountsByType("com.google").find { it.name == emailId }

        return if (selectedGoogleAccount != null) {
            val authToken = getAuthToken(context, accountManager, selectedGoogleAccount)
            try {
                authToken?.let {
                    coroutineScope {
                        withContext(Dispatchers.IO){
                            callGoogleAPIForCalendarEvents(context, selectedGoogleAccount.name, calendarIds)
                        }
                    }
                }
            }
            catch (e: Exception) {
                throw e
            }
        } else {
            null
        }
    }

    private fun callGoogleAPIForCalendarEvents(context: Context, emailId: String?, calendarIds: List<String>):
            List<Event> {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR_READONLY, GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = Account(emailId, "com.google")

        // Create a new authorized API client
        val client = com.google.api.services.calendar.Calendar.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(context.getString(R.string.app_name)).build()

        var events: Events? = null

        try {
            // Fetch events from all calendars in calendarIds
            calendarIds.forEach { calendarId ->
                if (events == null) {
                    events = client.events().list(calendarId)
                        .setMaxResults(100)
                        .setTimeMin(com.google.api.client.util.DateTime(System.currentTimeMillis()))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                } else {
                    var eventsForThisCalendar = client.events().list(calendarId)
                        .setMaxResults(100)
                        .setTimeMin(com.google.api.client.util.DateTime(System.currentTimeMillis()))
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()
                    events!!.items.addAll(eventsForThisCalendar.items)
                }
            }
        }
        catch (e: UserRecoverableAuthIOException)
        {
            throw e
            // Handle this exception in the UI layer
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            throw e
        }
        return events?.items ?: emptyList()
    }

    private fun callGoogleAPIForEmailsInThread(context: Context, emailId: String, threadId: String): Thread? {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR_READONLY, GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = Account(emailId, "com.google")

        // Create a new authorized API client
        val client = com.google.api.services.gmail.Gmail.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(context.getString(R.string.app_name)).build()

        // Fetch emails in the thread
        var thread: Thread? = null

        try {
            val resultOfExecution = client.users().threads()
                .get("me", threadId)
                .execute()
            if (resultOfExecution.messages != null) {
                thread = resultOfExecution
            }
        }
        catch (e: UserRecoverableAuthIOException) {
            // Launch the intent to request user consent
            Log.e("GmailService", "UserRecoverableAuthIOException")
        }
        catch (e: Exception) {
            Log.e("GmailService", "Error fetching emails in thread with id $threadId", e)
        }
        return thread
    }

    private fun callGoogleAPIForEmailsAfterDate(context: Context, emailId: String, date: Date): List<Message> {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(CalendarScopes.CALENDAR_READONLY, GmailScopes.GMAIL_READONLY)
        )
        credential.selectedAccount = Account(emailId, "com.google")

        // Create a new authorized API client
        val client = com.google.api.services.gmail.Gmail.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(context.getString(R.string.app_name)).build()

        // Fetch emails after the given date
        // val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault())
        // val startDateString = formatter.format(date)
        var messages = mutableListOf<Message>()
        try {
            val epochTime = date.time / 1000 // Convert to epoch time in seconds
            val resultOfExecution = client.users().messages()
                .list("me")
                .setLabelIds(listOf("INBOX", "CATEGORY_PERSONAL"))
                .setQ("after:${epochTime}")
                .setMaxResults(maxEmailsPerAccount.toLong())
                .execute()
            if (resultOfExecution.messages != null) {
                messages = resultOfExecution.messages
            }
        }
        catch (e: UserRecoverableAuthIOException) {
            // Launch the intent to request user consent
            Log.d("GmailService", "UserRecoverableAuthIOException")
        }
        // Filter out promotional and social emails
        var finalMessages = mutableListOf<Message>()
        for (message in messages) {
            try {
                var messageWithDetails =
                    client.users().messages().get("me", message.id).execute()
                // See if the message is a promotion or a social email, skip if so
                var labelIds = messageWithDetails.labelIds
                if (labelIds.contains("CATEGORY_PROMOTIONS") or labelIds.contains("CATEGORY_SOCIAL")) {
                    continue
                }
                finalMessages.add(messageWithDetails)
            }
            catch (e: Exception) {
                Log.d("GmailService", "Error fetching email with id ${message.id}", e)
            }
        }
        return finalMessages
    }

    suspend fun getEmailsAfterDate(context: Context, emailId: String, date: Date): List<Message>? {
        val accountManager = AccountManager.get(context)
        val selectedGoogleAccount = accountManager.getAccountsByType("com.google").find { it.name == emailId }

        return if (selectedGoogleAccount != null) {
            val authToken = getAuthToken(context, accountManager, selectedGoogleAccount)
            try {
                authToken?.let {
                    coroutineScope {
                        withContext(Dispatchers.IO){
                            callGoogleAPIForEmailsAfterDate(context, selectedGoogleAccount.name, date)
                        }
                    }
                }
            }
            catch (e: Exception) {
                throw e
            }
        } else {
            emptyList()
        }
    }

    fun getEmailAsString(email: Message, truncate: Boolean = true): String? {
        var subject =
            email.payload.headers.find { it.name == "Subject" }?.value
        var from = email.payload.headers.find { it.name == "From" }?.value
        var to = email.payload.headers.find { it.name == "To" }?.value
        var date = email.payload.headers.find { it.name == "Date" }?.value
        var dateStr = ""
        try{
            var dateInFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault()).parse(date)
            dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateInFormat)
        }
        catch (e: Exception) {
            Log.d("UserPersonaUpdate", "Error parsing date in format 1, trying new format: $date")
            try{
                var dateInFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault()).parse(date!!.substringBefore("(").trim())
                dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateInFormat)
            }
            catch (e:Exception) {
                var dateInFormat = SimpleDateFormat("d MMM yyyy HH:mm:ss Z", Locale.getDefault()).parse(date!!.substringBefore("(").trim())
                dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateInFormat)
            }
        }
        // var dateTimeWhenRead =
        // Get email body
        var parts: List<MessagePart>
        try{
            parts = email.payload.parts
        }
        catch(e: Exception) {
            return null
        }
        var body = ""
        // Only parse 1 part each for text and html
        var plainTextParsed = false
        var htmlParsed = false
        for (part in parts) {
            if ((part.mimeType == "text/plain") and (!plainTextParsed)){
                body = body + String(BaseEncoding.base64Url().decode(part.body.data))
                plainTextParsed = true
            }
            if ((part.mimeType == "text/html") and (!htmlParsed)) {
                body += Ksoup.parse(
                    String(
                        BaseEncoding.base64Url().decode(part.body.data)
                    )
                ).text()
                htmlParsed = true
            }
        }
        if (truncate) {
            // Truncate body text to 1000 characters
            if (body.length > 1000) {
                body = body.substring(0, 1000)
                body += " ... [Truncated for length]"
            }
        }
        // TODO: If body has <html> tags, fetch the full email and parse the body, get all text
        var id = email.id
        Log.d("UserPersonaUpdate", "Email with id $id fetched")
        val emailAsString = "Subject: $subject\nFrom: $from\nTo: $to\nDate: $dateStr\nBody: $body"

        return emailAsString
    }

    fun getEmailBody(email: Message): String {
        var parts: List<MessagePart>
        try{
            parts = email.payload.parts
        }
        catch(e: Exception) {
            return ""
        }
        var body = ""
        // Only parse 1 part each for text and html
        var plainTextParsed = false
        var htmlParsed = false
        for (part in parts) {
            if ((part.mimeType == "text/plain") and (!plainTextParsed)){
                body = body + String(BaseEncoding.base64Url().decode(part.body.data))
                plainTextParsed = true
            }
            if ((part.mimeType == "text/html") and (!htmlParsed)) {
                body += Ksoup.parse(
                    String(
                        BaseEncoding.base64Url().decode(part.body.data)
                    )
                ).text()
                htmlParsed = true
            }
        }
        return body
    }

    suspend fun getEmailsInThread(context: Context, emailId: String, threadId: String): Thread? {
        val accountManager = AccountManager.get(context)
        val selectedGoogleAccount = accountManager.getAccountsByType("com.google").find { it.name == emailId }

        return if (selectedGoogleAccount != null) {
            val authToken = getAuthToken(context, accountManager, selectedGoogleAccount)
            try {
                authToken?.let {
                    coroutineScope {
                        withContext(Dispatchers.IO){
                            callGoogleAPIForEmailsInThread(context, selectedGoogleAccount.name, threadId)
                        }
                    }
                }
            }
            catch (e: Exception) {
                throw e
            }
        } else {
            null
        }
    }

    fun getLast3EmailsBody(thread: Thread, emailId: String): String {
        var messages = thread.messages
        // Sort messages by most recent first
        messages.sortByDescending { it.internalDate }
        var finalBody = ""
        var count = 0
        val subjectOfEarliestMessage = messages.last().payload.headers.find { it.name == "Subject" }?.value
        finalBody += "Subject: $subjectOfEarliestMessage\n"
        var from: String
        var to: String
        for (message in messages) {
            if (count >= 3) {
                break
            }
            from = message.payload.headers.find { it.name == "From" }?.value ?: "Unknown"
            if (from != emailId) {
                to = emailId
            }
            else{
                to = message.payload.headers.find { it.name == "To" }?.value ?: "Unknown"
            }
            finalBody += getEmailBody(message)
            count++
        }
        return finalBody
    }
}