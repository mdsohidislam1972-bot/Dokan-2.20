package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Helper for creating camera FileProvider URIs and compressing product photos
 * to ~800px max dimension and ~200 KB JPEG before uploading to Firebase Storage.
 */
object ProductImageUtil {

    const val DEFAULT_MAX_DIMENSION_PX = 800
    const val DEFAULT_MAX_BYTES = 200 * 1024

    fun isLocalPhotoUri(uriString: String): Boolean {
        val trimmed = uriString.trim()
        return trimmed.isNotBlank() &&
            !trimmed.startsWith("http://", ignoreCase = true) &&
            !trimmed.startsWith("https://", ignoreCase = true)
    }

    fun createTempCameraPhotoUri(context: Context): Uri {
        val photoDir = File(context.cacheDir, "product_photos").apply { mkdirs() }
        val photoFile = File(photoDir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    fun saveCompressedBytesLocally(
        context: Context,
        fileName: String,
        bytes: ByteArray
    ): String {
        val dir = File(context.filesDir, "product_photos").apply { mkdirs() }
        val outFile = File(dir, fileName)
        outFile.writeBytes(bytes)
        return Uri.fromFile(outFile).toString()
    }

    fun compressImageForUpload(
        context: Context,
        uri: Uri,
        maxDimensionPx: Int = DEFAULT_MAX_DIMENSION_PX,
        maxBytes: Int = DEFAULT_MAX_BYTES
    ): ByteArray {
        val resolver = context.contentResolver

        // 1. Read dimensions without loading full bitmap into memory
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOpts)
        } ?: throw IllegalArgumentException("Could not open selected image")

        val rawWidth = boundsOpts.outWidth
        val rawHeight = boundsOpts.outHeight
        require(rawWidth > 0 && rawHeight > 0) { "Invalid or unsupported image file" }

        // 2. Read EXIF orientation if present
        val rotationDegrees = try {
            resolver.openInputStream(uri)?.use { exifStream ->
                val exif = ExifInterface(exifStream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (_: Exception) {
            0f
        }

        // 3. Subsample aggressively before full decode to avoid OOM on large camera photos
        var inSampleSize = 1
        val rawMax = max(rawWidth, rawHeight)
        while ((rawMax / (inSampleSize * 2)) >= maxDimensionPx) {
            inSampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            this.inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val sampledBitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOpts)
        } ?: throw IllegalArgumentException("Could not decode selected image")

        var workingBitmap = sampledBitmap

        // 4. Apply EXIF rotation if needed
        if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(
                workingBitmap,
                0,
                0,
                workingBitmap.width,
                workingBitmap.height,
                matrix,
                true
            )
            if (rotated != workingBitmap) {
                workingBitmap.recycle()
                workingBitmap = rotated
            }
        }

        // 5. Scale proportionally to fit within maxDimensionPx
        val longestSide = max(workingBitmap.width, workingBitmap.height)
        if (longestSide > maxDimensionPx) {
            val scale = maxDimensionPx.toFloat() / longestSide.toFloat()
            val targetW = max(1, (workingBitmap.width * scale).roundToInt())
            val targetH = max(1, (workingBitmap.height * scale).roundToInt())
            val scaled = Bitmap.createScaledBitmap(workingBitmap, targetW, targetH, true)
            if (scaled != workingBitmap) {
                workingBitmap.recycle()
                workingBitmap = scaled
            }
        }

        // 6. Compress JPEG down until within ~200 KB budget
        val baos = ByteArrayOutputStream()
        var quality = 85
        workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        while (baos.size() > maxBytes && quality > 35) {
            quality -= 10
            baos.reset()
            workingBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        }

        // 7. If still above target size, shrink dimensions slightly once or twice
        var shrinkAttempts = 0
        while (baos.size() > maxBytes && shrinkAttempts < 3) {
            shrinkAttempts++
            val nextW = max(1, (workingBitmap.width * 0.82f).roundToInt())
            val nextH = max(1, (workingBitmap.height * 0.82f).roundToInt())
            val smaller = Bitmap.createScaledBitmap(workingBitmap, nextW, nextH, true)
            if (smaller != workingBitmap) {
                workingBitmap.recycle()
                workingBitmap = smaller
            }
            baos.reset()
            workingBitmap.compress(Bitmap.CompressFormat.JPEG, 45, baos)
        }

        workingBitmap.recycle()
        return baos.toByteArray()
    }
}
