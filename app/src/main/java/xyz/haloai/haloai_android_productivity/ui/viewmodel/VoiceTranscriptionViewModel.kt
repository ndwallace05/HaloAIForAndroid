package xyz.haloai.haloai_android_productivity.ui.viewmodel

import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.haloai.haloai_android_productivity.services.repository.VoiceTranscriptionRepository

class VoiceTranscriptionViewModel(private val repository: VoiceTranscriptionRepository, private val activityResultRegistry: ActivityResultRegistry) : ViewModel() {

    private val _voiceInput = MutableStateFlow("")
    val voiceInput = _voiceInput.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        repository.initialize(::updateVoiceInput, activityResultRegistry)
    }

    fun startListening() {
        repository.startListening()
    }

    fun updateVoiceInput(input: String) {
        _voiceInput.value = input
    }
}