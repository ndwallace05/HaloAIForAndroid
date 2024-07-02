package xyz.haloai.haloai_android_productivity.services

import android.content.Context
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel

class AssistantModeFunctions(private val context: Context) {
     // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    suspend fun ask_ai(openAIViewModel: OpenAIViewModel, question: String): String {
        // Ask the AI a question
        return openAIViewModel.getChatGPTResponse("I am your AI Assistant. How can I help you?", question)
    }
}