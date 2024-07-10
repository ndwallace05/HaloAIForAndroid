package xyz.haloai.haloai_android_productivity.data.local.repository

import android.content.Context
import android.text.format.DateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.data.local.dao.ScheduleEntriesDao
import xyz.haloai.haloai_android_productivity.data.local.entities.ScheduleEntry
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumTimeSlotForTask
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel
import xyz.haloai.haloai_android_productivity.misc.AlarmHelper
import xyz.haloai.haloai_android_productivity.ui.viewmodel.GmailViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MicrosoftGraphViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.util.Calendar
import java.util.Date

class ScheduleDbRepository(private val scheduleDao: ScheduleEntriesDao): KoinComponent {
    // val allEvents: Flow<List<ScheduleEntry>> = scheduleDao.getAll(enumEventType.CALENDAR_EVENT)
    // val allUnscheduledTasks: Flow<List<ScheduleEntry>> = scheduleDao.getAll(enumEventType.UNSCHEDULED_TASK)
    // val allScheduledTasks: Flow<List<ScheduleEntry>> = scheduleDao.getAll(enumEventType.SCHEDULED_TASK)
    private val gmailViewModel: GmailViewModel by inject()
    private val microsoftGraphViewModel: MicrosoftGraphViewModel by inject()
    private val emailDbViewModel: EmailDbViewModel by inject()

    suspend fun getEventsBetween(start: Date, end: Date): List<ScheduleEntry> = withContext(
        Dispatchers.IO)
    {
        // We uniquely identify an event by using it's title and startTime
        val allEvents = scheduleDao.getDbEntriesBetween(start, end, enumEventType.CALENDAR_EVENT)
        val deduplicatedEvents = mutableListOf<ScheduleEntry>()
        val eventMap = mutableMapOf<String, ScheduleEntry>()
        for (event in allEvents)
        {
            val startTimeInHHMMFormat = DateFormat.format("HH:mm", event.startTime).toString()
            val eventKey = event.title.trim() + "_" + startTimeInHHMMFormat
            if (!eventMap.containsKey(eventKey))
            {
                eventMap[eventKey] = event
            }
        }
        for (event in eventMap.values)
        {
            deduplicatedEvents.add(event)
        }
        // Sort by start time, account for the time zone
        deduplicatedEvents.sortBy { it.startTime }
        return@withContext deduplicatedEvents
    }

    suspend fun deleteById(id: Long) {
        withContext(Dispatchers.IO) {
            scheduleDao.deleteById(id)
        }
    }

    suspend fun getTasksBetween(start: Date, end: Date): List<ScheduleEntry> = withContext(
        Dispatchers.IO)
    {
        var allTasks = scheduleDao.getDbEntriesBetween(start, end, enumEventType.SCHEDULED_TASK)
        // Check for tasks with startDate only or endDate only that lies between the start and end date
        return@withContext scheduleDao.getDbEntriesBetween(start, end, enumEventType.SCHEDULED_TASK)
    }

    fun getEventById(id: Long): ScheduleEntry
    {
        return scheduleDao.getById(id)
    }

    fun updateCompletionStatus(id: Long, completionStatus: Boolean)
    {
        if (completionStatus)
        {
            val completionTime = Calendar.getInstance()
            scheduleDao.markEventAsCompleted(id)
            scheduleDao.updateCompletionTime(id, completionTime.time)
        }
        else
        {
            scheduleDao.markEventAsCompleted(id, false)
            scheduleDao.updateCompletionTime(id, null)
        }
    }

    fun createNewEntry(
        title: String,
        entryType: enumEventType? = null,
        description: String?,
        startTime: Date?,
        endTime: Date?,
        location: String?,
        attendees: List<String>?,
        sourceEmailId: String,
        eventIdFromCal: String?,
        creationTime: Date? = null
    ): Long {
        val currentTime = Calendar.getInstance()
        if (creationTime != null)
        {
            currentTime.time = creationTime
        }
        var type = entryType ?: enumEventType.CALENDAR_EVENT
        if (endTime == null)
        {
            type = if (startTime == null) {
                enumEventType.UNSCHEDULED_TASK
            } else {
                enumEventType.SCHEDULED_TASK
            }
        }
        val newEvent = ScheduleEntry(
            id = 0, // Auto-generated
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            attendees = attendees,
            sourceEmailId = sourceEmailId,
            eventIdFromCal = eventIdFromCal,
            timeSlotVal = null,
            creationTime = currentTime.time,
            type = type
        )
        return scheduleDao.insert(newEvent)
    }

