package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Step 11: Expense Report Screen
 * Live expenses for selected period with category breakdown, detailed table,
 * and 4 export actions (PDF, Print, Excel/CSV, Share).
 */
@Composable
fun ExpenseReportScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by storeViewModel.businessFilteredExpenses.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    val totalExpenses = remember(expenses) {
        expenses.sumOf { it.amount }
    }

    val byCategory = remember(expenses) {
        expenses.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                storeViewModel.setBusinessCustomDateRange(start, end)
                showDatePicker = false
            }
        )
    }

    fun buildExportData(): ReportTableData {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val rows = expenses.map { exp ->
            listOf(
                dateFormat.format(Date(exp.date)),
                exp.category,
                exp.accountName,
                exp.note,
                "৳${"%.2f".format(exp.amount)}"
            )
        }
        val rawNumbers = expenses.map { exp ->
            listOf<Double?>(null, null, null, null, exp.amount)
        }
        val totals = listOf("Total Expenses", "", "", "${expenses.size} entries", "৳${"%.2f".format(totalExpenses)}")
        val rawTotals = listOf<Double?>(null, null, null, null, totalExpenses)

        return ReportTableData(
            title = "Expense Report",
            storeName = store?.name ?: "Store",
            storePhone = store?.phone ?: "",
            periodLabel = period.name,
            generatedAt = dateFormat.format(Date()),
            headers = listOf("Date", "Category", "Account", "Note", "Amount"),
            rows = rows,
            numericColumns = setOf(4),
            rawNumbers = rawNumbers,
            totals = totals,
            rawTotals = rawTotals,
            notes = "Total Expenses: ৳${"%.2f".format(totalExpenses)}"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = Color(0xF20F0F0F),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { storeViewModel.navigateBackInBusiness() },
                    modifier = Modifier.testTag("expense_report_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "খরচ রিপোর্ট (Expense Report)" else "Expense Report",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Period: ${period.name} • ${expenses.size} entries",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Period Tabs
            item {
                HistoryPeriodTabs(
                    currentPeriod = period,
                    customRange = customRange,
                    currentLanguage = currentLanguage,
                    onSelectPeriod = { selected ->
                        if (selected == HistoryPeriod.CUSTOM) {
                            showDatePicker = true
                        } else {
                            storeViewModel.setBusinessPeriod(selected)
                        }
                    }
                )
            }

            // Summary Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_report_summary_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x26FF5252)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = AlertRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "মোট ব্যয় / খরচ" else "Total Expenses",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = ShopLocale.formatMoney(totalExpenses, currentLanguage),
                            color = AlertRed,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Export Actions
            item {
                ExportActionBar(
                    onDownloadPdf = {
                        val file = ReportExportUtil.generatePdfReport(context, buildExportData(), "ExpenseReport_${System.currentTimeMillis()}")
                        ReportExportUtil.downloadReportFile(context, file)
                    },
                    onPrint = {
                        val file = ReportExportUtil.generatePdfReport(context, buildExportData(), "ExpenseReport_${System.currentTimeMillis()}")
                        ReportExportUtil.printReportFile(context, file, "ExpenseReport")
                    },
                    onExportCsv = {
                        val file = ReportExportUtil.generateCsvReport(context, buildExportData(), "ExpenseReport_${System.currentTimeMillis()}")
                        ReportExportUtil.downloadReportFile(context, file)
                    },
                    onShare = {
                        val file = ReportExportUtil.generatePdfReport(context, buildExportData(), "ExpenseReport_${System.currentTimeMillis()}")
                        ReportExportUtil.shareReportFile(context, file, "application/pdf", "Expense Report - ${store?.name}")
                    }
                )
            }

            // Breakdown by Category
            if (byCategory.isNotEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Category Breakdown",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            byCategory.forEach { (catName, catTotal) ->
                                val pct = if (totalExpenses > 0) (catTotal / totalExpenses * 100) else 0.0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = catName, color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "${"%.1f".format(pct)}% of total", color = TextMuted, fontSize = 11.sp)
                                    }
                                    Text(
                                        text = "৳${"%.2f".format(catTotal)}",
                                        color = AlertRed,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Entries List
            if (expenses.isEmpty()) {
                item {
                    PlainEmptyState(message = "No expenses recorded in this period")
                }
            } else {
                item {
                    Text(
                        text = "Transactions (${expenses.size})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
                items(expenses, key = { it.id }) { exp ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0x33FF5252)
                                    ) {
                                        Text(
                                            text = exp.category,
                                            color = AlertRed,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = exp.accountName,
                                        color = TextMuted,
                                        fontSize = 11.5.sp
                                    )
                                }
                                if (exp.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = exp.note,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(exp.date)),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "-৳${"%.2f".format(exp.amount)}",
                                color = AlertRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 11: Other Income Report Screen
 * Live income for selected period with category breakdown, detailed table,
 * and 4 export actions (PDF, Print, Excel/CSV, Share).
 */
@Composable
fun IncomeReportScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val incomes by storeViewModel.businessFilteredOtherIncomes.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    val totalIncome = remember(incomes) {
        incomes.sumOf { it.amount }
    }

    val byCategory = remember(incomes) {
        incomes.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                storeViewModel.setBusinessCustomDateRange(start, end)
                showDatePicker = false
            }
        )
    }

    fun buildExportData(): ReportTableData {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val rows = incomes.map { inc ->
            listOf(
                dateFormat.format(Date(inc.date)),
                inc.category,
                inc.accountName,
                inc.note,
                "৳${"%.2f".format(inc.amount)}"
            )
        }
        val rawNumbers = incomes.map { inc ->
            listOf<Double?>(null, null, null, null, inc.amount)
        }
        val totals = listOf("Total Other Income", "", "", "${incomes.size} entries", "+৳${"%.2f".format(totalIncome)}")
        val rawTotals = listOf<Double?>(null, null, null, null, totalIncome)

        return ReportTableData(
            title = "Other Income Report",
            storeName = store?.name ?: "Store",
            storePhone = store?.phone ?: "",
            periodLabel = period.name,
            generatedAt = dateFormat.format(Date()),
            headers = listOf("Date", "Category", "Account", "Note", "Amount"),
            rows = rows,
            numericColumns = setOf(4),
            rawNumbers = rawNumbers,
            totals = totals,
            rawTotals = rawTotals,
            notes = "Total Other Income: ৳${"%.2f".format(totalIncome)}"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = Color(0xF20F0F0F),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { storeViewModel.navigateBackInBusiness() },
                    modifier = Modifier.testTag("income_report_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "অন্যান্য আয় রিপোর্ট (Income Report)" else "Other Income Report",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Period: ${period.name} • ${incomes.size} entries",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Period Tabs
            item {
                HistoryPeriodTabs(
                    currentPeriod = period,
                    customRange = customRange,
                    currentLanguage = currentLanguage,
                    onSelectPeriod = { selected ->
                        if (selected == HistoryPeriod.CUSTOM) {
                            showDatePicker = true
                        } else {
                            storeViewModel.setBusinessPeriod(selected)
                        }
                    }
                )
            }

            // Summary Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_report_summary_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x265DD62C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = VividGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "মোট অন্যান্য আয়" else "Total Other Income",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = ShopLocale.formatMoney(totalIncome, currentLanguage),
                            color = VividGreen,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Export Actions
            item {
                ExportActionBar(
                    onDownloadPdf = {
                        val file = ReportExportUtil.generatePdfReport(context, buildExportData(), "IncomeReport_${System.currentTimeMillis()}")
                        ReportExportUtil.downloadReportFile(context, file)
                    },
                    onPrint = {
                        val file = ReportExportUtil.generatePdfReport(context, buildExportData(), "IncomeReport_${System.currentTimeMillis()}")
                        ReportExportUtil.printReportFile(context, file, "IncomeReport")
                    },
                    onExportCsv = {
                        val file = ReportExportUtil.generateCsvReport(context, buildExportData(), "IncomeReport_${System.currentTimeMillis()}")
                        ReportExportUtil.downloadReportFile(context, file)
                    },
                    onShare = {
                        val file = ReportExportUtil.generatePdfReport(context, buildExportData(), "IncomeReport_${System.currentTimeMillis()}")
                        ReportExportUtil.shareReportFile(context, file, "application/pdf", "Income Report - ${store?.name}")
                    }
                )
            }

            // Breakdown by Category
            if (byCategory.isNotEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Category Breakdown",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            byCategory.forEach { (catName, catTotal) ->
                                val pct = if (totalIncome > 0) (catTotal / totalIncome * 100) else 0.0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = catName, color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "${"%.1f".format(pct)}% of total", color = TextMuted, fontSize = 11.sp)
                                    }
                                    Text(
                                        text = "+৳${"%.2f".format(catTotal)}",
                                        color = VividGreen,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Entries List
            if (incomes.isEmpty()) {
                item {
                    PlainEmptyState(message = "No other income recorded in this period")
                }
            } else {
                item {
                    Text(
                        text = "Transactions (${incomes.size})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
                items(incomes, key = { it.id }) { inc ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0x335DD62C)
                                    ) {
                                        Text(
                                            text = inc.category,
                                            color = VividGreen,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = inc.accountName,
                                        color = TextMuted,
                                        fontSize = 11.5.sp
                                    )
                                }
                                if (inc.note.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = inc.note,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormat.format(Date(inc.date)),
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "+৳${"%.2f".format(inc.amount)}",
                                color = VividGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
