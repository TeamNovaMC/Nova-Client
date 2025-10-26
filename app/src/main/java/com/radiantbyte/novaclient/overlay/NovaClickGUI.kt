package com.radiantbyte.novaclient.overlay

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.radiantbyte.novaclient.game.ModuleCategory
import com.radiantbyte.novaclient.game.ModuleManager
import com.radiantbyte.novaclient.ui.theme.NovaColors
import com.radiantbyte.novaclient.util.translatedSelf

class NovaClickGUI : OverlayWindow() {

    override val layoutParams by lazy {
        WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = android.graphics.PixelFormat.TRANSLUCENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alpha = 1.0f
                blurBehindRadius = 20
                setFitInsetsTypes(0)
                setFitInsetsSides(0)
            }
        }
    }

    @Composable
    override fun Content() {
        var selectedCategory by remember { mutableStateOf(ModuleCategory.Combat) }
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isVisible = true
        }

        // Completely transparent background - no shade or visual elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    NovaOverlayManager.dismissOverlayWindow(this@NovaClickGUI)
                }
        ) {
            // Main GUI container with Nova entrance animation
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category sidebar with Nova theme
                    NovaCategorySidebar(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        modifier = Modifier.zIndex(2f)
                    )

                    // Module panel with Nova theme
                    NovaModulePanel(
                        category = selectedCategory,
                        modifier = Modifier.zIndex(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NovaCategorySidebar(
    selectedCategory: ModuleCategory,
    onCategorySelected: (ModuleCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "sidebar_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .width(180.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NovaColors.Surface.copy(alpha = 0.95f),
                        NovaColors.SurfaceVariant.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        NovaColors.Primary.copy(alpha = glowAlpha),
                        NovaColors.Secondary.copy(alpha = glowAlpha * 0.7f),
                        NovaColors.Accent.copy(alpha = glowAlpha * 0.5f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nova header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nova",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Light,
                    color = NovaColors.Primary.copy(alpha = glowAlpha + 0.6f)
                )

                IconButton(
                    onClick = {
                        NovaOverlayManager.hide()
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NovaColors.OnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                color = NovaColors.Primary.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Category buttons
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ModuleCategory.entries) { category ->
                    NovaCategoryButton(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Nova footer
            Text(
                text = "Enhanced Gaming",
                style = MaterialTheme.typography.bodySmall,
                color = NovaColors.OnSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun NovaCategoryButton(
    category: ModuleCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            NovaColors.Primary.copy(alpha = 0.2f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(300),
        label = "category_bg"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) {
            NovaColors.Primary.copy(alpha = 0.6f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(300),
        label = "category_border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                color = animatedBackgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isSelected) NovaColors.Primary else NovaColors.OnSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) NovaColors.Primary else NovaColors.OnSurface
            )
        }
    }
}

@Composable
private fun NovaModulePanel(
    category: ModuleCategory,
    modifier: Modifier = Modifier
) {
    val modules = remember(category) {
        ModuleManager.modules.filter { it.category == category }
    }

    Box(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NovaColors.Surface.copy(alpha = 0.95f),
                        NovaColors.SurfaceVariant.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        NovaColors.Secondary.copy(alpha = 0.4f),
                        NovaColors.Accent.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NovaColors.OnSurface
                )

                Badge(
                    containerColor = NovaColors.Primary.copy(alpha = 0.2f),
                    contentColor = NovaColors.Primary
                ) {
                    Text(
                        text = "${modules.size}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            HorizontalDivider(color = NovaColors.OnSurface.copy(alpha = 0.1f))

            // Module list
            if (modules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No modules available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = NovaColors.OnSurfaceVariant
                        )
                        Text(
                            text = "This category is empty",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaColors.OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modules) { module ->
                        NovaModuleItem(
                            module = module,
                            onToggle = {
                                module.isEnabled = !module.isEnabled
                            },
                            onSettings = {
                                NovaOverlayManager.showModuleSettings(module)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NovaModuleItem(
    module: com.radiantbyte.novaclient.game.Module,
    onToggle: () -> Unit,
    onSettings: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "module_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onToggle()
            }
            .animateContentSize()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .background(
                brush = if (module.isEnabled) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            NovaColors.Primary.copy(alpha = 0.1f),
                            NovaColors.Secondary.copy(alpha = 0.05f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            NovaColors.SurfaceVariant.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (module.isEnabled) {
                    NovaColors.Primary.copy(alpha = 0.4f)
                } else {
                    NovaColors.OnSurface.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = module.name.translatedSelf,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (module.isEnabled) NovaColors.Primary else NovaColors.OnSurface
                )
                Text(
                    text = "Module configuration available",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaColors.OnSurfaceVariant
                )
            }

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings icon
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Module Settings",
                        tint = if (module.values.isNotEmpty())
                            NovaColors.Secondary.copy(alpha = 0.8f)
                        else
                            NovaColors.OnSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Toggle switch with Nova glow
                Box {
                    Switch(
                        checked = module.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaColors.Primary,
                            checkedTrackColor = NovaColors.Primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = NovaColors.OnSurfaceVariant,
                            uncheckedTrackColor = NovaColors.OnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    // Nova glow effect when enabled
                    if (module.isEnabled) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            NovaColors.Primary.copy(alpha = 0.3f),
                                            Color.Transparent
                                        ),
                                        radius = 40f
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }
            }
        }
    }

    // Reset pressed state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}
