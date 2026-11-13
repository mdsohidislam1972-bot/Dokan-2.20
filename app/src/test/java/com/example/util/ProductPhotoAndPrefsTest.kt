package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProductPhotoAndPrefsTest {

    @Test
    fun compressImageForUpload_resizesLargeBitmapTo800PxAndUnder200Kb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempFile = File(context.cacheDir, "large_test_photo_${System.currentTimeMillis()}.png")

        // Create a high-res noisy bitmap (1600x1200) to exercise resize and JPEG compression
        val width = 1600
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val r = (x * 7 + y * 3) and 0xFF
                val g = (x * 11 + y * 5) and 0xFF
                val b = (x * 13 + y * 17) and 0xFF
                pixels[y * width + x] = Color.rgb(r, g, b)
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()

        val compressedBytes = ProductImageUtil.compressImageForUpload(context, Uri.fromFile(tempFile))
        tempFile.delete()

        assertTrue("Compressed bytes should not be empty", compressedBytes.isNotEmpty())
        assertTrue(
            "Compressed size (${compressedBytes.size} bytes) should be <= 200 KB",
            compressedBytes.size <= ProductImageUtil.DEFAULT_MAX_BYTES
        )

        val decoded = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
        val maxSide = max(decoded.width, decoded.height)
        decoded.recycle()

        assertTrue(
            "Max dimension ($maxSide px) should be <= 800 px",
            maxSide <= ProductImageUtil.DEFAULT_MAX_DIMENSION_PX
        )
    }

    @Test
    fun stockPreferences_persistsGridListChoiceAcrossInstances() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs1 = StockPreferences(context)
        prefs1.setStockGridView(true)
        assertTrue(prefs1.isStockGridView.first())

        // Recreate StockPreferences simulating app restart
        val prefs2 = StockPreferences(context)
        assertTrue(prefs2.isStockGridView.first())

        prefs2.setStockGridView(false)
        val prefs3 = StockPreferences(context)
        assertFalse(prefs3.isStockGridView.first())
    }
}
