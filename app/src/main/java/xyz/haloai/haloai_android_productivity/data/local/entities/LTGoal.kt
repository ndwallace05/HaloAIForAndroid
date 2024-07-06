package xyz.haloai.haloai_android_productivity.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ltGoalsDb")
data class LTGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "userContext")  val context: String, // Store any context that the user provides
    @ColumnInfo(name = "eventsPerWeek") val eventsPerWeek: Int,
    @ColumnInfo(name = "minutesPerWeek") val minutesPerWeek: Int,
    @ColumnInfo(name = "minMinutesPerEvent") val minMinutesPerEvent: Int,
    @ColumnInfo(name = "maxMinutesPerEvent") val maxMinutesPerEvent: Int,
    @ColumnInfo(name = "isActive") val isActive: Boolean,
    @ColumnInfo(name = "dateCreated") val dateCreated: Date,
    @ColumnInfo(name = "deadline") val deadline: Date? = null
)