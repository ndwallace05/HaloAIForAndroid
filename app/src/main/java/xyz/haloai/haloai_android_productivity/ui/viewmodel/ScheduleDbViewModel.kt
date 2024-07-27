package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.haloai.haloai_android_productivity.data.local.entities.ScheduleEntry
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumTimeSlotForTask
import xyz.haloai.haloai_android_productivity.data.local.repository.ScheduleDbRepository
import java.util.Date

class ScheduleDbViewModel(private val repository: ScheduleDbRepository, context: Context, refreshDb: Boolean = true) : ViewModel() {

    // val allUnscheduledTasks: Flow<List<ScheduleEntry>> = repository.allUnscheduledTasks

    private val _isDataLoaded = mutableStateOf(false)
    val isDataLoaded: State<Boolean> get() = _isDataLoaded

    private val _data = mutableStateOf<String?>(null)
    val data: State<String?> get() = _data
    val coroutineScope = viewModelScope

    init {
        // var context = getKoin().get<Context>()
        if (refreshDb)
        {
            loadData(context, coroutineScope)
        }
    }

    private fun loadData(context: Context, coroutineScope: CoroutineScope) {
        viewModelScope.launch {
            updateScheduleDb(context, coroutineScope)
            _isDataLoaded.value = true
        }
    }

    suspend fun deleteById(id: Long) {
        repository.deleteById(id)
    }

    suspend fun getEventsBetween(start: Date, end: Date): List<ScheduleEntry> {
        return repository.getEventsBetween(start, end)
    }

    suspend fun getTasksBetween(start: Date, end: Date): List<ScheduleEntry> {
        return repository.getTasksBetween(start, end)
    }

    suspend fun getEventById(id: Long): ScheduleEntry = withContext(Dispatchers.IO) {
        return@withContext repository.getEventById(id)
    }

    suspend fun updateCompletionStatus(id: Long, completionStatus: Boolean) {
        withContext(Dispatchers.IO) {
            repository.updateCompletionStatus(id, completionStatus)
        }
    }

    suspend fun postponeTaskToTomorrow(id: Long) {
        withContext(Dispatchers.IO) {
            repository.postponeTaskToTomorrow(id)
        }
    }

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
        return repository.createNewEvent(
            title,
            description,
            startTime,
            endTime,
            location,
            attendees,
            sourceEmailId,
            eventIdFromCal,
            creationTime
        )
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
        return repository.createNewTask(
            title,
            description,
            startTime,
            source,
            endTime,
            timeSlotVal,
            creationTime
        )
    }

    fun convertTaskToEvent(id: Long, startTime: Date, endTime: Date, location: String, attendees: List<String>, eventIdFromCal: String): Long {
        return repository.convertTaskToEvent(id, startTime, endTime, location, attendees, eventIdFromCal)
    }

    suspend fun updateScheduleEntryWithOnlyGivenFields(
        id: Long,
        type: enumEventType? = null,
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
        return repository.updateScheduleEntryWithOnlyGivenFields(
            id,
            type,
            title,
            description,
            startTime,
            endTime,
            location,
            attendees,
            sourceEmailId,
            eventIdFromCal,
            timeSlotVal,
            creationTime,
            completionTime,
            fieldsToSetAsNull
        )
    }

    suspend fun insertOrUpdate(
        id: Long? = null,
        type: enumEventType? = null,
        context: Context,
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
        fieldsToSetAsNull: List<String> = emptyList(),
    ): Long =
    withContext(Dispatchers.IO){
        return@withContext repository.insertOrUpdate(
            id,
            type,
            context,
            title,
            description,
            startTime,
            endTime,
            location,
            attendees,
            sourceEmailId,
            eventIdFromCal,
            timeSlotVal,
            creationTime,
            completionTime,
            fieldsToSetAsNull
        )
    }

    fun deleteAllEventsForEmail(context: Context, emailId: String)
    {
        repository.deleteAllEventsForEmail(context, emailId)
    }

    suspend fun deleteEventsNotInList(context: Context, emailId: String, eventIds: List<String>) =
    withContext(Dispatchers.IO){
        repository.deleteEventsNotInList(context, emailId, eventIds)
    }

    suspend fun updateScheduleDb(context: Context, coroutineScope: CoroutineScope, startDate: Date? = null, endDate: Date? = null)
    {
        repository.updateScheduleDb(
            context = context,
            coroutineScope = coroutineScope,
            startDate = startDate,
            endDate = endDate
        )
    }

    suspend fun getSuggestedTasksForDay(context: Context, date: Date): List<ScheduleEntry> =
        withContext(Dispatchers.IO){
        return@withContext repository.getSuggestedTasksForDay(context, date)
    }

    suspend fun getAllScheduledTasks(): List<ScheduleEntry> = withContext(Dispatchers.IO) {
        return@withContext repository.getAllScheduledTasks()
    }

    suspend fun getAllUnscheduledTasks(): List<ScheduleEntry> = withContext(Dispatchers.IO) {
        return@withContext repository.getAllUnscheduledTasks()
    }

    suspend fun markSuggestedTaskAsComplete(title: String, description: String?) {
        withContext(Dispatchers.IO) {
            repository.markSuggestedTaskAsComplete(title, description)
        }
    }

}

class ContentViewModelFactory(private val repository: ScheduleDbRepository, private val context: Context) : ViewModelProvider
    .Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleDbViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleDbViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
