package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Clean, vector-rendered QR Code Composable using ZXing BitMatrix.
 * Renders on a Compose Canvas without bitmap blur or network dependencies.
 */
@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 180,
    backgroundColor: Color = Color.White,
    codeColor: Color = Color.Black
) {
    val bitMatrix = remember(content) {
        try {
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 256, 256)
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitMatrix != null) {
            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height

            Canvas(modifier = Modifier.size((sizeDp - 20).dp)) {
                val moduleWidth = size.width / matrixWidth
                val moduleHeight = size.height / matrixHeight

                for (x in 0 until matrixWidth) {
                    for (y in 0 until matrixHeight) {
                        if (bitMatrix.get(x, y)) {
                            drawRect(
                                color = codeColor,
                                topLeft = Offset(x * moduleWidth, y * moduleHeight),
                                size = Size(moduleWidth + 0.5f, moduleHeight + 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
