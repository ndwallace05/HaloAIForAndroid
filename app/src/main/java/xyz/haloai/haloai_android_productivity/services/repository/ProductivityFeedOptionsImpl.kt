package xyz.haloai.haloai_android_productivity.services.repository

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import xyz.haloai.haloai_android_productivity.services.ProductivityFeedOptionsFunctions
import xyz.haloai.haloai_android_productivity.ui.viewmodel.LTGoalsViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.NotesDbViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface ProductivityFeedOptionsRepository {
    // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    suspend fun addToNote(title: String, description: String, extraDescription: String? = null)

    suspend fun addToTasks(title: String, description: String, extraDescription: String? = null,
                           deadline: Date? = null, priority: Int, context: Context)

    suspend fun addToLTGoals(title: String, description: String, extraDescription: String? = null,
                             deadline: Date? = null, priority: Int, context: Context)

    suspend fun addToCalendarAsEvent(title: String, description: String, extraDescription: String? = null, context: Context)
}

class ProductivityFeedOptionsImpl(private val productivityFeedOptionsFunctions: ProductivityFeedOptionsFunctions): ProductivityFeedOptionsRepository, KoinComponent {
    // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user

    val context = getKoin().get<Context>()
    val scheduleDbViewModel: ScheduleDbViewModel by inject { parametersOf(context, false) }
    val notesDbViewModel: NotesDbViewModel by inject()
    val openAIViewModel: OpenAIViewModel by inject()
    val ltGoalsViewModel: LTGoalsViewModel by inject()

    override suspend fun addToNote(title: String, description: String, extraDescription: String?) {
        // Add a note to the notes database
        // Get content to add to note
        val promptText = "You are a helpful assistant. Given some content that the user wants to " +
                "add to their notes, format a note that can be added to an existing note. Return " +
                "only the content that should be added to the note as a string (without any " +
                "additional formatting). Keep it short and concise."
        val contextText = "Title: $title\nDescription: $description\nExtra Description: $extraDescription"
        val contentToAdd = openAIViewModel.getChatGPTResponse(promptText, contextText, "gpt-3" +
                ".5-turbo-1106", 0.5)
        notesDbViewModel.addToSomeNote(
            content = contentToAdd,
            extraInfo = "Title: $title\nDescription: $description\nExtra Description: $extraDescription"
        )
    }

    override suspend fun addToTasks(title: String, description: String, extraDescription: String?,
                                    deadline: Date?, priority: Int, context: Context) {
        // Add a task to the tasks database
        // Get content to add to task
        val promptText = "You are a helpful assistant. Given some content that the user wants to " +
                "add to their tasks, format a task that can be added to an existing task. Return " +
                "the title and content of the task in the format 'Title: <title>\nDescription: " +
                "<description>\n Date: <date>'. Keep it short and concise. Do not generate any additional text, " +
                "and ensure that the title and description are separated by a newline. Format the date as 'YYYY-MM-DD'. If there is no date that the task is due, return Date: -1"
        val contextText = "Title: $title\nDescription: $description\nExtra Description: " +
                "$extraDescription\nDeadline: $deadline\nPriority: $priority"
        val responseText = openAIViewModel.getChatGPTResponse(promptText, contextText, "gpt-3" +
                ".5-turbo-1106", 0.5)
        // Parse the response
        val taskTitle = responseText.substringAfter("Title: ").substringBefore("Description: ")
        val taskDescription = responseText.substringAfter("Description: ").substringBefore("Date: ")
        val taskDeadline = responseText.substringAfter("Date: ").trim()
        val deadlineToUse = if (taskDeadline == "-1") {
            null
        } else {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(taskDeadline)
        }

        scheduleDbViewModel.insertOrUpdate(
            title = taskTitle,
            description = taskDescription,
            context = context,
            startTime = deadlineToUse,
            sourceEmailId = "ProductivityFeed"
        )
    }

    override suspend fun addToLTGoals(
        title: String,
        description: String,
        extraDescription: String?,
        deadline: Date?,
        priority: Int,
        context: Context
    ) {
        var desc = description
        if (extraDescription != null) {
            desc += "\n$extraDescription"
        }
        ltGoalsViewModel.insert(title, desc, deadline)
    }

    override suspend fun addToCalendarAsEvent(
        title: String,
        description: String,
        extraDescription: String?,
        context: Context
    ) {
        // Add an event to the calendar
        // Get content to add to event
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        val promptText = "You are a helpful assistant. Given some content that the user wants to " +
                "add to their calendar as an event, format an event that can be added to the " +
                "calendar. Return the title and description of the event in the format 'Title: " +
                "<title>\nDescription: <description>\nStart Time: <startTime>\n End Time: <endTime>'. Keep it short and concise. Do not generate " +
                "any additional text, and ensure that the title and description are separated by a " +
                "newline. Format the start and end time as 'YYYY-MM-DDTHH:MM:SS'. If there is no end time specified, set it to the same as the start time. If there is no start time specified, set it to 30 mins from the current time ($currentTime)"
        val contextText = "Title: $title\nDescription: $description\nExtra Description: " +
                "$extraDescription"
        val responseText = openAIViewModel.getChatGPTResponse(promptText, contextText, "gpt-3" +
                ".5-turbo-1106", 0.5)
        // Parse the response
        val eventTitle = responseText.substringAfter("Title: ").substringBefore("Description: ")
        val eventDescription = responseText.substringAfter("Description: ").substringBefore("Start Time: ")
        val startTime = responseText.substringAfter("Start Time: ").substringBefore("End Time: ")
        val endTime = responseText.substringAfter("End Time: ")
        val parsedStartDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(startTime)
        val parsedEndDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(endTime)

        scheduleDbViewModel.insertOrUpdate(
            title = eventTitle,
            description = eventDescription,
            startTime = parsedStartDateTime,
            endTime = parsedEndDateTime,
            context = context,
            sourceEmailId = "ProductivityFeed"
        )
    }

}