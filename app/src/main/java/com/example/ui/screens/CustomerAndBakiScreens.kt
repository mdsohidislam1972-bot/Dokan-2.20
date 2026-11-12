package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Customer
import com.example.domain.CalculationEngine
import com.example.domain.CustomerBalanceSummary
import com.example.domain.CustomerLedgerEntry
import com.example.domain.LedgerEntryType
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.SearchGlassField
import com.example.ui.components.VividButton
import com.example.ui.theme.AlertRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.BusinessSubScreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Baki & Customers Screen placed inside the Business tab.
 * Allows switching between Baki Debtor overview and All Customers list,
 * inspecting customer ledger, recording payments, and sharing payment reminders.
 */
@Composable
fun BakiMainScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val subScreen by storeViewModel.businessSubScreen.collectAsState()
    val selectedCustomer by storeViewModel.selectedCustomer.collectAsState()

    when {
        subScreen == BusinessSubScreen.CUSTOMER_DETAILS && selectedCustomer != null -> {
            CustomerDetailsScreen(
                storeViewModel = storeViewModel,
                customer = selectedCustomer!!,
                onBack = { storeViewModel.closeCustomerDetails() }
            )
        }
        else -> {
            BakiHubScreen(storeViewModel = storeViewModel, modifier = modifier)
        }
    }
}

/**
 * Hub screen with tab switch between Baki debtors and All Customers.
 */
