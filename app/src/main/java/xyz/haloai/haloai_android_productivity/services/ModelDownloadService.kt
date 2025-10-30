package xyz.haloai.haloai_android_productivity.services

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ModelDownloadService(private val context: Context) {

    private val client = OkHttpClient()

    suspend fun downloadModel(
        url: String,
        fileName: String,
        onProgress: (Float) -> Unit,
        maxRetries: Int = 3
    ) {
        withContext(Dispatchers.IO) {
            val file = File(context.filesDir, fileName)
            val tempFile = File(context.filesDir, "$fileName.tmp")
            var downloadedBytes = if (tempFile.exists()) tempFile.length() else 0L
            var totalBytes: Long? = null
            var attempt = 0
            var success = false

            while (attempt < maxRetries && !success) {
                try {
                    val requestBuilder = Request.Builder().url(url)
                    if (downloadedBytes > 0) {
                        requestBuilder.addHeader("Range", "bytes=$downloadedBytes-")
                    }
                    val request = requestBuilder.build()
                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        val body = response.body ?: throw Exception("Response body is null")
                        val contentLength = body.contentLength()
                        totalBytes = if (response.code == 206) contentLength + downloadedBytes else contentLength

                        val outputStream = FileOutputStream(tempFile, downloadedBytes > 0)
                        body.byteStream().use { inputStream ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            var totalBytesRead = downloadedBytes

                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                if (totalBytes != null && totalBytes > 0) {
                                    val progress = (totalBytesRead.toFloat() / totalBytes.toFloat()) * 100f
                                    withContext(Dispatchers.Main) {
                                        onProgress(progress)
                                    }
                                }
                            }
                        }
                        outputStream.close()

                        if (totalBytes == null || tempFile.length() == totalBytes) {
                            success = true
                        } else {
                            throw Exception("Downloaded file is incomplete.")
                        }

                    } else if (response.code == 416) { // Range Not Satisfiable
                        if (file.exists() && file.length() == downloadedBytes) {
                            success = true // Already fully downloaded
                        } else {
                            // File on disk is not what the server thinks it is, restart download
                            downloadedBytes = 0
                            tempFile.delete()
                            throw Exception("Range not satisfiable, restarting download.")
                        }
                    } else {
                        throw Exception("Download failed with code ${response.code}")
                    }
                } catch (e: Exception) {
                    attempt++
                    if (attempt >= maxRetries) {
                        tempFile.delete()
                        throw e
                    }
                }
            }

            if (success) {
                if(file.exists()) {
                    file.delete()
                }
                tempFile.renameTo(file)
            }
        }
    }
}
