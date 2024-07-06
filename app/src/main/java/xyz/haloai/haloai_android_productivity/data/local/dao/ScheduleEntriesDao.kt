package xyz.haloai.haloai_android_productivity.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xyz.haloai.haloai_android_productivity.data.local.entities.ScheduleEntry
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumTimeSlotForTask
import java.util.Date

@Dao
interface ScheduleEntriesDao {
    @Insert
    fun insert(calEvent: ScheduleEntry): Long

    @Query("SELECT * FROM scheduleDb WHERE type == :type ORDER BY startTime ASC")
    fun getAll(type: enumEventType): List<ScheduleEntry>

    @Query("SELECT * FROM scheduleDb WHERE id = :id")
    fun getById(id: Long): ScheduleEntry

    @Query("DELETE FROM scheduleDb WHERE id = :id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM scheduleDb WHERE ((startTime >= :start AND startTime <= :end) OR (endTime >= :start AND endTime <= :end) OR (startTime <= :start AND endTime >= :end)) AND type == :type ORDER BY startTime ASC")
    fun getDbEntriesBetween(start: Date, end: Date, type: enumEventType): List<ScheduleEntry>

    /*@Query("SELECT * FROM scheduleDb WHERE ((startTime >= :start AND startTime <= :end) OR (endTime >= :start AND endTime <= :end) OR (startTime <= :start AND endTime >= :end)) AND type == :type ORDER BY startTime ASC")
    suspend fun getCalendarEventsBetween(start: Date, end: Date, type: enumEventType = enumEventType.CALENDAR_EVENT): List<EventsAndTasks>

    @Query("SELECT * FROM scheduleDb WHERE ((startTime >= :start AND startTime <= :end) OR (endTime >= :start AND endTime <= :end) OR (startTime <= :start AND endTime >= :end)) AND (type == :type1 OR type == :type2) ORDER BY startTime ASC")
    suspend fun getTasksBetween(start: Date, end: Date, type1: enumEventType = enumEventType.UNSCHEDULED_TASK, type2: enumEventType = enumEventType.SCHEDULED_TASK): List<EventsAndTasks>

     @Query("SELECT * FROM scheduleDb WHERE type == :type ORDER BY startTime ASC")
    suspend fun getUnscheduledToDoEvents(type:enumEventType = enumEventType.UNSCHEDULED_TASK): List<EventsAndTasks>

    @Query("SELECT * FROM scheduleDb WHERE (type == :type1 OR type == :type2) ORDER BY startTime ASC")
    suspend fun getAllTasks(type1: enumEventType = enumEventType.UNSCHEDULED_TASK, type2: enumEventType = enumEventType.SCHEDULED_TASK): Flow<List<EventsAndTasks>>

    @Query("SELECT * FROM scheduleDb WHERE NOT isToDo ORDER BY startTime ASC")
    suspend fun getAllCalendarEvents(): List<CalendarEvent>*/

    @Query("DELETE FROM scheduleDb")
    fun deleteAll()

    @Query("SELECT eventIdFromCal FROM scheduleDb WHERE sourceEmailId = :emailId")
    fun getEventIdsForEmail(emailId: String): List<String>

    @Query("SELECT * FROM scheduleDb WHERE eventIdFromCal = :eventId AND sourceEmailId = :emailId")
    fun getByEventIdFromCal(eventId: String, emailId: String): ScheduleEntry?

    @Query("SELECT * FROM scheduleDb WHERE type == :type")
    fun getAllUnscheduledTasks(type: enumEventType = enumEventType.UNSCHEDULED_TASK): List<ScheduleEntry>

    @Query("UPDATE scheduleDb SET isCompleted = :isCompleted WHERE id = :id")
    fun markEventAsCompleted(id: Long, isCompleted: Boolean = true)

    @Query("UPDATE scheduleDb SET reminderTimeInMins = :reminderTimeInMins WHERE id = :id")
    fun updateReminderTimeInMins(id: Long, reminderTimeInMins: Int)

    @Query("UPDATE scheduleDb SET timeSlotVal = :timeSlotVal WHERE id = :id")
    fun updateTodoTimeSlot(id: Long, timeSlotVal: enumTimeSlotForTask)

    @Query("UPDATE scheduleDb SET completionTime = :completionTime WHERE id = :id")
    fun updateCompletionTime(id: Long, completionTime: Date? = null)

    @Query("UPDATE scheduleDb SET creationTime = :creationTime WHERE id = :id")
    fun updateCreationTime(id: Long, creationTime: Date)

}