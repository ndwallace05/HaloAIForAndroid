package xyz.haloai.haloai_android_productivity.ui.viewmodel

import androidx.lifecycle.ViewModel
import xyz.haloai.haloai_android_productivity.services.repository.AssistantModeFunctionsRepository

class AssistantModeFunctionsViewModel(private val assistantModeFunctionsRepository: AssistantModeFunctionsRepository): ViewModel() {
    suspend fun ask_ai(openAIViewModel: OpenAIViewModel, question: String): String {
        return assistantModeFunctionsRepository.ask_ai(openAIViewModel, question)
    }
}