@Composable
fun BakiHubScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val bakiOverview by storeViewModel.bakiOverview.collectAsState()
    val allCustomerSummaries by storeViewModel.allCustomerSummaries.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Baki (Due), 1: All Customers
    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    val filteredDebtors = remember(bakiOverview.debtors, searchQuery) {
        if (searchQuery.isBlank()) bakiOverview.debtors
        else bakiOverview.debtors.filter {
            it.customer.name.contains(searchQuery, ignoreCase = true) ||
                    it.customer.mobile.contains(searchQuery)
        }
    }

    val filteredAllCustomers = remember(allCustomerSummaries, searchQuery) {
        if (searchQuery.isBlank()) allCustomerSummaries
        else allCustomerSummaries.filter {
            it.customer.name.contains(searchQuery, ignoreCase = true) ||
                    it.customer.mobile.contains(searchQuery)
        }
    }

    if (showAddCustomerDialog) {
        val existingCustomers by storeViewModel.customers.collectAsState()
        AddEditCustomerDialog(
            existingCustomers = existingCustomers,
            currentLanguage = currentLanguage,
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, mobile, address, notes ->
                storeViewModel.addCustomer(name, mobile, address, notes) {
                    showAddCustomerDialog = false
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("baki_hub_screen")
    ) {
        // Top Switcher: Baki (Due) vs All Customers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (activeTab == 0) Color(0x33FFA000) else Color(0x20FFFFFF),
                border = BorderStroke(1.dp, if (activeTab == 0) Color(0xFFFFB300) else GlassBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeTab = 0 }
                    .testTag("baki_tab_btn")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = if (activeTab == 0) Color(0xFFFFB300) else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "বাকি খাতা (${bakiOverview.debtorCount})"
                        else "Baki Due (${bakiOverview.debtorCount})",
                        color = if (activeTab == 0) Color(0xFFFFB300) else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (activeTab == 1) Color(0x335DD62C) else Color(0x20FFFFFF),
                border = BorderStroke(1.dp, if (activeTab == 1) VividGreen else GlassBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeTab = 1 }
                    .testTag("customers_tab_btn")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = if (activeTab == 1) VividGreen else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "সকল কাস্টমার" else "All Customers",
                        color = if (activeTab == 1) VividGreen else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Search Bar & Add Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchGlassField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                label = if (activeTab == 0) "Search debtors by name or mobile..." else "Search customers...",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    @Composable {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                } else null,
                testTag = "baki_search_input"
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showAddCustomerDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0x335DD62C)),
                content = {
                    Icon(Icons.Default.Add, contentDescription = "Add Customer", tint = VividGreen)
                }
            )
        }

        // Main Content List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (activeTab == 0) {
                // Step 4: Summary card: how many customers owe, total amount owed (from CalculationEngine)
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("baki_summary_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "মোট পাওনা বাকি" else "Total Amount Owed",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ShopLocale.formatMoney(bakiOverview.totalBakiOwed, currentLanguage),
                                    color = if (bakiOverview.totalBakiOwed > 0.0) Color(0xFFFFB300) else VividGreen,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0x33FFA000),
                                border = BorderStroke(1.dp, Color(0x66FFA000))
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "${bakiOverview.debtorCount} জন দেনাদার"
                                    else "${bakiOverview.debtorCount} Customers Owe",
                                    color = Color(0xFFFFB300),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (filteredDebtors.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = VividGreen,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "বর্তমানে কোনো বাকি নেই" else "Nobody owes any Baki right now",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "কাস্টমারদের বাকিতে বিক্রয় করলে এখানে তাদের তালিকা দেখা যাবে।"
                                    else "When sales are recorded as Baki, debtors will appear here.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDebtors, key = { it.customer.id.ifBlank { it.customer.name } }) { debtor ->
                        CustomerRowCard(
                            summary = debtor,
                            currentLanguage = currentLanguage,
                            onClick = { storeViewModel.openCustomerDetails(debtor.customer) }
                        )
                    }
                }
            } else {
                // All Customers Tab (Step 8)
                if (filteredAllCustomers.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "কোনো কাস্টমার পাওয়া যায়নি" else "No customers found",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "উপরে '+' বাটনে চাপ দিয়ে নতুন কাস্টমার যোগ করুন।"
                                    else "Tap '+' above to add your first customer.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredAllCustomers, key = { it.customer.id.ifBlank { it.customer.name } }) { custSummary ->
                        CustomerRowCard(
                            summary = custSummary,
                            currentLanguage = currentLanguage,
                            onClick = { storeViewModel.openCustomerDetails(custSummary.customer) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card representing a customer row with their balance.
 */
@Composable
fun CustomerRowCard(
    summary: CustomerBalanceSummary,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    val cust = summary.customer
    val hasOwed = summary.currentBalance > 0.001

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        testTag = "customer_card_${cust.id.ifBlank { cust.name }}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (hasOwed) Color(0x33FFA000) else Color(0x265DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (hasOwed) Color(0xFFFFB300) else VividGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = cust.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cust.mobile.ifBlank { if (currentLanguage == AppLanguage.BN) "মোবাইল নেই" else "No phone" },
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ShopLocale.formatMoney(summary.currentBalance, currentLanguage),
                    color = if (hasOwed) Color(0xFFFFB300) else VividGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasOwed) (if (currentLanguage == AppLanguage.BN) "বাকি" else "Due")
                    else (if (currentLanguage == AppLanguage.BN) "পরিশোধিত" else "Paid"),
                    color = if (hasOwed) Color(0xFFFFB300) else VividGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Step 5: Customer details screen.
 * Shows customer name, mobile, address, current balance (big, green when zero, amber when owed),
 * total purchases, last purchase date, and a ledger (newest first) with every Baki sale and payment,
 * plus running balance after each line!
 */
@Composable
fun CustomerDetailsScreen(
    storeViewModel: StoreViewModel,
    customer: Customer,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val store by storeViewModel.store.collectAsState()
    val summary by storeViewModel.selectedCustomerSummary.collectAsState()
    val ledger by storeViewModel.selectedCustomerLedger.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCannotDeleteMessage by remember { mutableStateOf(false) }

    val activeSummary = summary ?: CustomerBalanceSummary(
        customer = customer,
        totalBakiSales = 0.0,
        totalPayments = 0.0,
        currentBalance = customer.currentBalance,
        totalPurchases = 0.0,
        lastPurchaseDate = null
    )

    BackHandler { onBack() }

    if (showPaymentDialog) {
        ReceivePaymentDialog(
            customer = activeSummary.customer,
            currentBalance = activeSummary.currentBalance,
            currentLanguage = currentLanguage,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, note, dateMillis ->
                storeViewModel.recordCustomerPayment(
                    customer = activeSummary.customer,
                    amount = amount,
                    note = note,
                    paymentDate = dateMillis
                ) {
                    showPaymentDialog = false
                }
            }
        )
    }

    if (showReminderDialog) {
        ShareReminderDialog(
            customerName = activeSummary.customer.name,
            customerPhone = activeSummary.customer.mobile,
            amountOwed = activeSummary.currentBalance,
            storeName = store?.name ?: "StoreOS",
            storePhone = store?.phone ?: "",
            currentLanguage = currentLanguage,
            onDismiss = { showReminderDialog = false }
        )
    }

    if (showEditDialog) {
        val existingCustomers by storeViewModel.customers.collectAsState()
        AddEditCustomerDialog(
            customerToEdit = activeSummary.customer,
            existingCustomers = existingCustomers,
            currentLanguage = currentLanguage,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, mobile, address, notes ->
                storeViewModel.updateCustomer(
                    activeSummary.customer.copy(
                        name = name,
                        mobile = mobile,
                        address = address,
                        notes = notes
                    )
                ) {
                    showEditDialog = false
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "কাস্টমার মুছে ফেলবেন?" else "Delete Customer?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (currentLanguage == AppLanguage.BN)
                        "'${activeSummary.customer.name}' কে স্থায়ীভাবে মুছে ফেলতে চান?"
                    else "Are you sure you want to delete customer '${activeSummary.customer.name}'?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        storeViewModel.deleteCustomer(activeSummary.customer) {
                            onBack()
                        }
                    }
                ) {
                    Text(if (currentLanguage == AppLanguage.BN) "মুছে ফেলুন" else "Delete", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(if (currentLanguage == AppLanguage.BN) "বাতিল" else "Cancel", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    if (showCannotDeleteMessage) {
        AlertDialog(
            onDismissRequest = { showCannotDeleteMessage = false },
            title = {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "মুছে ফেলা যাবে না" else "Cannot Delete",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (currentLanguage == AppLanguage.BN)
                        "এই কাস্টমারের পূর্বের বিক্রয় বা বাকি ব্যালেন্স রয়েছে। শুধুমাত্র ব্যালেন্স শূন্য এবং কোনো বিক্রয় না থাকলে মুছে ফেলা সম্ভব।"
                    else "This customer has sales or an outstanding balance. You can only delete a customer when their balance is zero and they have no sales.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showCannotDeleteMessage = false }) {
                    Text("OK", color = VividGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("customer_details_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x20FFFFFF))
                    .testTag("customer_details_back_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activeSummary.customer.name,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = activeSummary.customer.mobile.ifBlank { "No phone" },
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            val isCashSale = activeSummary.customer.name.equals("Cash Sale", ignoreCase = true)
            if (!isCashSale) {
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x20FFFFFF))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (CalculationEngine.canDeleteCustomer(activeSummary)) {
                            showDeleteConfirmDialog = true
                        } else {
                            showCannotDeleteMessage = true
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x20FFFFFF))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed, modifier = Modifier.size(18.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Big Balance Card (Green when zero, amber when owed)
            item {
                val hasOwed = activeSummary.currentBalance > 0.001
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_balance_card")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) "বর্তমান বকেয়া বাকি" else "Current Balance Owed",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (hasOwed) Color(0x33FFA000) else Color(0x265DD62C)
                            ) {
                                Text(
                                    text = if (hasOwed) (if (currentLanguage == AppLanguage.BN) "বাকি আছে" else "Due")
                                    else (if (currentLanguage == AppLanguage.BN) "পরিশোধিত" else "All Clear"),
                                    color = if (hasOwed) Color(0xFFFFB300) else VividGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ShopLocale.formatMoney(activeSummary.currentBalance, currentLanguage),
                            color = if (hasOwed) Color(0xFFFFB300) else VividGreen,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0x1FFFFFFF))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Total purchases & last purchase date (Step 8)
                        val lastDateStr = activeSummary.lastPurchaseDate?.let {
                            SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(it))
                        } ?: (if (currentLanguage == AppLanguage.BN) "নেই" else "None")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "মোট ক্রয়" else "Total Purchases",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = ShopLocale.formatMoney(activeSummary.totalPurchases, currentLanguage),
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.BN) "সর্বশেষ ক্রয়" else "Last Purchase",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = lastDateStr,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons: Receive Payment, Call, Share Reminder, Edit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VividButton(
                        text = if (currentLanguage == AppLanguage.BN) "জমা নিন" else "Receive Payment",
                        onClick = { showPaymentDialog = true },
                        icon = Icons.Default.CallReceived,
                        enabled = activeSummary.currentBalance > 0.001,
                        modifier = Modifier.weight(1.3f),
                        testTag = "receive_payment_btn"
                    )

                    GlassOutlinedButton(
                        text = if (currentLanguage == AppLanguage.BN) "কল" else "Call",
                        onClick = {
                            if (activeSummary.customer.mobile.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeSummary.customer.mobile}"))
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, "No mobile number provided", Toast.LENGTH_SHORT).show()
                            }
                        },
                        icon = Icons.Default.Call,
                        modifier = Modifier.weight(0.9f),
                        testTag = "call_customer_btn"
                    )

                    GlassOutlinedButton(
                        text = if (currentLanguage == AppLanguage.BN) "রিমাইন্ডার" else "Reminder",
                        onClick = { showReminderDialog = true },
                        icon = Icons.Default.Share,
                        enabled = activeSummary.currentBalance > 0.001,
                        modifier = Modifier.weight(1f),
                        testTag = "share_reminder_btn"
                    )
                }
            }

            // Ledger Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = VividGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "খতিয়ান বিবরণ (লেজার)" else "Ledger History",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${ledger.size} " + if (ledger.size == 1) "entry" else "entries",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (ledger.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "এই কাস্টমারের কোনো বাকির খতিয়ান নেই।"
                            else "No ledger entries yet. Credit sales and payments will be listed here.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                items(ledger, key = { it.id }) { entry ->
                    LedgerEntryCard(entry = entry, currentLanguage = currentLanguage)
                }
            }
        }
    }
}

/**
 * Single ledger entry displaying Baki sale or Payment In, with running balance after each line.
 */
@Composable
fun LedgerEntryCard(
    entry: CustomerLedgerEntry,
    currentLanguage: AppLanguage
) {
    val isSale = entry.type == LedgerEntryType.BAKI_SALE
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }
    val formattedDate = remember(entry.timestamp) {
        if (entry.timestamp > 0L) dateFormat.format(Date(entry.timestamp)) else "Recently"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "ledger_row_${entry.referenceNo}"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSale) Color(0x33FFA000) else Color(0x265DD62C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSale) Icons.Default.Receipt else Icons.Default.Payment,
                            contentDescription = null,
                            tint = if (isSale) Color(0xFFFFB300) else VividGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.referenceNo,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSale) Color(0x33FFA000) else Color(0x265DD62C)
                            ) {
                                Text(
                                    text = if (isSale) (if (currentLanguage == AppLanguage.BN) "বাকি বিক্রয়" else "Baki Sale")
                                    else (if (currentLanguage == AppLanguage.BN) "জমা (পরিশোধ)" else "Payment In"),
                                    color = if (isSale) Color(0xFFFFB300) else VividGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                        Text(
                            text = formattedDate,
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                // Amount
                Text(
                    text = (if (isSale) "- " else "+ ") + ShopLocale.formatMoney(entry.amount, currentLanguage),
                    color = if (isSale) Color(0xFFFFB300) else VividGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${entry.note}",
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0x14FFFFFF))
            Spacer(modifier = Modifier.height(4.dp))

            // Running balance after this transaction line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "অবশিষ্ট বাকি ব্যালেন্স:" else "Running Balance:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = ShopLocale.formatMoney(entry.runningBalance, currentLanguage),
                    color = if (entry.runningBalance > 0.0) Color(0xFFFFB300) else VividGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Step 6: Receive Payment (Payment In) Dialog.
 * Amount, date, note, method is Cash for now.
 * Blocks an amount above customer's current balance with a clear message.
 */
@Composable
fun ReceivePaymentDialog(
    customer: Customer,
    currentBalance: Double,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, Long) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "বাকি জমা গ্রহণ (Payment In)" else "Receive Payment (Payment In)",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${customer.name} • ${if (currentLanguage == AppLanguage.BN) "বাকি" else "Due"}: ${ShopLocale.formatMoney(currentBalance, currentLanguage)}",
                    color = Color(0xFFFFB300),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        val parsed = it.toDoubleOrNull() ?: 0.0
                        errorMessage = if (parsed > (currentBalance + 0.001)) {
                            if (currentLanguage == AppLanguage.BN) "জমার পরিমাণ বর্তমান বকেয়া ৳${currentBalance.toInt()} এর বেশি হতে পারবে না"
                            else "Amount cannot exceed outstanding balance of ৳${currentBalance.toInt()}"
                        } else null
                    },
                    label = if (currentLanguage == AppLanguage.BN) "জমার পরিমাণ (৳)" else "Payment Amount (৳)",
                    placeholder = "0",
                    leadingIcon = Icons.Default.Payment,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "receive_payment_amount_input"
                )

                // Quick presets: Full due and Half due
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x205DD62C),
                        border = BorderStroke(1.dp, VividGreen),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                amountInput = currentBalance.toInt().toString()
                                errorMessage = null
                            }
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "সম্পূর্ণ (৳${currentBalance.toInt()})"
                            else "Full (৳${currentBalance.toInt()})",
                            color = VividGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp),
                            maxLines = 1
                        )
                    }

                    if (currentBalance >= 20.0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x20FFFFFF),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    amountInput = (currentBalance / 2.0).toInt().toString()
                                    errorMessage = null
                                }
                        ) {
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) "অর্ধেক (৳${(currentBalance / 2.0).toInt()})"
                                else "Half (৳${(currentBalance / 2.0).toInt()})",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = if (currentLanguage == AppLanguage.BN) "নোট (ঐচ্ছিক)" else "Note (Optional)",
                    placeholder = "e.g. Cash received by owner",
                    leadingIcon = Icons.Default.Note,
                    testTag = "receive_payment_note_input"
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "পেমেন্ট মাধ্যম:" else "Payment Method:",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0x335DD62C)
                    ) {
                        Text(
                            text = "Cash",
                            color = VividGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val parsedAmt = amountInput.toDoubleOrNull() ?: 0.0
                val isValid = parsedAmt > 0.0 && parsedAmt <= (currentBalance + 0.001)

                VividButton(
                    text = if (currentLanguage == AppLanguage.BN) "জমা নিশ্চিত করুন" else "Confirm Payment",
                    onClick = {
                        if (isValid) {
                            onConfirm(parsedAmt, noteInput, System.currentTimeMillis())
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_receive_payment_btn"
                )
            }
        }
    }
}

