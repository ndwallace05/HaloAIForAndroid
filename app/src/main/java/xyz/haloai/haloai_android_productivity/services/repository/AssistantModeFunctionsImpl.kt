package xyz.haloai.haloai_android_productivity.services.repository

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import xyz.haloai.haloai_android_productivity.services.AssistantModeFunctions
import xyz.haloai.haloai_android_productivity.services.EnumFunctionTypes
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.ChatHistory

interface AssistantModeFunctionsRepository {
    // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    suspend fun ask_ai(conversation: List<ChatHistory.Message>): String
}

class AssistantModeFunctionsImpl(private val assistantModeFunctions: AssistantModeFunctions): AssistantModeFunctionsRepository, KoinComponent {

    // Functions used by the Assistant Mode (AI Assistant) to perform tasks like sending emails, scheduling events, etc. for the user
    private val openAIViewModel: OpenAIViewModel by inject()

    // Business Logic
    override suspend fun ask_ai(conversation: List<ChatHistory.Message>): String {
        // Ask the AI a question
        val rawResponse = assistantModeFunctions.ask_ai(conversation.last().text)
        val parsedResponse = assistantModeFunctions.parse_ai_response(rawResponse)
        var finalResponse = ""
        for (instruction in parsedResponse)
        {
            if (instruction.function == EnumFunctionTypes.ADD_TO_NOTES)
            {
                assistantModeFunctions.addToNotes(instruction.params["note"])
            }
            else if (instruction.function == EnumFunctionTypes.SEND_EMAIL)
            {
                assistantModeFunctions.sendEmail(instruction.params["email"])
            }
            else if (instruction.function == EnumFunctionTypes.RESPOND_TO_USER)
            {
                finalResponse = instruction.params["response"].toString()
            }
        }
        return rawResponse
    }

}