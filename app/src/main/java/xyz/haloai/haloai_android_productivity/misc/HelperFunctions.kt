package xyz.haloai.haloai_android_productivity.misc

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

fun normalizeToUTC(date: Date): Date {
    val calendar = Calendar.getInstance().apply {
        time = date
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return calendar.time
}