/**
 * Step 7: Share Reminder Dialog.
 * Builds polite reminder message in Bangla or English with language switch.
 * Shares through the phone's standard share sheet.
 */
@Composable
fun ShareReminderDialog(
    customerName: String,
    customerPhone: String,
    amountOwed: Double,
    storeName: String,
    storePhone: String,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isBangla by remember { mutableStateOf(currentLanguage == AppLanguage.BN) }

    val formattedAmount = "৳${amountOwed.toInt()}"

    val reminderMessage = remember(isBangla, customerName, amountOwed, storeName, storePhone) {
        if (isBangla) {
            "প্রিয় $customerName,\n" +
                    "$storeName ${if (storePhone.isNotBlank()) "($storePhone) " else ""}থেকে বিনীতভাবে জানানো যাচ্ছে যে আপনার বকেয়া বাকি রয়েছে $formattedAmount। " +
                    "অনুগ্রহ করে সুবিধাজনক সময়ে পরিশোধ করবেন।\nধন্যবাদ!"
        } else {
            "Dear $customerName,\n" +
                    "This is a polite reminder from $storeName ${if (storePhone.isNotBlank()) "($storePhone) " else ""}" +
                    "regarding your outstanding due balance of $formattedAmount. " +
                    "Kindly settle this at your earliest convenience.\nThank you!"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "বাকি রিমাইন্ডার শেয়ার" else "Share Payment Reminder",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Language switch inside dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isBangla) Color(0x335DD62C) else Color(0x20FFFFFF),
                        border = BorderStroke(1.dp, if (!isBangla) VividGreen else GlassBorder),
                        modifier = Modifier.clickable { isBangla = false }
                    ) {
                        Text(
                            text = "English",
                            color = if (!isBangla) VividGreen else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isBangla) Color(0x335DD62C) else Color(0x20FFFFFF),
                        border = BorderStroke(1.dp, if (isBangla) VividGreen else GlassBorder),
                        modifier = Modifier.clickable { isBangla = true }
                    ) {
                        Text(
                            text = "বাংলা",
                            color = if (isBangla) VividGreen else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Message Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x33202020),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reminderMessage,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassOutlinedButton(
                        text = if (isBangla) "কপি করুন" else "Copy Text",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Baki Reminder", reminderMessage)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, if (isBangla) "মেসেজ কপি করা হয়েছে" else "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "copy_reminder_btn"
                    )

                    VividButton(
                        text = if (isBangla) "শেয়ার করুন" else "Share via...",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, reminderMessage)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Reminder via"))
                        },
                        icon = Icons.Default.Share,
                        modifier = Modifier.weight(1.2f),
                        testTag = "send_share_reminder_btn"
                    )
                }
            }
        }
    }
}

