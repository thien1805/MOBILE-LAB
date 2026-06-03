package com.example.homework.ai

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ImageAnalyzer(private val context: Context) {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.DEFAULT_OPTIONS
    )

    suspend fun analyze(imageUrl: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)

                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                val drawable = result.drawable

                val bitmap = (drawable as? BitmapDrawable)?.bitmap
                    ?: return@withContext emptyList()

                val image = InputImage.fromBitmap(bitmap, 0)
                val labels = labeler.process(image).await()

                labels.take(3).map { label ->
                    label.text
                }

            } catch (e: Exception) {
                emptyList()
            }
        }
}