    // Function to create an event from individual fields
    fun createNewEvent(
        title: String,
        description: String?,
        startTime: Date?,
        endTime: Date?,
        location: String?,
        attendees: List<String>?,
        sourceEmailId: String,
        eventIdFromCal: String?,
        creationTime: Date? = null
    ): Long {

        val currentTime = Calendar.getInstance()
        if (creationTime != null)
        {
            currentTime.time = creationTime
        }
        val newEvent = ScheduleEntry(
            id = 0, // Auto-generated
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            attendees = attendees,
            sourceEmailId = sourceEmailId,
            eventIdFromCal = eventIdFromCal,
            timeSlotVal = null,
            creationTime = currentTime.time,
            type = enumEventType.CALENDAR_EVENT
        )
        return scheduleDao.insert(newEvent)
    }

    fun createNewTask(
        title: String,
        description: String?,
        startTime: Date?,
        source: String,
        endTime: Date?,
        timeSlotVal: enumTimeSlotForTask? = null,
        creationTime: Date? = null
    ): Long {

        val currentTime = Calendar.getInstance()
        if (creationTime != null)
        {
            currentTime.time = creationTime
        }
        var timeSlot = timeSlotVal
        if (timeSlotVal == null)
        {
            // Default to morning
            timeSlot = enumTimeSlotForTask.MORNING
        }
        var type = enumEventType.SCHEDULED_TASK
        if (startTime == null && endTime == null)
        {
            type = enumEventType.UNSCHEDULED_TASK
        }
        var sourceStr = "TASK_$source"
        val newTask = ScheduleEntry(
            id = 0, // Auto-generated
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = null,
            attendees = null,
            sourceEmailId = sourceStr,
            eventIdFromCal = null,
            timeSlotVal = timeSlot,
            creationTime = currentTime.time,
            type = type
        )
        return scheduleDao.insert(newTask)
    }

    fun convertTaskToEvent(id: Long, startTime: Date, endTime: Date, location: String, attendees: List<String>, eventIdFromCal: String): Long {
        val task = scheduleDao.getById(id)
        if (task != null)
        {
            val newEvent = ScheduleEntry(
                id = id,
                title = task.title,
                description = task.description,
                startTime = startTime,
                endTime = endTime,
                location = location,
                attendees = attendees,
                sourceEmailId = task.sourceEmailId,
                eventIdFromCal = eventIdFromCal,
                timeSlotVal = task.timeSlotVal,
                creationTime = task.creationTime,
                type = enumEventType.CALENDAR_EVENT
            )
            // Delete the old task
            scheduleDao.deleteById(id)
            return scheduleDao.insert(newEvent)
        }
        throw IllegalArgumentException("Task with ID $id not found")
    }

