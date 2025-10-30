package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.HaloAI
import xyz.haloai.haloai_android_productivity.services.LocalLlmService
import xyz.haloai.haloai_android_productivity.services.repository.OpenAIRepository

class LlmViewModel(
    private val openAIRepository: OpenAIRepository,
    private val localLlmService: LocalLlmService,
    private val context: Context
) : ViewModel() {
    suspend fun getResponse(initialPromptText: String, promptText: String,
                            modelToUse: String = "gpt-3.5-turbo-1106", temperature: Double = 0.0): String {
        val provider = HaloAI.getLlmProvider(context)
        return if (provider == "openai") {
            openAIRepository.getChatGPTResponse(initialPromptText, promptText, modelToUse, temperature)
        } else {
            localLlmService.getLocalLlmResponse(initialPromptText + "\n" + promptText)
        }
    }

    suspend fun generateImageFromPrompt(promptText: String, modelToUse: String = "dall-e-3"): String? {
        val provider = HaloAI.getLlmProvider(context)
        return if (provider == "openai") {
            openAIRepository.generateImageFromPrompt(promptText, modelToUse)
        } else {
            null
        }
    }
}