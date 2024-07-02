package xyz.haloai.haloai_android_productivity.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scheduleDb")
data class ScheduleEntry(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "type") var type: enumEventType, // CALENDAR_EVENT, SCHEDULED_TASK, UNSCHEDULED_TASK
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "startTime") var startTime: Date?,
    @ColumnInfo(name = "endTime") var endTime: Date?,
    @ColumnInfo(name = "location") var location: String?,
    @ColumnInfo(name = "attendees") var attendees: List<String>?,
    @ColumnInfo(name = "sourceEmailId") var sourceEmailId: String, // Stores the email ID of the calendar that the event belongs to
    @ColumnInfo(name = "eventIdFromCal") var eventIdFromCal: String? = null,
    @ColumnInfo(name = "isCompleted") var isCompleted: Boolean = false,
    @ColumnInfo(name = "reminderTimeInMins") var reminderTimeInMins: Int = 15,
    @ColumnInfo(name = "timeSlotVal") var timeSlotVal: enumTimeSlotForTask? = null, // options
    // are
    // "morning", "afternoon", "evening", "night"
    @ColumnInfo(name = "creationTime") var creationTime: Date? = null,
    @ColumnInfo(name = "completionTime") var completionTime: Date? = null
)

enum class enumEventType(val value: Int) {
    CALENDAR_EVENT(0),
    SCHEDULED_TASK(1),
    UNSCHEDULED_TASK(2)
}

enum class enumTimeSlotForTask(val value: Int) {
    MORNING(0),
    AFTERNOON(1),
    EVENING(2),
    NIGHT(3)
}