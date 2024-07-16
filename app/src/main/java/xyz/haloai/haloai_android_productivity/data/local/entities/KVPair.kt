package xyz.haloai.haloai_android_productivity.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class KVPair(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "k") val k: String,
    @ColumnInfo(name = "v") val v: String,
    @ColumnInfo(name = "type") val type: EnumValType = EnumValType.STRING
)

enum class EnumValType(val value: Int) {
    STRING(0),
    LIST_STRING(1),
}