package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.example.data.model.Account
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale

/**
 * Account picker component for Checkout, Add Purchase, Receive Payment,
 * Pay Supplier, Expense, and Other Income screens.
 *
 * Rules:
 * - Default: "Cash in Hand" (or last used account)
 * - Remember last account used
 * - Rule 6: Hide the picker when only one account exists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPickerField(
    selectedAccountId: String,
    selectedAccountName: String,
    onAccountSelected: (Account) -> Unit,
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier,
    label: String = "Payment Account"
) {
    val accounts by storeViewModel.accounts.collectAsState()
    val balances by storeViewModel.accountBalances.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()

    val activeAccounts = remember(accounts) {
        accounts.filter { !it.isArchived }
    }

    // Rule 6: Hide the picker when only one account exists
    if (activeAccounts.size <= 1) {
        return
    }

    var showDialog by remember { mutableStateOf(false) }

    val currentAccount = activeAccounts.find { it.id == selectedAccountId }
        ?: activeAccounts.find { it.isDefault || it.id == "cash_in_hand" }
        ?: activeAccounts.firstOrNull()

    val displayName = currentAccount?.name ?: selectedAccountName.ifBlank { "Cash in Hand" }
    val displayBal = currentAccount?.let { balances[it.id] ?: it.openingBalance } ?: 0.0

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .clickable { showDialog = true }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("account_picker_field"),
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
                        .background(Color(0x335DD62C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountIcon(currentAccount?.type ?: "Cash"),
                        contentDescription = null,
                        tint = VividGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Bal: ${ShopLocale.formatMoney(displayBal, currentLanguage)}",
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select account",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "অ্যাকাউন্ট নির্বাচন করুন" else "Select Account",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(activeAccounts, key = { it.id }) { acc ->
                            val isSelected = acc.id == (currentAccount?.id ?: "")
                            val accBal = balances[acc.id] ?: acc.openingBalance

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0x335DD62C) else GlassSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) VividGreen else GlassBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onAccountSelected(acc)
                                        showDialog = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("account_option_${acc.id}"),
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
                                            .background(Color(0x26FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getAccountIcon(acc.type),
                                            contentDescription = null,
                                            tint = if (isSelected) VividGreen else TextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = acc.name,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${acc.type}${if (acc.accountNumber.isNotBlank()) " • ${acc.accountNumber}" else ""}",
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = ShopLocale.formatMoney(accBal, currentLanguage),
                                        color = if (isSelected) VividGreen else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = VividGreen,
                                            modifier = Modifier.size(16.dp)
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
}

fun getAccountIcon(type: String): ImageVector {
    return when (type.lowercase()) {
        "bank" -> Icons.Default.AccountBalance
        "wallet" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Payments
    }
}
