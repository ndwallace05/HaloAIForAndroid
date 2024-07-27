package xyz.haloai.haloai_android_productivity.services

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts

class VoiceTranscriptionService() {

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
    }

    private lateinit var speechRecognitionLauncher: ActivityResultLauncher<Intent>
    private var onVoiceInputReceived: ((String) -> Unit)? = null

    fun register(onVoiceInputReceived: (String) -> Unit, activityResultRegistry: ActivityResultRegistry) {
        this.onVoiceInputReceived = onVoiceInputReceived
        speechRecognitionLauncher = activityResultRegistry.register(
            "voiceRecognition",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val voiceResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                voiceResults?.let {
                    if (it.isNotEmpty()) {
                        this.onVoiceInputReceived?.invoke(it[0])
                    }
                }
            }
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }

        try {
            speechRecognitionLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("VoiceRecognitionService", "Device doesn't support speech input", e)
        }
    }
}