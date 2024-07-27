package xyz.haloai.haloai_android_productivity.services.repository

import androidx.activity.result.ActivityResultRegistry
import xyz.haloai.haloai_android_productivity.services.VoiceTranscriptionService

class VoiceTranscriptionRepository(private val voiceRecognitionService: VoiceTranscriptionService) {

    fun initialize(updateVoiceInputFun: (String) -> Unit, activityResultRegistry: ActivityResultRegistry) {
        voiceRecognitionService.register(
            {
                input ->
                updateVoiceInputFun(input)
            },
            activityResultRegistry
        )
    }
    fun startListening() {
        voiceRecognitionService.startListening()
    }
}
