package xyz.haloai.haloai_android_productivity.data.local.typeConverters

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split("<DELIM>")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString("<DELIM>")
    }
}