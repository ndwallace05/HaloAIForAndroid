package xyz.haloai.haloai_android_productivity.services

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextExtractionFromImageService() {
    // Service to extract text from images using MLKit (Google's Machine Learning Kit)
    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun extractTextFromImage(image: Bitmap): Task<Text> {
        // Extract text from the image
        val inputImage = InputImage.fromBitmap(image, 0)
        return recognizer.process(inputImage)
    }
}