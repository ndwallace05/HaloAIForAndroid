package xyz.haloai.haloai_android_productivity.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.data.local.dao.ProductivityFeedDao
import xyz.haloai.haloai_android_productivity.data.local.entities.FeedCard
import xyz.haloai.haloai_android_productivity.data.local.entities.enumFeedCardType
import xyz.haloai.haloai_android_productivity.data.local.entities.enumImportanceScore
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import java.util.Date
import kotlin.io.encoding.ExperimentalEncodingApi

class ProductivityFeedRepository(private val productivityFeedDao: ProductivityFeedDao): KoinComponent {

    val openAIViewModel: OpenAIViewModel by inject() // To make AI calls

    val basePromptForImageGeneration = "A minimalistic illustration for a card meant to represent" +
            " the following task: \"<TITLE>\". The design should feature clean lines and a simple" +
            " color palette (like green and white, or blue and white, etc.). The " +
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
}