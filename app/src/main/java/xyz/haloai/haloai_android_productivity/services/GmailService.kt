package xyz.haloai.haloai_android_productivity.services

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import com.google.api.services.gmail.GmailScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import xyz.haloai.haloai_android_productivity.R
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GmailService(private val context: Context) {
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
}