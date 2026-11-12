package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.ShopLocale

@Composable
fun TestChecklistScreen(
    storeViewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by storeViewModel.currentLanguage.collectAsState()
    val store by storeViewModel.store.collectAsState()

    val checkedStates = remember {
        mutableStateMapOf(
            "phone_a" to true,
            "phone_b" to false,
            "add_sync" to false,
            "persistence" to false,
            "offline" to false
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("checklist_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Header
        item {
            Column {
                Text(
                    text = ShopLocale.get("checklist_title", currentLanguage),
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Verify your multi-device real-time shop setup",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Active Store & User Isolation Status
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x265DD62C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = VividGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Current Store: ${store?.name ?: "Loading..."}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "Firestore ID: ${store?.id ?: ""}",
                            color = TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Checklist Step 1: Sign in on Phone A
        item {
            ChecklistItem(
                title = ShopLocale.get("step_phone_a", currentLanguage),
                description = "Account created/signed in as Owner on first phone. Store document created at stores/{storeId}.",
                isChecked = checkedStates["phone_a"] == true,
                onToggle = { checkedStates["phone_a"] = !(checkedStates["phone_a"] ?: false) }
            )
        }

        // Checklist Step 2: Sign in on Phone B with same account
        item {
            ChecklistItem(
                title = ShopLocale.get("step_phone_b", currentLanguage),
                description = "Sign in on Phone B (or secondary browser/emulator) with the exact same account. The app looks up users/{userId}, finds storeId, and skips onboarding straight to this store.",
                isChecked = checkedStates["phone_b"] == true,
                onToggle = { checkedStates["phone_b"] = !(checkedStates["phone_b"] ?: false) }
            )
        }

        // Checklist Step 3: Add product on A and observe on B
        item {
            ChecklistItem(
                title = ShopLocale.get("step_add_sync", currentLanguage),
                description = "Tap '+ Add Product' on Phone A. In less than 1 second, Phone B reflects the new product and updated stock valuation via Firestore realtime snapshots.",
                isChecked = checkedStates["add_sync"] == true,
                onToggle = { checkedStates["add_sync"] = !(checkedStates["add_sync"] ?: false) }
            )
        }

        // Checklist Step 4: Log out and back in without losing data
        item {
            ChecklistItem(
                title = ShopLocale.get("step_persistence", currentLanguage),
                description = "Tap Log Out icon in top bar. User is signed out of FirebaseAuth. Sign back in with the same credentials: all products, sales history, and store settings remain intact.",
                isChecked = checkedStates["persistence"] == true,
                onToggle = { checkedStates["persistence"] = !(checkedStates["persistence"] ?: false) }
            )
        }

        // Checklist Step 5: Offline sync & device restart
        item {
            ChecklistItem(
                title = ShopLocale.get("step_offline", currentLanguage),
                description = "Enable Airplane mode, perform a sale checkout. Sale is buffered locally in offline cache. Force-stop app and reopen: app stays logged in and reconciles with Firestore when connectivity restores.",
                isChecked = checkedStates["offline"] == true,
                onToggle = { checkedStates["offline"] = !(checkedStates["offline"] ?: false) }
            )
        }

        // Firebase Console Setup Guide Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = VividGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ShopLocale.get("console_steps_title", currentLanguage),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. Firebase Console -> Authentication -> Sign-in method:\n   • Enable Google provider\n   • Enable Email/Password provider\n\n2. Authentication -> Settings -> Authorized domains:\n   • Add your production web domain for future web app support\n\n3. Firestore Database -> Rules:\n   • Ensure published rules restrict reading/writing to store members only.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Security Rules Text Display Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = ShopLocale.get("rule_desc", currentLanguage),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rules enforce: request.auth != null, request.auth.uid in store.memberUids, owner-only mutation for settings/members, and stock >= 0 on all products.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF141414),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Text(
                            text = """match /stores/{storeId} {
  allow create: if isSignedIn() && request.resource.data.ownerId == request.auth.uid;
  allow read: if isSignedIn() && request.auth.uid in resource.data.memberUids;
  allow update, delete: if isSignedIn() && resource.data.ownerId == request.auth.uid;
  
  match /products/{id} {
    allow read: if isStoreMember(storeId);
    allow create, update: if isStoreMember(storeId) && request.resource.data.stock >= 0;
  }
}""",
                            color = Color(0xFF9CCC65),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChecklistItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = VividGreen,
                    checkmarkColor = DarkBackground,
                    uncheckedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isChecked) VividGreen else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
