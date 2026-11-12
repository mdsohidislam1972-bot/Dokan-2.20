package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletedSaleReceipt
import com.example.data.model.Product
import com.example.data.model.SaleItem
import com.example.ui.components.DigitalReceiptContent
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.SlideToConfirmSale
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
import com.example.util.ShopLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val products by storeViewModel.products.collectAsState()
    val cart by storeViewModel.cart.collectAsState()
    val isBusy by storeViewModel.isBusy.collectAsState()
    val errorMessage by storeViewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var completedInvoice by remember { mutableStateOf<String?>(null) }

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchesCategory = (selectedCategory == "All") || (prod.category == selectedCategory)
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Point of Sale",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Tap + to add products to sale ticket",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search by item name...",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    } else null,
                    testTag = "sell_search_input"
                )
            }

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0x335DD62C) else Color(0x20FFFFFF),
                        border = BorderStroke(1.dp, if (isSelected) VividGreen else GlassBorder),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) VividGreen else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            // Products Grid / List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("sell_product_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val cartItem = cart.items[product.id]
                    val inCartQty = cartItem?.qty ?: 0L
                    val isOutOfStock = product.stock <= 0L
                    val canAddMore = inCartQty < product.stock

                    SellProductTile(
                        product = product,
                        inCartQty = inCartQty,
                        canAddMore = canAddMore,
                        isOutOfStock = isOutOfStock,
                        currentLanguage = currentLanguage,
                        onAdd = { storeViewModel.addProductToCart(product) }
                    )
                }
            }
        }

        // Floating Cart Pill (items and total) opening Checkout
        if (cart.items.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(elevation = 14.dp, shape = RoundedCornerShape(29.dp))
                        .clip(RoundedCornerShape(29.dp))
                        .clickable { showCheckoutSheet = true }
                        .testTag("floating_cart_pill"),
                    shape = RoundedCornerShape(29.dp),
                    color = Color(0xF21F1F1F),
                    border = BorderStroke(1.5.dp, VividGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VividGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cart.totalItems.toString(),
                                    color = DarkBackground,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${cart.items.size} Items in Cart",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    text = ShopLocale.formatMoney(cart.grandTotal, currentLanguage),
                                    color = VividGreen,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Checkout",
                                color = VividGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = VividGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Checkout Modal Bottom Sheet
        if (showCheckoutSheet) {
            CheckoutBottomSheet(
                storeViewModel = storeViewModel,
                currentLanguage = currentLanguage,
                isBusy = isBusy,
                onDismiss = { showCheckoutSheet = false }
            )
        }
    }
}

