package xyz.haloai.haloai_android_productivity.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import xyz.haloai.haloai_android_productivity.services.repository.TextExtractionRepository

class TextExtractionFromImageViewModel(private val textExtractionRepository: TextExtractionRepository): ViewModel() {
    // ViewModel for extracting text from images
    // This ViewModel is used by the TextExtractionFromImageView
    // to extract text from images using the TextExtractionFromImageService
    // and provide the extracted text to the UI

    suspend fun getTextFromBitmap(image: Bitmap, coroutineScope: CoroutineScope): String {
        // Get text from the image
        return textExtractionRepository.getTextFromBitmap(image, coroutineScope)
    }
}