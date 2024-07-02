package xyz.haloai.haloai_android_productivity.services

import android.content.Context
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import xyz.haloai.haloai_android_productivity.HaloAI
import kotlin.time.Duration.Companion.seconds

class OpenAIService(private val context: Context) {

    private var openAI = OpenAI(
        token = HaloAI.openAI_API_KEY,
        timeout = Timeout(request = 60.seconds),
        // additional configurations...
    )

    suspend fun getChatGPTResponse(initialPromptText:String, promptText: String,
                                   modelToUse:String = "gpt-3.5-turbo-1106", temperature: Double
                                   = 0.0): String {

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId(modelToUse),
            temperature = temperature,
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = initialPromptText
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = promptText
                )
            )
        )

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        // Log.d("xyz.haloai.app", "CGPT: ${completion.choices[0].message.content.toString()}")
        if (completion.choices[0].message.content != null) {
            return completion.choices[0].message.content!!
        }
        else {
            return ""
        }
        // return completion.choices[0].message.content
    }
}