package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.model.Product
import com.example.ui.components.FloatingGlassPillBottomBar
import com.example.ui.components.GlassCard
import com.example.ui.components.LanguageToggle
import com.example.ui.components.MetricCard
import com.example.ui.components.QuantityProgressRing
import com.example.ui.components.ShopNavTab
import com.example.ui.components.VividButton
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale
import com.example.util.SyncStatus

@Composable
fun MainShopScreen(
    authViewModel: AuthViewModel,
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(ShopNavTab.HOME) }
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val store by storeViewModel.store.collectAsState()
    val syncStatus by storeViewModel.syncStatus.collectAsState()

    var showAddWizard by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    val isBusy by storeViewModel.isBusy.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            ShopTopBar(
                storeName = store?.name ?: "Store",
                ownerRole = ShopLocale.get("owner_badge", currentLanguage),
                syncStatus = syncStatus,
                currentLanguage = currentLanguage,
                onToggleLanguage = { storeViewModel.toggleLanguage() },
                onLogout = { authViewModel.signOut(context) }
            )
        },
        bottomBar = {
            FloatingGlassPillBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ShopNavTab.HOME -> HomeDashboardContent(
                    storeViewModel = storeViewModel,
                    onNavigateToSell = { currentTab = ShopNavTab.SELL },
                    onNavigateToStock = { currentTab = ShopNavTab.STOCK },
                    onNavigateToBaki = { currentTab = ShopNavTab.BUSINESS },
                    onOpenAddProduct = { showAddWizard = true },
                    onEditProduct = { editingProduct = it }
                )
                ShopNavTab.STOCK -> StockScreen(
                    storeViewModel = storeViewModel
                )
                ShopNavTab.SELL -> SellScreen(
                    storeViewModel = storeViewModel
                )
                ShopNavTab.HISTORY -> HistoryScreen(
                    storeViewModel = storeViewModel,
                    onNavigateToStock = { currentTab = ShopNavTab.STOCK }
                )
                ShopNavTab.BUSINESS -> BusinessScreen(
                    authViewModel = authViewModel,
                    storeViewModel = storeViewModel
                )
            }
        }
    }

    if (showAddWizard) {
        AddProductWizardDialog(
            currentLanguage = currentLanguage,
            isBusy = isBusy,
            onDismiss = { showAddWizard = false },
            onConfirm = { name, nameBn, category, unit, buy, sell, stock, lowLimit, photo ->
                storeViewModel.addNewProductWizard(
                    name = name,
                    nameBn = nameBn,
                    category = category,
                    unit = unit,
                    buyPrice = buy,
                    sellPrice = sell,
                    stock = stock,
                    lowStockLimit = lowLimit,
                    photoUri = photo,
                    onSuccess = { showAddWizard = false }
                )
            }
        )
    }

    if (editingProduct != null) {
        AddProductWizardDialog(
            productToEdit = editingProduct,
            currentLanguage = currentLanguage,
            isBusy = isBusy,
            onDismiss = { editingProduct = null },
            onConfirm = { name, nameBn, category, unit, buy, sell, stock, lowLimit, photo ->
                storeViewModel.updateProductWizard(
                    productId = editingProduct!!.id,
                    name = name,
                    nameBn = nameBn,
                    category = category,
                    unit = unit,
                    buyPrice = buy,
                    sellPrice = sell,
                    stock = stock,
                    lowStockLimit = lowLimit,
                    photoUri = photo,
                    onSuccess = { editingProduct = null }
                )
            },
            onDelete = {
                storeViewModel.deleteProduct(editingProduct!!.id) {
                    editingProduct = null
                }
            }
        )
    }
}