    suspend fun updateScheduleEntryWithOnlyGivenFields(
        id: Long,
        entryType: enumEventType? = null,
        title: String? = null,
        description: String? = null,
        startTime: Date? = null,
        endTime: Date? = null,
        location: String? = null,
        attendees: List<String>? = null,
        sourceEmailId: String? = null,
        eventIdFromCal: String? = null,
        timeSlotVal: enumTimeSlotForTask? = null,
        creationTime: Date? = null,
        completionTime: Date? = null,
        fieldsToSetAsNull: List<String> = emptyList() // List of fields to set to null
    ): Long {
        return withContext(Dispatchers.IO) {
            val event = scheduleDao.getById(id)
            var newTitle = title ?: ""
            var newDescription = description
            var newStartTime = startTime
            var newEndTime = endTime
            var newLocation = location
            var newAttendees = attendees
            var newSourceEmailId = sourceEmailId ?: ""
            var newEventIdFromCal = eventIdFromCal
            var newTimeSlotVal = timeSlotVal
            var newCreationTime = creationTime
            var newCompletionTime = completionTime
            var type: enumEventType = entryType ?: enumEventType.CALENDAR_EVENT

            if (event != null) {
                newTitle = title ?: event.title
                newDescription = description ?: event.description
                newStartTime = startTime ?: event.startTime
                newEndTime = endTime ?: event.endTime
                newLocation = location ?: event.location
                newAttendees = attendees ?: event.attendees
                newSourceEmailId = sourceEmailId ?: event.sourceEmailId
                newEventIdFromCal = eventIdFromCal ?: event.eventIdFromCal
                newTimeSlotVal = timeSlotVal ?: event.timeSlotVal
                newCreationTime = creationTime ?: event.creationTime
                newCompletionTime = completionTime ?: event.completionTime
                type = entryType ?: event.type
            }

            if (fieldsToSetAsNull.contains("startTime")) {
                newStartTime = null
            }
            if (fieldsToSetAsNull.contains("endTime")) {
                newEndTime = null
            }
            if (fieldsToSetAsNull.contains("location")) {
                newLocation = null
            }
            if (fieldsToSetAsNull.contains("attendees")) {
                newAttendees = null
            }
            if (fieldsToSetAsNull.contains("sourceEmailId")) {
                newSourceEmailId = ""
            }
            if (fieldsToSetAsNull.contains("eventIdFromCal")) {
                newEventIdFromCal = null
            }
            if (fieldsToSetAsNull.contains("timeSlotVal")) {
                newTimeSlotVal = null
            }
            if (fieldsToSetAsNull.contains("creationTime")) {
                newCreationTime = null
            }
            if (fieldsToSetAsNull.contains("completionTime")) {
                newCompletionTime = null
            }

            val newEvent = ScheduleEntry(
                id = id,
                title = newTitle,
                description = newDescription,
                startTime = newStartTime,
                endTime = newEndTime,
                location = newLocation,
                attendees = newAttendees,
                sourceEmailId = newSourceEmailId,
                eventIdFromCal = newEventIdFromCal,
                timeSlotVal = newTimeSlotVal,
                creationTime = newCreationTime,
                completionTime = newCompletionTime,
                type = type
            )
            // Delete the old event
            scheduleDao.deleteById(id)
            // delay(100)
            return@withContext scheduleDao.insert(newEvent)
        }
    }

    suspend fun insertOrUpdate(
        id: Long? = null,
        entryType: enumEventType? = null,
        context: Context,
        title: String?,
        description: String?,
        startTime: Date?,
        endTime: Date?,
        location: String?,
        attendees: List<String>?,
        sourceEmailId: String?,
        eventIdFromCal: String?,
        timeSlotVal: enumTimeSlotForTask? = null,
        creationTime: Date? = null,
        completionTime: Date? = null,
        fieldsToSetAsNull: List<String> = emptyList(),
    ): Long
    {
        var type = entryType ?: enumEventType.CALENDAR_EVENT
        val alarmHelper by lazy { AlarmHelper(context) }
        var exists: Boolean = false
        var eventIdToReturn = 0L
        var event: ScheduleEntry? = null
        var oldStartTime: Date? = null
        var oldEventId: Long = 0
        // Check if the event exists in the database
        if (id == null)
        {
            if (eventIdFromCal != null) // If id is not provided, either it is a new event, or we do not know the id, for which we check the database for matches
            {
                event = scheduleDao.getByEventIdFromCal(eventIdFromCal, sourceEmailId!!)
            }
        }
        else // Id provided, use that
        {
            event = scheduleDao.getById(id)
        }
        if (event != null) // Event exists
        {
            exists = true
            oldStartTime = event.startTime
            oldEventId = event.id
            eventIdToReturn = event.id

            // Update the event
            updateScheduleEntryWithOnlyGivenFields(
                id = event.id,
                entryType = type,
                title = title,
                description = description,
                startTime = startTime,
                endTime = endTime,
                location = location,
                attendees = attendees,
                sourceEmailId = sourceEmailId,
                eventIdFromCal = eventIdFromCal,
                timeSlotVal = timeSlotVal,
                creationTime = creationTime,
                completionTime = completionTime,
                fieldsToSetAsNull = fieldsToSetAsNull
            )
        }
        else
        {
            // Insert the event
            eventIdToReturn = createNewEntry(
                title = title!!,
                entryType = type,
                description = description,
                startTime = startTime,
                endTime = endTime,
                location = location,
                attendees = attendees,
                sourceEmailId = sourceEmailId!!,
                eventIdFromCal = eventIdFromCal,
                creationTime = creationTime
            )
        }
        var newEvent = scheduleDao.getById(eventIdToReturn)
        if (exists)
        {
            if (oldStartTime != null)
            {
                if ((startTime != event!!.startTime)) { // If the start time has changed, update the notification reminder accordingly
                    alarmHelper.updateNotificationReminder(context, newEvent,
                        oldStartTime, oldEventId)
                }
            }
        }
        else
        {
            if (startTime != null)
            {
                // Schedule a notification for the event
                alarmHelper.scheduleNotificationReminder(context, newEvent)
            }
        }

        return eventIdToReturn
    }

