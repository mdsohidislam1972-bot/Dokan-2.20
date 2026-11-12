package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

data class ReportTableData(
    val title: String,
    val storeName: String,
    val storePhone: String = "",
    val storeAddress: String = "",
    val periodLabel: String,
    val generatedAt: String,
    val headers: List<String>,
    val rows: List<List<String>>,
    val numericColumns: Set<Int> = emptySet(),
    val rawNumbers: List<List<Double?>> = emptyList(),
    val totals: List<String>? = null,
    val rawTotals: List<Double?> = emptyList(),
    val notes: String? = null
)

object ReportExportUtil {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Generates a multi-page PDF using native Android PdfDocument.
     * Android's standard Typeface natively handles Bengali Unicode script with
     * correct ligatures, conjuncts, and vowel placements.
     */
    fun generatePdfReport(context: Context, data: ReportTableData, fileName: String): File {
        val file = File(getReportsDir(context), if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf")

        val pageWidth = 595 // Standard A4 points (72 dpi)
        val pageHeight = 842
        val margin = 36f
        val contentWidth = pageWidth - (margin * 2)

        val doc = PdfDocument()

        val embeddedBanglaFont = try {
            androidx.core.content.res.ResourcesCompat.getFont(context, com.example.R.font.noto_sans_bengali)
        } catch (e: Exception) {
            null
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 9f
            typeface = embeddedBanglaFont ?: Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f
            typeface = if (embeddedBanglaFont != null) Typeface.create(embeddedBanglaFont, Typeface.BOLD) else Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 8.5f
            typeface = embeddedBanglaFont ?: Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 9f
            typeface = if (embeddedBanglaFont != null) Typeface.create(embeddedBanglaFont, Typeface.BOLD) else Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val bgPaint = Paint().apply {
            style = Paint.Style.FILL
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.6f
            style = Paint.Style.STROKE
        }

        val colCount = data.headers.size.coerceAtLeast(1)
        val colWidth = contentWidth / colCount
        val rowHeight = 20f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = doc.startPage(pageInfo)
        var canvas = page.canvas

        fun drawPageHeader(c: Canvas): Float {
            var y = margin + 14f

            // Store Name
            c.drawText(data.storeName.ifBlank { "StoreOS Dokan" }, margin, y, titlePaint)
            y += 13f

            // Phone & Address if present
            val contactInfo = listOf(data.storePhone, data.storeAddress).filter { it.isNotBlank() }.joinToString(" • ")
            if (contactInfo.isNotBlank()) {
                c.drawText(contactInfo, margin, y, subPaint)
                y += 11f
            }

            // Report Title
            headerPaint.textSize = 12f
            c.drawText(data.title, margin, y, headerPaint)
            headerPaint.textSize = 9f
            y += 12f

            // Period & Generated At
            val metaStr = "Period: ${data.periodLabel}  |  Generated: ${data.generatedAt}"
            c.drawText(metaStr, margin, y, subPaint)
            y += 10f

            // Divider
            c.drawLine(margin, y, margin + contentWidth, y, linePaint)
            y += 10f

            // Table Column Headers Background
            bgPaint.color = Color.rgb(240, 243, 246)
            c.drawRect(margin, y - 2f, margin + contentWidth, y + rowHeight - 2f, bgPaint)

            // Table Column Headers Text
            for (i in data.headers.indices) {
                val headerText = data.headers[i]
                val isNum = data.numericColumns.contains(i)
                val x = if (isNum) {
                    margin + (i + 1) * colWidth - 4f
                } else {
                    margin + i * colWidth + 4f
                }
                if (isNum) {
                    headerPaint.textAlign = Paint.Align.RIGHT
                } else {
                    headerPaint.textAlign = Paint.Align.LEFT
                }
                c.drawText(headerText, x, y + 11f, headerPaint)
            }
            headerPaint.textAlign = Paint.Align.LEFT

            c.drawLine(margin, y + rowHeight - 2f, margin + contentWidth, y + rowHeight - 2f, linePaint)
            return y + rowHeight + 2f
        }

        var currentY = drawPageHeader(canvas)

        // Draw data rows
        for (rowIndex in data.rows.indices) {
            // Check page overflow
            if (currentY + rowHeight > pageHeight - margin - 40f) {
                // Draw footer page number
                cDrawPageFooter(canvas, pageNumber, margin, contentWidth, pageHeight, subPaint)
                doc.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas
                currentY = drawPageHeader(canvas)
            }

            // Alternating row background
            if (rowIndex % 2 == 1) {
                bgPaint.color = Color.rgb(249, 250, 252)
                canvas.drawRect(margin, currentY - 2f, margin + contentWidth, currentY + rowHeight - 2f, bgPaint)
            }

            val row = data.rows[rowIndex]
            for (colIndex in row.indices) {
                if (colIndex >= colCount) break
                val cellText = row[colIndex]
                val isNum = data.numericColumns.contains(colIndex)
                val x = if (isNum) {
                    margin + (colIndex + 1) * colWidth - 4f
                } else {
                    margin + colIndex * colWidth + 4f
                }
                if (isNum) {
                    textPaint.textAlign = Paint.Align.RIGHT
                } else {
                    textPaint.textAlign = Paint.Align.LEFT
                }
                canvas.drawText(truncateCell(cellText, colWidth - 8f, textPaint), x, currentY + 11f, textPaint)
            }
            textPaint.textAlign = Paint.Align.LEFT

            canvas.drawLine(margin, currentY + rowHeight - 2f, margin + contentWidth, currentY + rowHeight - 2f, linePaint)
            currentY += rowHeight
        }

        // Draw Totals row if provided
        if (data.totals != null) {
            if (currentY + rowHeight > pageHeight - margin - 40f) {
                cDrawPageFooter(canvas, pageNumber, margin, contentWidth, pageHeight, subPaint)
                doc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = doc.startPage(pageInfo)
                canvas = page.canvas
                currentY = drawPageHeader(canvas)
            }

            bgPaint.color = Color.rgb(235, 240, 245)
            canvas.drawRect(margin, currentY - 2f, margin + contentWidth, currentY + rowHeight - 2f, bgPaint)

            for (colIndex in data.totals.indices) {
                if (colIndex >= colCount) break
                val cellText = data.totals[colIndex]
                val isNum = data.numericColumns.contains(colIndex)
                val x = if (isNum) {
                    margin + (colIndex + 1) * colWidth - 4f
                } else {
                    margin + colIndex * colWidth + 4f
                }
                if (isNum) {
                    headerPaint.textAlign = Paint.Align.RIGHT
                } else {
                    headerPaint.textAlign = Paint.Align.LEFT
                }
                canvas.drawText(truncateCell(cellText, colWidth - 8f, headerPaint), x, currentY + 11f, headerPaint)
            }
            headerPaint.textAlign = Paint.Align.LEFT

            canvas.drawLine(margin, currentY + rowHeight - 2f, margin + contentWidth, currentY + rowHeight - 2f, linePaint)
            currentY += rowHeight
        }

        // Optional Notes
        if (!data.notes.isNullOrBlank()) {
            currentY += 8f
            subPaint.color = Color.DKGRAY
            canvas.drawText(data.notes, margin, currentY + 10f, subPaint)
            subPaint.color = Color.GRAY
        }

        cDrawPageFooter(canvas, pageNumber, margin, contentWidth, pageHeight, subPaint)
        doc.finishPage(page)

        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()

        return file
    }

    private fun cDrawPageFooter(canvas: Canvas, pageNumber: Int, margin: Float, contentWidth: Float, pageHeight: Int, paint: Paint) {
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Page $pageNumber", margin + contentWidth, pageHeight - margin + 10f, paint)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("StoreOS Dokan • Secure Offline-First POS", margin, pageHeight - margin + 10f, paint)
    }

    private fun truncateCell(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.length > 1 && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.substring(0, truncated.length - 1)
        }
        return "$truncated..."
    }

    /**
     * Generates RFC 4180 compliant CSV file.
     * CRITICAL: Prepends UTF-8 Byte Order Mark (\uFEFF) so Excel on Windows, Mac, and Android
     * immediately opens Bangla Unicode text without broken characters (mojibake).
     * Real numeric cells are written as unquoted numbers so formulas work.
     */
    fun generateCsvReport(context: Context, data: ReportTableData, fileName: String): File {
        val file = File(getReportsDir(context), if (fileName.endsWith(".csv")) fileName else "$fileName.csv")

        FileOutputStream(file).use { fos ->
            // 1. Write UTF-8 BOM
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            val writer = fos.bufferedWriter(StandardCharsets.UTF_8)

            // Shop Header
            writer.write(csvEscape(data.storeName.ifBlank { "StoreOS Dokan" }))
            writer.newLine()
            if (data.storePhone.isNotBlank() || data.storeAddress.isNotBlank()) {
                writer.write(csvEscape("${data.storePhone} ${data.storeAddress}".trim()))
                writer.newLine()
            }
            writer.write(csvEscape(data.title))
            writer.newLine()
            writer.write(csvEscape("Period: ${data.periodLabel}"))
            writer.write(",")
            writer.write(csvEscape("Generated At: ${data.generatedAt}"))
            writer.newLine()
            writer.newLine()

            // Headers
            writer.write(data.headers.joinToString(",") { csvEscape(it) })
            writer.newLine()

            // Rows
            for (rowIndex in data.rows.indices) {
                val row = data.rows[rowIndex]
                val rawRowNumbers = data.rawNumbers.getOrNull(rowIndex)
                val lineParts = mutableListOf<String>()

                for (colIndex in row.indices) {
                    val rawVal = rawRowNumbers?.getOrNull(colIndex)
                    if (data.numericColumns.contains(colIndex)) {
                        if (rawVal != null) {
                            // Unquoted numeric literal for Excel formulas
                            lineParts.add(if (rawVal % 1.0 == 0.0) rawVal.toLong().toString() else String.format(java.util.Locale.US, "%.2f", rawVal))
                        } else {
                            // Strip currency symbols and commas to ensure numeric cell
                            val sanitized = row[colIndex].replace("৳", "").replace(",", "").trim()
                            val parsed = sanitized.toDoubleOrNull()
                            if (parsed != null) {
                                lineParts.add(if (parsed % 1.0 == 0.0) parsed.toLong().toString() else String.format(java.util.Locale.US, "%.2f", parsed))
                            } else {
                                lineParts.add(csvEscape(row[colIndex]))
                            }
                        }
                    } else {
                        lineParts.add(csvEscape(row[colIndex]))
                    }
                }
                writer.write(lineParts.joinToString(","))
                writer.newLine()
            }

            // Totals row
            if (data.totals != null) {
                val totalParts = mutableListOf<String>()
                for (colIndex in data.totals.indices) {
                    val rawVal = data.rawTotals.getOrNull(colIndex)
                    if (data.numericColumns.contains(colIndex)) {
                        if (rawVal != null) {
                            totalParts.add(if (rawVal % 1.0 == 0.0) rawVal.toLong().toString() else String.format(java.util.Locale.US, "%.2f", rawVal))
                        } else {
                            val sanitized = data.totals[colIndex].replace("৳", "").replace(",", "").trim()
                            val parsed = sanitized.toDoubleOrNull()
                            if (parsed != null) {
                                totalParts.add(if (parsed % 1.0 == 0.0) parsed.toLong().toString() else String.format(java.util.Locale.US, "%.2f", parsed))
                            } else {
                                totalParts.add(csvEscape(data.totals[colIndex]))
                            }
                        }
                    } else {
                        totalParts.add(csvEscape(data.totals[colIndex]))
                    }
                }
                writer.write(totalParts.joinToString(","))
                writer.newLine()
            }

            if (!data.notes.isNullOrBlank()) {
                writer.newLine()
                writer.write(csvEscape("Note: ${data.notes}"))
                writer.newLine()
            }

            writer.flush()
        }

        return file
    }

    private fun csvEscape(value: String): String {
        val trimmed = value.trim()
        val mustQuote = trimmed.contains(",") || trimmed.contains("\"") || trimmed.contains("\n") || trimmed.contains("\r")
        return if (mustQuote) {
            "\"${trimmed.replace("\"", "\"\"")}\""
        } else {
            trimmed
        }
    }

    /**
     * Native share sheet integration using FileProvider.
     */
    fun shareReportFile(context: Context, file: File, mimeType: String, subject: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Native print integration using Android PrintManager.
     * Streams the generated PDF directly to the system Print Spooler.
     */
    fun printReportFile(context: Context, file: File, reportName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Printing not supported on this device", Toast.LENGTH_SHORT).show()
            shareReportFile(context, file, "application/pdf", reportName)
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("$reportName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()

                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                if (destination == null) {
                    callback?.onWriteFailed("Missing destination file descriptor")
                    return
                }

                try {
                    FileInputStream(file).use { input ->
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            val buf = ByteArray(8192)
                            var len: Int
                            while (input.read(buf).also { len = it } > 0) {
                                if (cancellationSignal?.isCanceled == true) {
                                    callback?.onWriteCancelled()
                                    return
                                }
                                output.write(buf, 0, len)
                            }
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(reportName, printAdapter, PrintAttributes.Builder().build())
    }

    /**
     * Download notification / confirmation to user.
     */
    fun downloadReportFile(context: Context, file: File) {
        Toast.makeText(context, "Saved to ${file.name}", Toast.LENGTH_LONG).show()
    }
}
