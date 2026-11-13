package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.GlassCard
import com.example.ui.components.PlainEmptyState
import com.example.ui.components.SalesBarChart
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.BusinessNavDestination
import com.example.ui.viewmodel.HistoryPeriod
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale

/**
 * Main Business tab screen router.
 * Replaces the old "Coming soon" screen and directs to all dashboards, reports, and tools.
 */
@Composable
fun BusinessScreen(
    authViewModel: AuthViewModel,
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val destination by storeViewModel.businessNavDestination.collectAsState()
    val isOwner by storeViewModel.isOwner.collectAsState()
    val canReports by storeViewModel.canViewReportsFlow.collectAsState()
    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()
    val canBaki by storeViewModel.canAccessCustomersAndBakiFlow.collectAsState()
    val canSuppliers by storeViewModel.canAccessSuppliersAndPurchasesFlow.collectAsState()
    val canStock by storeViewModel.canViewStockFlow.collectAsState()
    val canHistory by storeViewModel.canSeeHistoryFlow.collectAsState()
    val canCashBank by storeViewModel.canAccessCashAndBankFlow.collectAsState()
    val canExpenses by storeViewModel.canAccessExpensesAndIncomeFlow.collectAsState()
    val canNotes by storeViewModel.canAccessNotesFlow.collectAsState()

    val isDestinationAllowed = when (destination) {
        BusinessNavDestination.DASHBOARD -> canReports
        BusinessNavDestination.BAKI_SCREEN -> canBaki
        BusinessNavDestination.SUPPLIERS_SCREEN -> canSuppliers
        BusinessNavDestination.LOW_STOCK_SCREEN -> canStock && canReports
        BusinessNavDestination.SLOW_SELLERS_SCREEN -> canStock && canReports
        BusinessNavDestination.REPORT_SALES -> canReports && canHistory
        BusinessNavDestination.REPORT_PURCHASES -> canReports && canSuppliers
        BusinessNavDestination.REPORT_DAY_BOOK -> canReports
        BusinessNavDestination.REPORT_PROFIT_LOSS -> canReports && canSeeProfit
        BusinessNavDestination.REPORT_CUSTOMER_STATEMENT -> canReports && canBaki
        BusinessNavDestination.REPORT_SUPPLIER_STATEMENT -> canReports && canSuppliers
        BusinessNavDestination.REPORT_INVENTORY -> canReports && canStock
        BusinessNavDestination.MONTHLY_REPORT -> canReports && canSeeProfit
        BusinessNavDestination.DATA_HEALTH_CHECK -> isOwner
        BusinessNavDestination.TEST_CHECKLIST -> isOwner
        BusinessNavDestination.CASH_BANK_SCREEN -> canCashBank
        BusinessNavDestination.ACCOUNT_DETAILS_SCREEN -> canCashBank
        BusinessNavDestination.EXPENSES_SCREEN -> canExpenses
        BusinessNavDestination.OTHER_INCOMES_SCREEN -> canExpenses
        BusinessNavDestination.MANAGE_CATEGORIES_SCREEN -> canExpenses
        BusinessNavDestination.NOTES_SCREEN -> canNotes
        BusinessNavDestination.REPORT_EXPENSES -> canReports && canExpenses
        BusinessNavDestination.REPORT_INCOMES -> canReports && canExpenses
        BusinessNavDestination.STAFF_MANAGEMENT_SCREEN -> isOwner
        BusinessNavDestination.STAFF_MEMBER_DETAILS_SCREEN -> isOwner
        BusinessNavDestination.REPORT_STAFF -> canReports
    }

    BackHandler(enabled = destination != BusinessNavDestination.DASHBOARD) {
        storeViewModel.navigateBackInBusiness()
    }

    if (!isDestinationAllowed) {
        com.example.ui.components.NoPermissionScreen(
            onBack = if (destination != BusinessNavDestination.DASHBOARD) {
                { storeViewModel.navigateBackInBusiness() }
            } else null,
            modifier = modifier
        )
        return
    }

    when (destination) {
        BusinessNavDestination.DASHBOARD -> {
            BusinessDashboardScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.BAKI_SCREEN -> {
            BakiMainScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.SUPPLIERS_SCREEN -> {
            SuppliersScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.LOW_STOCK_SCREEN -> {
            LowStockScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.SLOW_SELLERS_SCREEN -> {
            SlowSellersScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_SALES -> {
            SalesReportScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_PURCHASES -> {
            PurchaseReportScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_DAY_BOOK -> {
            DayBookScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_PROFIT_LOSS -> {
            ProfitAndLossScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_CUSTOMER_STATEMENT -> {
            CustomerStatementScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_SUPPLIER_STATEMENT -> {
            SupplierStatementScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_INVENTORY -> {
            InventoryReportScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.MONTHLY_REPORT -> {
            MonthlyReportScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.DATA_HEALTH_CHECK -> {
            DataHealthCheckScreen(
                authViewModel = authViewModel,
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.TEST_CHECKLIST -> {
            TestChecklistScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.CASH_BANK_SCREEN -> {
            CashAndBankScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.ACCOUNT_DETAILS_SCREEN -> {
            AccountDetailsScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.EXPENSES_SCREEN -> {
            ExpensesScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.OTHER_INCOMES_SCREEN -> {
            OtherIncomesScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.MANAGE_CATEGORIES_SCREEN -> {
            ManageCategoriesScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.NOTES_SCREEN -> {
            StoreNotesScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_EXPENSES -> {
            ExpenseReportScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_INCOMES -> {
            IncomeReportScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.STAFF_MANAGEMENT_SCREEN -> {
            StaffManagementScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.STAFF_MEMBER_DETAILS_SCREEN -> {
            StaffMemberDetailsScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
        BusinessNavDestination.REPORT_STAFF -> {
            StaffReportScreen(
                storeViewModel = storeViewModel,
                modifier = modifier
            )
        }
    }
}

/**
 * Step 2: Business Tab Dashboard
 */
@Composable
fun BusinessDashboardScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.businessPeriod.collectAsState()
    val customRange by storeViewModel.businessCustomDateRange.collectAsState()

    val isOwner by storeViewModel.isOwner.collectAsState()
    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()
    val canBaki by storeViewModel.canAccessCustomersAndBakiFlow.collectAsState()
    val canSuppliers by storeViewModel.canAccessSuppliersAndPurchasesFlow.collectAsState()
    val canCashBank by storeViewModel.canAccessCashAndBankFlow.collectAsState()
    val canExpenses by storeViewModel.canAccessExpensesAndIncomeFlow.collectAsState()
    val canNotes by storeViewModel.canAccessNotesFlow.collectAsState()
    val canReports by storeViewModel.canViewReportsFlow.collectAsState()
    val canStock by storeViewModel.canViewStockFlow.collectAsState()
    val canHistory by storeViewModel.canSeeHistoryFlow.collectAsState()

    val financials by storeViewModel.businessFinancials.collectAsState()
    val moneyNow by storeViewModel.moneyNow.collectAsState()
    val totalBalance by storeViewModel.totalBalance.collectAsState()
    val filteredExpenses by storeViewModel.businessFilteredExpenses.collectAsState()
    val filteredOtherIncomes by storeViewModel.businessFilteredOtherIncomes.collectAsState()
    val chartPoints by storeViewModel.salesChartPoints.collectAsState()
    val topProducts by storeViewModel.topSellingProducts.collectAsState()
    val categoryStats by storeViewModel.salesByCategory.collectAsState()
    val slowSellers by storeViewModel.slowSellingProducts.collectAsState()
    val lowStockList by storeViewModel.lowStockProducts.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Step 1: Shared period filter
        item {
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Top KPI Cards: Total Sales, Total Profit (if allowed), Orders
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = if (currentLanguage == AppLanguage.BN) "মোট বিক্রয়" else "Total Sales",
                        value = ShopLocale.formatMoney(financials.totalSold, currentLanguage),
                        icon = Icons.Default.TrendingUp,
                        iconTint = VividGreen,
                        modifier = Modifier.weight(1f)
                    )
                    if (canSeeProfit) {
                        KpiStatCard(
                            title = if (currentLanguage == AppLanguage.BN) "মোট লাভ" else "Total Profit",
                            value = ShopLocale.formatMoney(financials.totalProfit, currentLanguage),
                            icon = Icons.Default.AccountBalance,
                            iconTint = Color(0xFF00E5FF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    KpiStatCard(
                        title = if (currentLanguage == AppLanguage.BN) "অর্ডার" else "Orders",
                        value = "${financials.totalOrders}",
                        icon = Icons.Default.ReceiptLong,
                        iconTint = Color(0xFFFFB300),
                        modifier = Modifier.weight(0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Money Now Card: Stock value at cost (if allowed), Baki (if allowed), You owe suppliers (if allowed)
        if (canCashBank || canSeeProfit || canBaki || canSuppliers) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("money_now_card")
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = VividGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "বর্তমান স্থিতি (Money Now)" else "Money Now",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (canCashBank) {
                                Spacer(modifier = Modifier.height(12.dp))

                                // Total Balance across Cash + Bank (tap navigates to Cash & Bank screen)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x1A5DD62C))
                                        .border(1.dp, Color(0x335DD62C), RoundedCornerShape(12.dp))
                                        .clickable { storeViewModel.navigateInBusiness(BusinessNavDestination.CASH_BANK_SCREEN) }
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = VividGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (currentLanguage == AppLanguage.BN) "মোট স্থিতি (ক্যাশ + ব্যাংক)" else "Total Balance (Cash + Bank)",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = ShopLocale.formatMoney(totalBalance, currentLanguage),
                                            color = VividGreen,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = VividGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            if (canSeeProfit || canBaki || canSuppliers) {
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (canSeeProfit) {
                                        MoneyNowItem(
                                            title = if (currentLanguage == AppLanguage.BN) "মজুদ পণ্যের মূল্য" else "Stock Value",
                                            value = ShopLocale.formatMoney(moneyNow.stockValueAtCost, currentLanguage),
                                            subtitle = "At buying cost",
                                            valueColor = TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (canBaki) {
                                        MoneyNowItem(
                                            title = if (currentLanguage == AppLanguage.BN) "পাওনা বাকি" else "Baki (Owed to me)",
                                            value = ShopLocale.formatMoney(moneyNow.bakiCustomersOwe, currentLanguage),
                                            subtitle = "${moneyNow.debtorsCount} debtors",
                                            valueColor = Color(0xFFFFB300),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (canSuppliers) {
                                        MoneyNowItem(
                                            title = if (currentLanguage == AppLanguage.BN) "মহাজন দেনা" else "You Owe Suppliers",
                                            value = ShopLocale.formatMoney(moneyNow.debtToSuppliers, currentLanguage),
                                            subtitle = "${moneyNow.suppliersCount} suppliers",
                                            valueColor = Color(0xFFFF5252),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }

        // Sales Trend Chart (Day or Month)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) "বিক্রয় চিত্র (Sales Chart)" else "Sales Trend",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (period == HistoryPeriod.YEAR || period == HistoryPeriod.ALL) "Monthly" else "Daily",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        SalesBarChart(points = chartPoints)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Best Selling Products (Top 5)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "সর্বোচ্চ বিক্রিত পণ্য (Top 5 Products)" else "Best-Selling Products",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (topProducts.isEmpty()) {
                            PlainEmptyState(message = "No products sold in this period")
                        } else {
                            topProducts.forEachIndexed { idx, stat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0x265DD62C),
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    color = VividGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = stat.productName,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${stat.quantitySold} ${stat.unit} sold",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = ShopLocale.formatMoney(stat.totalSalesAmount, currentLanguage),
                                        color = VividGreen,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Sales by Category Donut Chart
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "ক্যাটেগরি অনুযায়ী বিক্রয়" else "Sales by Category",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CategoryDonutChart(stats = categoryStats)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Tappable Action Cards: Staff, Baki, Suppliers, Low Stock, Slow Sellers, Cash & Bank, Expenses, Notes
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Staff & Permissions card (Owner only)
                if (isOwner) {
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "স্টাফ ও অনুমতি (Staff & Permissions)" else "Staff & Permissions",
                        subtitle = "Manage members, invites & role permissions",
                        icon = Icons.Default.Badge,
                        iconTint = VividGreen,
                        testTag = "card_open_staff",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.STAFF_MANAGEMENT_SCREEN) }
                    )
                }

                // Baki card (how many customers owe, total, tap opens Baki screen)
                if (canBaki) {
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "বাকি হিসাব (Baki Ledger)" else "Customer Baki",
                        subtitle = "${moneyNow.debtorsCount} customers owe ${ShopLocale.formatMoney(moneyNow.bakiCustomersOwe, currentLanguage)}",
                        icon = Icons.Default.People,
                        iconTint = Color(0xFFFFB300),
                        testTag = "card_open_baki",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.BAKI_SCREEN) }
                    )
                }

                // Suppliers card (total I owe, tap opens Suppliers screen)
                if (canSuppliers) {
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "মহাজনদের তালিকা (Suppliers)" else "Suppliers & Credit",
                        subtitle = "You owe ${ShopLocale.formatMoney(moneyNow.debtToSuppliers, currentLanguage)} to ${moneyNow.suppliersCount} suppliers",
                        icon = Icons.Default.ShoppingCart,
                        iconTint = Color(0xFFFF5252),
                        testTag = "card_open_suppliers",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.SUPPLIERS_SCREEN) }
                    )
                }

                // Low Stock card (count, tap opens low stock list)
                if (canStock) {
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "স্বল্প মজুদের পণ্য (Low Stock)" else "Low Stock Alert",
                        subtitle = "${lowStockList.size} items at or below alert limit",
                        icon = Icons.Default.Warning,
                        iconTint = Color(0xFFFFB300),
                        testTag = "card_open_low_stock",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.LOW_STOCK_SCREEN) }
                    )

                    // Slow Sellers card (products with stock but no sales in the period, with stock count)
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "অচল পণ্য (Slow Sellers)" else "Slow-Selling Products",
                        subtitle = "${slowSellers.size} products in stock with zero sales in this period",
                        icon = Icons.Default.HourglassEmpty,
                        iconTint = Color(0xFFB388FF),
                        testTag = "card_open_slow_sellers",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.SLOW_SELLERS_SCREEN) }
                    )
                }

                // Cash & Bank Accounts card
                if (canCashBank) {
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "ক্যাশ ও ব্যাংক (Cash & Bank)" else "Cash & Bank Accounts",
                        subtitle = "Total: ${ShopLocale.formatMoney(totalBalance, currentLanguage)} across accounts",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = VividGreen,
                        testTag = "card_open_cash_bank",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.CASH_BANK_SCREEN) }
                    )
                }

                // Store Expenses card
                if (canExpenses) {
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "দোকানের খরচ (Expenses)" else "Store Expenses",
                        subtitle = "${filteredExpenses.size} expenses • ${ShopLocale.formatMoney(filteredExpenses.sumOf { it.amount }, currentLanguage)} in this period",
                        icon = Icons.Default.Receipt,
                        iconTint = AlertRed,
                        testTag = "card_open_expenses",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.EXPENSES_SCREEN) }
                    )

                    // Other Income card
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "অন্যান্য আয় (Other Income)" else "Other Income",
                        subtitle = "${filteredOtherIncomes.size} entries • ${ShopLocale.formatMoney(filteredOtherIncomes.sumOf { it.amount }, currentLanguage)} in this period",
                        icon = Icons.Default.AttachMoney,
                        iconTint = VividGreen,
                        testTag = "card_open_other_income",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.OTHER_INCOMES_SCREEN) }
                    )

                    // Categories card
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "ক্যাটেগরি ব্যবস্থাপনা (Categories)" else "Manage Categories",
                        subtitle = "Customize expense & income categories",
                        icon = Icons.Default.Category,
                        iconTint = Color(0xFF00E5FF),
                        testTag = "card_open_categories",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.MANAGE_CATEGORIES_SCREEN) }
                    )
                }

                // Store Notes card
                if (canNotes) {
                    ActionSummaryCard(
                        title = if (currentLanguage == AppLanguage.BN) "দোকানের নোটসমূহ (Store Notes)" else "Store Notes & Memos",
                        subtitle = "Memos and reminders (does not affect balances)",
                        icon = Icons.Default.StickyNote2,
                        iconTint = Color(0xFFFFB300),
                        testTag = "card_open_notes",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.NOTES_SCREEN) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Step 3: Reports Section listing Reports + Monthly Report
        if (canReports || isOwner) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "ব্যবসায়িক রিপোর্টসমূহ (Reports)" else "Financial Reports",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live reports with PDF, Excel/CSV, Print & Share export",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (canHistory) {
                        ReportNavRow(
                            title = "Sales Report",
                            description = if (canSeeProfit) {
                                "Invoices, customers, profit, Paid & Baki status"
                            } else {
                                "Invoices, customers, Paid & Baki status"
                            },
                            icon = Icons.Default.ReceiptLong,
                            testTag = "report_link_sales",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_SALES) }
                        )
                    }

                    if (canSuppliers) {
                        ReportNavRow(
                            title = "Purchase Report",
                            description = "Purchase orders, suppliers, paid & credit, returns",
                            icon = Icons.Default.ShoppingBag,
                            testTag = "report_link_purchases",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_PURCHASES) }
                        )
                    }

                    ReportNavRow(
                        title = "Day Book",
                        description = "All cash, baki, purchases, returns in time order",
                        icon = Icons.Default.Book,
                        testTag = "report_link_daybook",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_DAY_BOOK) }
                    )

                    if (canSeeProfit) {
                        ReportNavRow(
                            title = "Profit and Loss",
                            description = "Gross & Net profit with stock-based toggle",
                            icon = Icons.Default.Assessment,
                            testTag = "report_link_pnl",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_PROFIT_LOSS) }
                        )
                    }

                    if (canExpenses) {
                        ReportNavRow(
                            title = "Expense Report",
                            description = "Category breakdowns, payment accounts, and timeline",
                            icon = Icons.Default.TrendingDown,
                            testTag = "report_link_expense_report",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_EXPENSES) }
                        )

                        ReportNavRow(
                            title = "Other Income Report",
                            description = "Non-sale revenue streams, categories, and accounts",
                            icon = Icons.Default.TrendingUp,
                            testTag = "report_link_income_report",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_INCOMES) }
                        )
                    }

                    if (canBaki) {
                        ReportNavRow(
                            title = "Customer Statement",
                            description = "Opening balance, Baki purchases, payments, closing",
                            icon = Icons.Default.People,
                            testTag = "report_link_cust_statement",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_CUSTOMER_STATEMENT) }
                        )
                    }

                    if (canSuppliers) {
                        ReportNavRow(
                            title = "Supplier Statement",
                            description = "Credit purchases, payments out, returns, balance",
                            icon = Icons.Default.ShoppingCart,
                            testTag = "report_link_sup_statement",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_SUPPLIER_STATEMENT) }
                        )
                    }

                    if (canStock) {
                        ReportNavRow(
                            title = "Inventory Report",
                            description = if (canSeeProfit) {
                                "Stock, buying price, valuation, low stock filter"
                            } else {
                                "Stock, sell price, low stock filter"
                            },
                            icon = Icons.Default.Inventory,
                            testTag = "report_link_inventory",
                            onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_INVENTORY) }
                        )
                    }

                    // Staff Performance Report
                    ReportNavRow(
                        title = if (currentLanguage == AppLanguage.BN) "স্টাফ পারফর্মেন্স রিপোর্ট" else "Staff Report",
                        description = if (canSeeProfit) {
                            "Sales count, items sold, profit, payments received per staff member"
                        } else {
                            "Sales count, items sold, payments received per staff member"
                        },
                        icon = Icons.Default.Badge,
                        testTag = "report_link_staff",
                        onClick = { storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_STAFF) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (canSeeProfit) {
                        // Step 5: Monthly Report Banner
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { storeViewModel.navigateInBusiness(BusinessNavDestination.MONTHLY_REPORT) }
                                .testTag("link_monthly_report")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x3300E5FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Monthly Performance Report",
                                            color = TextPrimary,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Month picker with Bangla/English PDF export",
                                            color = TextSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Step 6: Data Health Check Banner (Owner only)
                    if (isOwner) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { storeViewModel.navigateInBusiness(BusinessNavDestination.DATA_HEALTH_CHECK) }
                                .testTag("link_data_health")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x335DD62C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HealthAndSafety,
                                            contentDescription = null,
                                            tint = VividGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Data Health Check",
                                            color = TextPrimary,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Scan & fix negative stock, price bugs, orphan sales",
                                            color = TextSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                Spacer(modifier = Modifier.height(14.dp))

                // Step 7: Test Checklist link
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { storeViewModel.navigateInBusiness(BusinessNavDestination.TEST_CHECKLIST) }
                        .testTag("link_test_checklist")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x26FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = VividGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Feature Verification Checklist",
                                    color = TextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Verify exports, calculations, and rules against live data",
                                    color = TextSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
fun KpiStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MoneyNowItem(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 4.dp)) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 9.5.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun ActionSummaryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ReportNavRow(
    title: String,
    description: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = GlassSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x1A5DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = description,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SuppliersScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val suppliers by storeViewModel.suppliers.collectAsState()
    val moneyNow by storeViewModel.moneyNow.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { storeViewModel.navigateBackInBusiness() },
                modifier = Modifier.testTag("suppliers_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "মহাজনদের তালিকা (Suppliers)" else "Suppliers & Credit",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "মোট দেনা: ${ShopLocale.formatMoney(moneyNow.debtToSuppliers, currentLanguage)}" else "Total Debt: ${ShopLocale.formatMoney(moneyNow.debtToSuppliers, currentLanguage)}",
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp
                )
            }
        }

        if (suppliers.isEmpty()) {
            PlainEmptyState(
                message = if (currentLanguage == AppLanguage.BN) "কোন মহাজন নেই" else "No Suppliers Found"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suppliers, key = { it.id }) { supplier ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                storeViewModel.selectStatementSupplier(supplier)
                                storeViewModel.navigateInBusiness(BusinessNavDestination.REPORT_SUPPLIER_STATEMENT)
                            },
                        testTag = "supplier_item_${supplier.id}"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = supplier.name,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (supplier.mobile.isNotBlank()) {
                                    Text(
                                        text = supplier.mobile,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                if (supplier.company.isNotBlank()) {
                                    Text(
                                        text = supplier.company,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = ShopLocale.formatMoney(supplier.balance, currentLanguage),
                                    color = if (supplier.balance > 0) Color(0xFFFF5252) else VividGreen,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (supplier.balance > 0) {
                                        if (currentLanguage == AppLanguage.BN) "পাবে (Payable)" else "Owed"
                                    } else {
                                        if (currentLanguage == AppLanguage.BN) "পরিশোধিত" else "Settled"
                                    },
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val lowStockList by storeViewModel.lowStockProducts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { storeViewModel.navigateBackInBusiness() },
                modifier = Modifier.testTag("low_stock_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "স্বল্প মজুদের পণ্য (Low Stock)" else "Low Stock Alert",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "${lowStockList.size}টি পণ্যের স্টক কম" else "${lowStockList.size} items at or below alert limit",
                    color = Color(0xFFFFB300),
                    fontSize = 12.sp
                )
            }
        }

        if (lowStockList.isEmpty()) {
            PlainEmptyState(
                message = if (currentLanguage == AppLanguage.BN) "সব পণ্য পর্যাপ্ত আছে" else "All Products Well-Stocked"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lowStockList, key = { it.id }) { product ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "low_stock_item_${product.id}"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN && product.nameBn.isNotBlank()) product.nameBn else product.name,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState().value) {
                                        "${product.category.ifBlank { "General" }} • Buy: ৳${product.buyPrice.toInt()} • Sell: ৳${product.sellPrice.toInt()}"
                                    } else {
                                        "${product.category.ifBlank { "General" }} • Sell: ৳${product.sellPrice.toInt()}"
                                    },
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${product.stock} ${product.unit}",
                                    color = if (product.stock <= 0) Color(0xFFFF5252) else Color(0xFFFFB300),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "সতর্কসীমা: ${product.lowStockLimit}" else "Limit: ${product.lowStockLimit}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlowSellersScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val slowSellers by storeViewModel.slowSellingProducts.collectAsState()

    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { storeViewModel.navigateBackInBusiness() },
                modifier = Modifier.testTag("slow_sellers_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "অচল পণ্য (Slow-Selling)" else "Slow-Selling Products",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "${slowSellers.size}টি পণ্যে এই সময়ে কোনো বিক্রি হয়নি" else "${slowSellers.size} items in stock with zero sales in this period",
                    color = Color(0xFFB388FF),
                    fontSize = 12.sp
                )
            }
        }

        if (slowSellers.isEmpty()) {
            PlainEmptyState(
                message = if (currentLanguage == AppLanguage.BN) "কোন অচল পণ্য নেই" else "Zero Stagnant Products"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(slowSellers, key = { it.id }) { product ->
                    val lockedCapital = product.stock * product.buyPrice
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "slow_seller_item_${product.id}"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN && product.nameBn.isNotBlank()) product.nameBn else product.name,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (canSeeProfit) {
                                        "Stock: ${product.stock} ${product.unit} • Buy: ৳${product.buyPrice.toInt()}"
                                    } else {
                                        "Stock: ${product.stock} ${product.unit} • Sell: ৳${product.sellPrice.toInt()}"
                                    },
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            if (canSeeProfit) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = ShopLocale.formatMoney(lockedCapital, currentLanguage),
                                        color = Color(0xFFB388FF),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (currentLanguage == AppLanguage.BN) "আটকা মূলধন" else "Tied-Up Capital",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
