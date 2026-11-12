package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.CategorySalesStat
import com.example.domain.DailySalesPoint
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen

/**
 * 4 Export buttons that stay strictly on ONE line:
 * [ Download PDF ] [ Print ] [ Export Excel/CSV ] [ Share ]
 */
@Composable
fun ExportActionBar(
    onDownloadPdf: () -> Unit,
    onPrint: () -> Unit,
    onExportCsv: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExportActionButton(
            label = "Download PDF",
            icon = Icons.Default.Download,
            testTag = "btn_download_pdf",
            onClick = onDownloadPdf
        )

        ExportActionButton(
            label = "Print",
            icon = Icons.Default.Print,
            testTag = "btn_print",
            onClick = onPrint
        )

        ExportActionButton(
            label = "Export Excel/CSV",
            icon = Icons.Default.TableChart,
            testTag = "btn_export_csv",
            onClick = onExportCsv
        )

        ExportActionButton(
            label = "Share",
            icon = Icons.Default.Share,
            testTag = "btn_share",
            onClick = onShare
        )
    }
}

@Composable
private fun ExportActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = Color(0x1FFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VividGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

/**
 * Plain empty state message with no fake data.
 */
@Composable
fun PlainEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Sales Bar Chart: Interactive, clean Canvas rendering of daily or monthly sales.
 */
@Composable
fun SalesBarChart(
    points: List<DailySalesPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        PlainEmptyState(message = "No sales recorded in this period")
        return
    }

    val maxSales = remember(points) { points.maxOfOrNull { it.salesAmount }?.coerceAtLeast(1.0) ?: 1.0 }

    Column(modifier = modifier.fillMaxWidth()) {
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            points.forEach { pt ->
                val ratio = (pt.salesAmount / maxSales).toFloat().coerceIn(0.04f, 1f)
                val barHeight = 120.dp * ratio

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(42.dp)
                ) {
                    // Sales amount above bar
                    Text(
                        text = if (pt.salesAmount >= 1000) "${(pt.salesAmount / 1000).toInt()}k" else pt.salesAmount.toInt().toString(),
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Bar
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (ratio >= 0.9f) VividGreen else Color(0x665DD62C))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Date label below bar
                    Text(
                        text = pt.label,
                        color = TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Donut chart with colors and legends for category sales.
 */
@Composable
fun CategoryDonutChart(
    stats: List<CategorySalesStat>,
    modifier: Modifier = Modifier
) {
    if (stats.isEmpty()) {
        PlainEmptyState(message = "No category sales recorded in this period")
        return
    }

    val palette = remember {
        listOf(
            Color(0xFF5DD62C), // VividGreen
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFFB300), // Amber
            Color(0xFFFF5252), // Coral
            Color(0xFFB388FF), // Purple
            Color(0xFF00B0FF), // Light Blue
            Color(0xFFFF80AB)  // Pink
        )
    }

    val totalSales = remember(stats) { stats.sumOf { it.salesAmount } }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Canvas Donut
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                var startAngle = -90f
                val stroke = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Butt)

                for (i in stats.indices) {
                    val stat = stats[i]
                    val sweep = if (totalSales > 0) ((stat.salesAmount / totalSales) * 360f).toFloat() else 0f
                    val col = palette[i % palette.size]

                    if (sweep > 0.5f) {
                        drawArc(
                            color = col,
                            startAngle = startAngle,
                            sweepAngle = sweep - 1.5f,
                            useCenter = false,
                            style = stroke,
                            topLeft = Offset(14.dp.toPx(), 14.dp.toPx()),
                            size = Size(size.width - 28.dp.toPx(), size.height - 28.dp.toPx())
                        )
                    }
                    startAngle += sweep
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "৳${totalSales.toInt()}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            stats.take(5).forEachIndexed { idx, stat ->
                val col = palette[idx % palette.size]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(col)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stat.category,
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "৳${stat.salesAmount.toInt()} (${stat.percentage.toInt()}%)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
