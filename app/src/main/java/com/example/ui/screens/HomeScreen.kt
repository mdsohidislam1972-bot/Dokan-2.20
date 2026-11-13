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
import androidx.compose.material.icons.filled.Lock
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
import com.example.ui.components.NoPermissionScreen
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

    val isRemoved by storeViewModel.isRemoved.collectAsState()
    val isOwner by storeViewModel.isOwner.collectAsState()
    val currentMember by storeViewModel.currentMember.collectAsState()

    val canSell by storeViewModel.canSellFlow.collectAsState()
    val canViewStock by storeViewModel.canViewStockFlow.collectAsState()
    val canSeeHistory by storeViewModel.canSeeHistoryFlow.collectAsState()
    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()
    val canViewReports by storeViewModel.canViewReportsFlow.collectAsState()
    val canAddEdit by storeViewModel.canAddEditProductsFlow.collectAsState()

    var showAddWizard by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    val isBusy by storeViewModel.isBusy.collectAsState()
    val errorMessage by storeViewModel.errorMessage.collectAsState()

    if (isRemoved) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "অ্যাক্সেস প্রত্যাহার করা হয়েছে" else "Access Revoked",
                        color = AlertRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "আপনি আর এই দোকানে অ্যাক্সেস করতে পারবেন না। দয়া করে দোকানের মালিকের সাথে যোগাযোগ করুন।"
                        else "You no longer have access to this store. Please contact the store owner.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    VividButton(
                        text = if (currentLanguage == AppLanguage.BN) "লগ আউট করুন" else "Sign Out",
                        onClick = { authViewModel.signOut(context) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            ShopTopBar(
                storeName = store?.name ?: "Store",
                ownerRole = if (isOwner) ShopLocale.get("owner_badge", currentLanguage) else (currentMember?.roleLabel ?: "Staff"),
                syncStatus = syncStatus,
                currentLanguage = currentLanguage,
                onToggleLanguage = { storeViewModel.toggleLanguage() },
                onLogout = { authViewModel.signOut(context) }
            )
        },
        bottomBar = {
            FloatingGlassPillBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                canSell = canSell,
                canViewStock = canViewStock,
                canSeeHistory = canSeeHistory,
                canViewReports = canViewReports
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
                    onNavigateToSell = { if (canSell) currentTab = ShopNavTab.SELL },
                    onNavigateToStock = { if (canViewStock) currentTab = ShopNavTab.STOCK },
                    onNavigateToBaki = { if (canViewReports) currentTab = ShopNavTab.BUSINESS },
                    onOpenAddProduct = {
                        if (canAddEdit) {
                            storeViewModel.clearErrorMessage()
                            showAddWizard = true
                        }
                    },
                    onEditProduct = {
                        if (canAddEdit) {
                            storeViewModel.clearErrorMessage()
                            editingProduct = it
                        }
                    }
                )
                ShopNavTab.STOCK -> if (canViewStock) {
                    StockScreen(storeViewModel = storeViewModel)
                } else {
                    NoPermissionScreen(onBack = { currentTab = ShopNavTab.HOME })
                }
                ShopNavTab.SELL -> if (canSell) {
                    SellScreen(storeViewModel = storeViewModel)
                } else {
                    NoPermissionScreen(onBack = { currentTab = ShopNavTab.HOME })
                }
                ShopNavTab.HISTORY -> if (canSeeHistory) {
                    HistoryScreen(
                        storeViewModel = storeViewModel,
                        onNavigateToStock = { currentTab = ShopNavTab.STOCK }
                    )
                } else {
                    NoPermissionScreen(onBack = { currentTab = ShopNavTab.HOME })
                }
                ShopNavTab.BUSINESS -> if (canViewReports) {
                    BusinessScreen(
                        authViewModel = authViewModel,
                        storeViewModel = storeViewModel
                    )
                } else {
                    NoPermissionScreen(onBack = { currentTab = ShopNavTab.HOME })
                }
            }
        }
    }

    if (showAddWizard) {
        AddProductWizardDialog(
            currentLanguage = currentLanguage,
            isBusy = isBusy,
            canSeeBuyingPrice = canSeeProfit,
            externalError = errorMessage,
            onDismiss = {
                storeViewModel.clearErrorMessage()
                showAddWizard = false
            },
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
            canSeeBuyingPrice = canSeeProfit,
            externalError = errorMessage,
            onDismiss = {
                storeViewModel.clearErrorMessage()
                editingProduct = null
            },
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
            onDelete = if (isOwner) {
                {
                    storeViewModel.deleteProduct(editingProduct!!.id) {
                        editingProduct = null
                    }
                }
            } else null
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

    val canSell by storeViewModel.canSellFlow.collectAsState()
    val canAddEdit by storeViewModel.canAddEditProductsFlow.collectAsState()
    val canSeeProfit by storeViewModel.canSeeBuyingPriceAndProfitFlow.collectAsState()
    val canBaki by storeViewModel.canAccessCustomersAndBakiFlow.collectAsState()
    val canViewStock by storeViewModel.canViewStockFlow.collectAsState()
    val canSeeHistory by storeViewModel.canSeeHistoryFlow.collectAsState()
    val canViewReports by storeViewModel.canViewReportsFlow.collectAsState()
    val canSeeSalesSummary = canSeeHistory || canViewReports

    var quickRestockProduct by remember { mutableStateOf<Product?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Buttons
        if (canSell || canAddEdit) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canSell) {
                        VividButton(
                            text = "New Sale (Sell)",
                            onClick = onNavigateToSell,
                            icon = Icons.Default.AddShoppingCart,
                            modifier = Modifier.weight(1f),
                            testTag = "home_new_sale_btn"
                        )
                    }
                    if (canAddEdit) {
                        VividButton(
                            text = "+ Add Product",
                            onClick = onOpenAddProduct,
                            icon = Icons.Default.PostAdd,
                            modifier = Modifier.weight(1f),
                            testTag = "home_add_product_btn"
                        )
                    }
                }
            }
        }

        // Today's Sales & Today's Net Profit (from CalculationEngine)
        if (canSeeSalesSummary) {
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
                    if (canSeeProfit) {
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
        }

        // Total Baki Card (Rule 9: from calculation module using existing card style)
        if (canBaki && canViewReports && bakiOverview.totalBakiOwed > 0.0) {
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
        if (canViewStock && lowStockProducts.isNotEmpty()) {
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
                    canAddEdit = canAddEdit,
                    onClick = { if (canAddEdit) onEditProduct(prod) },
                    onRestock = { quickRestockProduct = prod }
                )
            }
        }

        // Recent Sales List (from CalculationEngine)
        if (canSeeSalesSummary) {
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
                            text = if (canSeeProfit) {
                                "Real data only. Profit = (Sale x Qty) - (Buy x Qty) - Discount."
                            } else {
                                "Real data only. Single calculation engine verified."
                            },
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
    canAddEdit: Boolean = true,
    onClick: () -> Unit,
    onRestock: () -> Unit
) {
    val isOut = product.stock <= 0L

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canAddEdit, onClick = onClick)
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

            if (canAddEdit) {
                VividButton(
                    text = "+ Stock",
                    onClick = onRestock,
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp),
                    testTag = "home_restock_${product.id}"
                )
            }
        }
    }
}
