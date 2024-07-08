package xyz.haloai.haloai_android_productivity.services

import android.content.Context

class ProductivityFeedOptionsFunctions(private val context: Context) {
    // Functions used by the Productivity Feed Options to perform tasks like sending emails, scheduling events, etc. for the user
    suspend fun getFeedOptions(): List<String> {
        // Get the list of feed options
        return listOf("Email", "Schedule Event", "Create Task", "Set Reminder", "View Calendar")
    }
}