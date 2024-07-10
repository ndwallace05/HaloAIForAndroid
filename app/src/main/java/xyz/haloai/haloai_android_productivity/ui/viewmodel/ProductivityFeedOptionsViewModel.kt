package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.services.repository.ProductivityFeedOptionsRepository
import java.util.Date

class ProductivityFeedOptionsViewModel(private val productivityFeedOptionsRepository: ProductivityFeedOptionsRepository) : ViewModel() {

    suspend fun addToNote(title: String, description: String, extraDescription: String? = null)
    {
        productivityFeedOptionsRepository.addToNote(title, description, extraDescription)
    }

    suspend fun addToTasks(title: String, description: String, extraDescription: String? = null,
                           deadline: Date? = null, priority: Int, context: Context) {
        productivityFeedOptionsRepository.addToTasks(title, description, extraDescription, deadline, priority, context)
    }

    suspend fun addToLTGoals(title: String, description: String, extraDescription: String? = null,
                             deadline: Date? = null, priority: Int, context: Context) {
        productivityFeedOptionsRepository.addToLTGoals(title, description, extraDescription, deadline, priority, context)
    }

    suspend fun addToCalendarAsEvent(title: String, description: String, extraDescription: String? = null, context: Context) {
        productivityFeedOptionsRepository.addToCalendarAsEvent(title, description, extraDescription, context)
    }

}