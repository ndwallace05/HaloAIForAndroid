package xyz.haloai.haloai_android_productivity.services.repository

import org.koin.core.component.KoinComponent
import xyz.haloai.haloai_android_productivity.services.OpenAIService

interface OpenAIRepository {

    suspend fun getChatGPTResponse(initialPromptText: String, promptText: String,
                                   modelToUse: String = "gpt-3.5-turbo-1106", temperature: Double = 0.0): String

}


class OpenAIRepositoryImpl(private val openAIService: OpenAIService) :
    OpenAIRepository, KoinComponent {

    override suspend fun getChatGPTResponse(initialPromptText: String, promptText: String,
                                           modelToUse: String, temperature: Double): String {
        return openAIService.getChatGPTResponse(initialPromptText, promptText, modelToUse, temperature)
    }
}