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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.util.AppLanguage
import com.example.util.ShopLocale

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBusy by authViewModel.isBusy.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val successMessage by authViewModel.successMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Sign Up, 2: Forgot Pass
    var currentLanguage by remember { mutableStateOf(AppLanguage.EN) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Language switcher in top corner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
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
                .padding(horizontal = 20.dp, vertical = 40.dp)
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Icon & Title
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0x265DD62C), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = VividGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = ShopLocale.get("app_title", currentLanguage),
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ShopLocale.get("app_subtitle", currentLanguage),
                color = TextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "auth_card"
            ) {
                Column {
                    // Instant Demo Mode Button
                    GlassOutlinedButton(
                        text = if (currentLanguage == AppLanguage.BN) "⚡ তাৎক্ষণিক ডেমো স্টোর দেখুন (লগইন ছাড়া)" else "⚡ Explore Demo Store (Instant Access)",
                        onClick = {
                            authViewModel.clearMessages()
                            authViewModel.launchDemoMode()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "explore_demo_store_btn"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Sign-In Primary Action
                    VividButton(
                        text = ShopLocale.get("sign_in_with_google", currentLanguage),
                        onClick = {
                            authViewModel.clearMessages()
                            authViewModel.signInWithGoogle(context)
                        },
                        isLoading = isBusy,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "google_sign_in_btn"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0x20FFFFFF)
                        )
                        Text(
                            text = " " + ShopLocale.get("or_email", currentLanguage) + " ",
                            color = TextMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0x20FFFFFF)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab selector
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = VividGreen,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = VividGreen
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                authViewModel.clearMessages()
                            },
                            text = {
                                Text(
                                    ShopLocale.get("sign_in", currentLanguage),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                authViewModel.clearMessages()
                            },
                            text = {
                                Text(
                                    ShopLocale.get("sign_up", currentLanguage),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = {
                                selectedTab = 2
                                authViewModel.clearMessages()
                            },
                            text = {
                                Text(
                                    ShopLocale.get("forgot_password", currentLanguage),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Inputs based on Tab
                    GlassTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = ShopLocale.get("email", currentLanguage),
                        placeholder = "owner@store.com",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        testTag = "email_input"
                    )

                    if (selectedTab != 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = ShopLocale.get("password", currentLanguage),
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            testTag = "password_input"
                        )
                    }

                    if (selectedTab == 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = ShopLocale.get("confirm_password", currentLanguage),
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            testTag = "confirm_password_input"
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = AlertRed,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    if (successMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = successMessage ?: "",
                            color = VividGreen,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when (selectedTab) {
                        0 -> {
                            VividButton(
                                text = ShopLocale.get("sign_in", currentLanguage),
                                onClick = {
                                    authViewModel.signInWithEmail(email, password)
                                },
                                isLoading = isBusy,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "email_sign_in_btn"
                            )
                        }
                        1 -> {
                            VividButton(
                                text = ShopLocale.get("sign_up", currentLanguage),
                                onClick = {
                                    authViewModel.signUpWithEmail(email, password, confirmPassword)
                                },
                                isLoading = isBusy,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "email_sign_up_btn"
                            )
                        }
                        2 -> {
                            VividButton(
                                text = ShopLocale.get("send_reset_link", currentLanguage),
                                onClick = {
                                    authViewModel.sendPasswordReset(email)
                                },
                                isLoading = isBusy,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "reset_password_btn"
                            )
                        }
                    }
                }
            }
        }
    }
}
