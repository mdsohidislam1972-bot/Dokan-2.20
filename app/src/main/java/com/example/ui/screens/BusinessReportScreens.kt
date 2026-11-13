package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.Customer
import com.example.data.model.Supplier
import com.example.domain.CalculationEngine
import com.example.ui.components.ExportActionBar
import com.example.ui.components.GlassCard
import com.example.ui.components.PlainEmptyState
import com.example.ui.theme.AlertRed
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

// =================================================================
// 1. SALES REPORT
// =================================================================
@Composable
fun SalesReportScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val sales by storeViewModel.businessFilteredSales.collectAsState()
    val store by storeViewModel.store.collectAsState()
    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    val totalAmount = remember(sales) { sales.sumOf { it.grandTotal } }
    val totalDiscount = remember(sales) { sales.sumOf { it.discount } }
    val totalProfit = remember(sales) { sales.sumOf { it.profit } }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e ->
                storeViewModel.setBusinessCustomDateRange(s, e)
                showDatePicker = false
            }
        )
    }

    fun buildReportData(): ReportTableData {
        val rows = sales.map { s ->
            val dateStr = s.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "—"
            val status = if (s.paymentType == "baki") "Baki" else "Paid"
            if (canSeeProfit) {
                listOf(
                    s.invoiceNo,
                    dateStr,
                    s.customerName,
                    ShopLocale.formatMoney(s.grandTotal, currentLanguage),
                    ShopLocale.formatMoney(s.discount, currentLanguage),
                    ShopLocale.formatMoney(s.profit, currentLanguage),
                    status
                )
            } else {
                listOf(
                    s.invoiceNo,
                    dateStr,
                    s.customerName,
                    ShopLocale.formatMoney(s.grandTotal, currentLanguage),
                    ShopLocale.formatMoney(s.discount, currentLanguage),
                    status
                )
            }
        }
        val rawRows = sales.map { s ->
            if (canSeeProfit) {
                listOf(null, null, null, s.grandTotal, s.discount, s.profit, null)
            } else {
                listOf(null, null, null, s.grandTotal, s.discount, null)
            }
        }
        val totals = if (canSeeProfit) {
            listOf(
                "TOTAL (${sales.size} orders)",
                "",
                "",
                ShopLocale.formatMoney(totalAmount, currentLanguage),
                ShopLocale.formatMoney(totalDiscount, currentLanguage),
                ShopLocale.formatMoney(totalProfit, currentLanguage),
                ""
            )
        } else {
            listOf(
                "TOTAL (${sales.size} orders)",
                "",
                "",
                ShopLocale.formatMoney(totalAmount, currentLanguage),
                ShopLocale.formatMoney(totalDiscount, currentLanguage),
                ""
            )
        }
        val rawTotals = if (canSeeProfit) {
            listOf(null, null, null, totalAmount, totalDiscount, totalProfit, null)
        } else {
            listOf(null, null, null, totalAmount, totalDiscount, null)
        }

        return ReportTableData(
            title = "Sales Report",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = period.labelEn,
            generatedAt = nowFormatted,
            headers = if (canSeeProfit) {
                listOf("Invoice", "Date", "Customer", "Total", "Discount", "Profit", "Status")
            } else {
                listOf("Invoice", "Date", "Customer", "Total", "Discount", "Status")
            },
            rows = rows,
            numericColumns = if (canSeeProfit) setOf(3, 4, 5) else setOf(3, 4),
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
            title = "Sales Report",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = {
                if (it == HistoryPeriod.CUSTOM) showDatePicker = true
                else storeViewModel.setBusinessPeriod(it)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Step 4: 4 Export buttons strictly on ONE line
        ExportActionBar(
            onDownloadPdf = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "SalesReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onPrint = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "SalesReport_${System.currentTimeMillis()}")
                ReportExportUtil.printReportFile(context, file, "SalesReport")
            },
            onExportCsv = {
                val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "SalesReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onShare = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "SalesReport_${System.currentTimeMillis()}")
                ReportExportUtil.shareReportFile(context, file, "application/pdf", "Sales Report - ${store?.name}")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (sales.isEmpty()) {
            PlainEmptyState(message = "No sales found for the chosen period")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    ReportTableHeaderRow(
                        headers = if (canSeeProfit) {
                            listOf("Invoice / Date", "Customer", "Total", "Profit")
                        } else {
                            listOf("Invoice / Date", "Customer", "Total")
                        }
                    )
                }

                items(sales) { s ->
                    val dateStr = s.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
                    val isBaki = s.paymentType == "baki"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = s.invoiceNo,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateStr,
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = s.customerName,
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isBaki) "Baki (Credit)" else "Cash Paid",
                                    color = if (isBaki) Color(0xFFFFB300) else VividGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = ShopLocale.formatMoney(s.grandTotal, currentLanguage),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            if (canSeeProfit) {
                                Text(
                                    text = ShopLocale.formatMoney(s.profit, currentLanguage),
                                    color = VividGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportTableTotalsBar(
                        label = "TOTAL (${sales.size} Orders)",
                        subValue = "Discounts: ${ShopLocale.formatMoney(totalDiscount, currentLanguage)}",
                        mainValue = ShopLocale.formatMoney(totalAmount, currentLanguage),
                        profitValue = if (canSeeProfit) "Profit: ${ShopLocale.formatMoney(totalProfit, currentLanguage)}" else ""
                    )
                }
            }
        }
    }
}

// =================================================================
// 2. PURCHASE REPORT
// =================================================================
@Composable
fun PurchaseReportScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val purchases by storeViewModel.businessFilteredPurchases.collectAsState()
    val returns by storeViewModel.businessFilteredReturns.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    val grossPurchases = remember(purchases) { purchases.sumOf { it.totalAmount } }
    val totalReturns = remember(returns) { returns.sumOf { it.amount } }
    val netPurchases = remember(grossPurchases, totalReturns) { (grossPurchases - totalReturns).coerceAtLeast(0.0) }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e ->
                storeViewModel.setBusinessCustomDateRange(s, e)
                showDatePicker = false
            }
        )
    }

    fun buildReportData(): ReportTableData {
        val rows = purchases.map { p ->
            val dateStr = p.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "—"
            val status = if (p.paymentType == "credit") "Credit (Due)" else "Paid"
            listOf(
                p.purchaseNo,
                dateStr,
                p.supplierName,
                ShopLocale.formatMoney(p.totalAmount, currentLanguage),
                ShopLocale.formatMoney(p.paidAmount, currentLanguage),
                ShopLocale.formatMoney(p.dueAmount, currentLanguage),
                status
            )
        }
        val rawRows = purchases.map { p ->
            listOf(null, null, null, p.totalAmount, p.paidAmount, p.dueAmount, null)
        }
        val totals = listOf(
            "NET PURCHASES (Minus ৳${totalReturns.toInt()} returns)",
            "",
            "",
            ShopLocale.formatMoney(netPurchases, currentLanguage),
            "",
            "",
            ""
        )
        return ReportTableData(
            title = "Purchase Report",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = period.labelEn,
            generatedAt = nowFormatted,
            headers = listOf("Order #", "Date", "Supplier", "Total", "Paid", "Due", "Status"),
            rows = rows,
            numericColumns = setOf(3, 4, 5),
            rawNumbers = rawRows,
            totals = totals,
            notes = if (totalReturns > 0) "Returns deducted: ৳${"%.2f".format(totalReturns)}" else null
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ReportTopBar(
            title = "Purchase Report",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = {
                if (it == HistoryPeriod.CUSTOM) showDatePicker = true
                else storeViewModel.setBusinessPeriod(it)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ExportActionBar(
            onDownloadPdf = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "PurchaseReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onPrint = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "PurchaseReport_${System.currentTimeMillis()}")
                ReportExportUtil.printReportFile(context, file, "PurchaseReport")
            },
            onExportCsv = {
                val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "PurchaseReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onShare = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "PurchaseReport_${System.currentTimeMillis()}")
                ReportExportUtil.shareReportFile(context, file, "application/pdf", "Purchase Report - ${store?.name}")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (purchases.isEmpty()) {
            PlainEmptyState(message = "No purchases recorded in this period")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    ReportTableHeaderRow(
                        headers = listOf("Order # / Date", "Supplier", "Paid", "Total")
                    )
                }

                items(purchases) { p ->
                    val dateStr = p.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "—"
                    val isCredit = p.paymentType == "credit"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = p.purchaseNo,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateStr,
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = p.supplierName,
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isCredit) "Credit (Due: ${p.dueAmount.toInt()})" else "Paid in Full",
                                    color = if (isCredit) Color(0xFFFF5252) else VividGreen,
                                    fontSize = 10.5.sp
                                )
                            }
                            Text(
                                text = ShopLocale.formatMoney(p.paidAmount, currentLanguage),
                                color = TextSecondary,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = ShopLocale.formatMoney(p.totalAmount, currentLanguage),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportTableTotalsBar(
                        label = "NET PURCHASES",
                        subValue = "Returns deducted: ৳${totalReturns.toInt()}",
                        mainValue = ShopLocale.formatMoney(netPurchases, currentLanguage),
                        profitValue = "Gross: ${ShopLocale.formatMoney(grossPurchases, currentLanguage)}"
                    )
                }
            }
        }
    }
}