@Composable
fun ShopTopBar(
    storeName: String,
    ownerRole: String,
    syncStatus: SyncStatus,
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onLogout: () -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x265DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = storeName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x335DD62C)
                        ) {
                            Text(
                                text = ownerRole,
                                color = VividGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))

                        // Sync Indicator (Step 8: online, offline, syncing)
                        when (syncStatus) {
                            SyncStatus.ONLINE -> {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Sync Online",
                                    tint = VividGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = " " + ShopLocale.get("sync_status_online", currentLanguage),
                                    color = VividGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                            SyncStatus.SYNCING -> {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Syncing",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = " Syncing...",
                                    color = Color(0xFFFFB300),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                            SyncStatus.OFFLINE -> {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline Cache",
                                    tint = AlertRed,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = " Offline (Cache Active)",
                                    color = AlertRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                LanguageToggle(
                    currentLanguage = currentLanguage,
                    onToggle = onToggleLanguage
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x20FFFFFF))
                        .testTag("top_bar_logout_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log Out",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDashboardContent(
    storeViewModel: StoreViewModel,
    onNavigateToSell: () -> Unit,
    onNavigateToStock: () -> Unit,
    onNavigateToBaki: () -> Unit = {},
    onOpenAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val financials by storeViewModel.financials.collectAsState()
    val lowStockProducts by storeViewModel.lowStockProducts.collectAsState()
    val recentSales by storeViewModel.recentSales.collectAsState()
    val bakiOverview by storeViewModel.bakiOverview.collectAsState()

    var quickRestockProduct by remember { mutableStateOf<Product?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VividButton(
                    text = "New Sale (Sell)",
                    onClick = onNavigateToSell,
                    icon = Icons.Default.AddShoppingCart,
                    modifier = Modifier.weight(1f),
                    testTag = "home_new_sale_btn"
                )
                VividButton(
                    text = "+ Add Product",
                    onClick = onOpenAddProduct,
                    icon = Icons.Default.PostAdd,
                    modifier = Modifier.weight(1f),
                    testTag = "home_add_product_btn"
                )
            }
        }

        // Today's Sales & Today's Net Profit (from CalculationEngine)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Today's Sales",
                    value = ShopLocale.formatMoney(financials.todaySales, currentLanguage),
                    icon = Icons.Default.MonetizationOn,
                    isHighlight = true,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSell
                )
                MetricCard(
                    title = "Net Profit",
                    value = ShopLocale.formatMoney(financials.todayProfit, currentLanguage),
                    icon = Icons.Default.TrendingUp,
                    isHighlight = true,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSell
                )
            }
        }

        // Orders & Items Sold (from CalculationEngine)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Orders Completed",
                    value = ShopLocale.formatNumber(financials.todayOrdersCount, currentLanguage),
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSell
                )
                MetricCard(
                    title = "Items Sold",
                    value = ShopLocale.formatNumber(financials.todayItemsSold, currentLanguage),
                    icon = Icons.Default.ShoppingBag,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSell
                )
            }
        }

        // Total Baki Card (Rule 9: from calculation module using existing card style)
        if (bakiOverview.totalBakiOwed > 0.0) {
            item {
                MetricCard(
                    title = if (currentLanguage == AppLanguage.BN) "মোট বাকি (বকেয়া)" else "Total Baki Owed",
                    value = ShopLocale.formatMoney(bakiOverview.totalBakiOwed, currentLanguage),
                    icon = Icons.Default.CreditCard,
                    isHighlight = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToBaki
                )
            }
        }

        // Low-Stock Alerts Section (Tapping opens product edit/details)
        if (lowStockProducts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Low Stock Attention (${lowStockProducts.size})",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "View Stock",
                        color = VividGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNavigateToStock() }
                    )
                }
            }

            items(lowStockProducts.take(3), key = { "low_${it.id}" }) { prod ->
                LowStockAlertCard(
                    product = prod,
                    currentLanguage = currentLanguage,
                    onClick = { onEditProduct(prod) },
                    onRestock = { quickRestockProduct = prod }
                )
            }
        }

        // Recent Sales List (from CalculationEngine)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sales Invoices",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (recentSales.isNotEmpty()) {
                    Text(
                        text = "${recentSales.size} recorded",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (recentSales.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No sales recorded yet today. Tap 'SELL' below to create your first order.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(recentSales.take(5), key = { "recent_${it.id}" }) { sale ->
                SaleReceiptCard(sale = sale, currentLanguage = currentLanguage)
            }
        }

        // Calculation Module Integrity Verification Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Single Calculation Engine Verified",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Real data only. Profit = (Sale x Qty) - (Buy x Qty) - Discount.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    if (quickRestockProduct != null) {
        QuickRestockDialog(
            product = quickRestockProduct!!,
            onDismiss = { quickRestockProduct = null },
            onConfirm = { amount ->
                storeViewModel.addQuickStock(quickRestockProduct!!.id, amount)
                quickRestockProduct = null
            }
        )
    }
}

@Composable
fun LowStockAlertCard(
    product: Product,
    currentLanguage: AppLanguage,
    onClick: () -> Unit,
    onRestock: () -> Unit
) {
    val isOut = product.stock <= 0L

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                QuantityProgressRing(
                    quantity = product.stock,
                    lowStockLimit = product.lowStockLimit,
                    size = 42.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = product.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isOut) "Out of Stock" else "Only ${product.stock} ${product.unit} left (Limit: ${product.lowStockLimit})",
                        color = if (isOut) AlertRed else Color(0xFFFFB300),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            VividButton(
                text = "+ Stock",
                onClick = onRestock,
                modifier = Modifier.defaultMinSize(minHeight = 36.dp),
                testTag = "home_restock_${product.id}"
            )
        }
    }
}
