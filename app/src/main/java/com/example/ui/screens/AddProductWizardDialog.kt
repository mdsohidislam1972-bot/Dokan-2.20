package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Product
import com.example.domain.CalculationEngine
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.VividButton
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AlertRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import com.example.util.AppLanguage
import com.example.util.ProductImageUtil
import com.example.util.ShopLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductWizardDialog(
    productToEdit: Product? = null,
    currentLanguage: AppLanguage,
    isBusy: Boolean,
    canSeeBuyingPrice: Boolean = true,
    externalError: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        nameBn: String,
        category: String,
        unit: String,
        buyPrice: Double,
        sellPrice: Double,
        stock: Long,
        lowStockLimit: Long,
        photoUri: String
    ) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isEditMode = productToEdit != null
    var step by remember { mutableIntStateOf(1) } // 1: Basics, 2: Pricing, 3: Stock

    // Step 1: Basics
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var nameBn by remember { mutableStateOf(productToEdit?.nameBn ?: "") }
    var selectedCategory by remember { mutableStateOf(productToEdit?.category?.ifBlank { "Grocery" } ?: "Grocery") }
    var selectedUnit by remember { mutableStateOf(productToEdit?.unit?.ifBlank { "pcs" } ?: "pcs") }
    var photoUriString by remember { mutableStateOf(productToEdit?.photoUri ?: "") }
    var showPhotoSourceChoices by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Step 2: Pricing
    var buyPriceInput by remember {
        mutableStateOf(if (isEditMode) String.format(java.util.Locale.US, "%.2f", productToEdit!!.buyPrice) else "")
    }
    var sellPriceInput by remember {
        mutableStateOf(if (isEditMode) String.format(java.util.Locale.US, "%.2f", productToEdit!!.sellPrice) else "")
    }

    // Step 3: Stock
    var stockInput by remember {
        mutableStateOf(if (isEditMode) productToEdit!!.stock.toString() else "10")
    }
    var lowStockLimitInput by remember {
        mutableStateOf(if (isEditMode) productToEdit!!.lowStockLimit.toString() else "5")
    }

    var stepError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Photo picker using Android Photo Picker (zero broad storage permission required)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUriString = uri.toString()
            showPhotoSourceChoices = false
            stepError = null
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingCameraUri
        if (success && captured != null) {
            photoUriString = captured.toString()
            showPhotoSourceChoices = false
            stepError = null
        }
    }

    fun launchCameraCapture() {
        try {
            val uri = ProductImageUtil.createTempCameraPhotoUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            stepError = e.localizedMessage ?: "Unable to open camera"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            stepError = "Camera permission is required to take a product photo"
        }
    }

    fun onTakePhotoClick() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val categories = listOf("Grocery", "Beverages", "Snacks", "Medicine", "Electronics", "Personal Care", "General")
    val units = listOf("pcs", "kg", "ltr", "box", "pack")

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1E1E1E),
            border = BorderStroke(1.dp, if (showDeleteConfirm) AlertRed else GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (showDeleteConfirm) {
                // Inlined Delete Confirmation (avoids SurfaceSyncGroup multi-window deadlock)
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delete Product?",
                            color = AlertRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        IconButton(
                            onClick = { showDeleteConfirm = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Are you sure you want to delete '${productToEdit?.name}'? All current inventory and details will be removed. This action cannot be undone.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassOutlinedButton(
                            text = "Cancel",
                            onClick = { showDeleteConfirm = false },
                            modifier = Modifier.weight(1f)
                        )
                        VividButton(
                            text = "Delete",
                            onClick = {
                                showDeleteConfirm = false
                                onDelete?.invoke()
                            },
                            containerColor = AlertRed,
                            contentColor = Color.White,
                            isLoading = isBusy,
                            icon = Icons.Default.Delete,
                            modifier = Modifier.weight(1f),
                            testTag = "confirm_delete_btn"
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.padding(22.dp)) {
                    // Header with Step Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isEditMode) "Edit Product" else "Add Product",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = when (step) {
                                    1 -> "Step 1 of 3: Basics"
                                    2 -> "Step 2 of 3: Pricing"
                                    else -> "Step 3 of 3: Stock & Review"
                                },
                                color = VividGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isEditMode && onDelete != null) {
                                IconButton(
                                    onClick = { showDeleteConfirm = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AlertRed)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Step Dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 1..3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(if (i <= step) VividGreen else Color(0x33FFFFFF))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Contents (scrollable so photo preview + buttons never clip on small phones)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                    when (step) {
                        1 -> {
                            // STEP 1: BASICS
                            Column {
                                GlassTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        stepError = null
                                    },
                                    label = "Product Name (English) *",
                                    placeholder = "e.g. Miniket Rice 5kg",
                                    testTag = "wizard_name_en"
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                GlassTextField(
                                    value = nameBn,
                                    onValueChange = { nameBn = it },
                                    label = "Product Name (বাংলা - Optional)",
                                    placeholder = "যেমনঃ মিনিকেট চাল ৫ কেজি",
                                    testTag = "wizard_name_bn"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Category",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(categories) { cat ->
                                        val isSelected = selectedCategory == cat
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) Color(0x335DD62C) else Color(0x20FFFFFF),
                                            border = BorderStroke(1.dp, if (isSelected) VividGreen else GlassBorder),
                                            modifier = Modifier.clickable { selectedCategory = cat }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = getCategoryIcon(cat),
                                                    contentDescription = null,
                                                    tint = if (isSelected) VividGreen else TextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = cat,
                                                    color = if (isSelected) VividGreen else TextPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Unit selection
                                Text(
                                    text = "Unit of Measure",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    units.forEach { u ->
                                        val isSelected = selectedUnit == u
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(0x335DD62C) else Color(0x20FFFFFF),
                                            border = BorderStroke(1.dp, if (isSelected) VividGreen else GlassBorder),
                                            modifier = Modifier.clickable { selectedUnit = u }
                                        ) {
                                            Text(
                                                text = u,
                                                color = if (isSelected) VividGreen else TextPrimary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Add Optional Photo (Take Photo / Choose from Gallery / Preview with Change & Remove)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x20FFFFFF))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = if (photoUriString.isNotBlank()) "Product Photo" else "Add Optional Photo",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (photoUriString.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            ProductThumbnail(
                                                photoUri = photoUriString,
                                                category = selectedCategory,
                                                contentDescription = name.ifBlank { "Product photo preview" },
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .testTag("wizard_photo_preview"),
                                                iconSize = 28.dp,
                                                cornerRadius = 12.dp
                                            )
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = if (ProductImageUtil.isLocalPhotoUri(photoUriString)) {
                                                        "Compressed before upload"
                                                    } else {
                                                        "Saved product photo"
                                                    },
                                                    color = TextMuted,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        onClick = { showPhotoSourceChoices = !showPhotoSourceChoices },
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color(0x265DD62C),
                                                        border = BorderStroke(1.dp, VividGreen),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .defaultMinSize(minHeight = 44.dp)
                                                            .testTag("wizard_photo_change_btn")
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                                            horizontalArrangement = Arrangement.Center,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = null,
                                                                tint = VividGreen,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Change",
                                                                color = VividGreen,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                    Surface(
                                                        onClick = {
                                                            photoUriString = ""
                                                            showPhotoSourceChoices = false
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = Color(0x26FF5252),
                                                        border = BorderStroke(1.dp, AlertRed),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .defaultMinSize(minHeight = 44.dp)
                                                            .testTag("wizard_photo_remove_btn")
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                                            horizontalArrangement = Arrangement.Center,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = null,
                                                                tint = AlertRed,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Remove",
                                                                color = AlertRed,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                softWrap = false,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "Uses category icon if omitted",
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (photoUriString.isBlank() || showPhotoSourceChoices) {
                                        Surface(
                                            onClick = { onTakePhotoClick() },
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0x1FFFFFFF),
                                            border = BorderStroke(1.dp, GlassBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("wizard_take_photo_btn")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoCamera,
                                                    contentDescription = null,
                                                    tint = VividGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Take Photo",
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Surface(
                                            onClick = {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0x1FFFFFFF),
                                            border = BorderStroke(1.dp, GlassBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("wizard_choose_gallery_btn")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = null,
                                                    tint = VividGreen,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Choose from Gallery",
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // STEP 2: PRICING (LIVE PROFIT & MARGIN VIA CALCULATION ENGINE)
                            val buy = buyPriceInput.toDoubleOrNull() ?: 0.0
                            val sell = sellPriceInput.toDoubleOrNull() ?: 0.0
                            val profitPerPiece = CalculationEngine.calculateProfitPerPiece(sell, buy)
                            val margin = CalculationEngine.calculateMarginPercent(sell, buy)

                            Column {
                                if (canSeeBuyingPrice) {
                                    GlassTextField(
                                        value = buyPriceInput,
                                        onValueChange = {
                                            buyPriceInput = it
                                            stepError = null
                                        },
                                        label = "Cost Price (Buy) ৳",
                                        placeholder = "0.00",
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        testTag = "wizard_buy_price"
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                GlassTextField(
                                    value = sellPriceInput,
                                    onValueChange = {
                                        sellPriceInput = it
                                        stepError = null
                                    },
                                    label = "Selling Price (Sale) ৳",
                                    placeholder = "0.00",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    testTag = "wizard_sell_price"
                                )

                                if (canSeeBuyingPrice) {
                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Live Profit & Margin Card
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.TrendingUp,
                                                        contentDescription = null,
                                                        tint = if (profitPerPiece >= 0.0) VividGreen else AlertRed,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Profit per ${selectedUnit}:",
                                                        color = TextSecondary,
                                                        fontSize = 13.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                                Text(
                                                    text = ShopLocale.formatMoney(profitPerPiece, currentLanguage),
                                                    color = if (profitPerPiece >= 0.0) VividGreen else AlertRed,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0x20FFFFFF))
                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Profit Margin:",
                                                    color = TextSecondary,
                                                    fontSize = 13.sp,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = String.format(java.util.Locale.US, "%.1f%%", margin),
                                                    color = if (margin >= 0.0) VividGreen else AlertRed,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // STEP 3: STOCK & REVIEW
                            val buy = buyPriceInput.toDoubleOrNull() ?: 0.0
                            val sell = sellPriceInput.toDoubleOrNull() ?: 0.0
                            val qty = stockInput.toLongOrNull() ?: 0L
                            val lowLimit = lowStockLimitInput.toLongOrNull() ?: 5L

                            Column {
                                GlassTextField(
                                    value = stockInput,
                                    onValueChange = {
                                        stockInput = it
                                        stepError = null
                                    },
                                    label = "Stock Quantity (${selectedUnit})",
                                    placeholder = "10",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    testTag = "wizard_stock_qty"
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                GlassTextField(
                                    value = lowStockLimitInput,
                                    onValueChange = {
                                        lowStockLimitInput = it
                                        stepError = null
                                    },
                                    label = "Low-Stock Alert Limit",
                                    placeholder = "5",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    testTag = "wizard_low_limit"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Review Card
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        Text(
                                            text = "Summary Review",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "$name (${selectedCategory} • ${selectedUnit})",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                        val priceReviewText = if (canSeeBuyingPrice) {
                                            "Buy: ${ShopLocale.formatMoney(buy, currentLanguage)} | Sell: ${ShopLocale.formatMoney(sell, currentLanguage)}"
                                        } else {
                                            "Sale Price: ${ShopLocale.formatMoney(sell, currentLanguage)}"
                                        }
                                        Text(
                                            text = priceReviewText,
                                            color = VividGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Stock: $qty ${selectedUnit} (Low alert at: $lowLimit)",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }

                    val displayedError = stepError ?: externalError
                    if (!displayedError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayedError,
                            color = AlertRed,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (step > 1) {
                            GlassOutlinedButton(
                                text = "Back",
                                onClick = {
                                    step -= 1
                                    stepError = null
                                },
                                icon = Icons.Default.ArrowBack,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (step < 3) {
                            VividButton(
                                text = "Next",
                                onClick = {
                                    if (step == 1) {
                                        if (name.isBlank()) {
                                            stepError = "Please enter product name"
                                            return@VividButton
                                        }
                                    }
                                    if (step == 2) {
                                        val buy = if (canSeeBuyingPrice) buyPriceInput.toDoubleOrNull() else 0.0
                                        val sell = sellPriceInput.toDoubleOrNull()
                                        if (buy == null || sell == null || buy < 0.0 || sell < 0.0) {
                                            stepError = if (canSeeBuyingPrice) {
                                                "Enter valid cost and selling prices"
                                            } else {
                                                "Enter a valid selling price"
                                            }
                                            return@VividButton
                                        }
                                    }
                                    step += 1
                                    stepError = null
                                },
                                icon = Icons.Default.ArrowForward,
                                modifier = Modifier.weight(1f),
                                testTag = "wizard_next_btn"
                            )
                        } else {
                            VividButton(
                                text = if (isEditMode) "Save Changes" else "Save Product",
                                onClick = {
                                    val buy = buyPriceInput.toDoubleOrNull() ?: 0.0
                                    val sell = sellPriceInput.toDoubleOrNull() ?: 0.0
                                    val qty = stockInput.toLongOrNull() ?: 0L
                                    val lowLimit = lowStockLimitInput.toLongOrNull() ?: 5L
                                    onConfirm(
                                        name,
                                        nameBn,
                                        selectedCategory,
                                        selectedUnit,
                                        buy,
                                        sell,
                                        qty,
                                        lowLimit,
                                        photoUriString
                                    )
                                },
                                isLoading = isBusy,
                                icon = Icons.Default.Check,
                                modifier = Modifier.weight(1f),
                                testTag = "wizard_save_product_btn"
                            )
                        }
                    }
                }
            }
        }
    }
}
