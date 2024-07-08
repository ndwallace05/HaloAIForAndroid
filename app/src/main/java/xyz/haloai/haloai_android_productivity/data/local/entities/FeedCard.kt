package xyz.haloai.haloai_android_productivity.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "productivityFeed")
data class FeedCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "extraDescription")val extraDescription: String,
    @ColumnInfo(name = "primaryActionType")val primaryActionType: enumFeedCardType,
    @ColumnInfo(name = "importanceScore")val importanceScore: enumImportanceScore,
    @ColumnInfo(name = "imgBase64")val imgBase64: String,
    @ColumnInfo(name = "creationTime")val creationTime: Date,
    @ColumnInfo(name = "deadline")val deadline: Date? = null,
    @ColumnInfo(name = "isCompleted")val isCompleted: Boolean = false,
)

enum class enumFeedCardType(val value: Int) {
    OTHER(0),
    TASK_SUGGESTION(1),
    POTENTIAL_TASK(2),
    POTENTIAL_EVENT(3),
    POTENTIAL_NOTE(4),
    POTENTIAL_LTGOAL(5),
    QUOTE(6),
    NEWSLETTER(7),
    YOUTUBE_VIDEO(8),
}

enum class enumImportanceScore(val value: Int) {
    VERY_LOW(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4)
}