package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoreInvite
import com.example.data.model.StoreMember
import com.example.data.model.StorePermissions
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.PlainEmptyState
import com.example.ui.components.QrCodeView
import com.example.ui.components.VividButton
import com.example.ui.theme.AlertRed
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// -------------------------------------------------------------
// STAFF MANAGEMENT SCREEN (Owner Only)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val members by storeViewModel.members.collectAsState()
    val invites by storeViewModel.invites.collectAsState()
    val store by storeViewModel.store.collectAsState()
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val isOwner by storeViewModel.isOwner.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var createdInviteForDisplay by remember { mutableStateOf<StoreInvite?>(null) }

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    if (!isOwner) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "You do not have permission to access Staff Management.",
                color = AlertRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    val activeInvites = remember(invites) {
        invites.filter { it.status == "active" && System.currentTimeMillis() <= it.expiresAt }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("staff_management_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Navigation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { storeViewModel.navigateBackInBusiness() },
                    modifier = Modifier.testTag("staff_back_btn")
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
                        text = if (currentLanguage == AppLanguage.BN) "স্টাফ ও অনুমতি (Staff)" else "Staff & Permissions",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Manage staff access, roles, and invite codes",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                VividButton(
                    text = "+ Add Staff",
                    onClick = { showAddDialog = true },
                    modifier = Modifier.height(40.dp),
                    testTag = "btn_add_staff"
                )
            }
        }

        // Active Invites Section
        if (activeInvites.isNotEmpty()) {
            item {
                Text(
                    text = "Pending Invites (${activeInvites.size})",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            items(activeInvites, key = { it.code }) { inv ->
                InviteCard(
                    invite = inv,
                    onViewQr = { createdInviteForDisplay = inv },
                    onCancel = { storeViewModel.cancelInvite(inv.code) }
                )
            }
        }

        // Members Section
        item {
            Text(
                text = "Store Members (${members.size})",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (members.isEmpty()) {
            item {
                PlainEmptyState(message = "No staff members yet. Tap '+ Add Staff' to invite team members.")
            }
        } else {
            items(members, key = { it.userId }) { member ->
                val isStoreOwner = member.userId == store?.ownerId || member.roleLabel.equals("Owner", ignoreCase = true)
                MemberRowCard(
                    member = member,
                    isStoreOwner = isStoreOwner,
                    onClick = {
                        storeViewModel.selectMember(member)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddStaffInviteDialog(
            currentLanguage = currentLanguage,
            onDismiss = { showAddDialog = false },
            onInviteCreated = { inv ->
                showAddDialog = false
                createdInviteForDisplay = inv
            },
            storeViewModel = storeViewModel
        )
    }

    if (createdInviteForDisplay != null) {
        InviteQrDisplayDialog(
            invite = createdInviteForDisplay!!,
            onDismiss = { createdInviteForDisplay = null }
        )
    }
}

// -------------------------------------------------------------
// MEMBER ROW CARD
// -------------------------------------------------------------
@Composable
fun MemberRowCard(
    member: StoreMember,
    isStoreOwner: Boolean,
    onClick: () -> Unit
) {
    val isRemoved = member.status.equals("Removed", ignoreCase = true)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        testTag = "member_row_${member.userId}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isStoreOwner -> Color(0x335DD62C)
                            isRemoved -> Color(0x33E53935)
                            else -> Color(0x22FFFFFF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isStoreOwner) Icons.Default.Security else Icons.Default.Person,
                    contentDescription = null,
                    tint = when {
                        isStoreOwner -> VividGreen
                        isRemoved -> AlertRed
                        else -> TextPrimary
                    },
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.displayName.ifBlank { if (isStoreOwner) "Store Owner" else "Staff (${member.userId.take(6)})" },
                        color = if (isRemoved) TextMuted else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isStoreOwner) Color(0x335DD62C) else Color(0x22FFFFFF)
                    ) {
                        Text(
                            text = member.roleLabel,
                            color = if (isStoreOwner) VividGreen else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (isStoreOwner) "Owner • Full Access" else "Level: ${member.permissionLevel} • Status: ${member.status}",
                    color = if (isRemoved) AlertRed else TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// -------------------------------------------------------------
// PENDING INVITE CARD
// -------------------------------------------------------------
@Composable
fun InviteCard(
    invite: StoreInvite,
    onViewQr: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invite.staffName.ifBlank { "Invited Staff" },
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0x3300E5FF)
                    ) {
                        Text(
                            text = invite.roleLabel,
                            color = Color(0xFF00E5FF),
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Code: ${invite.code} • Level: ${invite.permissionLevel}",
                    color = VividGreen,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onViewQr) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "View QR",
                    tint = VividGreen
                )
            }

            IconButton(
                onClick = {
                    val shareText = "Join my shop '${invite.storeName}' on StoreOS!\nRole: ${invite.roleLabel}\nInvite Code: ${invite.code}\nValid for 24 hours."
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share StoreOS Invite"))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextSecondary
                )
            }

            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = AlertRed
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ADD STAFF INVITE DIALOG
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffInviteDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onInviteCreated: (StoreInvite) -> Unit,
    storeViewModel: StoreViewModel
) {
    var staffName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Sales") }
    var selectedLevel by remember { mutableStateOf("Limited") } // Full, Half, Limited, Selected

    val roles = listOf("Partner", "Manager", "Accountant", "Sales", "Stock", "Entry")
    val levels = listOf("Limited", "Half", "Full", "Selected")

    val permissionMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            putAll(StorePermissions.forLevel("Limited"))
        }
    }

    val featureLabels = listOf(
        StorePermissions.SELL to ("Sell (Make Sales)" to "বিক্রয় করা"),
        StorePermissions.VIEW_STOCK to ("View Stock" to "স্টক দেখা"),
        StorePermissions.ADD_EDIT_PRODUCTS to ("Add & Edit Products" to "পণ্য যোগ ও সম্পাদনা"),
        StorePermissions.SEE_BUYING_PRICE_AND_PROFIT to ("See Buying Price & Profit" to "কেনা দাম ও লাভ দেখা"),
        StorePermissions.HISTORY to ("History & Timeline" to "লেনদেন ইতিহাস"),
        StorePermissions.CUSTOMERS_AND_BAKI to ("Customers & Baki Ledger" to "বাকি খাতা ও কাস্টমার"),
        StorePermissions.RECEIVE_PAYMENT to ("Receive Customer Payments" to "কাস্টমার পেমেন্ট গ্রহণ"),
        StorePermissions.SUPPLIERS_AND_PURCHASES to ("Suppliers & Purchases" to "সাপ্লায়ার ও ক্রয়"),
        StorePermissions.PAY_SUPPLIER to ("Pay Supplier" to "সাপ্লায়ারকে পরিশোধ"),
        StorePermissions.REPORTS to ("Reports" to "ব্যবসায়িক রিপোর্ট"),
        StorePermissions.CASH_AND_BANK to ("Cash & Bank Accounts" to "ক্যাশ ও ব্যাংক হিসাব"),
        StorePermissions.EXPENSES_AND_OTHER_INCOME to ("Expenses & Other Income" to "খরচ ও অন্যান্য আয়"),
        StorePermissions.NOTES to ("Store Notes" to "দোকানের নোট")
    )

    fun onLevelPicked(level: String) {
        selectedLevel = level
        if (level != "Selected") {
            permissionMap.clear()
            permissionMap.putAll(StorePermissions.forLevel(level))
        }
    }

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
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Staff Member",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                GlassTextField(
                    value = staffName,
                    onValueChange = { staffName = it },
                    label = "Staff Member Name *",
                    placeholder = "e.g. Shakil Ahmed",
                    testTag = "input_staff_name"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Role Label",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.take(3).forEach { r ->
                        FilterChip(
                            selected = selectedRole == r,
                            onClick = { selectedRole = r },
                            label = { Text(r, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividGreen,
                                selectedLabelColor = DarkBackground,
                                containerColor = GlassSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.drop(3).forEach { r ->
                        FilterChip(
                            selected = selectedRole == r,
                            onClick = { selectedRole = r },
                            label = { Text(r, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividGreen,
                                selectedLabelColor = DarkBackground,
                                containerColor = GlassSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Permission Level",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    levels.forEach { lvl ->
                        FilterChip(
                            selected = selectedLevel == lvl,
                            onClick = { onLevelPicked(lvl) },
                            label = { Text(lvl, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividGreen,
                                selectedLabelColor = DarkBackground,
                                containerColor = GlassSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Features Included in $selectedLevel:",
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    featureLabels.forEach { (key, labels) ->
                        val isChecked = permissionMap[key] == true
                        val canToggle = selectedLevel == "Selected"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canToggle) {
                                    permissionMap[key] = !isChecked
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = if (canToggle) { { permissionMap[key] = it } } else null,
                                enabled = canToggle,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VividGreen,
                                    checkmarkColor = DarkBackground,
                                    uncheckedColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = labels.first,
                                color = if (isChecked) TextPrimary else TextMuted,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Owner-only note
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x22FFFFFF)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Staff & permissions, balance adjustments, archiving accounts, and data fixes remain strictly owner-only.",
                            color = TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                VividButton(
                    text = "Create Invite Code",
                    onClick = {
                        storeViewModel.createStaffInvite(
                            staffName = staffName,
                            roleLabel = selectedRole,
                            permissionLevel = selectedLevel,
                            customPermissions = permissionMap.toMap(),
                            onSuccess = onInviteCreated
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_submit_invite"
                )
            }
        }
    }
}

// -------------------------------------------------------------
// INVITE QR DISPLAY DIALOG
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteQrDisplayDialog(
    invite: StoreInvite,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Store Invite Code",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Invite for ${invite.staffName} (${invite.roleLabel})",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // QR Code View
                QrCodeView(
                    content = invite.code,
                    sizeDp = 180
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Code Display Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GlassSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = invite.code,
                            color = VividGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Valid for 24 hours • Usable once",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = "Share",
                        onClick = {
                            val shareText = "Join my shop '${invite.storeName}' on StoreOS!\nRole: ${invite.roleLabel}\nInvite Code: ${invite.code}\nValid for 24 hours."
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share StoreOS Invite"))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    VividButton(
                        text = "Done",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// STAFF MEMBER DETAILS & PERMISSION EDIT SCREEN (Owner Only)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffMemberDetailsScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val member = storeViewModel.selectedMember.collectAsState().value
    val store by storeViewModel.store.collectAsState()
    val isOwner by storeViewModel.isOwner.collectAsState()
    val isBusy by storeViewModel.isBusy.collectAsState()

    BackHandler {
        storeViewModel.navigateBackInBusiness()
    }

    if (member == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            PlainEmptyState(message = "No member selected")
        }
        return
    }

    val isStoreOwner = member.userId == store?.ownerId || member.roleLabel.equals("Owner", ignoreCase = true)
    val isRemoved = member.status.equals("Removed", ignoreCase = true)

    var currentRole by remember(member) { mutableStateOf(member.roleLabel) }
    var currentLevel by remember(member) { mutableStateOf(member.permissionLevel) }

    val permissionMap = remember(member) {
        mutableStateMapOf<String, Boolean>().apply {
            putAll(member.permissions)
        }
    }

    var showConfirmSaveDialog by remember { mutableStateOf(false) }
    var showConfirmRemoveDialog by remember { mutableStateOf(false) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }

    val roles = listOf("Partner", "Manager", "Accountant", "Sales", "Stock", "Entry")
    val levels = listOf("Limited", "Half", "Full", "Selected")

    val featureLabels = listOf(
        StorePermissions.SELL to "Sell (Make Sales)",
        StorePermissions.VIEW_STOCK to "View Stock",
        StorePermissions.ADD_EDIT_PRODUCTS to "Add & Edit Products",
        StorePermissions.SEE_BUYING_PRICE_AND_PROFIT to "See Buying Price & Profit",
        StorePermissions.HISTORY to "History & Timeline",
        StorePermissions.CUSTOMERS_AND_BAKI to "Customers & Baki Ledger",
        StorePermissions.RECEIVE_PAYMENT to "Receive Customer Payments",
        StorePermissions.SUPPLIERS_AND_PURCHASES to "Suppliers & Purchases",
        StorePermissions.PAY_SUPPLIER to "Pay Supplier",
        StorePermissions.REPORTS to "Reports",
        StorePermissions.CASH_AND_BANK to "Cash & Bank Accounts",
        StorePermissions.EXPENSES_AND_OTHER_INCOME to "Expenses & Other Income",
        StorePermissions.NOTES to "Store Notes"
    )

    fun onLevelPicked(level: String) {
        currentLevel = level
        if (level != "Selected") {
            permissionMap.clear()
            permissionMap.putAll(StorePermissions.forLevel(level))
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("staff_details_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { storeViewModel.navigateBackInBusiness() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = member.displayName.ifBlank { if (isStoreOwner) "Store Owner" else "Staff Member" },
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isStoreOwner) "Owner Account (Protected)" else "Staff Member Details & Access",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Profile Overview Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isStoreOwner) Color(0x335DD62C) else if (isRemoved) Color(0x33E53935) else Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isStoreOwner) Icons.Default.Security else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isStoreOwner) VividGreen else if (isRemoved) AlertRed else TextPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = member.displayName.ifBlank { "Staff" },
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (member.email.isNotBlank()) {
                                Text(
                                    text = member.email,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "Status: ${member.status} • Joined: ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(member.joinedTime))}",
                                color = if (isRemoved) AlertRed else TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
        }

        if (isStoreOwner) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x225DD62C)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = VividGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This is the store owner record. Owners always have full permissions and cannot be removed or demoted.",
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            // Role & Permissions Config (Owner Only)
            item {
                Text(
                    text = "Role Label",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.take(3).forEach { r ->
                        FilterChip(
                            selected = currentRole == r,
                            onClick = { currentRole = r },
                            label = { Text(r, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividGreen,
                                selectedLabelColor = DarkBackground,
                                containerColor = GlassSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.drop(3).forEach { r ->
                        FilterChip(
                            selected = currentRole == r,
                            onClick = { currentRole = r },
                            label = { Text(r, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividGreen,
                                selectedLabelColor = DarkBackground,
                                containerColor = GlassSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Permission Level",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    levels.forEach { lvl ->
                        FilterChip(
                            selected = currentLevel == lvl,
                            onClick = { onLevelPicked(lvl) },
                            label = { Text(lvl, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividGreen,
                                selectedLabelColor = DarkBackground,
                                containerColor = GlassSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Feature Permissions:",
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassSurface)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    featureLabels.forEach { (key, label) ->
                        val isChecked = permissionMap[key] == true
                        val canToggle = currentLevel == "Selected"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canToggle) {
                                    permissionMap[key] = !isChecked
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = if (canToggle) { { permissionMap[key] = it } } else null,
                                enabled = canToggle,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = VividGreen,
                                    checkmarkColor = DarkBackground,
                                    uncheckedColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                color = if (isChecked) TextPrimary else TextMuted,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            // Save & Remove/Restore actions
            item {
                Spacer(modifier = Modifier.height(10.dp))
                VividButton(
                    text = "Save Permission Changes",
                    onClick = { showConfirmSaveDialog = true },
                    isLoading = isBusy,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isRemoved) {
                    GlassOutlinedButton(
                        text = "Restore Staff Member",
                        onClick = { showConfirmRestoreDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    GlassOutlinedButton(
                        text = "Remove Staff Member",
                        onClick = { showConfirmRemoveDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0x22E53935)
                    )
                }
            }
        }
    }

    if (showConfirmSaveDialog) {
        BasicAlertDialog(onDismissRequest = { showConfirmSaveDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Save Permissions?", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Change ${member.displayName}'s role to '$currentRole' with '$currentLevel' permissions? Changes take effect immediately on their phone.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassOutlinedButton(text = "Cancel", onClick = { showConfirmSaveDialog = false }, modifier = Modifier.weight(1f))
                        VividButton(
                            text = "Confirm",
                            onClick = {
                                storeViewModel.updateMemberPermissions(
                                    member = member,
                                    roleLabel = currentRole,
                                    permissionLevel = currentLevel,
                                    permissions = permissionMap.toMap(),
                                    onSuccess = {
                                        showConfirmSaveDialog = false
                                        storeViewModel.navigateBackInBusiness()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showConfirmRemoveDialog) {
        BasicAlertDialog(onDismissRequest = { showConfirmRemoveDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Remove Staff Member?", color = AlertRed, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Are you sure you want to remove ${member.displayName}? They will immediately lose access to this store. Their past recorded sales and entries will stay preserved with their name.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassOutlinedButton(text = "Cancel", onClick = { showConfirmRemoveDialog = false }, modifier = Modifier.weight(1f))
                        VividButton(
                            text = "Remove",
                            onClick = {
                                storeViewModel.removeMember(member) {
                                    showConfirmRemoveDialog = false
                                    storeViewModel.navigateBackInBusiness()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showConfirmRestoreDialog) {
        BasicAlertDialog(onDismissRequest = { showConfirmRestoreDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Restore Staff Member?", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Restore ${member.displayName} to active status? They will regain access to the store with their configured permissions.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassOutlinedButton(text = "Cancel", onClick = { showConfirmRestoreDialog = false }, modifier = Modifier.weight(1f))
                        VividButton(
                            text = "Restore",
                            onClick = {
                                storeViewModel.restoreMember(member) {
                                    showConfirmRestoreDialog = false
                                    storeViewModel.navigateBackInBusiness()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
