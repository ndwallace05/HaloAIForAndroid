package xyz.haloai.haloai_android_productivity.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "title") val title: String, // The title of the note
    @ColumnInfo(name = "summary") val summary: String, // A short summary of the note, AI generated
    @ColumnInfo(name = "content") var content: String, // The actual content of the note
    @ColumnInfo(name = "tag") val tag: String? = null
)