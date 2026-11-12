package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.LanguageToggle
import com.example.ui.components.VividButton
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
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
    var storeName by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }

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
                text = ShopLocale.get("welcome_title", currentLanguage),
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ShopLocale.get("welcome_subtitle", currentLanguage),
                color = TextSecondary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(28.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "onboarding_card"
            ) {
                Column {
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
                }
            }
        }
    }
}
