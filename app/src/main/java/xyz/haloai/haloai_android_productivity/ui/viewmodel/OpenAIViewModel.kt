package xyz.haloai.haloai_android_productivity.ui.viewmodel

import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.services.repository.OpenAIRepository

class OpenAIViewModel(private val openAIRepository: OpenAIRepository) : ViewModel() {
    suspend fun getChatGPTResponse(initialPromptText: String, promptText: String,
                                   modelToUse: String = "gpt-3.5-turbo-1106", temperature: Double = 0.0): String {
        return openAIRepository.getChatGPTResponse(initialPromptText, promptText, modelToUse, temperature)
    }
}