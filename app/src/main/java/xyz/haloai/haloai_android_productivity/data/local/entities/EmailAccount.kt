package xyz.haloai.haloai_android_productivity.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emailDb")
data class EmailAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "emailType")  val emailType: enumEmailType,
    @ColumnInfo(name = "isActive") val isActive: Boolean,
    @ColumnInfo(name = "calendarIds") val calendarIds: List<String>,
    @ColumnInfo(name = "lastCalendarUpdateDateTime") val lastCalendarUpdateDateTime: Long,
)

enum class enumEmailType(val value: Int) {
    GMAIL(0),
    MICROSOFT(1),
    OTHER(2)
}