package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountStatementEntry
import com.example.data.model.AccountStatementReport
import com.example.ui.components.ExportActionBar
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.PlainEmptyState
import com.example.ui.components.VividButton
import com.example.ui.components.getAccountIcon
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.BusinessNavDestination
import com.example.ui.viewmodel.HistoryPeriod
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ReportExportUtil
import com.example.util.ReportTableData
import com.example.util.ShopLocale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Step 2: Cash & Bank screen
 * Reachable from Business tab and More menu.
 * - Overall balance at top (all accounts together)
 * - Accounts list with each balance, tap opens account
 * - "New Account" button
 * - Plain empty message if only default account
 */
@Composable
fun CashAndBankScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by storeViewModel.accounts.collectAsState()
    val balances by storeViewModel.accountBalances.collectAsState()
    val totalBalance by storeViewModel.totalBalance.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()

    var showNewAccountDialog by remember { mutableStateOf(false) }

    val activeAccounts = remember(accounts) {
        accounts.filter { !it.isArchived }
    }
    val nonDefaultCount = activeAccounts.count { !it.isDefault && it.id != "cash_in_hand" }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
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
                    modifier = Modifier.testTag("cash_bank_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "ক্যাশ ও ব্যাংক (Cash & Bank)" else "Cash & Bank",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${activeAccounts.size} active accounts",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }
                VividButton(
                    text = "+ New Account",
                    onClick = { showNewAccountDialog = true },
                    icon = Icons.Default.Add,
                    testTag = "btn_new_account"
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Overall Balance Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overall_balance_card")
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
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = VividGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.BN) "সর্বমোট স্থিতি" else "Total Balance",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "All accounts combined",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = ShopLocale.formatMoney(totalBalance, currentLanguage),
                            color = VividGreen,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Plain empty message if only default account
            if (nonDefaultCount == 0) {
                item {
                    PlainEmptyState(
                        message = if (currentLanguage == AppLanguage.BN)
                            "বর্তমানে শুধুমাত্র ডিফল্ট 'Cash in Hand' অ্যাকাউন্ট রয়েছে। নতুন ব্যাংক অ্যাকাউন্ট বা মোবাইল ওয়ালেট যোগ করতে উপরে '+ New Account' চাপুন।"
                        else
                            "Only default 'Cash in Hand' account present. Tap '+ New Account' to add a bank account or mobile wallet."
                    )
                }
            }

            // Accounts List Header
            item {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "অ্যাকাউন্ট তালিকা" else "Accounts List",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Accounts list items
            items(activeAccounts, key = { it.id }) { acc ->
                val accBalance = balances[acc.id] ?: acc.openingBalance
                AccountRowCard(
                    account = acc,
                    balance = accBalance,
                    currentLanguage = currentLanguage,
                    onClick = {
                        storeViewModel.selectAccount(acc)
                        storeViewModel.navigateInBusiness(BusinessNavDestination.ACCOUNT_DETAILS_SCREEN)
                    }
                )
            }
        }
    }

    if (showNewAccountDialog) {
        NewAccountDialog(
            accounts = accounts,
            currentLanguage = currentLanguage,
            onDismiss = { showNewAccountDialog = false },
            onConfirm = { name, type, accNumber, openingBal ->
                storeViewModel.createAccount(
                    name = name,
                    type = type,
                    accountNumber = accNumber,
                    openingBalance = openingBal,
                    onSuccess = { showNewAccountDialog = false }
                )
            }
        )
    }
}

