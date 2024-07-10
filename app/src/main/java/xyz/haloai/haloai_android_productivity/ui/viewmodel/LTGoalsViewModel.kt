package xyz.haloai.haloai_android_productivity.ui.viewmodel

import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.data.local.entities.LTGoal
import xyz.haloai.haloai_android_productivity.data.local.repository.LTGoalsRepository
import java.util.Date

class LTGoalsViewModel(private val repository: LTGoalsRepository) : ViewModel() {

    suspend fun insert(title: String, content: String, deadline: Date? = null): Long {
        return repository.insert(title, content, deadline)
    }

    suspend fun deleteAllLTGoals() {
        repository.deleteAllLTGoals()
    }

    suspend fun deleteLTGoalById(id: Long) {
        repository.deleteLTGoalById(id)
    }

    suspend fun getAllLTGoals(): List<LTGoal> {
        return repository.getAllLTGoals()
    }

    suspend fun getLTGoalById(id: Long): LTGoal {
        return repository.getLTGoalById(id)
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
    ) {
        repository.updateLTGoal(id, title, content, deadline, eventsPerWeek, minutesPerWeek, minMinutesPerEvent, maxMinutesPerEvent, isActive)
    }

}