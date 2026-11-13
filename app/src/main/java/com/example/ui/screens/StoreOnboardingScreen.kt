package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.LanguageToggle
import com.example.ui.components.QrScannerDialog
import com.example.ui.components.VividButton
import com.example.ui.components.extractInviteCodeFromQr
import com.example.data.model.InviteCodeConfig
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale

@Composable
fun StoreOnboardingScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBusy by authViewModel.isBusy.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var currentLanguage by remember { mutableStateOf(AppLanguage.EN) }
    var isOnboardingModeJoin by remember { mutableStateOf(false) }
    var storeName by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var showQrScanner by remember { mutableStateOf(false) }
    var cameraPermissionMessage by remember { mutableStateOf<String?>(null) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraPermissionMessage = null
            showQrScanner = true
        } else {
            cameraPermissionMessage = if (currentLanguage == AppLanguage.BN) {
                "QR কোড স্ক্যান করতে ক্যামেরা অনুমতি প্রয়োজন। অথবা নিচে ${InviteCodeConfig.CODE_LENGTH} অক্ষরের কোডটি লিখুন।"
            } else {
                "Camera permission is required to scan QR codes. You can still type the ${InviteCodeConfig.CODE_LENGTH}-character code below."
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Top Bar: Log Out & Language Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassOutlinedButton(
                text = ShopLocale.get("log_out", currentLanguage),
                onClick = { authViewModel.signOut(context) },
                testTag = "onboarding_logout_btn"
            )
            LanguageToggle(
                currentLanguage = currentLanguage,
                onToggle = {
                    currentLanguage = if (currentLanguage == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 60.dp)
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0x265DD62C), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = VividGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isOnboardingModeJoin) {
                    if (currentLanguage == AppLanguage.BN) "দোকানে যোগ দিন" else "Join Existing Store"
                } else {
                    ShopLocale.get("welcome_title", currentLanguage)
                },
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isOnboardingModeJoin) {
                    if (currentLanguage == AppLanguage.BN) "দোকানের মালিকের দেওয়া ${InviteCodeConfig.CODE_LENGTH} অক্ষরের কোড লিখুন" else "Enter the ${InviteCodeConfig.CODE_LENGTH}-character invite code from your store owner"
                } else {
                    ShopLocale.get("welcome_subtitle", currentLanguage)
                },
                color = TextSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Selector: Create Store vs Join Store
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1AFFFFFF))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (!isOnboardingModeJoin) VividGreen else Color.Transparent)
                        .clickable { isOnboardingModeJoin = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "নতুন দোকান তৈরি" else "Create Store",
                        color = if (!isOnboardingModeJoin) DarkBackground else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isOnboardingModeJoin) VividGreen else Color.Transparent)
                        .clickable { isOnboardingModeJoin = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.BN) "স্টাফ হিসেবে যোগ দিন" else "Join as Staff",
                        color = if (isOnboardingModeJoin) DarkBackground else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "onboarding_card"
            ) {
                Column {
                    if (!isOnboardingModeJoin) {
                        // CREATE STORE FORM
                        GlassTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = ShopLocale.get("store_name", currentLanguage),
                            placeholder = "e.g. Dhaka Super Shop",
                            leadingIcon = Icons.Default.Business,
                            testTag = "store_name_input"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlassTextField(
                            value = storePhone,
                            onValueChange = { storePhone = it },
                            label = ShopLocale.get("store_phone", currentLanguage),
                            placeholder = "e.g. +8801712345678",
                            leadingIcon = Icons.Default.Phone,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            testTag = "store_phone_input"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlassTextField(
                            value = storeAddress,
                            onValueChange = { storeAddress = it },
                            label = ShopLocale.get("store_address", currentLanguage),
                            placeholder = "e.g. Dhanmondi 27, Dhaka",
                            leadingIcon = Icons.Default.LocationOn,
                            singleLine = true,
                            testTag = "store_address_input"
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = AlertRed,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        VividButton(
                            text = ShopLocale.get("create_store_btn", currentLanguage),
                            onClick = {
                                authViewModel.createStore(storeName, storePhone, storeAddress)
                            },
                            isLoading = isBusy,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "create_store_submit_btn"
                        )
                    } else {
                        // JOIN STORE FORM
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "দোকানের আমন্ত্রণ কোড বা QR" else "Scan QR or Enter Invite Code",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.BN) "মালিকের ফোনের QR কোড স্ক্যান করুন অথবা ${InviteCodeConfig.CODE_LENGTH} অক্ষরের কোড লিখুন (মেয়াদ ২৪ ঘণ্টা)"
                            else "Scan the owner's QR code or enter the ${InviteCodeConfig.CODE_LENGTH}-character code (valid for 24 hours, one-time use)",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Scan Owner QR Code Button
                        GlassOutlinedButton(
                            text = if (currentLanguage == AppLanguage.BN) "মালিকের QR কোড স্ক্যান করুন" else "Scan Owner's QR Code",
                            icon = Icons.Default.QrCodeScanner,
                            onClick = {
                                authViewModel.clearMessages()
                                cameraPermissionMessage = null
                                val hasCameraPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCameraPerm) {
                                    showQrScanner = true
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            enabled = !isBusy,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_scan_owner_qr"
                        )

                        if (cameraPermissionMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cameraPermissionMessage ?: "",
                                color = AlertRed,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x20FFFFFF))
                            Text(
                                text = if (currentLanguage == AppLanguage.BN) " অথবা কোড লিখুন " else " OR ENTER CODE ",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x20FFFFFF))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        GlassTextField(
                            value = inviteCode,
                            onValueChange = { inviteCode = InviteCodeConfig.sanitizeInput(it) },
                            label = "${InviteCodeConfig.CODE_LENGTH}-Character Code *",
                            placeholder = "e.g. ${InviteCodeConfig.EXAMPLE_CODE}",
                            singleLine = true,
                            testTag = "invite_code_input"
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = AlertRed,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        VividButton(
                            text = if (currentLanguage == AppLanguage.BN) "দোকানে যোগ দিন" else "Join Store",
                            onClick = {
                                authViewModel.joinStoreWithCode(inviteCode)
                            },
                            isLoading = isBusy,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "join_store_submit_btn"
                        )
                    }
                }
            }
        }
    }

    if (showQrScanner) {
        QrScannerDialog(
            currentLanguage = currentLanguage,
            onQrScanned = { rawText ->
                val scannedCode = InviteCodeConfig.sanitizeInput(extractInviteCodeFromQr(rawText))
                inviteCode = scannedCode
                showQrScanner = false
                authViewModel.clearMessages()
                authViewModel.joinStoreWithCode(scannedCode)
            },
            onDismiss = { showQrScanner = false }
        )
    }
}
