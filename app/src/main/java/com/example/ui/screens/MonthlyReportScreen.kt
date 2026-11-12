package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.ExportActionBar
import com.example.ui.components.GlassCard
import com.example.ui.components.PlainEmptyState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ReportExportUtil
import com.example.util.ReportTableData
import com.example.util.ShopLocale
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Step 5: Monthly Report Screen with Month Picker and Bangla/English PDF/CSV export dialog.
 */
@Composable
fun MonthlyReportScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val year by storeViewModel.monthlyReportYear.collectAsState()
    val month by storeViewModel.monthlyReportMonth.collectAsState()
    val reportData by storeViewModel.monthlyReportData.collectAsState()
    val store by storeViewModel.store.collectAsState()

    var showExportLanguageModal by remember { mutableStateOf(false) }
    var selectedExportFormat by remember { mutableStateOf("PDF") } // "PDF" or "CSV"

    val monthNames = remember { DateFormatSymbols(Locale.US).months }
    val currentMonthLabel = "${monthNames[month]} $year"
    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    fun buildMonthlyReportTableData(lang: AppLanguage): ReportTableData {
        val isBn = lang == AppLanguage.BN
        val headers = if (isBn) {
            listOf("পণ্য", "বিক্রিত পরিমাণ", "মোট বিক্রয়", "লাভ")
        } else {
            listOf("Product", "Quantity Sold", "Total Amount", "Profit")
        }

        val rows = reportData.itemsSold.map { stat ->
            listOf(
                stat.productName,
                "${stat.quantitySold} ${stat.unit}",
                ShopLocale.formatMoney(stat.totalSalesAmount, lang),
                ShopLocale.formatMoney(stat.totalProfit, lang)
            )
        }
        val rawRows = reportData.itemsSold.map { stat ->
            listOf(null, stat.quantitySold.toDouble(), stat.totalSalesAmount, stat.totalProfit)
        }

        val totals = if (isBn) {
            listOf(
                "সর্বমোট (${reportData.totalOrders} অর্ডার)",
                "${reportData.totalItemsSold} আইটেম",
                ShopLocale.formatMoney(reportData.totalSales, lang),
                ShopLocale.formatMoney(reportData.totalProfit, lang)
            )
        } else {
            listOf(
                "TOTAL (${reportData.totalOrders} Orders)",
                "${reportData.totalItemsSold} items",
                ShopLocale.formatMoney(reportData.totalSales, lang),
                ShopLocale.formatMoney(reportData.totalProfit, lang)
            )
        }
        val rawTotals = listOf(null, reportData.totalItemsSold.toDouble(), reportData.totalSales, reportData.totalProfit)

        val title = if (isBn) "মাসিক পারফরম্যান্স রিপোর্ট: $currentMonthLabel" else "Monthly Performance Report: $currentMonthLabel"
        val notes = if (isBn) {
            "মজুদ পণ্যের মূল্য: ৳${reportData.stockValueAtCost.toInt()} | পাওনা বাকি: ৳${reportData.bakiCustomersOwe.toInt()} | মহাজন দেনা: ৳${reportData.debtToSuppliers.toInt()}"
        } else {
            "Stock Value: ৳${reportData.stockValueAtCost.toInt()} | Total Baki: ৳${reportData.bakiCustomersOwe.toInt()} | Supplier Debt: ৳${reportData.debtToSuppliers.toInt()}"
        }

        return ReportTableData(
            title = title,
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = currentMonthLabel,
            generatedAt = nowFormatted,
            headers = headers,
            rows = rows,
            numericColumns = setOf(1, 2, 3),
            rawNumbers = rawRows,
            totals = totals,
            rawTotals = rawTotals,
            notes = notes
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { storeViewModel.navigateBackInBusiness() },
                modifier = Modifier.testTag("monthly_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Monthly Report",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Month Picker Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = GlassSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        val newMonth = if (month == 0) 11 else month - 1
                        val newYear = if (month == 0) year - 1 else year
                        storeViewModel.setMonthlyReportMonth(newYear, newMonth)
                    },
                    modifier = Modifier.testTag("btn_prev_month")
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = TextPrimary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = VividGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentMonthLabel,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        val newMonth = if (month == 11) 0 else month + 1
                        val newYear = if (month == 11) year + 1 else year
                        storeViewModel.setMonthlyReportMonth(newYear, newMonth)
                    },
                    modifier = Modifier.testTag("btn_next_month")
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Export Bar with Language Switcher Modal trigger
        ExportActionBar(
            onDownloadPdf = {
                selectedExportFormat = "PDF"
                showExportLanguageModal = true
            },
            onPrint = {
                val data = buildMonthlyReportTableData(currentLanguage)
                val file = ReportExportUtil.generatePdfReport(context, data, "MonthlyReport_${year}_${month + 1}")
                ReportExportUtil.printReportFile(context, file, "MonthlyReport")
            },
            onExportCsv = {
                selectedExportFormat = "CSV"
                showExportLanguageModal = true
            },
            onShare = {
                val data = buildMonthlyReportTableData(currentLanguage)
                val file = ReportExportUtil.generatePdfReport(context, data, "MonthlyReport_${year}_${month + 1}")
                ReportExportUtil.shareReportFile(context, file, "application/pdf", "Monthly Report: $currentMonthLabel")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 1. Month Summary (Sales, Profit, Orders, Items)
            item {
                Text(
                    text = "Month Summary",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryPill(
                        title = "Total Sales",
                        value = ShopLocale.formatMoney(reportData.totalSales, currentLanguage),
                        color = VividGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryPill(
                        title = "Total Profit",
                        value = ShopLocale.formatMoney(reportData.totalProfit, currentLanguage),
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryPill(
                        title = "Orders Completed",
                        value = "${reportData.totalOrders}",
                        color = Color(0xFFFFB300),
                        modifier = Modifier.weight(0.9f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Current Money Now Status
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Current Money Now",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Stock at Cost", color = TextSecondary, fontSize = 11.sp)
                                Text(text = ShopLocale.formatMoney(reportData.stockValueAtCost, currentLanguage), color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Total Customer Baki", color = TextSecondary, fontSize = 11.sp)
                                Text(text = ShopLocale.formatMoney(reportData.bakiCustomersOwe, currentLanguage), color = Color(0xFFFFB300), fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Total Owed to Suppliers", color = TextSecondary, fontSize = 11.sp)
                                Text(text = ShopLocale.formatMoney(reportData.debtToSuppliers, currentLanguage), color = Color(0xFFFF5252), fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Sales By Customer (Top buyers)
            item {
                Text(
                    text = "Sales by Customer (${reportData.customerStats.size})",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (reportData.customerStats.isEmpty()) {
                    PlainEmptyState(message = "No customer purchases in $currentMonthLabel")
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            reportData.customerStats.forEachIndexed { idx, cust ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.4f)) {
                                        Text(text = cust.customerName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "Paid: ৳${cust.paidAmount.toInt()} • Baki: ৳${cust.bakiAmount.toInt()}",
                                            color = if (cust.bakiAmount > 0) Color(0xFFFFB300) else TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = ShopLocale.formatMoney(cust.totalBought, currentLanguage),
                                        color = TextPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (idx < reportData.customerStats.size - 1) {
                                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Items Sold in the Month
            item {
                Text(
                    text = "Items Sold (${reportData.itemsSold.size})",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (reportData.itemsSold.isEmpty()) {
                    PlainEmptyState(message = "No items sold in $currentMonthLabel")
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            reportData.itemsSold.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.3f)) {
                                        Text(text = item.productName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "${item.quantitySold} ${item.unit} sold", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = ShopLocale.formatMoney(item.totalSalesAmount, currentLanguage), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "+${ShopLocale.formatMoney(item.totalProfit, currentLanguage)} profit", color = VividGreen, fontSize = 11.sp)
                                    }
                                }
                                if (idx < reportData.itemsSold.size - 1) {
                                    HorizontalDivider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to choose English or Bangla for export
    if (showExportLanguageModal) {
        Dialog(onDismissRequest = { showExportLanguageModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                var chosenLang by remember { mutableStateOf(AppLanguage.EN) }

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = VividGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Choose Report Language",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chosenLang = AppLanguage.EN }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = chosenLang == AppLanguage.EN,
                            onClick = { chosenLang = AppLanguage.EN },
                            colors = RadioButtonDefaults.colors(selectedColor = VividGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "English (Recommended for universal accounting)", color = TextPrimary, fontSize = 13.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chosenLang = AppLanguage.BN }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = chosenLang == AppLanguage.BN,
                            onClick = { chosenLang = AppLanguage.BN },
                            colors = RadioButtonDefaults.colors(selectedColor = VividGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "বাংলা (Bengali Unicode)", color = TextPrimary, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { showExportLanguageModal = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                showExportLanguageModal = false
                                val data = buildMonthlyReportTableData(chosenLang)
                                val fileName = "MonthlyReport_${year}_${month + 1}_${chosenLang.name}"
                                val file = if (selectedExportFormat == "PDF") {
                                    ReportExportUtil.generatePdfReport(context, data, fileName)
                                } else {
                                    ReportExportUtil.generateCsvReport(context, data, fileName)
                                }
                                ReportExportUtil.downloadReportFile(context, file)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VividGreen)
                        ) {
                            Text("Export $selectedExportFormat", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryPill(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, color = TextSecondary, fontSize = 10.5.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
