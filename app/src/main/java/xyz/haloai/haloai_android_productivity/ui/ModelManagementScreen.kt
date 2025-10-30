package xyz.haloai.haloai_android_productivity.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.haloai.haloai_android_productivity.services.LocalLlmService
import xyz.haloai.haloai_android_productivity.services.ModelDownloadService
import org.koin.androidx.compose.get

@Composable
fun ModelManagementScreen() {
    val modelDownloadService = get<ModelDownloadService>()
    val localLlmService = get<LocalLlmService>()
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = {
                isDownloading = true
                downloadCompleted = false
                downloadProgress = 0f
                coroutineScope.launch {
                    try {
                        modelDownloadService.downloadModel(
                            url = "https://storage.googleapis.com/mediapipe-models/large_language_model/gemma-2b-it-cpu/gemma-2b-it-cpu-int4.bin",
                            fileName = "gemma-2b-it-cpu-int4.bin",
                            onProgress = { progress ->
                                downloadProgress = progress
                            }
                        )
                        localLlmService.loadModel()
                        downloadCompleted = true
                    } finally {
                        isDownloading = false
                    }
                }
            },
            enabled = !isDownloading
        ) {
            Text(text = "Download Gemma-2B Model")
        }

        if (isDownloading) {
            LinearProgressIndicator(progress = downloadProgress / 100f)
            Text(text = "Downloading: ${downloadProgress.toInt()}%")
        }

        if (downloadCompleted) {
            Text(text = "Download complete!")
        }
    }
}
