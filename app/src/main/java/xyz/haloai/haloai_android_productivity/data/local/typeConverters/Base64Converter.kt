package xyz.haloai.haloai_android_productivity.data.local.typeConverters

import androidx.room.TypeConverter

class Base64Converter {
    @TypeConverter
    fun fromString(value: String?): ByteArray? {
        return value?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
    }

    @TypeConverter
    fun toString(value: ByteArray?): String? {
        return value?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }
    }
}