    fun deleteAllEventsForEmail(context: Context, emailId: String)
    {
        val alarmHelper by lazy { AlarmHelper(context) }
        val eventIds = scheduleDao.getEventIdsForEmail(emailId)
        for (eventId in eventIds)
        {
            val event = scheduleDao.getByEventIdFromCal(eventId, emailId)
            if (event != null)
            {
                // Delete the alarm for the event
                alarmHelper.deleteAlarmForEventReminder(event.id, event.startTime!!)

                // Delete the event
                scheduleDao.deleteById(event.id)
            }
        }
    }

    fun deleteEventsNotInList(context: Context, emailId: String, eventIds: List<String>)
    {
        val alarmHelper by lazy { AlarmHelper(context) }
        val allEventIds = scheduleDao.getEventIdsForEmail(emailId)
        for (eventId in allEventIds)
        {
            if (!eventIds.contains(eventId))
            {
                val event = scheduleDao.getByEventIdFromCal(eventId, emailId)
                if (event != null)
                {
                    if (event.startTime != null) {
                        // Delete the alarm for the event
                        alarmHelper.deleteAlarmForEventReminder(event.id, event.startTime!!)
                    }
                    else {
                        println("Event with ID ${event.id} does not have a start time. Cannot delete alarm.")
                    }

                    // Delete the event
                    scheduleDao.deleteById(event.id)
                }
            }
        }
    }

    suspend fun updateScheduleDb(scheduleDbViewModel: ScheduleDbViewModel, context: Context,
                                 coroutineScope: CoroutineScope, startDate: Date? = null, endDate:
                                 Date? = null)
    {
        // Looks up emailDb, gets all emails, and updates the scheduleDb for each email
        val allGoogleEmails = emailDbViewModel.getEmailsOfType(enumEmailType.GMAIL)
        for (email in allGoogleEmails)
        {
            val calendarIds = emailDbViewModel.getCalendarIdsForEmail(email.email)
            gmailViewModel.updateScheduleDbForEmail(scheduleDbViewModel, context, email.email,
                calendarIds) // TODO: Add startDate and endDate support
        }
        val allMicrosoftEmails = emailDbViewModel.getEmailsOfType(enumEmailType.MICROSOFT)
        for (email in allMicrosoftEmails)
        {
            microsoftGraphViewModel.updateScheduleDbForEmail(emailId = email.email, context =
            context, coroutineScope = coroutineScope, startDate = startDate, endDate = endDate)
        }
    }

    suspend fun getSuggestedTasksForDay(context: Context, date: Date): List<ScheduleEntry> {
        val unscheduledTasks = scheduleDao.getAllUnscheduledTasks()
        // Make GPT call to get suggested tasks
        val suggestedTasks = mutableListOf<ScheduleEntry>()
        for (task in unscheduledTasks)
        {
            if (task.timeSlotVal != null)
            {
                suggestedTasks.add(task)
            }
        }
        return suggestedTasks
    }

    suspend fun getAllScheduledTasks(): List<ScheduleEntry> = withContext(Dispatchers.IO) {
        return@withContext scheduleDao.getAll(enumEventType.SCHEDULED_TASK)
    }

    suspend fun getAllUnscheduledTasks(): List<ScheduleEntry> = withContext(Dispatchers.IO) {
        return@withContext scheduleDao.getAll(enumEventType.UNSCHEDULED_TASK)
    }
}