// =================================================================
// 3. DAY BOOK
// =================================================================
@Composable
fun DayBookScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val entries by storeViewModel.dayBookEntries.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    val totalIn = remember(entries) { entries.filter { it.isIncome }.sumOf { it.amount } }
    val totalOut = remember(entries) { entries.filter { !it.isIncome }.sumOf { it.amount } }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e ->
                storeViewModel.setBusinessCustomDateRange(s, e)
                showDatePicker = false
            }
        )
    }

    fun buildReportData(): ReportTableData {
        val rows = entries.map { e ->
            val dateStr = dateFormat.format(Date(e.timestamp))
            val direction = if (e.isIncome) "IN (+)" else "OUT (-)"
            listOf(
                dateStr,
                e.type.replace('_', ' '),
                e.referenceNo,
                e.partyName,
                direction,
                ShopLocale.formatMoney(e.amount, currentLanguage)
            )
        }
        val rawRows = entries.map { e ->
            listOf(null, null, null, null, null, if (e.isIncome) e.amount else -e.amount)
        }
        val totals = listOf(
            "NET FLOW",
            "",
            "",
            "Total In: ৳${totalIn.toInt()}  |  Total Out: ৳${totalOut.toInt()}",
            "",
            ShopLocale.formatMoney(totalIn - totalOut, currentLanguage)
        )
        return ReportTableData(
            title = "Day Book",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = period.labelEn,
            generatedAt = nowFormatted,
            headers = listOf("Date/Time", "Type", "Ref #", "Party", "Flow", "Amount"),
            rows = rows,
            numericColumns = setOf(5),
            rawNumbers = rawRows,
            totals = totals
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ReportTopBar(
            title = "Day Book",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = {
                if (it == HistoryPeriod.CUSTOM) showDatePicker = true
                else storeViewModel.setBusinessPeriod(it)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ExportActionBar(
            onDownloadPdf = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "DayBook_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onPrint = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "DayBook_${System.currentTimeMillis()}")
                ReportExportUtil.printReportFile(context, file, "DayBook")
            },
            onExportCsv = {
                val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "DayBook_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onShare = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "DayBook_${System.currentTimeMillis()}")
                ReportExportUtil.shareReportFile(context, file, "application/pdf", "Day Book - ${store?.name}")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (entries.isEmpty()) {
            PlainEmptyState(message = "No transactions found in this period")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    ReportTableHeaderRow(
                        headers = listOf("Time / Type", "Party / Ref", "", "Amount")
                    )
                }

                items(entries) { e ->
                    val dateStr = dateFormat.format(Date(e.timestamp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = dateStr,
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatDayBookType(e.type),
                                    color = if (e.isIncome) VividGreen else Color(0xFFFF5252),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = e.partyName,
                                    color = TextPrimary,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${e.referenceNo}${if (e.note.isNotBlank()) " • ${e.note}" else ""}",
                                    color = TextMuted,
                                    fontSize = 10.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${if (e.isIncome) "+" else "-"}৳${e.amount.toInt()}",
                                color = if (e.isIncome) VividGreen else Color(0xFFFF5252),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportTableTotalsBar(
                        label = "NET CASH FLOW",
                        subValue = "In: ৳${totalIn.toInt()}  |  Out: ৳${totalOut.toInt()}",
                        mainValue = "${if (totalIn >= totalOut) "+" else "-"}৳${kotlin.math.abs(totalIn - totalOut).toInt()}",
                        profitValue = "${entries.size} transactions"
                    )
                }
            }
        }
    }
}

private fun formatDayBookType(type: String): String {
    return when (type) {
        "SALE_CASH" -> "Cash Sale"
        "SALE_BAKI" -> "Baki Sale"
        "PURCHASE_PAID" -> "Cash Purchase"
        "PURCHASE_CREDIT" -> "Credit Purchase"
        "PAYMENT_IN" -> "Customer Payment In"
        "PURCHASE_RETURN" -> "Purchase Return"
        else -> type
    }
}

// =================================================================
// 4. PROFIT AND LOSS
// =================================================================
@Composable
fun ProfitAndLossScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val isStockBased by storeViewModel.isPnlStockBased.collectAsState()
    val pnl by storeViewModel.pnlStatement.collectAsState()
    val store by storeViewModel.store.collectAsState()
    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()
    val canViewReports by storeViewModel.canViewReportsFlow.collectAsState()

    if (!canSeeProfit || !canViewReports) {
        com.example.ui.components.NoPermissionScreen(
            onBack = { storeViewModel.navigateBackInBusiness() },
            modifier = modifier
        )
        return
    }

    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e ->
                storeViewModel.setBusinessCustomDateRange(s, e)
                showDatePicker = false
            }
        )
    }

    fun buildReportData(): ReportTableData {
        val rows = mutableListOf(
            listOf("Net Sales Revenue", ShopLocale.formatMoney(pnl.netSales, currentLanguage)),
            listOf("Cost of Goods Sold (COGS) [${if (isStockBased) "Stock-Based" else "Purchases-Based"}]", "-${ShopLocale.formatMoney(pnl.costOfGoodsSold, currentLanguage)}"),
            listOf("Gross Profit", ShopLocale.formatMoney(pnl.grossProfit, currentLanguage)),
            listOf("Other Income", "+${ShopLocale.formatMoney(pnl.otherIncome, currentLanguage)}"),
            listOf("Operating Expenses", "-${ShopLocale.formatMoney(pnl.expenses, currentLanguage)}")
        )
        pnl.expensesByCategory.forEach { (cat, amt) ->
            rows.add(listOf("  • $cat", "-${ShopLocale.formatMoney(amt, currentLanguage)}"))
        }
        rows.add(listOf("Net Profit", ShopLocale.formatMoney(pnl.netProfit, currentLanguage)))

        val rawRows = mutableListOf(
            listOf<Double?>(null, pnl.netSales),
            listOf<Double?>(null, -pnl.costOfGoodsSold),
            listOf<Double?>(null, pnl.grossProfit),
            listOf<Double?>(null, pnl.otherIncome),
            listOf<Double?>(null, -pnl.expenses)
        )
        pnl.expensesByCategory.forEach { (_, amt) ->
            rawRows.add(listOf<Double?>(null, -amt))
        }
        rawRows.add(listOf<Double?>(null, pnl.netProfit))
        return ReportTableData(
            title = "Profit and Loss Statement",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = period.labelEn,
            generatedAt = nowFormatted,
            headers = listOf("Financial Line Item", "Amount"),
            rows = rows,
            numericColumns = setOf(1),
            rawNumbers = rawRows,
            totals = listOf("NET PROFIT", ShopLocale.formatMoney(pnl.netProfit, currentLanguage)),
            notes = if (isStockBased) {
                "Stock-based matches buying cost directly against items sold in period."
            } else {
                "Purchase-based counts all goods bought this period regardless of sales."
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ReportTopBar(
            title = "Profit & Loss",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = {
                if (it == HistoryPeriod.CUSTOM) showDatePicker = true
                else storeViewModel.setBusinessPeriod(it)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        ExportActionBar(
            onDownloadPdf = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "ProfitLoss_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onPrint = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "ProfitLoss_${System.currentTimeMillis()}")
                ReportExportUtil.printReportFile(context, file, "ProfitLoss")
            },
            onExportCsv = {
                val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "ProfitLoss_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onShare = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "ProfitLoss_${System.currentTimeMillis()}")
                ReportExportUtil.shareReportFile(context, file, "application/pdf", "Profit and Loss - ${store?.name}")
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
            // Toggle Stock-based with plain-words explanation
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Stock-based COGS",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isStockBased) {
                                        "Matches buying cost directly against items sold in period."
                                    } else {
                                        "Counts all goods bought this period regardless of sales."
                                    },
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                            Switch(
                                checked = isStockBased,
                                onCheckedChange = { storeViewModel.togglePnlStockBased() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VividGreen,
                                    checkedTrackColor = Color(0x335DD62C)
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // P&L Statement Line Items
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        PnlRowItem(
                            label = "Net Sales Revenue",
                            amount = ShopLocale.formatMoney(pnl.netSales, currentLanguage),
                            isPositive = true,
                            isBold = false
                        )
                        HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))

                        PnlRowItem(
                            label = if (isStockBased) "Cost of Goods Sold (Buying cost of sold items)" else "Cost of Purchases Made",
                            amount = "-${ShopLocale.formatMoney(pnl.costOfGoodsSold, currentLanguage)}",
                            isPositive = false,
                            isBold = false
                        )
                        HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))

                        PnlRowItem(
                            label = "Gross Profit",
                            amount = ShopLocale.formatMoney(pnl.grossProfit, currentLanguage),
                            isPositive = pnl.grossProfit >= 0,
                            isBold = true
                        )
                        HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))

                        if (pnl.otherIncome > 0.0) {
                            PnlRowItem(
                                label = "Other Income",
                                amount = "+${ShopLocale.formatMoney(pnl.otherIncome, currentLanguage)}",
                                isPositive = true,
                                isBold = false
                            )
                            HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))
                        }

                        PnlRowItem(
                            label = "Operating Expenses",
                            amount = "-${ShopLocale.formatMoney(pnl.expenses, currentLanguage)}",
                            isPositive = false,
                            isBold = false
                        )
                        if (pnl.expensesByCategory.isNotEmpty()) {
                            pnl.expensesByCategory.forEach { (categoryName, catAmt) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• $categoryName",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "-${ShopLocale.formatMoney(catAmt, currentLanguage)}",
                                        color = AlertRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))

                        PnlRowItem(
                            label = "Net Profit",
                            amount = ShopLocale.formatMoney(pnl.netProfit, currentLanguage),
                            isPositive = pnl.netProfit >= 0,
                            isBold = true,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PnlRowItem(
    label: String,
    amount: String,
    isPositive: Boolean,
    isBold: Boolean,
    subLabel: String? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = if (isBold) TextPrimary else TextSecondary,
                fontSize = fontSize,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            )
            if (!subLabel.isNullOrBlank()) {
                Text(
                    text = subLabel,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
        Text(
            text = amount,
            color = if (isBold && isPositive) VividGreen else if (!isPositive && isBold) Color(0xFFFF5252) else TextPrimary,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

// =================================================================
// 5. CUSTOMER STATEMENT
// =================================================================
@Composable
fun CustomerStatementScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val customers by storeViewModel.customers.collectAsState()
    val selectedCustomer by storeViewModel.selectedStatementCustomer.collectAsState()
    val report by storeViewModel.customerStatementReport.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    var showPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e ->
                storeViewModel.setBusinessCustomDateRange(s, e)
                showDatePicker = false
            }
        )
    }

    fun buildReportData(): ReportTableData {
        val rep = report ?: return ReportTableData("Customer Statement", store?.name ?: "", "", "", period.labelEn, nowFormatted, emptyList(), emptyList())
        val rows = rep.entries.map { e ->
            val dStr = dateFormat.format(Date(e.timestamp))
            val typeStr = if (e.type == com.example.domain.LedgerEntryType.BAKI_SALE) "Baki Sale" else "Payment In"
            listOf(
                dStr,
                typeStr,
                e.referenceNo,
                if (e.type == com.example.domain.LedgerEntryType.BAKI_SALE) "+৳${e.amount.toInt()}" else "-৳${e.amount.toInt()}",
                "৳${e.runningBalance.toInt()}"
            )
        }
        val totals = listOf(
            "CLOSING BALANCE",
            "",
            "",
            "Baki: ৳${rep.totalBakiSales.toInt()} | Paid: ৳${rep.totalPayments.toInt()}",
            "৳${rep.closingBalance.toInt()}"
        )
        return ReportTableData(
            title = "Customer Statement: ${rep.customer.name}",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = period.labelEn,
            generatedAt = nowFormatted,
            headers = listOf("Date", "Type", "Ref #", "Amount", "Balance"),
            rows = rows,
            numericColumns = setOf(3, 4),
            totals = totals,
            notes = "Opening Balance: ৳${rep.openingBalance.toInt()}"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ReportTopBar(
            title = "Customer Statement",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        // Customer Selector Card
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker = true }
                    .testTag("picker_select_customer")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Customer",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = selectedCustomer?.name ?: "Tap to choose a customer...",
                            color = if (selectedCustomer != null) TextPrimary else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (selectedCustomer == null) "Select" else "Change",
                        color = VividGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = {
                if (it == HistoryPeriod.CUSTOM) showDatePicker = true
                else storeViewModel.setBusinessPeriod(it)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedCustomer != null) {
            ExportActionBar(
                onDownloadPdf = {
                    val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "CustomerStatement_${selectedCustomer?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.downloadReportFile(context, file)
                },
                onPrint = {
                    val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "CustomerStatement_${selectedCustomer?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.printReportFile(context, file, "CustomerStatement")
                },
                onExportCsv = {
                    val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "CustomerStatement_${selectedCustomer?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.downloadReportFile(context, file)
                },
                onShare = {
                    val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "CustomerStatement_${selectedCustomer?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.shareReportFile(context, file, "application/pdf", "Statement for ${selectedCustomer?.name}")
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedCustomer == null) {
            PlainEmptyState(message = "Please choose a customer to generate their statement")
        } else if (report == null || report!!.entries.isEmpty()) {
            PlainEmptyState(message = "No transactions found for ${selectedCustomer!!.name} in this period.\nOpening Balance: ৳${(report?.openingBalance ?: 0.0).toInt()}")
        } else {
            val rep = report!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = Color(0x1A00E5FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Opening Balance", color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = ShopLocale.formatMoney(rep.openingBalance, currentLanguage), color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    ReportTableHeaderRow(
                        headers = listOf("Date / Type", "Ref #", "Amount", "Balance")
                    )
                }

                items(rep.entries) { e ->
                    val dStr = dateFormat.format(Date(e.timestamp))
                    val isSale = e.type == com.example.domain.LedgerEntryType.BAKI_SALE
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(text = dStr, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                Text(text = if (isSale) "Baki Sale" else "Payment In", color = if (isSale) Color(0xFFFFB300) else VividGreen, fontSize = 10.5.sp)
                            }
                            Text(text = e.referenceNo, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text(
                                text = "${if (isSale) "+" else "-"}৳${e.amount.toInt()}",
                                color = if (isSale) Color(0xFFFFB300) else VividGreen,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "৳${e.runningBalance.toInt()}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportTableTotalsBar(
                        label = "CLOSING BALANCE",
                        subValue = "Baki: ৳${rep.totalBakiSales.toInt()} | Paid: ৳${rep.totalPayments.toInt()}",
                        mainValue = ShopLocale.formatMoney(rep.closingBalance, currentLanguage),
                        profitValue = "Net Due"
                    )
                }
            }
        }
    }

    if (showPicker) {
        CustomerPickerModal(
            customers = customers,
            onDismiss = { showPicker = false },
            onSelect = {
                storeViewModel.selectStatementCustomer(it)
                showPicker = false
            }
        )
    }
}

// =================================================================
// 6. SUPPLIER STATEMENT
// =================================================================
@Composable
fun SupplierStatementScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val suppliers by storeViewModel.suppliers.collectAsState()
    val selectedSupplier by storeViewModel.selectedStatementSupplier.collectAsState()
    val report by storeViewModel.supplierStatementReport.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    var showPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { s, e ->
                storeViewModel.setBusinessCustomDateRange(s, e)
                showDatePicker = false
            }
        )
    }

    fun buildReportData(): ReportTableData {
        val rep = report ?: return ReportTableData("Supplier Statement", store?.name ?: "", "", "", period.labelEn, nowFormatted, emptyList(), emptyList())
        val rows = rep.entries.map { e ->
            val dStr = dateFormat.format(Date(e.timestamp))
            listOf(
                dStr,
                e.type.replace('_', ' '),
                e.referenceNo,
                ShopLocale.formatMoney(e.amount, currentLanguage),
                ShopLocale.formatMoney(e.runningBalance, currentLanguage)
            )
        }
        val totals = listOf(
            "CLOSING BALANCE",
            "",
            "",
            "Purchases: ৳${rep.totalPurchases.toInt()} | Returns: ৳${rep.totalReturns.toInt()}",
            ShopLocale.formatMoney(rep.closingBalance, currentLanguage)
        )
        return ReportTableData(
            title = "Supplier Statement: ${rep.supplier.name}",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = period.labelEn,
            generatedAt = nowFormatted,
            headers = listOf("Date", "Type", "Ref #", "Amount", "Balance"),
            rows = rows,
            numericColumns = setOf(3, 4),
            totals = totals,
            notes = "Opening Balance: ৳${rep.openingBalance.toInt()}"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ReportTopBar(
            title = "Supplier Statement",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        // Supplier Selector Card
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker = true }
                    .testTag("picker_select_supplier")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Supplier",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = selectedSupplier?.name ?: "Tap to choose a supplier...",
                            color = if (selectedSupplier != null) TextPrimary else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (selectedSupplier == null) "Select" else "Change",
                        color = VividGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = {
                if (it == HistoryPeriod.CUSTOM) showDatePicker = true
                else storeViewModel.setBusinessPeriod(it)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedSupplier != null) {
            ExportActionBar(
                onDownloadPdf = {
                    val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "SupplierStatement_${selectedSupplier?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.downloadReportFile(context, file)
                },
                onPrint = {
                    val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "SupplierStatement_${selectedSupplier?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.printReportFile(context, file, "SupplierStatement")
                },
                onExportCsv = {
                    val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "SupplierStatement_${selectedSupplier?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.downloadReportFile(context, file)
                },
                onShare = {
                    val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "SupplierStatement_${selectedSupplier?.name}_${System.currentTimeMillis()}")
                    ReportExportUtil.shareReportFile(context, file, "application/pdf", "Statement for ${selectedSupplier?.name}")
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedSupplier == null) {
            PlainEmptyState(message = "Please choose a supplier to generate their statement")
        } else if (report == null || report!!.entries.isEmpty()) {
            PlainEmptyState(message = "No transactions found for ${selectedSupplier!!.name} in this period.\nOpening Balance: ৳${(report?.openingBalance ?: 0.0).toInt()}")
        } else {
            val rep = report!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = Color(0x1AFFA000),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Opening Balance", color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text(text = ShopLocale.formatMoney(rep.openingBalance, currentLanguage), color = Color(0xFFFFB300), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    ReportTableHeaderRow(
                        headers = listOf("Date / Type", "Ref #", "Amount", "Balance")
                    )
                }

                items(rep.entries) { e ->
                    val dStr = dateFormat.format(Date(e.timestamp))
                    val isReturn = e.type == "PURCHASE_RETURN"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(text = dStr, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
                                Text(text = e.type.replace('_', ' '), color = if (isReturn) VividGreen else Color(0xFFFF5252), fontSize = 10.5.sp)
                            }
                            Text(text = e.referenceNo, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text(
                                text = "${if (isReturn) "-" else "+"}৳${e.amount.toInt()}",
                                color = if (isReturn) VividGreen else Color(0xFFFF5252),
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "৳${e.runningBalance.toInt()}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportTableTotalsBar(
                        label = "CLOSING BALANCE",
                        subValue = "Purchases: ৳${rep.totalPurchases.toInt()} | Returns: ৳${rep.totalReturns.toInt()}",
                        mainValue = ShopLocale.formatMoney(rep.closingBalance, currentLanguage),
                        profitValue = "Total Owed"
                    )
                }
            }
        }
    }

    if (showPicker) {
        SupplierPickerModal(
            suppliers = suppliers,
            onDismiss = { showPicker = false },
            onSelect = {
                storeViewModel.selectStatementSupplier(it)
                showPicker = false
            }
        )
    }
}

