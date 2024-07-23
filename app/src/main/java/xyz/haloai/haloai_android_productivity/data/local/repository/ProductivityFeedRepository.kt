package xyz.haloai.haloai_android_productivity.data.local.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.data.local.dao.ProductivityFeedDao
import xyz.haloai.haloai_android_productivity.data.local.entities.FeedCard
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumFeedCardType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumImportanceScore
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.encoding.ExperimentalEncodingApi

class ProductivityFeedRepository(private val productivityFeedDao: ProductivityFeedDao): KoinComponent {

    val openAIViewModel: OpenAIViewModel by inject() // To make AI calls

    val basePromptForImageGeneration = "A minimalistic illustration for a card meant to represent" +
            " the following task: \"<TITLE>\". The design should feature clean lines and a simple" +
            " color palette (like green and white, or blue and white, and other such color schemes). The " +
            "text <TITLE> should be prominently displayed, with an abstract icon or symbol " +
            "integrated into the design (like calendars, clocks, paper planes, etc.)."

    suspend fun getAllFeedCards(): List<FeedCard> = withContext(Dispatchers.IO) {
        return@withContext productivityFeedDao.getAll()
    }

    suspend fun getFeedCardById(id: Long): FeedCard = withContext(Dispatchers.IO) {
        return@withContext productivityFeedDao.getById(id)
    }

    suspend fun deleteAllFeedCards() = withContext(Dispatchers.IO) {
        productivityFeedDao.deleteAll()
    }

