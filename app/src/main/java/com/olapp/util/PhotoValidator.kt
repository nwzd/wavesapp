package com.olapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

sealed class PhotoValidationResult {
    object Valid : PhotoValidationResult()
    object NoFace : PhotoValidationResult()
    object TooManyFaces : PhotoValidationResult()
    object Inappropriate : PhotoValidationResult()
}

object PhotoValidator {

    private val INAPPROPRIATE_LABELS = setOf(
        "Swimwear", "Undergarment", "Lingerie", "Bikini", "Brassiere"
    )
    private const val INAPPROPRIATE_THRESHOLD = 0.75f
    private const val MAX_BITMAP_SIDE = 1024

    suspend fun validate(context: Context, uri: Uri): PhotoValidationResult {
        val bitmap = loadScaledBitmap(context, uri)
            ?: return PhotoValidationResult.NoFace
        val image = InputImage.fromBitmap(bitmap, 0)

        val faceOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        val faces = FaceDetection.getClient(faceOptions).process(image).await()
        when {
            faces.isEmpty() -> return PhotoValidationResult.NoFace
            faces.size > 1 -> return PhotoValidationResult.TooManyFaces
        }

        val labels = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            .process(image).await()
        val inappropriate = labels.any { label ->
            label.text in INAPPROPRIATE_LABELS && label.confidence >= INAPPROPRIATE_THRESHOLD
        }
        if (inappropriate) return PhotoValidationResult.Inappropriate

        return PhotoValidationResult.Valid
    }

    private fun loadScaledBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { s ->
                BitmapFactory.decodeStream(s, null, opts)
            }
            val scale = maxOf(opts.outWidth, opts.outHeight) / MAX_BITMAP_SIDE
            opts.inJustDecodeBounds = false
            opts.inSampleSize = maxOf(1, scale)
            context.contentResolver.openInputStream(uri)?.use { s ->
                BitmapFactory.decodeStream(s, null, opts)
            }
        } catch (e: Exception) {
            null
        }
    }
}