@Composable
fun SellProductTile(
    product: Product,
    inCartQty: Long,
    canAddMore: Boolean,
    isOutOfStock: Boolean,
    currentLanguage: com.example.util.AppLanguage,
    onAdd: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "sell_tile_${product.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x20FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(product.category),
                    contentDescription = null,
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
                        text = ShopLocale.formatMoney(product.sellPrice, currentLanguage),
                        color = VividGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " • Stock: ${product.stock} ${product.unit}",
                        color = if (isOutOfStock) AlertRed else TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Add (+) Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (canAddMore) VividGreen else Color(0x33FFFFFF))
                    .clickable(enabled = canAddMore, onClick = onAdd)
                    .testTag("add_to_cart_${product.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (inCartQty > 0L) {
                    Text(
                        text = inCartQty.toString(),
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = if (canAddMore) DarkBackground else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    storeViewModel: StoreViewModel,
    currentLanguage: com.example.util.AppLanguage,
    isBusy: Boolean,
    onDismiss: () -> Unit
) {
    val cart by storeViewModel.cart.collectAsState()
    val products by storeViewModel.products.collectAsState()

    var discountInput by remember { mutableStateOf(if (cart.invoiceDiscount > 0.0) cart.invoiceDiscount.toString() else "") }
    var discountError by remember { mutableStateOf<String?>(null) }

    var selectedPaymentType by remember { mutableStateOf(cart.paymentType) } // "cash" or "baki"
    var customerNameInput by remember { mutableStateOf(cart.customerName) }
    var customerPhoneInput by remember { mutableStateOf(cart.customerPhone) }
    var notesInput by remember { mutableStateOf(cart.notes) }
    var completedReceipt by remember { mutableStateOf<CompletedSaleReceipt?>(null) }

    val allCustomerSummaries by storeViewModel.allCustomerSummaries.collectAsState()
    var showCustomerPicker by remember { mutableStateOf(false) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    if (showCustomerPicker) {
        CustomerPickerDialog(
            customerSummaries = allCustomerSummaries,
            currentLanguage = currentLanguage,
            onDismiss = { showCustomerPicker = false },
            onSelectCustomer = { cust ->
                customerNameInput = cust.name
                customerPhoneInput = cust.mobile
                storeViewModel.selectCustomerForCheckout(cust)
            },
            onSelectCashSale = {
                customerNameInput = "Cash Sale"
                customerPhoneInput = ""
                storeViewModel.setCashSaleForCheckout()
            },
            onAddNewCustomer = {
                showAddCustomerDialog = true
            }
        )
    }

    if (showAddCustomerDialog) {
        val existingCustomers by storeViewModel.customers.collectAsState()
        AddEditCustomerDialog(
            existingCustomers = existingCustomers,
            currentLanguage = currentLanguage,
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, mobile, address, notes ->
                storeViewModel.addCustomer(name, mobile, address, notes) { created ->
                    customerNameInput = created.name
                    customerPhoneInput = created.mobile
                    storeViewModel.selectCustomerForCheckout(created)
                    showAddCustomerDialog = false
                }
            }
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF181818),
        scrimColor = Color(0x99000000)
    ) {
        if (completedReceipt != null) {
            DigitalReceiptContent(
                receipt = completedReceipt!!,
                onDismiss = onDismiss,
                onStartNewSale = {
                    completedReceipt = null
                    onDismiss()
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .imePadding()
            ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Checkout (${cart.totalItems} items)",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cart Items List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cart.items.values.forEach { item ->
                    val prod = products.find { it.id == item.productId }
                    if (prod != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0x33262626),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${ShopLocale.formatMoney(item.sellPrice, currentLanguage)} each",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                // Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0x30FFFFFF), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { storeViewModel.updateCartItemQty(item.productId, prod, item.qty - 1L) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.qty <= 1L) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                            contentDescription = null,
                                            tint = if (item.qty <= 1L) AlertRed else TextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = item.qty.toString(),
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { storeViewModel.updateCartItemQty(item.productId, prod, item.qty + 1L) },
                                        enabled = item.qty < prod.stock,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = if (item.qty < prod.stock) VividGreen else TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = ShopLocale.formatMoney(item.lineTotal, currentLanguage),
                                    color = VividGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Discount Input (Strictly bounded!)
            GlassTextField(
                value = discountInput,
                onValueChange = { input ->
                    discountInput = input
                    val disc = input.toDoubleOrNull() ?: 0.0
                    if (disc > cart.subtotal) {
                        discountError = "Discount cannot exceed ৳${String.format(java.util.Locale.US, "%.0f", cart.subtotal)}"
                    } else if (disc < 0.0) {
                        discountError = "Discount cannot be negative"
                    } else {
                        discountError = null
                        storeViewModel.setInvoiceDiscount(disc)
                    }
                },
                label = "Discount (৳)",
                placeholder = "0.00",
                isError = discountError != null,
                errorMessage = discountError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                testTag = "checkout_discount_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Type: Paid in Full vs Baki (Credit)
            Text(
                text = "Payment Status",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedPaymentType == "cash") Color(0x335DD62C) else Color(0x20FFFFFF),
                    border = BorderStroke(1.dp, if (selectedPaymentType == "cash") VividGreen else GlassBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedPaymentType = "cash"
                            storeViewModel.setPaymentType("cash")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = if (selectedPaymentType == "cash") VividGreen else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Paid in Full (Cash)",
                            color = if (selectedPaymentType == "cash") VividGreen else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedPaymentType == "baki") Color(0x33FFA000) else Color(0x20FFFFFF),
                    border = BorderStroke(1.dp, if (selectedPaymentType == "baki") Color(0xFFFFB300) else GlassBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedPaymentType = "baki"
                            storeViewModel.setPaymentType("baki")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (selectedPaymentType == "baki") Color(0xFFFFB300) else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Baki (Credit)",
                            color = if (selectedPaymentType == "baki") Color(0xFFFFB300) else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 3: Connect the Checkout customer picker to real customers
            val isCashSale = customerNameInput.trim().equals("Cash Sale", ignoreCase = true) || customerNameInput.isBlank()
            val isBakiBlockedForCash = selectedPaymentType == "baki" && isCashSale

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isBakiBlockedForCash) Color(0x33FF3B30) else Color(0x20FFFFFF),
                border = BorderStroke(1.dp, if (isBakiBlockedForCash) AlertRed else GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomerPicker = true }
                    .testTag("checkout_customer_selector_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isCashSale) Color(0x20FFFFFF) else Color(0x335DD62C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCashSale) Icons.Default.Person else Icons.Default.People,
                                contentDescription = null,
                                tint = if (isCashSale) TextSecondary else VividGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isCashSale) "Customer: Cash Sale" else "Customer: $customerNameInput",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!isCashSale && customerPhoneInput.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = customerPhoneInput,
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x205DD62C)
                    ) {
                        Text(
                            text = if (isCashSale) "Select" else "Change",
                            color = VividGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1
                        )
                    }
                }
            }

            // Warning message if Baki is selected with Cash Sale
            if (isBakiBlockedForCash) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Choose a customer for Baki",
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes
            GlassTextField(
                value = notesInput,
                onValueChange = {
                    notesInput = it
                    storeViewModel.setNotes(it)
                },
                label = "Notes (Optional)",
                placeholder = "Add special instructions or delivery details...",
                leadingIcon = Icons.Default.Note,
                testTag = "checkout_notes"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Totals Review Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0x33202020),
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = TextSecondary, fontSize = 13.sp)
                        Text(ShopLocale.formatMoney(cart.subtotal, currentLanguage), color = TextPrimary, fontSize = 13.sp)
                    }
                    if (cart.totalDiscount > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount", color = AlertRed, fontSize = 13.sp)
                            Text("- " + ShopLocale.formatMoney(cart.totalDiscount, currentLanguage), color = AlertRed, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color(0x20FFFFFF))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Grand Total", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Profit: ${ShopLocale.formatMoney(cart.projectedProfit, currentLanguage)}", color = VividGreen, fontSize = 11.sp)
                        }
                        Text(
                            text = ShopLocale.formatMoney(cart.grandTotal, currentLanguage),
                            color = VividGreen,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            val checkoutError by storeViewModel.errorMessage.collectAsState()
            if (checkoutError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AlertRedContainer,
                    border = BorderStroke(1.dp, AlertRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = checkoutError ?: "",
                            color = AlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Slide to Confirm Sale Component
            val isBakiBlocked = selectedPaymentType == "baki" && (customerNameInput.trim().equals("Cash Sale", ignoreCase = true) || customerNameInput.isBlank())
            val isSliderAllowed = (discountError == null) && (cart.items.isNotEmpty()) && !isBusy && !isBakiBlocked

            SlideToConfirmSale(
                onConfirmed = {
                    storeViewModel.checkoutCurrentCart { receipt ->
                        completedReceipt = receipt
                    }
                },
                enabled = isSliderAllowed,
                text = if (selectedPaymentType == "baki") "Slide to confirm Baki sale" else "Slide to confirm sale"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}