// =================================================================
// 7. INVENTORY REPORT
// =================================================================
@Composable
fun InventoryReportScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val products by storeViewModel.products.collectAsState()
    val store by storeViewModel.store.collectAsState()
    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()

    var lowStockFilterOnly by remember { mutableStateOf(false) }

    val filteredProducts = remember(products, lowStockFilterOnly) {
        if (lowStockFilterOnly) products.filter { it.stock <= it.lowStockLimit } else products
    }

    val totalItemsCount = remember(filteredProducts) { filteredProducts.sumOf { it.stock } }
    val totalCostValue = remember(filteredProducts) { filteredProducts.sumOf { it.stock * it.buyPrice } }
    val totalSaleValue = remember(filteredProducts) { filteredProducts.sumOf { it.stock * it.sellPrice } }
    val nowFormatted = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()) }

    fun buildReportData(): ReportTableData {
        val rows = filteredProducts.map { p ->
            if (canSeeProfit) {
                val costVal = p.stock * p.buyPrice
                listOf(
                    p.name,
                    "${p.stock} ${p.unit}",
                    ShopLocale.formatMoney(p.buyPrice, currentLanguage),
                    ShopLocale.formatMoney(p.sellPrice, currentLanguage),
                    ShopLocale.formatMoney(costVal, currentLanguage)
                )
            } else {
                val saleVal = p.stock * p.sellPrice
                listOf(
                    p.name,
                    "${p.stock} ${p.unit}",
                    ShopLocale.formatMoney(p.sellPrice, currentLanguage),
                    ShopLocale.formatMoney(saleVal, currentLanguage)
                )
            }
        }
        val rawRows = filteredProducts.map { p ->
            if (canSeeProfit) {
                listOf(null, p.stock.toDouble(), p.buyPrice, p.sellPrice, p.stock * p.buyPrice)
            } else {
                listOf(null, p.stock.toDouble(), p.sellPrice, p.stock * p.sellPrice)
            }
        }
        val totals = if (canSeeProfit) {
            listOf(
                "TOTAL (${filteredProducts.size} Products)",
                "$totalItemsCount items",
                "",
                "",
                ShopLocale.formatMoney(totalCostValue, currentLanguage)
            )
        } else {
            listOf(
                "TOTAL (${filteredProducts.size} Products)",
                "$totalItemsCount items",
                "",
                ShopLocale.formatMoney(totalSaleValue, currentLanguage)
            )
        }
        return ReportTableData(
            title = "Inventory Valuation Report${if (lowStockFilterOnly) " (Low Stock Only)" else ""}",
            storeName = store?.name ?: "StoreOS Dokan",
            storePhone = store?.phone ?: "",
            storeAddress = store?.address ?: "",
            periodLabel = "As of $nowFormatted",
            generatedAt = nowFormatted,
            headers = if (canSeeProfit) {
                listOf("Product", "Stock", "Buy Price", "Sell Price", "Cost Value")
            } else {
                listOf("Product", "Stock", "Sell Price", "Sale Value")
            },
            rows = rows,
            numericColumns = if (canSeeProfit) setOf(1, 2, 3, 4) else setOf(1, 2, 3),
            rawNumbers = rawRows,
            totals = totals
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        ReportTopBar(
            title = "Inventory Report",
            onBack = { storeViewModel.navigateBackInBusiness() }
        )

        // Filter: Low stock toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = VividGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Low Stock Filter Only",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Switch(
                checked = lowStockFilterOnly,
                onCheckedChange = { lowStockFilterOnly = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = VividGreen,
                    checkedTrackColor = Color(0x335DD62C)
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        ExportActionBar(
            onDownloadPdf = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "InventoryReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onPrint = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "InventoryReport_${System.currentTimeMillis()}")
                ReportExportUtil.printReportFile(context, file, "InventoryReport")
            },
            onExportCsv = {
                val file = ReportExportUtil.generateCsvReport(context, buildReportData(), "InventoryReport_${System.currentTimeMillis()}")
                ReportExportUtil.downloadReportFile(context, file)
            },
            onShare = {
                val file = ReportExportUtil.generatePdfReport(context, buildReportData(), "InventoryReport_${System.currentTimeMillis()}")
                ReportExportUtil.shareReportFile(context, file, "application/pdf", "Inventory Report - ${store?.name}")
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredProducts.isEmpty()) {
            PlainEmptyState(message = if (lowStockFilterOnly) "No low stock products found! All items well stocked." else "No products found in inventory.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    ReportTableHeaderRow(
                        headers = if (canSeeProfit) {
                            listOf("Product", "Qty / Unit", "Buy / Sell", "Cost Value")
                        } else {
                            listOf("Product", "Qty / Unit", "Sell Price", "Sale Value")
                        }
                    )
                }

                items(filteredProducts) { p ->
                    val isLow = p.stock <= p.lowStockLimit
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        color = Color(0x0DFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.3f)) {
                                Text(
                                    text = p.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = p.category,
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                            Text(
                                text = "${p.stock} ${p.unit}",
                                color = if (isLow) Color(0xFFFFB300) else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isLow) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                if (canSeeProfit) {
                                    Text(text = "৳${p.buyPrice.toInt()}", color = TextSecondary, fontSize = 11.5.sp)
                                }
                                Text(text = "৳${p.sellPrice.toInt()}", color = VividGreen, fontSize = 11.5.sp)
                            }
                            Text(
                                text = if (canSeeProfit) {
                                    "৳${(p.stock * p.buyPrice).toInt()}"
                                } else {
                                    "৳${(p.stock * p.sellPrice).toInt()}"
                                },
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ReportTableTotalsBar(
                        label = if (canSeeProfit) "TOTAL COST VALUE" else "TOTAL SALE VALUE",
                        subValue = if (canSeeProfit) "Sale Value: ৳${totalSaleValue.toInt()}" else "${filteredProducts.size} products",
                        mainValue = ShopLocale.formatMoney(if (canSeeProfit) totalCostValue else totalSaleValue, currentLanguage),
                        profitValue = "$totalItemsCount total items"
                    )
                }
            }
        }
    }
}

