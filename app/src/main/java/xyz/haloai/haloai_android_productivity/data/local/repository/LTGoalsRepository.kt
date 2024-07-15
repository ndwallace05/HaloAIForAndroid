package xyz.haloai.haloai_android_productivity.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.data.local.dao.LTGoalDao
import xyz.haloai.haloai_android_productivity.data.local.entities.LTGoal
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LTGoalsRepository(private val ltGoalDao: LTGoalDao): KoinComponent {

    val openAIViewModel: OpenAIViewModel by inject() // To make AI calls

    suspend fun insert(title: String, content: String, deadline: Date? = null): Long = withContext(Dispatchers.IO) {
        var currentDate = Date()
        // Get events per week, minutes per week, min minutes per event, max minutes per event from GPT
        var configDetails = getTimeParams(title, content, deadline)
        val ltGoalToInsert = LTGoal(
            id = 0,
            title = title,
            context = content,
            eventsPerWeek = configDetails.eventsPerWeek,
            minutesPerWeek = configDetails.minutesPerWeek,
            minMinutesPerEvent = configDetails.minMinutesPerEvent,
            maxMinutesPerEvent = configDetails.maxMinutesPerEvent,
            isActive = true,
            dateCreated = currentDate,
            deadline = deadline
        )
        return@withContext ltGoalDao.insertGoal(ltGoalToInsert)
    }

    suspend fun getLTGoalById(id: Long): LTGoal = withContext(Dispatchers.IO) {
        return@withContext ltGoalDao.getById(id)
    }

    suspend fun getAllLTGoals(): List<LTGoal> = withContext(Dispatchers.IO) {
        return@withContext ltGoalDao.getAll()
    }

    suspend fun deleteAllLTGoals() = withContext(Dispatchers.IO) {
        ltGoalDao.deleteAll()
    }

    suspend fun deleteLTGoalById(id: Long) = withContext(Dispatchers.IO) {
        ltGoalDao.deleteById(id)
    }

    suspend fun updateLTGoal(
        id: Long,
        title: String? = null,
        content: String? = null,
        deadline: Date? = null,
        eventsPerWeek: Int? = null,
        minutesPerWeek: Int? = null,
        minMinutesPerEvent: Int? = null,
        maxMinutesPerEvent: Int? = null,
        isActive: Boolean? = null
    ) = withContext(Dispatchers.IO) {
        /*if (title == null && content == null && deadline == null)
        {
            return@withContext false
        }*/
        val ltGoal = ltGoalDao.getById(id)
        val titleToUse = title ?: ltGoal.title
        val contentToUse = content ?: ltGoal.context
        val deadlineToUse = deadline ?: ltGoal.deadline
        val eventsPerWeekToUse = eventsPerWeek ?: ltGoal.eventsPerWeek
        val minutesPerWeekToUse = minutesPerWeek ?: ltGoal.minutesPerWeek
        val minMinutesPerEventToUse = minMinutesPerEvent ?: ltGoal.minMinutesPerEvent
        val maxMinutesPerEventToUse = maxMinutesPerEvent ?: ltGoal.maxMinutesPerEvent
        val isActiveToUse = isActive ?: ltGoal.isActive
        val updatedLTGoal = LTGoal(
            id = id,
            title = titleToUse,
            context = contentToUse,
            eventsPerWeek = eventsPerWeekToUse,
            minutesPerWeek = minutesPerWeekToUse,
            minMinutesPerEvent = minMinutesPerEventToUse,
            maxMinutesPerEvent = maxMinutesPerEventToUse,
            isActive = isActiveToUse,
            dateCreated = ltGoal.dateCreated,
            deadline = deadlineToUse
        )
        ltGoalDao.deleteById(id)
        ltGoalDao.insertGoal(updatedLTGoal)
    }

    private suspend fun getTimeParams(title: String, content: String, deadline: Date?): LTGoalConfigDetails = withContext(Dispatchers.IO) {
        var defaultParams: LTGoalConfigDetails = LTGoalConfigDetails(
            eventsPerWeek = 2,
            minutesPerWeek = 60,
            minMinutesPerEvent = 15,
            maxMinutesPerEvent = 60
        )
        val promptText = "You are a helpful assistant, helping the user achieve a long-term goal of theirs by helping them stay on track, and scheduling events in their calendar.\n" +
                "Given the title, deadline, and some details about this goal relevant to the user (like their preferences, how regular they've been, etc.), provide the following details:\n" +
                "1. Number of events per week\n" +
                "2. Minutes per week\n" +
                "3. Minimum minutes per event\n" +
                "4. Maximum minutes per event.\n" +
                "Respond with 4 comma separated values, in order, corresponding to each of the above. They should all be integers. Only output the integers, and no other details."
        var deadlineAsString = "" // Format deadline as "YYYY-MM-DD"
        if (deadline != null)
        {
            deadlineAsString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(deadline)
        }
        else {
            val oneYearFromNow = Calendar.getInstance()
            oneYearFromNow.add(Calendar.YEAR, 1)
            deadlineAsString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(oneYearFromNow.time)
        }
        val contextText = "Title: $title\nContext: $content\nDeadline: $deadlineAsString"
        val response = openAIViewModel.getChatGPTResponse(promptText, contextText)
        // Parse response to get the 4 values
        var parsedResponse = response.split(",")
        if (parsedResponse.size == 4) {
            try {
                defaultParams = LTGoalConfigDetails(
                    eventsPerWeek = parsedResponse[0].trim().toInt(),
                    minutesPerWeek = parsedResponse[1].trim().toInt(),
                    minMinutesPerEvent = parsedResponse[2].trim().toInt(),
                    maxMinutesPerEvent = parsedResponse[3].trim().toInt()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return@withContext defaultParams
    }



    class LTGoalConfigDetails(
        val eventsPerWeek: Int,
        val minutesPerWeek: Int,
        val minMinutesPerEvent: Int,
        val maxMinutesPerEvent: Int
    )

}