    suspend fun deleteFeedCardById(id: Long) = withContext(Dispatchers.IO) {
        productivityFeedDao.deleteById(id)
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun insertFeedCard(title: String, description: String, extraDescription: String? =
        null, deadline: Date? = null, importanceScore: Int? = null, primaryActionType: enumFeedCardType): Long =
        withContext(Dispatchers.IO) {
            val promptForImageGeneration = basePromptForImageGeneration.replace("<TITLE>", title)
            val imgBase64 = openAIViewModel.generateImageFromPrompt(promptForImageGeneration)
            val creationTime = Date()

            val impScore = importanceScore ?: enumImportanceScore.MEDIUM.value // TODO:
            // Ask Assistant to suggest importance score

            val feedCardToInsert = FeedCard(
                id = 0,
                title = title,
                description = description,
                extraDescription = extraDescription ?: "",
                deadline = deadline,
                importanceScore = enumImportanceScore.entries[impScore],
                imgBase64 = imgBase64,
                primaryActionType = primaryActionType,
                creationTime = creationTime
            )
            return@withContext productivityFeedDao.insert(feedCardToInsert)
        }

    suspend fun markFeedCardAsCompleted(id: Long, isCompleted: Boolean = true) = withContext(Dispatchers.IO) {
        productivityFeedDao.markFeedCardAsCompleted(id, isCompleted)
    }

    suspend fun getTopFeedCards(count: Int): List<FeedCard> = withContext(Dispatchers.IO) {
        var allCards = productivityFeedDao.getAll()
        // Sort by importance score (largest first) and return the top count cards
        allCards = allCards.sortedByDescending { it.importanceScore.value }
        return@withContext allCards.subList(0, count.coerceAtMost(allCards.size))
    }

    suspend fun processEmailContent(emailId: String, emailType: enumEmailType, emailSubject: String, emailSnippet: String, emailSender: String, emailBody: String) = withContext(Dispatchers.IO) {
        // Add logic to process email content
        // Example: repository.processEmailContent(emailId, emailType, emailSubject, emailSnippet, emailSender, emailBody)
        try {
            var initialPromptText =
                "You are a helpful assistant, whose job is to help the user be productive. \n" +
                        "\n" +
                        "Given an email received by the user, classify it into one of the following categories: \n" +
                        "1. Task: The email mentions a task that the user needs to perform (like sending a file, sending a calendar invite, doing some work, etc.)\n" +
                        "2. Newsletter: The email is a newsletter that the user has signed up " +
                        "for, that contains articles that they can read through later (regarding " +
                        "some relevant news). " +
                        "(It shouldn't just be updates from an event they signed up for, " +
                        "that's a notification).\n" +
                        "3. Respond: The email requires the user to respond to it\n" +
                        "4. Promotional: This is a promotional email (like one from Nike, " +
                        "Amazon, etc.), trying to sell the user something.\n" +
                        "5. Follow Up: The user has sent an email, and is expecting a response to" +
                        " this email. (The email should be one which the user expects a response " +
                        "to)." +
                        "6. Notification: This email is a notification about an " +
                        "order/event/something relevant to the user, which doesn't need their " +
                        "immediate attention.\n" +
                        "7. None: The email does not require the user to do anything (they " +
                        "can just read it and not do anything if they so choose). \n" +
                        "\n" +
                        "Only respond with one of \"Task\", \"Newsletter\", \"Respond\", " +
                        "\"Promotional\", \"Follow Up\", \"Notification\", or \"None\". Do not " +
                        "add " +
                        "any additional" +
                        " " +
                        "text" +
                        " or formatting."

            var truncatedEmailBody = emailBody
            if (truncatedEmailBody.length > 1000) {
                truncatedEmailBody = truncatedEmailBody.substring(0, 2000.coerceAtMost
                    (truncatedEmailBody.length))
            }

            var promptText = "The user's email id is: ${emailId}.\n" + truncatedEmailBody
            val response = openAIViewModel.getChatGPTResponse(initialPromptText, promptText)
            if (response.trim().lowercase() == "task") {
                // Get the tasks from the email, and add them to the feed
                initialPromptText =
                    "You are a helpful assistant, whose job is to help the user be productive. \n" +
                            "\n" +
                            "Given an email received by the user, extract the tasks mentioned in the email.\n" +
                            "Tasks can be anything that the user needs to do, like sending a file, sending a calendar invite, doing some work, etc.\n" +
                            "Only respond with the tasks mentioned in the email, in the following format:\n" +
                            "<Task Name> <DELIM> <Task Description> <DELIM> <Snippet> <DELIM> " +
                            "<Importance Score> <DELIM> <Deadline>\n" +
                            "<Task Name> <DELIM> <Task Description> <DELIM> <Snippet> <DELIM> " +
                            "<Importance Score> <DELIM> <Deadline>\n" +
                            "...\n" +
                            "\n" +
                            "The explanation for each field is given below:\n" +
                            "1. Task Name: A short 1-liner about the task\n" +
                            "2. Task Description: A longer description of the task (1-2 lines).\n" +
                            "3. Snippet: Snippet from email from which this is generated\n" +
                            "4. Importance Score: A Score between 1 (lowest priority) to 5 " +
                            "(highest priority) on how important this task is.\n" +
                            "5. Deadline: A string representing the deadline in the format \"MM/DD/YYYY\". If there is no deadline, respond with -1.\n" +
                            "\n" +
                            "You can generate multiple tasks, ensure that you respond with one per line. " +
                            "Also, instead of generating options like \"Confirm\", \"Deny\", etc., come " +
                            "up with one task \"Decide on the options\" and add it to the list of tasks. " +
                            "We want a set of different things that the user needs to decide on. If there" +
                            " are no tasks, respond with \"No tasks found\".\n"

                promptText = emailBody
                val allTasks = openAIViewModel.getChatGPTResponse(
                    initialPromptText,
                    promptText, modelToUse = "gpt-4o"
                )
                // Parse the tasks and add them to the schedule db
                if (allTasks.trim().lowercase() == "no tasks found") {
                    return@withContext
                }
                val tasks = allTasks.split("\n")
                for (task in tasks) {
                    val taskParts = task.split("<DELIM>")
                    if (taskParts.size == 5) {
                        var deadline: Date? = null
                        if (taskParts[4].trim() != "-1") {
                            deadline = SimpleDateFormat(
                                "MM/dd/yyyy",
                                Locale.getDefault()
                            ).parse(taskParts[4])
                        }
                        insertFeedCard(
                            title = taskParts[0],
                            description = taskParts[1],
                            extraDescription = taskParts[2],
                            deadline = deadline,
                            importanceScore = taskParts[3].trim().toInt() - 1,
                            primaryActionType = enumFeedCardType.POTENTIAL_TASK
                        )
                    }
                }
            } else if (response.trim().lowercase() == "newsletter") {
                // Add the email to the feed as a newsletter
                // TODO: Add a confirmation call to GPT 4o to confirm that this is a newsletter.
                return@withContext
                /*initialPromptText = "Tell me if this is a newsletter or not. Respond with \"Yes\"/\"No\" only."
                promptText = emailBody
                val isNewsletter = openAIViewModel.getChatGPTResponse(initialPromptText, promptText, modelToUse = "gpt-4o")
                if (isNewsletter.trim().lowercase() == "no") {
                    return@withContext
                }

                var emailTypeString = when (emailType) {
                    enumEmailType.GMAIL -> "Gmail"
                    enumEmailType.MICROSOFT -> "Microsoft"
                    enumEmailType.OTHER -> "Other Email Apps"
                }
                insertFeedCard(
                    title = emailSubject,
                    description = emailSnippet,
                    extraDescription = "${emailTypeString}: ${emailId}",
                    primaryActionType = enumFeedCardType.NEWSLETTER
                )*/
            } else if (response.trim().lowercase() == "respond") {
                // Add the email to the feed as a newsletter
                // <Sender Name>: <Subject>
                val desc = emailSender +
                        ": " + emailSubject
                insertFeedCard(
                    title = "You might want to respond to this.",
                    description = desc,
                    extraDescription = "Gmail: ${emailId}",
                    primaryActionType = enumFeedCardType.POTENTIAL_TASK
                )
            }
            /*else if (response.trim().lowercase() == "follow up") {
                insertFeedCard(
                    title = "Follow Up?",
                    description = emailSnippet,
                    extraDescription = "${emailTypeString}: ${emailId}",
                    primaryActionType = enumFeedCardType.NEWSLETTER
                )
            }*/
            else {
                // Do nothing
            }
        }
        catch (e: Exception) {
            Log.e("ProductivityFeedRepository", "Error processing email content: ${e.message}")
            Log.e("ProductivityFeedRepository", "Stack Trace: ${e.stackTraceToString()}")
        }
    }
}