package xyz.haloai.haloai_android_productivity.services.repository

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import xyz.haloai.haloai_android_productivity.services.TextExtractionFromImageService
import kotlin.coroutines.resumeWithException

interface TextExtractionRepository {
    suspend fun getTextFromBitmap(image: Bitmap, coroutineScope: CoroutineScope): String
}

class TextExtractionRepositoryImpl(private val service: TextExtractionFromImageService): TextExtractionRepository {

    private val _extractedText = MutableLiveData<String>()
    val extractedText: LiveData<String> get() = _extractedText

    private val liveDataMap = mutableMapOf<String, MutableLiveData<String>>()

    private fun extractTextFromImage(image: Bitmap) {
        service.extractTextFromImage(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text// .replace("HALO AI", "")
                liveDataMap[image.hashCode().toString()]?.postValue(extractedText)
                // _extractedText.postValue(extractedText)
            }
            .addOnFailureListener { e ->
                Log.e("TextExtraction", "Failed to extract text", e)
                // _extractedText.postValue("")
                liveDataMap[image.hashCode().toString()]?.postValue("")
            }
    }

    private fun getLiveData(key: String): MutableLiveData<String> {
        return liveDataMap.getOrPut(key) { MutableLiveData<String>() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getTextFromBitmap(image: Bitmap, coroutineScope: CoroutineScope): String {
        return suspendCancellableCoroutine { continuation ->
            val resultLiveData = getLiveData(image.hashCode().toString())

            // Extract text from the image
            coroutineScope.launch {
                extractTextFromImage(image)
            }

            // Observe LiveData for results
            val observer = object : Observer<String> {
                override fun onChanged(value: String) {
                    resultLiveData.removeObserver(this)
                    if (continuation.isActive) {
                        continuation.resume(value) {
                            continuation.resumeWithException(it)
                        }
                    }
                    liveDataMap.remove(image.hashCode().toString()) // Cleanup LiveData
                }
            }

            coroutineScope.launch(Dispatchers.Main) {
                resultLiveData.observeForever(observer)
            }

            continuation.invokeOnCancellation {
                coroutineScope.launch(Dispatchers.Main) {
                    resultLiveData.removeObserver(observer)
                    liveDataMap.remove(image.hashCode().toString()) // Cleanup LiveData
                }
            }
        }
    }
}