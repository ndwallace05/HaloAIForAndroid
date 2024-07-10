package xyz.haloai.haloai_android_productivity.services

import android.content.Context
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import okhttp3.OkHttpClient
import okhttp3.Request
import xyz.haloai.haloai_android_productivity.HaloAI
import java.io.IOException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
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

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun generateImageFromPrompt(promptText: String, modelToUse: String = "dall-e-3"):
            String {
        val imageCreationRequest = ImageCreation(
            prompt = promptText,
            model = ModelId(modelToUse),
            n = 1,
            size = ImageSize.is1024x1024
        )

        val images = openAI.imageURL( // or openAI.imageJSON
            creation = imageCreationRequest
        )

        val imageUrl = images.firstOrNull()?.url ?: throw IOException("No image URL found")
        val imageRequest = Request.Builder().url(imageUrl).build()

        val client = OkHttpClient()

        client.newCall(imageRequest).execute().use { imageResponse ->
            if (!imageResponse.isSuccessful) throw IOException("Unexpected code $imageResponse")

            val imageBytes = imageResponse.body?.bytes() ?: throw IOException("Empty image body")
            return Base64.encode(imageBytes)
        }

    }
}