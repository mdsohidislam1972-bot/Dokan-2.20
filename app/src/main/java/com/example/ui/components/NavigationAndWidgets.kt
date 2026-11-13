package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VividGreen
import kotlin.math.roundToInt

enum class ShopNavTab {
    HOME,
    STOCK,
    SELL,
    HISTORY,
    BUSINESS
}

@Composable
fun FloatingGlassPillBottomBar(
    currentTab: ShopNavTab,
    onTabSelected: (ShopNavTab) -> Unit,
    modifier: Modifier = Modifier,
    canSell: Boolean = true,
    canViewStock: Boolean = true,
    canSeeHistory: Boolean = true,
    canViewReports: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating pill container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(34.dp),
                    ambientColor = Color(0x99000000),
                    spotColor = Color(0x66000000)
                ),
            shape = RoundedCornerShape(34.dp),
            color = Color(0xF21C1C1C),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Home
                PillNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = currentTab == ShopNavTab.HOME,
                    onClick = { onTabSelected(ShopNavTab.HOME) },
                    testTag = "nav_home"
                )

                // Stock
                if (canViewStock) {
                    PillNavItem(
                        label = "Stock",
                        icon = Icons.Default.Inventory2,
                        isSelected = currentTab == ShopNavTab.STOCK,
                        onClick = { onTabSelected(ShopNavTab.STOCK) },
                        testTag = "nav_stock"
                    )
                }

                // Placeholder space for the raised center SELL button
                if (canSell) {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                // History
                if (canSeeHistory) {
                    PillNavItem(
                        label = "History",
                        icon = Icons.Default.History,
                        isSelected = currentTab == ShopNavTab.HISTORY,
                        onClick = { onTabSelected(ShopNavTab.HISTORY) },
                        testTag = "nav_history"
                    )
                }

                // Business (hidden when user does not have Reports permission)
                if (canViewReports) {
                    PillNavItem(
                        label = "Business",
                        icon = Icons.Default.Business,
                        isSelected = currentTab == ShopNavTab.BUSINESS,
                        onClick = { onTabSelected(ShopNavTab.BUSINESS) },
                        testTag = "nav_business"
                    )
                }
            }
        }

        // Big Raised Vivid Green SELL Button in Center (if canSell)
        if (canSell) {
            Box(
                modifier = Modifier
                    .offset(y = (-14).dp)
                    .size(64.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = VividGreen,
                        ambientColor = VividGreen
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF75E945),
                                VividGreen,
                                Color(0xFF337418)
                            )
                        )
                    )
                    .clickable { onTabSelected(ShopNavTab.SELL) }
                    .testTag("nav_sell_fab"),
                contentAlignment = Alignment.Center
            ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = "SELL",
                    tint = DarkBackground,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "SELL",
                    color = DarkBackground,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
            }
        }
    }
    }
}

@Composable
fun PillNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) VividGreen else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) VividGreen else TextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Circular progress ring for stock quantity.
 * Amber when low, VividGreen when safe, Red when 0.
 */
@Composable
fun QuantityProgressRing(
    quantity: Long,
    lowStockLimit: Long,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    val isOut = quantity <= 0L
    val isLow = quantity in 1..lowStockLimit

    val ringColor = when {
        isOut -> AlertRed
        isLow -> Color(0xFFFFB300) // Amber
        else -> VividGreen
    }

    val maxRef = (lowStockLimit * 3L).coerceAtLeast(15L)
    val progressFraction = (quantity.toFloat() / maxRef.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            // Track
            drawArc(
                color = Color(0x33FFFFFF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            // Value
            if (!isOut) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progressFraction,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = quantity.toString(),
            color = if (isOut) AlertRed else TextPrimary,
            fontSize = if (size > 44.dp) 13.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/**
 * Interactive "Slide to confirm sale" widget
 */
@Composable
fun SlideToConfirmSale(
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Slide to confirm sale"
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isSuccess by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF262626))
            .testTag("slide_to_confirm_container")
    ) {
        val density = LocalDensity.current
        val widthPx = constraints.maxWidth.toFloat()
        val thumbSizePx = with(density) { 52.dp.toPx() }
        val maxOffsetPx = (widthPx - thumbSizePx).coerceAtLeast(0f)

        val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "sliderThumb")

        // Filled active track behind thumb
        val fillWidthRatio = if (maxOffsetPx > 0f) (animatedOffsetX / maxOffsetPx).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = fillWidthRatio)
                .background(Color(0x335DD62C))
        )

        // Centered prompt text
        Text(
            text = if (isSuccess) "Sale Confirmed!" else text,
            color = if (isSuccess) VividGreen else TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 60.dp),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )

        // Draggable green thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffsetX.roundToInt(), 2.dp.roundToPx()) }
                .size(52.dp)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(if (isSuccess) VividGreen else VividGreen)
                .draggable(
                    enabled = enabled && !isSuccess,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(0f, maxOffsetPx)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        if (offsetX >= maxOffsetPx * 0.82f) {
                            offsetX = maxOffsetPx
                            isSuccess = true
                            onConfirmed()
                        } else {
                            offsetX = 0f
                        }
                    }
                )
                .testTag("slide_thumb"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.ArrowForward,
                contentDescription = "Slide thumb",
                tint = DarkBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Returns matching Material icon for a category name
 */
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "grocery", "মুদি", "groceries" -> Icons.Default.ShoppingBasket
        "beverage", "beverages", "পানীয়", "drinks" -> Icons.Default.LocalDrink
        "snacks", "স্ন্যাক্স", "food" -> Icons.Default.Fastfood
        "medicine", "ওষুধ", "pharmacy" -> Icons.Default.LocalPharmacy
        "electronics", "ইলেকট্রনিক্স" -> Icons.Default.Devices
        "personal care", "ব্যক্তিগত যত্ন", "cosmetics" -> Icons.Default.Spa
        else -> Icons.Default.Category
    }
}

@Composable
fun NoPermissionScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Restricted",
                    tint = AlertRed,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "You do not have permission",
                    color = AlertRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Contact the store owner if you need access to this screen.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                if (onBack != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    GlassOutlinedButton(
                        text = "Go Back",
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        testTag = "no_permission_back_btn"
                    )
                }
            }
        }
    }
}

@Composable
fun ProductThumbnail(
    photoUri: String,
    category: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    cornerRadius: Dp = 14.dp
) {
    var loadFailed by remember(photoUri) { mutableStateOf(false) }
    val showImage = photoUri.isNotBlank() && !loadFailed

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0x20FFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = if (showImage) null else contentDescription,
            tint = VividGreen,
            modifier = Modifier.size(iconSize)
        )
        if (showImage) {
            AsyncImage(
                model = photoUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