// =================================================================
// REUSABLE REPORT ROW COMPONENTS
// =================================================================

@Composable
fun ReportTopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("report_back_btn")) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReportTableHeaderRow(
    headers: List<String>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color(0x1A5DD62C),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            headers.forEachIndexed { idx, h ->
                val align = if (idx >= 2) TextAlign.End else TextAlign.Start
                Text(
                    text = h,
                    color = VividGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = align,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ReportTableTotalsBar(
    label: String,
    subValue: String,
    mainValue: String,
    profitValue: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        color = Color(0x265DD62C),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4D5DD62C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                if (subValue.isNotBlank()) {
                    Text(
                        text = subValue,
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = mainValue,
                    color = VividGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (profitValue.isNotBlank()) {
                    Text(
                        text = profitValue,
                        color = TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerPickerModal(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onSelect: (Customer) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Customer",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(customers) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(c) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = c.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                if (c.mobile.isNotBlank()) {
                                    Text(text = c.mobile, color = TextSecondary, fontSize = 11.5.sp)
                                }
                            }
                            Text(
                                text = "Due: ৳${c.currentBalance.toInt()}",
                                color = if (c.currentBalance > 0) Color(0xFFFFB300) else TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplierPickerModal(
    suppliers: List<Supplier>,
    onDismiss: () -> Unit,
    onSelect: (Supplier) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Supplier",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(suppliers) { s ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(s) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = s.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = s.company.ifBlank { s.mobile }, color = TextSecondary, fontSize = 11.5.sp)
                            }
                            Text(
                                text = "Owed: ৳${s.balance.toInt()}",
                                color = if (s.balance > 0) Color(0xFFFF5252) else TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                    }
                }
            }
        }
    }
}
