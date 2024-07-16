package xyz.haloai.haloai_android_productivity.misc

import android.content.Context
import android.content.Intent
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

fun launchGmailSearch(searchQuery: String, emailAddress: String, context: Context) {
    val intent = Intent(Intent.ACTION_MAIN)

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addCategory(Intent.CATEGORY_APP_EMAIL)

    // Add uri to open a specific account
    // intent.data = android.net.Uri.parse("mailto:")
    intent.`package` = "com.google.android.gm"
    intent.putExtra("u", emailAddress)

    context.startActivity(intent)
}

fun launchOutlookSearch(searchQuery: String, emailAddress: String, context: Context) {
    val intent = Intent(Intent.ACTION_MAIN)

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addCategory(Intent.CATEGORY_APP_EMAIL)

    // Add uri to open a specific account
    // intent.data = android.net.Uri.parse("mailto:")
    intent.`package` = "com.microsoft.office.outlook"
    // intent.putExtra("u", emailAddress)

    context.startActivity(intent)
}