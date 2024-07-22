package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.haloai.haloai_android_productivity.services.repository.ProductivityFeedOptionsRepository
import java.util.Date

class ProductivityFeedOptionsViewModel(private val productivityFeedOptionsRepository: ProductivityFeedOptionsRepository) : ViewModel() {

    suspend fun addToNote(title: String, description: String, extraDescription: String? = null) =
        viewModelScope.launch (Dispatchers.IO) {
        productivityFeedOptionsRepository.addToNote(title, description, extraDescription)
    }

    suspend fun addToTasks(title: String, description: String, extraDescription: String? = null,
                           deadline: Date? = null, priority: Int, context: Context) =
        viewModelScope.launch (Dispatchers.IO) {
        productivityFeedOptionsRepository.addToTasks(title, description, extraDescription, deadline, priority, context)
    }

    suspend fun addToLTGoals(title: String, description: String, extraDescription: String? = null,
                             deadline: Date? = null, priority: Int, context: Context)  =
        viewModelScope.launch (Dispatchers.IO){
        productivityFeedOptionsRepository.addToLTGoals(title, description, extraDescription, deadline, priority, context)
    }

    suspend fun addToCalendarAsEvent(title: String, description: String, extraDescription: String? = null, context: Context) =
        viewModelScope.launch (Dispatchers.IO) {
        productivityFeedOptionsRepository.addToCalendarAsEvent(title, description, extraDescription, context)
    }

}