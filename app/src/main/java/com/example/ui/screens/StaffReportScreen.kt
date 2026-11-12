package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.StaffPerformanceRow
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
import com.example.ui.viewmodel.HistoryPeriod
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ReportExportUtil
import com.example.util.ReportTableData
import com.example.util.ShopLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffReportScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.historyPeriod.collectAsState()
    val customRange by storeViewModel.historyCustomDateRange.collectAsState()
    val staffSummary by storeViewModel.staffReport.collectAsState()
    val store by storeViewModel.store.collectAsState()
    val canSeeProfit = storeViewModel.canSeeBuyingPriceAndProfit()

    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    var showDatePicker by remember { mutableStateOf(false) }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e ->
                storeViewModel.setHistoryCustomDateRange(s, e)
                showDatePicker = false
            }
        )
    }

    fun buildReportData(): ReportTableData {
        val headers = if (canSeeProfit) {
            listOf("Staff Member", "Role", "Orders", "Items Sold", "Sales Total", "Profit", "Payments In")
        } else {
            listOf("Staff Member", "Role", "Orders", "Items Sold", "Sales Total", "Payments In")
        }

        val rows = staffSummary.rows.map { row ->
            if (canSeeProfit) {
                listOf(
                    row.memberName,
                    row.roleLabel,
                    row.salesCount.toString(),
                    row.itemsSold.toString(),
                    ShopLocale.formatMoney(row.totalSales, currentLanguage),
                    ShopLocale.formatMoney(row.profit, currentLanguage),
                    ShopLocale.formatMoney(row.paymentsReceived, currentLanguage)
                )
            } else {
                listOf(
                    row.memberName,
                    row.roleLabel,
                    row.salesCount.toString(),
                    row.itemsSold.toString(),
                    ShopLocale.formatMoney(row.totalSales, currentLanguage),
                    ShopLocale.formatMoney(row.paymentsReceived, currentLanguage)
                )
            }
        }

        val rawRows = staffSummary.rows.map { row ->
            if (canSeeProfit) {
                listOf(null, null, row.salesCount, row.itemsSold, row.totalSales, row.profit, row.paymentsReceived)
            } else {
                listOf(null, null, row.salesCount, row.itemsSold, row.totalSales, row.paymentsReceived)
            }
        }

        val totals = if (canSeeProfit) {
            listOf(
                "TOTAL (${staffSummary.rows.size} staff)",
                "",
                staffSummary.totalSalesCount.toString(),
                staffSummary.totalItemsSold.toString(),
                ShopLocale.formatMoney(staffSummary.totalSalesAmount, currentLanguage),
                ShopLocale.formatMoney(staffSummary.totalProfit, currentLanguage),
                ShopLocale.formatMoney(staffSummary.totalPaymentsReceived, currentLanguage)
            )
        } else {
            listOf(
                "TOTAL (${staffSummary.rows.size} staff)",
                "",
                staffSummary.totalSalesCount.toString(),
                staffSummary.totalItemsSold.toString(),
                ShopLocale.formatMoney(staffSummary.totalSalesAmount, currentLanguage),
                ShopLocale.formatMoney(staffSummary.totalPaymentsReceived, currentLanguage)
            )
        }

        val rawTotals = if (canSeeProfit) {
            listOf(null, null, staffSummary.totalSalesCount, staffSummary.totalItemsSold, staffSummary.totalSalesAmount, staffSummary.totalProfit, staffSummary.totalPaymentsReceived)
        } else {
            listOf(null, null, staffSummary.totalSalesCount, staffSummary.totalItemsSold, staffSummary.totalSalesAmount, staffSummary.totalPaymentsReceived)
        }

        return ReportTableData(
            title = "Staff Performance Report",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = period.labelEn,
            generatedAt = nowFormatted,
            headers = headers,
            rows = rows,
            numericColumns = if (canSeeProfit) setOf(2, 3, 4, 5, 6) else setOf(2, 3, 4, 5),
            rawNumbers = rawRows,
            totals = totals,
            rawTotals = rawTotals
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ReportTopBar(
            title = if (currentLanguage == AppLanguage.BN) "স্টাফ পারফর্মেন্স রিপোর্ট" else "Staff Report",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = {
                if (it == HistoryPeriod.CUSTOM) showDatePicker = true
                else storeViewModel.setHistoryPeriod(it)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4 Export buttons strictly on ONE line
        ExportActionBar(
            onDownloadPdf = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "StaffReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
                Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_SHORT).show()
            },
            onPrint = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "StaffReport_${System.currentTimeMillis()}")
                ReportExportUtil.printReportFile(context, file, "StaffReport")
            },
            onExportCsv = {
                val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "StaffReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
                Toast.makeText(context, "CSV saved to Downloads", Toast.LENGTH_SHORT).show()
            },
            onShare = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "StaffReport_${System.currentTimeMillis()}")
                ReportExportUtil.shareReportFile(context, file)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("staff_report_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // KPI Overview Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiStatCard(
                        title = if (currentLanguage == AppLanguage.BN) "মোট বিক্রয়" else "Total Sales",
                        value = ShopLocale.formatMoney(staffSummary.totalSalesAmount, currentLanguage),
                        icon = Icons.Default.TrendingUp,
                        iconTint = VividGreen,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = if (currentLanguage == AppLanguage.BN) "অর্ডার" else "Orders",
                        value = staffSummary.totalSalesCount.toString(),
                        icon = Icons.Default.Receipt,
                        iconTint = Color(0xFFFFB300),
                        modifier = Modifier.weight(1f)
                    )
                    if (canSeeProfit) {
                        KpiStatCard(
                            title = if (currentLanguage == AppLanguage.BN) "মোট লাভ" else "Profit",
                            value = ShopLocale.formatMoney(staffSummary.totalProfit, currentLanguage),
                            icon = Icons.Default.ShoppingCart,
                            iconTint = Color(0xFF00E5FF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (staffSummary.rows.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    PlainEmptyState(
                        title = if (currentLanguage == AppLanguage.BN) "কোন স্টাফ কার্যক্রম নেই" else "No Staff Activity",
                        subtitle = if (currentLanguage == AppLanguage.BN) "এই সময়ের মধ্যে কোনো বিক্রয় বা কালেকশন রেকর্ড নেই" else "No sales or collections recorded by staff in this period"
                    )
                }
            } else {
                // Table Title Header
                item {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "স্টাফ পারফর্মেন্স তালিকা" else "Staff Performance Breakdown",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Table Horizontal Scroll View
                item {
                    val hScroll = rememberScrollState()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        color = GlassSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.horizontalScroll(hScroll)) {
                            // Header Row
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFF1E2638))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Staff Member", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
                                Text(text = "Role", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp))
                                Text(text = "Orders", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(60.dp))
                                Text(text = "Items Sold", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                                Text(text = "Sales Total", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(110.dp))
                                if (canSeeProfit) {
                                    Text(text = "Profit", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(100.dp))
                                }
                                Text(text = "Payments In", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(110.dp))
                            }

                            HorizontalDivider(color = GlassBorder, thickness = 1.dp)

                            // Rows
                            staffSummary.rows.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = row.memberName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(130.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x225DD62C))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = row.roleLabel,
                                            color = VividGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(text = row.salesCount.toString(), color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.width(60.dp))
                                    Text(text = row.itemsSold.toString(), color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                                    Text(text = ShopLocale.formatMoney(row.totalSales, currentLanguage), color = VividGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(110.dp))
                                    if (canSeeProfit) {
                                        Text(text = ShopLocale.formatMoney(row.profit, currentLanguage), color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.width(100.dp))
                                    }
                                    Text(text = ShopLocale.formatMoney(row.paymentsReceived, currentLanguage), color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.End, modifier = Modifier.width(110.dp))
                                }
                                HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)
                            }

                            // Totals Row
                            Row(
                                modifier = Modifier
                                    .background(Color(0x2600E5FF))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "TOTAL", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(130.dp))
                                Text(text = "${staffSummary.rows.size} staff", color = TextMuted, fontSize = 11.sp, modifier = Modifier.width(90.dp))
                                Text(text = staffSummary.totalSalesCount.toString(), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(60.dp))
                                Text(text = staffSummary.totalItemsSold.toString(), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(80.dp))
                                Text(text = ShopLocale.formatMoney(staffSummary.totalSalesAmount, currentLanguage), color = VividGreen, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, modifier = Modifier.width(110.dp))
                                if (canSeeProfit) {
                                    Text(text = ShopLocale.formatMoney(staffSummary.totalProfit, currentLanguage), color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.End, modifier = Modifier.width(100.dp))
                                }
                                Text(text = ShopLocale.formatMoney(staffSummary.totalPaymentsReceived, currentLanguage), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.width(110.dp))
                            }
                        }
                    }
                }

                // Individual Card Breakdown
                items(staffSummary.rows) { row ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("staff_card_${row.memberId}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = VividGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = row.memberName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x225DD62C))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = row.roleLabel,
                                        color = VividGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Sales Recorded", color = TextMuted, fontSize = 11.sp)
                                    Text(text = ShopLocale.formatMoney(row.totalSales, currentLanguage), color = VividGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Orders / Items", color = TextMuted, fontSize = 11.sp)
                                    Text(text = "${row.salesCount} / ${row.itemsSold}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                if (canSeeProfit) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Profit Generated", color = TextMuted, fontSize = 11.sp)
                                        Text(text = ShopLocale.formatMoney(row.profit, currentLanguage), color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (row.paymentsReceived > 0.0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x14FFFFFF))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Payment, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Customer Payments Received", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Text(text = ShopLocale.formatMoney(row.paymentsReceived, currentLanguage), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
