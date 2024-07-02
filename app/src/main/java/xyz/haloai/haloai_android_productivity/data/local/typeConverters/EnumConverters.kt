package xyz.haloai.haloai_android_productivity.data.local.typeConverters

import androidx.room.TypeConverter
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEventType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumTimeSlotForTask

class timeSlotConverter {
    @TypeConverter
    fun fromInt(value: Int?): enumTimeSlotForTask? {
        return value?.let { enumTimeSlotForTask.entries.find { it.value == value } }
    }

    @TypeConverter
    fun toInt(todoTimeSlot: enumTimeSlotForTask?): Int? {
        return todoTimeSlot?.value
    }
}

class eventTypeConverter {
    @TypeConverter
    fun fromInt(value: Int?): enumEventType? {
        return value?.let { enumEventType.entries.find { it.value == value } }
    }

    @TypeConverter
    fun toInt(eventType: enumEventType?): Int? {
        return eventType?.value
    }
}

class emailTypeConverter {
    @TypeConverter
    fun fromInt(value: Int?): enumEmailType? {
        return value?.let { enumEmailType.entries.find { it.value == value } }
    }

    @TypeConverter
    fun toInt(eventType: enumEmailType?): Int? {
        return eventType?.value
    }
}