/**
 * Step 2: Add Customer and Edit Customer screen/dialog.
 * Name is required. Warns if mobile already exists.
 * On phone app: adds "Pick from phone contacts" (asks permission first).
 */
@Composable
fun AddEditCustomerDialog(
    customerToEdit: Customer? = null,
    existingCustomers: List<Customer> = emptyList(),
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (name: String, mobile: String, address: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var nameInput by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var mobileInput by remember { mutableStateOf(customerToEdit?.mobile ?: "") }
    var addressInput by remember { mutableStateOf(customerToEdit?.address ?: "") }
    var notesInput by remember { mutableStateOf(customerToEdit?.notes ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isEditing = customerToEdit != null

    // Check if device supports phone contacts intent (hidden on web)
    val isContactsSupported = remember(context) {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
    }

    // Check for duplicate mobile number
    val cleanMobile = remember(mobileInput) { mobileInput.trim().replace(" ", "").replace("-", "") }
    val duplicateMobileExists = remember(cleanMobile, existingCustomers, customerToEdit) {
        if (cleanMobile.length >= 6) {
            existingCustomers.any {
                it.id != customerToEdit?.id &&
                it.mobile.trim().replace(" ", "").replace("-", "") == cleanMobile
            }
        } else false
    }

    // Contact picker launcher
    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            try {
                val cursor = context.contentResolver.query(contactUri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val pickedName = it.getString(nameIndex)
                            if (!pickedName.isNullOrBlank()) {
                                nameInput = pickedName
                                nameError = null
                            }
                        }

                        val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                        val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        if (idIndex != -1 && hasPhoneIndex != -1) {
                            val contactId = it.getString(idIndex)
                            val hasPhone = it.getInt(hasPhoneIndex)
                            if (hasPhone > 0) {
                                val phoneCursor = context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    arrayOf(contactId),
                                    null
                                )
                                phoneCursor?.use { pCursor ->
                                    if (pCursor.moveToFirst()) {
                                        val pNumIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        if (pNumIndex != -1) {
                                            val phone = pCursor.getString(pNumIndex)
                                            if (!phone.isNullOrBlank()) {
                                                mobileInput = phone.replace(" ", "").replace("-", "")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not read contact: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for READ_CONTACTS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pickContactLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permission to read contacts is required to pick from contacts.", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) (if (currentLanguage == AppLanguage.BN) "কাস্টমার সম্পাদনা" else "Edit Customer")
                        else (if (currentLanguage == AppLanguage.BN) "নতুন কাস্টমার যোগ করুন" else "Add New Customer"),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // "Pick from phone contacts" button (on phone app only, hidden on web)
                if (isContactsSupported) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x205DD62C),
                        border = BorderStroke(1.dp, Color(0x665DD62C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                            .testTag("pick_from_contacts_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = VividGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) "ফোন কনট্যাক্ট থেকে নির্বাচন করুন" else "Pick from phone contacts",
                                color = VividGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                GlassTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        nameError = if (it.trim().isBlank()) "Name is required" else null
                    },
                    label = if (currentLanguage == AppLanguage.BN) "কাস্টমারের নাম *" else "Customer Name *",
                    placeholder = "e.g. Rahim Ahmed",
                    leadingIcon = Icons.Default.Person,
                    testTag = "customer_name_input"
                )

                if (nameError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(nameError ?: "", color = AlertRed, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = mobileInput,
                    onValueChange = { mobileInput = it },
                    label = if (currentLanguage == AppLanguage.BN) "মোবাইল নম্বর (ঐচ্ছিক)" else "Mobile Number (Optional)",
                    placeholder = "017...",
                    leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    testTag = "customer_mobile_input"
                )

                if (duplicateMobileExists) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "সতর্কতা: এই মোবাইল নম্বরে ইতিমধ্যে একজন কাস্টমার রয়েছে"
                            else "Warning: Another customer already has this mobile number",
                            color = Color(0xFFFFB300),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = if (currentLanguage == AppLanguage.BN) "ঠিকানা (ঐচ্ছিক)" else "Address (Optional)",
                    placeholder = "e.g. Mirpur, Dhaka",
                    leadingIcon = Icons.Default.LocationOn,
                    testTag = "customer_address_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = if (currentLanguage == AppLanguage.BN) "নোট (ঐচ্ছিক)" else "Notes (Optional)",
                    placeholder = "Special notes about this customer...",
                    leadingIcon = Icons.Default.Note,
                    testTag = "customer_notes_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                VividButton(
                    text = if (isEditing) (if (currentLanguage == AppLanguage.BN) "সংরক্ষণ করুন" else "Update Customer")
                    else (if (currentLanguage == AppLanguage.BN) "যোগ করুন" else "Save Customer"),
                    onClick = {
                        if (nameInput.trim().isBlank()) {
                            nameError = "Customer name is required"
                            return@VividButton
                        }
                        onConfirm(nameInput.trim(), mobileInput.trim(), addressInput.trim(), notesInput.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_save_customer_btn"
                )
            }
        }
    }
}

/**
 * Step 3: Checkout customer selector dialog.
 * Search by name or mobile, "+ Add new customer" inside picker, Cash Sale option.
 */
@Composable
fun CustomerPickerDialog(
    customerSummaries: List<CustomerBalanceSummary>,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelectCustomer: (Customer) -> Unit,
    onSelectCashSale: () -> Unit,
    onAddNewCustomer: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(customerSummaries, searchQuery) {
        if (searchQuery.isBlank()) customerSummaries
        else customerSummaries.filter {
            it.customer.name.contains(searchQuery, ignoreCase = true) ||
                    it.customer.mobile.contains(searchQuery)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "কাস্টমার নির্বাচন করুন" else "Select Customer",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SearchGlassField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    label = "Search by name or mobile...",
                    leadingIcon = Icons.Default.Search,
                    testTag = "picker_search_input"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons: Cash Sale & Add New Customer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x20FFFFFF),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onSelectCashSale()
                                onDismiss()
                            }
                            .testTag("picker_cash_sale_option")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cash Sale", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x335DD62C),
                        border = BorderStroke(1.dp, VividGreen),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onDismiss()
                                onAddNewCustomer()
                            }
                            .testTag("picker_add_new_customer_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = VividGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ New Customer", color = VividGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered, key = { it.customer.id.ifBlank { it.customer.name } }) { summary ->
                        val cust = summary.customer
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x261E1E1E),
                            border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCustomer(cust)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cust.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    if (cust.mobile.isNotBlank()) {
                                        Text(cust.mobile, color = TextMuted, fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                                if (summary.currentBalance > 0.0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = ShopLocale.formatMoney(summary.currentBalance, currentLanguage),
                                            color = Color(0xFFFFB300),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("Due", color = Color(0xFFFFB300), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
