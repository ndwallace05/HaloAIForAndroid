package xyz.haloai.haloai_android_productivity.services.repository

import xyz.haloai.haloai_android_productivity.services.AssistantModeFunctions
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel

interface AssistantModeFunctionsRepository {
    // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    suspend fun ask_ai(openAIViewModel: OpenAIViewModel, question: String): String
}

class AssistantModeFunctionsImpl(private val assistantModeFunctions: AssistantModeFunctions): AssistantModeFunctionsRepository {
    // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    override suspend fun ask_ai(openAIViewModel: OpenAIViewModel, question: String): String {
        // Ask the AI a question
        return assistantModeFunctions.ask_ai(openAIViewModel, question)
    }
}