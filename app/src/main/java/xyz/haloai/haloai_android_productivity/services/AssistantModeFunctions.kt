package xyz.haloai.haloai_android_productivity.services

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel

class AssistantModeFunctions(private val context: Context): KoinComponent {
     // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    val openAIViewModel: OpenAIViewModel by inject()

    suspend fun ask_ai(question: String): String {
        // Ask the AI a question
        return openAIViewModel.getChatGPTResponse("I am your AI Assistant. How can I help you?", question)
    }

    suspend fun addToNotes(noteText: String?) {
        // Add the user's request to the notes
    }

    suspend fun sendEmail(emailText: String?) {
        // Send an email to the user
    }

    suspend fun parse_ai_response(rawResponse: String): List<FunctionsWithParams> {
        // Parse the AI's response
        // return rawResponse.split("\n")
        return listOf(
            FunctionsWithParams(EnumFunctionTypes.ADD_TO_NOTES, mapOf("note" to "This is a note")),
            FunctionsWithParams(EnumFunctionTypes.SEND_EMAIL, mapOf("email" to "This is an email")),
            FunctionsWithParams(EnumFunctionTypes.RESPOND_TO_USER, mapOf("response" to "This is a response"))
        )
    }
}

data class FunctionsWithParams(
    val function: EnumFunctionTypes,
    val params: Map<String, String>
)

enum class EnumFunctionTypes {
    ADD_TO_NOTES,
    SEND_EMAIL,
    RESPOND_TO_USER
}