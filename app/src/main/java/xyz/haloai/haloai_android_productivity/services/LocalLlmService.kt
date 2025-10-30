package xyz.haloai.haloai_android_productivity.services

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class LocalLlmService(private val context: Context) {

    private var llmInference: LlmInference? = null

    private fun getModelPath(): String {
        val modelName = "gemma-2b-it-cpu-int4.bin"
        val modelFile = File(context.filesDir, modelName)
        return if (modelFile.exists()) {
            modelFile.absolutePath
        } else {
            ""
        }
    }

    fun loadModel() {
        val modelPath = getModelPath()
        if (modelPath.isNotEmpty()) {
            try {
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .build()
                llmInference = LlmInference.createFromOptions(context, options)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        loadModel()
    }

    fun getLocalLlmResponse(promptText: String): String {
        if (llmInference == null) {
            return "Model not loaded. Please download the model from the settings screen."
        }
        return try {
            val response = llmInference?.generateResponse(promptText)
            response ?: "No response from model."
        } catch (e: Exception) {
            // Handle inference exception
            e.printStackTrace()
            "Error during inference."
        }
    }
}
