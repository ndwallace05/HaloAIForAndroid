package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.haloai.haloai_android_productivity.services.repository.AssistantModeFunctionsRepository
import xyz.haloai.haloai_android_productivity.xyz.haloai.haloai_android_productivity.data.ui.screens.ChatHistory

class AssistantModeFunctionsViewModel(private val assistantModeFunctionsRepository: AssistantModeFunctionsRepository): ViewModel() {
    suspend fun ask_ai(conversation: List<ChatHistory.Message>): String {
        return assistantModeFunctionsRepository.ask_ai(conversation = conversation)
    }

    suspend fun processScreenshot(screenshot: Bitmap) = viewModelScope.launch {
        assistantModeFunctionsRepository.processScreenshot(screenshot = screenshot)
    }
}