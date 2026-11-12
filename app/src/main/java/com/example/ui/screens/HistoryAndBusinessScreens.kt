package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import com.example.data.model.CompletedSaleReceipt
import com.example.data.model.Payment
import com.example.data.model.Expense
import com.example.data.model.OtherIncome
import com.example.data.model.AccountAdjustment
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.viewmodel.HistoryTimelineItem
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Sale
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ComingSoonScreen(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("coming_soon_${title.lowercase()}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0x265DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x335DD62C)
                ) {
                    Text(
                        text = "COMING SOON",
                        color = VividGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun HistoryScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier,
    onNavigateToStock: (String?) -> Unit = {}
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.historyPeriod.collectAsState()
    val filter by storeViewModel.historyFilter.collectAsState()
    val viewMode by storeViewModel.historyViewMode.collectAsState()
    val summary by storeViewModel.historySummary.collectAsState()
    val filteredSales by storeViewModel.historyFilteredSales.collectAsState()
    val timelineItems by storeViewModel.historyTimelineItems.collectAsState()
    val productsSold by storeViewModel.historyProductsSold.collectAsState()
    val isGrid by storeViewModel.productsSoldIsGrid.collectAsState()
    val selectedSale by storeViewModel.selectedSaleDetail.collectAsState()
    val customRange by storeViewModel.historyCustomDateRange.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    // If a sale is selected, show the full read-only Sale Details Screen
    if (selectedSale != null) {
        SaleDetailsScreen(
            sale = selectedSale!!,
            currentLanguage = currentLanguage,
            onBack = { storeViewModel.selectSaleDetail(null) }
        )
        return
    }

    if (showDatePicker) {
        CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { startMillis, endMillis ->
                storeViewModel.setHistoryCustomDateRange(startMillis, endMillis)
                showDatePicker = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Step 2: Period tabs: Today, Week, Month, Year, All, Custom Date
        HistoryPeriodTabs(
            currentPeriod = period,
            customRange = customRange,
            currentLanguage = currentLanguage,
            onSelectPeriod = { selected ->
                if (selected == com.example.ui.viewmodel.HistoryPeriod.CUSTOM) {
                    showDatePicker = true
                } else {
                    storeViewModel.setHistoryPeriod(selected)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Step 3: Filter chips: All, Sales Only, Purchases Only
        HistoryFilterChips(
            currentFilter = filter,
            currentLanguage = currentLanguage,
            onSelectFilter = { storeViewModel.setHistoryFilter(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 4: Summary card for chosen period and filter: Sold, Bought, Profit, Orders (from calculation module)
        HistorySummaryCard(
            summary = summary,
            currentLanguage = currentLanguage,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // View Mode Switcher: Transactions vs Products Sold
        HistoryViewModeSwitcher(
            currentMode = viewMode,
            currentLanguage = currentLanguage,
            isGrid = isGrid,
            onSelectMode = { storeViewModel.setHistoryViewMode(it) },
            onToggleGrid = { storeViewModel.toggleProductsSoldGrid() },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main content area based on filter and view mode
        if (filter == com.example.ui.viewmodel.HistoryFilter.PURCHASES_ONLY) {
            // Step 3 requirement: Purchases Only shows plain message "No purchases yet"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "এখনো কোনো কেনাকাটা নেই" else "No purchases yet",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "ক্রয় ব্যবস্থাপনা ফিচারটি শীঘ্রই আসছে।" else "Purchase tracking will be available in an upcoming update.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else if (viewMode == com.example.ui.viewmodel.HistoryViewMode.TRANSACTIONS) {
            // Step 5: Transactions list grouped by day (newest first)
            HistoryTransactionsList(
                timelineItems = timelineItems,
                currentLanguage = currentLanguage,
                onSaleClick = { storeViewModel.selectSaleDetail(it) }
            )
        } else {
            // Step 7: Products sold list for the period (grid / list toggle)
            HistoryProductsSoldList(
                productsSold = productsSold,
                isGrid = isGrid,
                currentLanguage = currentLanguage,
                onProductClick = { stat ->
                    onNavigateToStock(stat.productName)
                }
            )
        }
    }
}

/**
 * Step 2: Period tabs: Today, Week, Month, Year, All, Custom Date
 */
@Composable
fun HistoryPeriodTabs(
    currentPeriod: com.example.ui.viewmodel.HistoryPeriod,
    customRange: Pair<Long, Long>?,
    currentLanguage: AppLanguage,
    onSelectPeriod: (com.example.ui.viewmodel.HistoryPeriod) -> Unit
) {
    val periods = com.example.ui.viewmodel.HistoryPeriod.values()
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.US) }

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(periods.size) { index ->
            val p = periods[index]
            val isSelected = currentPeriod == p
            val label = if (p == com.example.ui.viewmodel.HistoryPeriod.CUSTOM && customRange != null) {
                val s = dateFormat.format(Date(customRange.first))
                val e = dateFormat.format(Date(customRange.second))
                "$s - $e"
            } else {
                if (currentLanguage == AppLanguage.BN) p.labelBn else p.labelEn
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectPeriod(p) }
                    .testTag("period_tab_${p.name.lowercase()}"),
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) VividGreen else Color(0x1AFFFFFF),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) VividGreen else Color(0x33FFFFFF)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (p == com.example.ui.viewmodel.HistoryPeriod.CUSTOM) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = if (isSelected) DarkBackground else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = label,
                        color = if (isSelected) DarkBackground else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Step 3: Filter chips: All, Sales Only, Purchases Only
 */
@Composable
fun HistoryFilterChips(
    currentFilter: com.example.ui.viewmodel.HistoryFilter,
    currentLanguage: AppLanguage,
    onSelectFilter: (com.example.ui.viewmodel.HistoryFilter) -> Unit
) {
    val filters = com.example.ui.viewmodel.HistoryFilter.values()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { f ->
            val isSelected = currentFilter == f
            val label = if (currentLanguage == AppLanguage.BN) f.labelBn else f.labelEn

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectFilter(f) }
                    .testTag("filter_chip_${f.name.lowercase()}"),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0x335DD62C) else Color(0x0DFFFFFF),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) VividGreen else Color(0x20FFFFFF)
                )
            ) {
                Text(
                    text = label,
                    color = if (isSelected) VividGreen else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Step 4: Summary card for chosen period and filter:
 * Sold, Bought, Profit, Orders (strictly from calculation module)
 */
@Composable
fun HistorySummaryCard(
    summary: com.example.domain.PeriodFinancialSummary,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "history_summary_card"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sold (Total Sales)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "বিক্রয় (Sold)" else "Sold",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ShopLocale.formatMoney(summary.totalSold, currentLanguage),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                // Bought (Total Purchases - ৳0 for now)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "ক্রয় (Bought)" else "Bought",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ShopLocale.formatMoney(summary.totalBought, currentLanguage),
                        color = TextSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x1AFFFFFF))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profit
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "লাভ (Profit)" else "Profit",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ShopLocale.formatMoney(summary.totalProfit, currentLanguage),
                        color = VividGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }

                // Orders count
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "অর্ডার সংখ্যা" else "Orders",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ShopLocale.formatNumber(summary.totalOrders, currentLanguage),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Sub-navigation: Transactions vs Products Sold + Grid/List toggle
 */
@Composable
fun HistoryViewModeSwitcher(
    currentMode: com.example.ui.viewmodel.HistoryViewMode,
    currentLanguage: AppLanguage,
    isGrid: Boolean,
    onSelectMode: (com.example.ui.viewmodel.HistoryViewMode) -> Unit,
    onToggleGrid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectMode(com.example.ui.viewmodel.HistoryViewMode.TRANSACTIONS) },
                color = if (currentMode == com.example.ui.viewmodel.HistoryViewMode.TRANSACTIONS) VividGreen else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "লেনদেন" else "Transactions",
                    color = if (currentMode == com.example.ui.viewmodel.HistoryViewMode.TRANSACTIONS) DarkBackground else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 1
                )
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectMode(com.example.ui.viewmodel.HistoryViewMode.PRODUCTS_SOLD) },
                color = if (currentMode == com.example.ui.viewmodel.HistoryViewMode.PRODUCTS_SOLD) VividGreen else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "বিক্রিত পণ্য" else "Products Sold",
                    color = if (currentMode == com.example.ui.viewmodel.HistoryViewMode.PRODUCTS_SOLD) DarkBackground else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    maxLines = 1
                )
            }
        }

        if (currentMode == com.example.ui.viewmodel.HistoryViewMode.PRODUCTS_SOLD) {
            IconButton(
                onClick = onToggleGrid,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                    .testTag("toggle_products_grid")
            ) {
                Icon(
                    imageVector = if (isGrid) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = "Toggle Grid or List",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Step 5: Transactions list grouped by day (newest first).
 * Each row shows invoice number, time, customer name (or Cash Sale), total, and a small label: Paid or Baki.
 * Clear empty state in plain words when period has no sales.
 */
@Composable
fun HistoryTransactionsList(
    timelineItems: List<HistoryTimelineItem>,
    currentLanguage: AppLanguage,
    onSaleClick: (Sale) -> Unit
) {
    if (timelineItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "এই সময়ে কোনো লেনদেন নেই" else "No transactions recorded in this period",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "নতুন বিক্রয় বা বাকি জমা গ্রহণ করলে এখানে লেনদেন দেখা যাবে।"
                        else "Sales and payments will appear here as transactions.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }

    // Group items by day
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayDayFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }
    val todayKey = remember { dayFormat.format(Date()) }
    val yesterdayKey = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        dayFormat.format(cal.time)
    }

    val groupedItems = remember(timelineItems) {
        val map = linkedMapOf<String, MutableList<HistoryTimelineItem>>()
        for (item in timelineItems) {
            val date = Date(item.timestamp)
            val key = dayFormat.format(date)
            map.getOrPut(key) { mutableListOf() }.add(item)
        }
        map
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        groupedItems.forEach { (dateKey, itemsInDay) ->
            item(key = "header_$dateKey") {
                val headerLabel = when (dateKey) {
                    todayKey -> if (currentLanguage == AppLanguage.BN) "আজকে" else "Today"
                    yesterdayKey -> if (currentLanguage == AppLanguage.BN) "গতকাল" else "Yesterday"
                    else -> {
                        val parsed = try { dayFormat.parse(dateKey) } catch (e: Exception) { null }
                        parsed?.let { displayDayFormat.format(it) } ?: dateKey
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = headerLabel,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${itemsInDay.size} " + if (itemsInDay.size == 1) "transaction" else "transactions",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            items(itemsInDay, key = { it.id }) { item ->
                when (item) {
                    is HistoryTimelineItem.SaleItemRow -> {
                        HistorySaleRow(
                            sale = item.sale,
                            currentLanguage = currentLanguage,
                            onClick = { onSaleClick(item.sale) }
                        )
                    }
                    is HistoryTimelineItem.PaymentItemRow -> {
                        HistoryPaymentRow(
                            payment = item.payment,
                            currentLanguage = currentLanguage
                        )
                    }
                    is HistoryTimelineItem.ExpenseItemRow -> {
                        HistoryExpenseRow(
                            expense = item.expense,
                            currentLanguage = currentLanguage
                        )
                    }
                    is HistoryTimelineItem.OtherIncomeItemRow -> {
                        HistoryOtherIncomeRow(
                            income = item.income,
                            currentLanguage = currentLanguage
                        )
                    }
                    is HistoryTimelineItem.AdjustmentItemRow -> {
                        HistoryAdjustmentRow(
                            adjustment = item.adjustment,
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}

/**
 * Payment In row in History feed (does not count as sales or profit)
 */
@Composable
fun HistoryPaymentRow(
    payment: Payment,
    currentLanguage: AppLanguage
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val formattedTime = remember(payment.paymentDate, payment.createdAt) {
        val ts = if (payment.paymentDate > 0L) payment.paymentDate else (payment.createdAt?.toDate()?.time ?: 0L)
        if (ts > 0L) timeFormat.format(Date(ts)) else "--:--"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "payment_row_${payment.paymentNo}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = payment.paymentNo,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${payment.customerName}${if (payment.note.isNotBlank()) " • ${payment.note}" else ""}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+ " + ShopLocale.formatMoney(payment.amount, currentLanguage),
                    color = VividGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0x335DD62C)
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "জমা (Payment In)" else "Payment In",
                        color = VividGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryExpenseRow(
    expense: Expense,
    currentLanguage: AppLanguage
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val formattedTime = remember(expense.date, expense.createdAt) {
        val ts = if (expense.date > 0L) expense.date else (expense.createdAt?.toDate()?.time ?: 0L)
        if (ts > 0L) timeFormat.format(Date(ts)) else "--:--"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "expense_row_${expense.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.category,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${expense.accountName}${if (expense.note.isNotBlank()) " • ${expense.note}" else ""}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "- " + ShopLocale.formatMoney(expense.amount, currentLanguage),
                    color = AlertRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AlertRedContainer
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "খরচ (Expense)" else "Expense",
                        color = AlertRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryOtherIncomeRow(
    income: OtherIncome,
    currentLanguage: AppLanguage
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val formattedTime = remember(income.date, income.createdAt) {
        val ts = if (income.date > 0L) income.date else (income.createdAt?.toDate()?.time ?: 0L)
        if (ts > 0L) timeFormat.format(Date(ts)) else "--:--"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "other_income_row_${income.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = income.category,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${income.accountName}${if (income.note.isNotBlank()) " • ${income.note}" else ""}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+ " + ShopLocale.formatMoney(income.amount, currentLanguage),
                    color = VividGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0x335DD62C)
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "অন্যান্য আয়" else "Other Income",
                        color = VividGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryAdjustmentRow(
    adjustment: AccountAdjustment,
    currentLanguage: AppLanguage
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val formattedTime = remember(adjustment.createdAt) {
        val ts = adjustment.createdAt?.toDate()?.time ?: 0L
        if (ts > 0L) timeFormat.format(Date(ts)) else "--:--"
    }
    val isPositive = adjustment.difference >= 0.0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "adj_row_${adjustment.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = adjustment.accountName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = adjustment.note,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isPositive) "+ " else "- ") + ShopLocale.formatMoney(kotlin.math.abs(adjustment.difference), currentLanguage),
                    color = if (isPositive) VividGreen else AlertRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isPositive) Color(0x335DD62C) else AlertRedContainer
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "সমন্বয় (Adjustment)" else "Adjustment",
                        color = if (isPositive) VividGreen else AlertRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Each row shows invoice number, time, customer name (or Cash Sale), total, and a small label: Paid or Baki
 */
@Composable
fun HistorySaleRow(
    sale: Sale,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.US) }
    val formattedTime = remember(sale.createdAt) {
        sale.createdAt?.toDate()?.let { timeFormat.format(it) } ?: "--:--"
    }
    val isBaki = sale.paymentType == "baki"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        testTag = "sale_row_${sale.invoiceNo}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sale.invoiceNo,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = sale.customerName.ifBlank { "Cash Sale" },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ShopLocale.formatMoney(sale.grandTotal, currentLanguage),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isBaki) Color(0x33FFA000) else Color(0x265DD62C)
                ) {
                    Text(
                        text = if (isBaki) "Baki" else "Paid",
                        color = if (isBaki) Color(0xFFFFB300) else VividGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Step 7: Products sold list for the period: each product with quantity sold,
 * sales amount and profit, with a grid/list toggle. Sorted by quantity sold, highest first.
 * Tap a product to open it in Stock.
 */
@Composable
fun HistoryProductsSoldList(
    productsSold: List<com.example.domain.ProductSoldStat>,
    isGrid: Boolean,
    currentLanguage: AppLanguage,
    onProductClick: (com.example.domain.ProductSoldStat) -> Unit
) {
    if (productsSold.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "এই সময়ে কোনো পণ্য বিক্রয় হয়নি" else "No products sold in this period",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
        return
    }

    if (isGrid) {
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(productsSold, key = { it.productId.ifBlank { it.productName } }) { stat ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_sold_grid_${stat.productId}"),
                    onClick = { onProductClick(stat) }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stat.productName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stat.quantitySold} ${stat.unit}",
                                color = VividGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Text(
                                text = ShopLocale.formatMoney(stat.totalSalesAmount, currentLanguage),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = (if (currentLanguage == AppLanguage.BN) "লাভ: " else "Profit: ") +
                                    ShopLocale.formatMoney(stat.totalProfit, currentLanguage),
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    } else {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(productsSold, key = { it.productId.ifBlank { it.productName } }) { stat ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_sold_list_${stat.productId}"),
                    onClick = { onProductClick(stat) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stat.productName,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = (if (currentLanguage == AppLanguage.BN) "মোট লাভ: " else "Profit: ") +
                                        ShopLocale.formatMoney(stat.totalProfit, currentLanguage),
                                color = TextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${stat.quantitySold} ${stat.unit}",
                                color = VividGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ShopLocale.formatMoney(stat.totalSalesAmount, currentLanguage),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 6: Sale details screen (tap a row):
 * invoice number, date and time, customer, every item with quantity, price and line total,
 * discount, total, Paid or Baki, notes, and who made the sale. Read-only.
 */
@Composable
fun SaleDetailsScreen(
    sale: Sale,
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onBack)

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    val formattedDateTime = remember(sale.createdAt) {
        sale.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
    }
    val isBaki = sale.paymentType == "baki"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x1AFFFFFF), CircleShape)
                    .testTag("sale_detail_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = sale.invoiceNo,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = formattedDateTime,
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Customer & Status Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "ক্রেতার নাম" else "Customer",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = sale.customerName.ifBlank { "Cash Sale" },
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                if (sale.customerPhone.isNotBlank()) {
                                    Text(
                                        text = sale.customerPhone,
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isBaki) Color(0x33FFA000) else Color(0x265DD62C)
                            ) {
                                Text(
                                    text = if (isBaki) "Baki (Credit)" else "Paid (Cash)",
                                    color = if (isBaki) Color(0xFFFFB300) else VividGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    maxLines = 1
                                )
                            }
                        }

                        if (sale.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0x14FFFFFF))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (if (currentLanguage == AppLanguage.BN) "নোট: " else "Notes: ") + sale.notes,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (if (currentLanguage == AppLanguage.BN) "বিক্রয়কারী: " else "Sold by: ") +
                                    (sale.soldBy.ifBlank { "Store Staff" }),
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Items Purchased Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "পণ্য তালিকা (${sale.items.size})" else "Items Sold (${sale.items.size})",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        sale.items.forEachIndexed { idx, item ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color(0x0FFFFFFF)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.qty} ${item.unit} x " + ShopLocale.formatMoney(item.sellPrice, currentLanguage),
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    if (item.discount > 0.0) {
                                        Text(
                                            text = (if (currentLanguage == AppLanguage.BN) "ছাড়: " else "Disc: -") +
                                                    ShopLocale.formatMoney(item.discount, currentLanguage),
                                            color = Color(0xFFFFB300),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Text(
                                    text = ShopLocale.formatMoney(item.lineTotal, currentLanguage),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Financial Breakdown Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) "সাবটোটাল" else "Subtotal",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Text(
                                text = ShopLocale.formatMoney(sale.subtotal, currentLanguage),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }

                        if (sale.discount > 0.0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "মোট ছাড়" else "Total Discount",
                                    color = Color(0xFFFFB300),
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "- " + ShopLocale.formatMoney(sale.discount, currentLanguage),
                                    color = Color(0xFFFFB300),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) "সর্বমোট (Total)" else "Grand Total",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = ShopLocale.formatMoney(sale.grandTotal, currentLanguage),
                                color = VividGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Share & Copy Receipt Actions
            item {
                val context = LocalContext.current
                val clipboardManager = LocalClipboardManager.current
                val completedReceipt = remember(sale) {
                    CompletedSaleReceipt(
                        invoiceNo = sale.invoiceNo,
                        storeName = "StoreOS Dokan",
                        items = sale.items,
                        totalItems = sale.totalItems,
                        subtotal = sale.subtotal,
                        discount = sale.discount,
                        grandTotal = sale.grandTotal,
                        paymentType = sale.paymentType,
                        customerName = sale.customerName,
                        customerPhone = sale.customerPhone,
                        notes = sale.notes,
                        timestamp = sale.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = {
                            val shareText = completedReceipt.formatTextReceipt()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Receipt Via")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "রসিদ পাঠান" else "Share Receipt",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(completedReceipt.formatTextReceipt()))
                            Toast.makeText(context, if (currentLanguage == AppLanguage.BN) "রসিদ কপি করা হয়েছে" else "Receipt copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF444444)),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "কপি স্লিপ" else "Copy Slip",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom scrollable calendar date range picker dialog for Step 2
 */
@Composable
fun CustomDateRangePickerDialog(
    initialRange: Pair<Long, Long>?,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (startMillis: Long, endMillis: Long) -> Unit
) {
    var calendarMonth by remember {
        val cal = Calendar.getInstance()
        mutableStateOf(cal)
    }

    var selectedStartMillis by remember {
        mutableStateOf<Long?>(initialRange?.first)
    }
    var selectedEndMillis by remember {
        mutableStateOf<Long?>(initialRange?.second)
    }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val displayDayFormat = remember { SimpleDateFormat("dd MMM", Locale.US) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header with chosen range
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "তারিখ পরিসর নির্বাচন করুন" else "Select Date Range",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val startText = selectedStartMillis?.let { displayDayFormat.format(Date(it)) } ?: "Start Day"
                    val endText = selectedEndMillis?.let { displayDayFormat.format(Date(it)) } ?: "End Day"
                    Text(
                        text = "$startText  ➔  $endText",
                        color = VividGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = calendarMonth.timeInMillis
                            add(Calendar.MONTH, -1)
                        }
                        calendarMonth = newCal
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = TextPrimary
                        )
                    }

                    Text(
                        text = monthYearFormat.format(calendarMonth.time),
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    IconButton(onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = calendarMonth.timeInMillis
                            add(Calendar.MONTH, 1)
                        }
                        calendarMonth = newCal
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Days of week header
                val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    dayNames.forEach { d ->
                        Text(
                            text = d,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Calendar days grid
                val calForGrid = Calendar.getInstance().apply {
                    timeInMillis = calendarMonth.timeInMillis
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val daysInMonth = calForGrid.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfWeek = calForGrid.get(Calendar.DAY_OF_WEEK) - 1 // 0-based for Sunday

                val totalCells = firstDayOfWeek + daysInMonth
                val totalRows = (totalCells + 6) / 7

                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val thisDayCal = Calendar.getInstance().apply {
                                    timeInMillis = calendarMonth.timeInMillis
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val dayMillis = thisDayCal.timeInMillis
                                val isStart = selectedStartMillis != null && dayMillis == selectedStartMillis
                                val isEnd = selectedEndMillis != null && dayMillis == selectedEndMillis
                                val inRange = selectedStartMillis != null && selectedEndMillis != null &&
                                        dayMillis in selectedStartMillis!!..selectedEndMillis!!

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isStart || isEnd -> VividGreen
                                                inRange -> Color(0x335DD62C)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            if (selectedStartMillis == null || (selectedStartMillis != null && selectedEndMillis != null)) {
                                                selectedStartMillis = dayMillis
                                                selectedEndMillis = null
                                            } else {
                                                if (dayMillis >= selectedStartMillis!!) {
                                                    selectedEndMillis = dayMillis
                                                } else {
                                                    selectedEndMillis = selectedStartMillis
                                                    selectedStartMillis = dayMillis
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isStart || isEnd) DarkBackground else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isStart || isEnd || inRange) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Cancel & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "বাতিল" else "Cancel",
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = selectedStartMillis != null) {
                                val start = selectedStartMillis ?: return@clickable
                                val end = selectedEndMillis ?: start
                                val endOfDay = Calendar.getInstance().apply {
                                    timeInMillis = end
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }.timeInMillis
                                onConfirm(start, endOfDay)
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedStartMillis != null) VividGreen else Color(0x33FFFFFF)
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "প্রয়োগ করুন" else "Apply",
                            color = if (selectedStartMillis != null) DarkBackground else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaleReceiptCard(
    sale: Sale,
    currentLanguage: AppLanguage
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val formattedDate = remember(sale.createdAt) {
        sale.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
    }

    val isBaki = sale.paymentType == "baki"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "sale_card_${sale.id}"
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isBaki) Color(0x33FFA000) else Color(0x265DD62C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBaki) Icons.Default.CreditCard else Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = if (isBaki) Color(0xFFFFB300) else VividGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = sale.invoiceNo,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = formattedDate,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isBaki) Color(0x33FFA000) else Color(0x265DD62C)
                ) {
                    Text(
                        text = if (isBaki) "BAKI (CREDIT)" else "CASH",
                        color = if (isBaki) Color(0xFFFFB300) else VividGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name & Items count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sale.customerName} • ${sale.totalItems} items",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ShopLocale.formatMoney(sale.grandTotal, currentLanguage),
                    color = VividGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}