@Composable
fun AccountRowCard(
    account: Account,
    balance: Double,
    currentLanguage: AppLanguage,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        testTag = "account_card_${account.id}"
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x265DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountIcon(account.type),
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (account.isDefault) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x335DD62C)
                            ) {
                                Text(
                                    text = "Default",
                                    color = VividGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = buildString {
                            append(account.type)
                            if (account.accountNumber.isNotBlank()) {
                                append(" • ")
                                append(account.accountNumber)
                            }
                        },
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ShopLocale.formatMoney(balance, currentLanguage),
                    color = if (balance >= 0) VividGreen else AlertRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = "Current Balance",
                    color = TextMuted,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

/**
 * Step 3: New Account Dialog
 * Choose Wallet or Bank, name, account ID, and current balance (opening balance).
 * Name required. Duplicate name blocked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAccountDialog(
    accounts: List<Account>,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, accNumber: String, openingBal: Double) -> Unit
) {
    var selectedType by remember { mutableStateOf("Bank") } // Bank or Wallet
    var name by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var openingBalanceText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.BN) "নতুন অ্যাকাউন্ট যোগ করুন" else "Add New Account",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Type selector: Bank or Wallet
                Text(
                    text = "Account Type",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Bank", "Wallet").forEach { typeOption ->
                        val isSelected = selectedType == typeOption
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0x335DD62C) else GlassSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) VividGreen else GlassBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedType = typeOption }
                                .padding(vertical = 10.dp)
                                .testTag("type_option_$typeOption"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (typeOption == "Bank") Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (isSelected) VividGreen else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = typeOption,
                                    color = if (isSelected) VividGreen else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        localError = null
                    },
                    label = if (selectedType == "Bank") "Bank Name (e.g. Dutch Bangla Bank)" else "Wallet Name (e.g. bKash / Nagad)",
                    placeholder = if (selectedType == "Bank") "Dutch Bangla Bank" else "bKash Merchant",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "input_account_name"
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = "Account / Phone Number (Optional)",
                    placeholder = "e.g. 151.110.1234 or 01711...",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "input_account_number"
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlassTextField(
                    value = openingBalanceText,
                    onValueChange = { openingBalanceText = it },
                    label = "Current / Opening Balance (৳)",
                    placeholder = "0.00",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    testTag = "input_opening_balance"
                )

                if (localError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localError ?: "",
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        testTag = "dialog_cancel_account"
                    )
                    VividButton(
                        text = "Create Account",
                        onClick = {
                            val trimmedName = name.trim()
                            if (trimmedName.isBlank()) {
                                localError = "Account name is required"
                                return@VividButton
                            }
                            if (accounts.any { !it.isArchived && it.name.trim().equals(trimmedName, ignoreCase = true) }) {
                                localError = "An account with this name already exists"
                                return@VividButton
                            }
                            val bal = openingBalanceText.toDoubleOrNull() ?: 0.0
                            onConfirm(trimmedName, selectedType, accountNumber.trim(), bal)
                        },
                        modifier = Modifier.weight(1.3f),
                        testTag = "dialog_confirm_account"
                    )
                }
            }
        }
    }
}

/**
 * Step 4: Account details and statement
 * Current balance, period filter, statement list (newest first) of every money-in/out line
 * with date, type (Sale, Payment In, Purchase, Payment Out, Expense, Other Income, Adjustment),
 * who or what it was for, amount in or out, and running balance.
 * Export buttons (PDF, Print, CSV, Share).
 * Step 5: Adjust Balance button opens AdjustBalanceDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val account = storeViewModel.selectedAccount.collectAsState().value
    val statement = storeViewModel.selectedAccountStatement.collectAsState().value
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val period by storeViewModel.historyPeriod.collectAsState()
    val customRange by storeViewModel.historyCustomDateRange.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val context = LocalContext.current
    var showAdjustDialog by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }
    val isOwner by storeViewModel.isOwner.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    if (account == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            PlainEmptyState(message = "No account selected")
        }
        return
    }

    val currentBalance = statement?.closingBalance ?: account.openingBalance

    if (showDatePicker) {
        com.example.ui.screens.CustomDateRangePickerDialog(
            initialRange = customRange,
            currentLanguage = currentLanguage,
            onDismiss = { showDatePicker = false },
            onConfirm = { start, end ->
                storeViewModel.setHistoryCustomDateRange(start, end)
                showDatePicker = false
            }
        )
    }

    fun buildStatementExportData(): ReportTableData {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        val rows = (statement?.entries ?: emptyList()).map { entry ->
            val dateStr = dateFormat.format(Date(entry.timestamp))
            val inStr = if (entry.moneyIn > 0.0) "+৳${"%.2f".format(entry.moneyIn)}" else "-"
            val outStr = if (entry.moneyOut > 0.0) "-৳${"%.2f".format(entry.moneyOut)}" else "-"
            val balStr = "৳${"%.2f".format(entry.runningBalance)}"
            listOf(dateStr, entry.type, entry.partyOrCategory, entry.note, inStr, outStr, balStr)
        }
        val rawNumbers = (statement?.entries ?: emptyList()).map { entry ->
            listOf<Double?>(
                null,
                null,
                null,
                null,
                if (entry.moneyIn > 0.0) entry.moneyIn else null,
                if (entry.moneyOut > 0.0) entry.moneyOut else null,
                entry.runningBalance
            )
        }
        val totalIn = statement?.totalMoneyIn ?: 0.0
        val totalOut = statement?.totalMoneyOut ?: 0.0
        val totals = listOf(
            "Totals",
            "",
            "",
            "",
            "+৳${"%.2f".format(totalIn)}",
            "-৳${"%.2f".format(totalOut)}",
            "Closing: ৳${"%.2f".format(currentBalance)}"
        )
        val rawTotals = listOf<Double?>(null, null, null, null, totalIn, totalOut, currentBalance)

        return ReportTableData(
            title = "Account Statement: ${account.name}",
            storeName = store?.name ?: "Store",
            storePhone = store?.phone ?: "",
            periodLabel = period.name,
            generatedAt = dateFormat.format(Date()),
            headers = listOf("Date", "Type", "Party / Category", "Note", "Money In", "Money Out", "Balance"),
            rows = rows,
            numericColumns = setOf(4, 5, 6),
            rawNumbers = rawNumbers,
            totals = totals,
            rawTotals = rawTotals,
            notes = "Account: ${account.name} (${account.type}). Opening: ৳${"%.2f".format(statement?.openingBalance ?: account.openingBalance)}"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Header
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
                    modifier = Modifier.testTag("account_details_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(account.type)
                            if (account.accountNumber.isNotBlank()) {
                                append(" • ")
                                append(account.accountNumber)
                            }
                        },
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }

                // Action buttons: Adjust & Archive (Owner only)
                if (isOwner) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassOutlinedButton(
                            text = "Adjust",
                            onClick = { showAdjustDialog = true },
                            icon = Icons.Default.Edit,
                            testTag = "btn_adjust_balance"
                        )

                        if (!account.isDefault && account.id != "cash_in_hand") {
                            IconButton(
                                onClick = {
                                    if (abs(currentBalance) > 0.001) {
                                        Toast.makeText(
                                            context,
                                            "Account can only be archived when balance is ৳0.00 (current: ৳${"%.2f".format(currentBalance)})",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        showArchiveConfirm = true
                                    }
                                },
                                modifier = Modifier.testTag("btn_archive_account")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = "Archive Account",
                                    tint = if (abs(currentBalance) <= 0.001) TextSecondary else TextMuted
                                )
                            }
                        }
                    }
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
            // Balance Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_statement_balance_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Balance",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0x265DD62C)
                            ) {
                                Text(
                                    text = account.type,
                                    color = VividGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = ShopLocale.formatMoney(currentBalance, currentLanguage),
                            color = if (currentBalance >= 0) VividGreen else AlertRed,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Period summary row: Money In vs Money Out
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Period In (+)",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = ShopLocale.formatMoney(statement?.totalMoneyIn ?: 0.0, currentLanguage),
                                    color = VividGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Period Out (-)",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = ShopLocale.formatMoney(statement?.totalMoneyOut ?: 0.0, currentLanguage),
                                    color = AlertRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Period Filter
            item {
                com.example.ui.screens.HistoryPeriodTabs(
                    currentPeriod = period,
                    customRange = customRange,
                    currentLanguage = currentLanguage,
                    onSelectPeriod = { selected ->
                        if (selected == HistoryPeriod.CUSTOM) {
                            showDatePicker = true
                        } else {
                            storeViewModel.setHistoryPeriod(selected)
                        }
                    }
                )
            }

            // Export Actions (PDF, Print, CSV, Share)
            item {
                ExportActionBar(
                    onDownloadPdf = {
                        val file = ReportExportUtil.generatePdfReport(
                            context,
                            buildStatementExportData(),
                            "Statement_${account.name}_${System.currentTimeMillis()}"
                        )
                        ReportExportUtil.downloadReportFile(context, file)
                    },
                    onPrint = {
                        val file = ReportExportUtil.generatePdfReport(
                            context,
                            buildStatementExportData(),
                            "Statement_${account.name}_${System.currentTimeMillis()}"
                        )
                        ReportExportUtil.printReportFile(context, file, "AccountStatement")
                    },
                    onExportCsv = {
                        val file = ReportExportUtil.generateCsvReport(
                            context,
                            buildStatementExportData(),
                            "Statement_${account.name}_${System.currentTimeMillis()}"
                        )
                        ReportExportUtil.downloadReportFile(context, file)
                    },
                    onShare = {
                        val file = ReportExportUtil.generatePdfReport(
                            context,
                            buildStatementExportData(),
                            "Statement_${account.name}_${System.currentTimeMillis()}"
                        )
                        ReportExportUtil.shareReportFile(
                            context,
                            file,
                            "application/pdf",
                            "Statement for ${account.name}"
                        )
                    }
                )
            }

            // Statement Timeline Items
            val entries = statement?.entries ?: emptyList()
            if (entries.isEmpty()) {
                item {
                    PlainEmptyState(message = "No transactions found in this period for this account")
                }
            } else {
                item {
                    Text(
                        text = "Transactions (${entries.size})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(entries, key = { it.id }) { entry ->
                    StatementEntryRow(
                        entry = entry,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }

    if (showAdjustDialog) {
        AdjustBalanceDialog(
            account = account,
            currentBalance = currentBalance,
            currentLanguage = currentLanguage,
            onDismiss = { showAdjustDialog = false },
            onConfirm = { newBal, note ->
                storeViewModel.adjustAccountBalance(
                    account = account,
                    newBalance = newBal,
                    note = note,
                    onSuccess = { showAdjustDialog = false }
                )
            }
        )
    }

    if (showArchiveConfirm) {
        BasicAlertDialog(onDismissRequest = { showArchiveConfirm = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Archive Account?",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Are you sure you want to archive '${account.name}'? It will no longer appear in account pickers.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassOutlinedButton(
                            text = "Cancel",
                            onClick = { showArchiveConfirm = false },
                            modifier = Modifier.weight(1f)
                        )
                        VividButton(
                            text = "Archive",
                            onClick = {
                                storeViewModel.archiveAccount(
                                    account = account,
                                    onSuccess = {
                                        showArchiveConfirm = false
                                        storeViewModel.navigateBackInBusiness()
                                    }
                                )
                            },
                            containerColor = AlertRed,
                            contentColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatementEntryRow(
    entry: AccountStatementEntry,
    currentLanguage: AppLanguage
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.US) }
    val isMoneyIn = entry.moneyIn > 0.0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "statement_entry_${entry.id}"
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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isMoneyIn) Color(0x265DD62C) else Color(0x26FF5252)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMoneyIn) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = if (isMoneyIn) VividGreen else AlertRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isMoneyIn) Color(0x335DD62C) else Color(0x33FF5252)
                        ) {
                            Text(
                                text = entry.type,
                                color = if (isMoneyIn) VividGreen else AlertRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        if (entry.partyOrCategory.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = entry.partyOrCategory,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (entry.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = entry.note,
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateFormat.format(Date(entry.timestamp)),
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val amt = if (isMoneyIn) entry.moneyIn else entry.moneyOut
                Text(
                    text = "${if (isMoneyIn) "+" else "-"}৳${"%.2f".format(amt)}",
                    color = if (isMoneyIn) VividGreen else AlertRed,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Bal: ৳${"%.2f".format(entry.runningBalance)}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Step 5: Adjust Balance Dialog
 * Enter real current balance. App calculates difference (up or down).
 * Note is required.
 * Confirmation step shows old balance, new balance, and difference before saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustBalanceDialog(
    account: Account,
    currentBalance: Double,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (newBalance: Double, note: String) -> Unit
) {
    var newBalanceText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val newBalanceParsed = newBalanceText.toDoubleOrNull()
    val difference = if (newBalanceParsed != null) newBalanceParsed - currentBalance else 0.0

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            color = DarkBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                if (!isConfirmStep) {
                    Text(
                        text = "Adjust Balance: ${account.name}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter actual physical balance. An adjustment entry will be saved.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Current recorded balance
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassSurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recorded Balance:",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = ShopLocale.formatMoney(currentBalance, currentLanguage),
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = newBalanceText,
                        onValueChange = {
                            newBalanceText = it
                            localError = null
                        },
                        label = "Real Current Balance (৳)",
                        placeholder = "e.g. 5000",
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        testTag = "input_real_balance"
                    )

                    if (newBalanceParsed != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val isUp = difference >= 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Adjustment Difference:",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${if (isUp) "+" else ""}৳${"%.2f".format(difference)} (${if (isUp) "Money In" else "Money Out"})",
                                color = if (isUp) VividGreen else AlertRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = note,
                        onValueChange = {
                            note = it
                            localError = null
                        },
                        label = "Adjustment Reason / Note (Required)",
                        placeholder = "e.g. Physical count reconciliation, bank fee correction",
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "input_adjust_note"
                    )

                    if (localError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localError ?: "",
                            color = AlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

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
                            text = "Review",
                            onClick = {
                                if (newBalanceParsed == null) {
                                    localError = "Please enter a valid real balance"
                                    return@VividButton
                                }
                                if (note.trim().isBlank()) {
                                    localError = "A note explaining the adjustment reason is required"
                                    return@VividButton
                                }
                                if (abs(difference) < 0.0001) {
                                    localError = "New balance is identical to the current balance"
                                    return@VividButton
                                }
                                isConfirmStep = true
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "btn_review_adjustment"
                        )
                    }
                } else {
                    // Confirmation Step
                    Text(
                        text = "Confirm Balance Adjustment",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please verify the adjustment details. Balances are never silently modified.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Old Balance:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = "৳${"%.2f".format(currentBalance)}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "New Balance:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = "৳${"%.2f".format(newBalanceParsed ?: 0.0)}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Difference:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = "${if (difference >= 0) "+" else ""}৳${"%.2f".format(difference)}",
                                color = if (difference >= 0) VividGreen else AlertRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Reason / Note:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = note.trim(),
                                color = TextPrimary,
                                fontSize = 12.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassOutlinedButton(
                            text = "Back",
                            onClick = { isConfirmStep = false },
                            modifier = Modifier.weight(1f)
                        )
                        VividButton(
                            text = "Confirm & Save",
                            onClick = {
                                onConfirm(newBalanceParsed ?: currentBalance, note.trim())
                            },
                            modifier = Modifier.weight(1.3f),
                            testTag = "btn_confirm_adjustment"
                        )
                    }
                }
            }
        }
    }
}
