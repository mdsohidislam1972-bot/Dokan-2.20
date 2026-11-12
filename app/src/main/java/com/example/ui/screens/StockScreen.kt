package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.MetricCard
import com.example.ui.components.QuantityProgressRing
import com.example.ui.components.VividButton
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale

@Composable
fun StockScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val products by storeViewModel.products.collectAsState()
    val stockSummary by storeViewModel.stockSummary.collectAsState()
    val isBusy by storeViewModel.isBusy.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showAddProductWizard by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var restockProduct by remember { mutableStateOf<Product?>(null) }

    // Categories available
    val availableCategories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filteredProducts = remember(products, searchQuery, selectedCategoryFilter) {
        products.filter { prod ->
            val matchesCategory = (selectedCategoryFilter == "All") || (prod.category == selectedCategoryFilter)
            val matchesSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.nameBn.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("stock_screen_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stock Inventory",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Real-time multi-device stock records",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    VividButton(
                        text = "+ Product",
                        onClick = { showAddProductWizard = true },
                        icon = Icons.Default.Add,
                        testTag = "stock_add_product_btn"
                    )
                }
            }

            // Stat Tiles: Items, Low Stock, Stock Value at Cost (from CalculationEngine)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Items",
                        value = ShopLocale.formatNumber(stockSummary.totalItems, currentLanguage),
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Low Stock",
                        value = ShopLocale.formatNumber(stockSummary.lowStockCount, currentLanguage),
                        icon = Icons.Default.WarningAmber,
                        isHighlight = stockSummary.lowStockCount > 0,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Value (Cost)",
                        value = ShopLocale.formatMoney(stockSummary.stockValueAtCost, currentLanguage),
                        icon = Icons.Default.MonetizationOn,
                        isHighlight = true,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }

            // Search Bar
            item {
                GlassTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search products...",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    } else null,
                    testTag = "stock_search_input"
                )
            }

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(availableCategories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0x335DD62C) else Color(0x20FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) VividGreen else GlassBorder
                            ),
                            modifier = Modifier.clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) VividGreen else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Products List
            if (filteredProducts.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No products found in this category",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    StockProductCard(
                        product = product,
                        currentLanguage = currentLanguage,
                        onClick = { editingProduct = product },
                        onRestock = { restockProduct = product }
                    )
                }
            }
        }

        // Add Product 3-Step Wizard Dialog
        if (showAddProductWizard) {
            AddProductWizardDialog(
                currentLanguage = currentLanguage,
                isBusy = isBusy,
                onDismiss = { showAddProductWizard = false },
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
                        onSuccess = { showAddProductWizard = false }
                    )
                }
            )
        }

        // Edit Product Wizard Dialog
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

        // Quick Restock Dialog (Atomic FieldValue.increment)
        if (restockProduct != null) {
            QuickRestockDialog(
                product = restockProduct!!,
                onDismiss = { restockProduct = null },
                onConfirm = { amount ->
                    storeViewModel.addQuickStock(restockProduct!!.id, amount)
                    restockProduct = null
                }
            )
        }
    }
}

@Composable
fun StockProductCard(
    product: Product,
    currentLanguage: AppLanguage,
    onClick: () -> Unit,
    onRestock: () -> Unit
) {
    val isOut = product.stock <= 0L

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        testTag = "stock_product_${product.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon or Ring
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x20FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(product.category),
                    contentDescription = product.category,
                    tint = VividGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (product.nameBn.isNotBlank()) {
                    Text(
                        text = product.nameBn,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sale: ${ShopLocale.formatMoney(product.sellPrice, currentLanguage)}",
                        color = VividGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " • Cost: ${ShopLocale.formatMoney(product.buyPrice, currentLanguage)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                if (isOut) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AlertRedContainer
                    ) {
                        Text(
                            text = "Out of Stock",
                            color = AlertRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity Ring with Stock number (Amber when low, red at 0)
            QuantityProgressRing(
                quantity = product.stock,
                lowStockLimit = product.lowStockLimit,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Quick "+ Stock" Restock Button (Uses FieldValue.increment)
            VividButton(
                text = "+ Stock",
                onClick = onRestock,
                modifier = Modifier.defaultMinSize(minHeight = 36.dp),
                testTag = "restock_btn_${product.id}"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickRestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long) -> Unit
) {
    var amountText by remember { mutableStateOf("10") }
    var error by remember { mutableStateOf<String?>(null) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1E1E),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Stock",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${product.name} (Current: ${product.stock} ${product.unit})",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = "Quantity to Add (${product.unit})",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "restock_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick buttons (+5, +10, +25, +50)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5L, 10L, 25L, 50L).forEach { qty ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x20FFFFFF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountText = qty.toString() }
                        ) {
                            Text(
                                text = "+$qty",
                                color = VividGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = error ?: "", color = AlertRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    VividButton(
                        text = "Confirm",
                        onClick = {
                            val qty = amountText.toLongOrNull()
                            if (qty == null || qty <= 0L) {
                                error = "Enter a valid positive quantity"
                            } else {
                                onConfirm(qty)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "confirm_restock_btn"
                    )
                }
            }
        }
    }
}
