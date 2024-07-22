package xyz.haloai.haloai_android_productivity.misc

import android.content.Context
import android.content.Intent
import android.net.Uri
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
    intent.data = android.net.Uri.parse("https://mail.google.com/mail/u/$emailAddress/#search?q=$searchQuery")
    intent.`package` = "com.google.android.gm"

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Fallback: Show an error message or handle the situation when Outlook is not installed
        // For example, you could open a browser with the Outlook web app
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://mail.google.com/mail/u/$emailAddress/#search?q=$searchQuery")
        }
        webIntent.`package` = "com.android.chrome"

        context.startActivity(webIntent)
    }

    // Add uri to open a specific account
    // intent.data = android.net.Uri.parse("mailto:")
    // intent.`package` = "com.google.android.gm"
    // intent.putExtra("u", emailAddress)
    // Uri: https://mail.google.com/mail/u/{emailAddress}/#search?q={searchQuery}
    // intent.data = android.net.Uri.parse("https://mail.google.com/mail/u/$emailAddress/#search?q=$searchQuery")

    // Open in chrome

    context.startActivity(intent)
}

fun launchOutlookSearch(searchQuery: String, emailAddress: String, context: Context) {
    val intent = Intent(Intent.ACTION_MAIN)

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addCategory(Intent.ACTION_VIEW)

    // Add uri to open a specific account
    // intent.data = android.net.Uri.parse("mailto:")
    intent.`package` = "com.microsoft.office.outlook"
    // intent.putExtra("u", emailAddress)

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Fallback: Show an error message or handle the situation when Outlook is not installed
        // For example, you could open a browser with the Outlook web app
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://outlook.live.com/mail/0/inbox")
        }
        context.startActivity(webIntent)
    }

    // context.startActivity(intent)
}