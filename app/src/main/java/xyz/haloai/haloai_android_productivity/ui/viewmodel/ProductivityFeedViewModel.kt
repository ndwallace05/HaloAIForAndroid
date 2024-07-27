package xyz.haloai.haloai_android_productivity.ui.viewmodel

import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.data.local.entities.FeedCard
import xyz.haloai.haloai_android_productivity.data.local.entities.enumEmailType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumFeedCardType
import xyz.haloai.haloai_android_productivity.data.local.repository.ProductivityFeedRepository
import java.util.Date

class ProductivityFeedViewModel(private val repository: ProductivityFeedRepository) : ViewModel() {

    suspend fun getAllFeedCards(): List<FeedCard> {
        return repository.getAllFeedCards()
    }

    suspend fun getFeedCardById(id: Long): FeedCard {
        return repository.getFeedCardById(id)
    }

    suspend fun insertFeedCard(title: String, description: String, extraDescription: String? = null, deadline: Date? = null, importanceScore: Int? = null, primaryActionType: enumFeedCardType): Long {
        return repository.insertFeedCard(title, description, extraDescription, deadline, importanceScore, primaryActionType)
    }

    suspend fun deleteFeedCardById(id: Long) {
        repository.deleteFeedCardById(id)
    }

    suspend fun deleteAllFeedCards() {
        repository.deleteAllFeedCards()
    }

    suspend fun markFeedCardAsCompleted(id: Long) {
        repository.markFeedCardAsCompleted(id)
    }

    suspend fun getTopFeedCards(count: Int): List<FeedCard> {
        return repository.getTopFeedCards(count)
    }

    suspend fun processEmailContent(emailId: String, emailType: enumEmailType, emailSubject: String, emailSnippet: String, emailSender: String, emailBody: String) {
        repository.processEmailContent(emailId, emailType, emailSubject, emailSnippet, emailSender, emailBody)
    }

    suspend fun updateSuggestedTasks() {
        repository.updateSuggestedTasks()
    }

    suspend fun getSuggestedTasks(): List<FeedCard> {
        return repository.getSuggestedTasks()
    }

}