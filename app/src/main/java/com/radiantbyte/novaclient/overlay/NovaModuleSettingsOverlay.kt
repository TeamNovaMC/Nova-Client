package com.radiantbyte.novaclient.overlay

import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.radiantbyte.novaclient.game.*
import com.radiantbyte.novaclient.ui.theme.NovaColors
import com.radiantbyte.novaclient.util.translatedSelf

class NovaModuleSettingsOverlay(
    private val module: Module
) : OverlayWindow() {

    override val layoutParams: WindowManager.LayoutParams by lazy {
        WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = android.graphics.PixelFormat.TRANSLUCENT
        }
    }

    @Composable
    override fun Content() {
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isVisible = true
        }

        // Transparent background - no visual elements behind settings
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        NovaOverlayManager.dismissOverlayWindow(this@NovaModuleSettingsOverlay)
                    }
                }
        ) {
            // Main settings container
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(400)) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(400)
                ),
                exit = fadeOut(animationSpec = tween(300)) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.Center)
            ) {
                NovaSettingsPanel(
                    module = module,
                    onDismiss = {
                        NovaOverlayManager.dismissOverlayWindow(this@NovaModuleSettingsOverlay)
                    }
                )
            }
        }
    }
}

@Composable
private fun NovaSettingsPanel(
    module: Module,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(480.dp)
            .heightIn(max = 320.dp)
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
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        NovaColors.Primary.copy(alpha = 0.6f),
                        NovaColors.Secondary.copy(alpha = 0.4f),
                        NovaColors.Accent.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* Prevent click through */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nova header
            NovaSettingsHeader(
                module = module,
                onDismiss = onDismiss
            )

            // Divider with Nova glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                NovaColors.Primary.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Configuration section
            if (module.values.isEmpty()) {
                NovaEmptyState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(module.values) { value ->
                        NovaConfigurationItem(value = value)
                    }
                }
            }
        }
    }
}

@Composable
private fun NovaSettingsHeader(
    module: Module,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = module.name.translatedSelf,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                color = NovaColors.Primary
            )
            Text(
                text = module.category.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = NovaColors.OnSurfaceVariant
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = NovaColors.OnSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NovaEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No Configuration Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Light,
                color = NovaColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = "This module doesn't have any configurable settings",
                style = MaterialTheme.typography.bodyMedium,
                color = NovaColors.OnSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NovaConfigurationItem(value: Value<*>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NovaColors.SurfaceVariant.copy(alpha = 0.4f),
                        NovaColors.Surface.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = NovaColors.Primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Value name
            Text(
                text = value.name.translatedSelf,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = NovaColors.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Configuration control based on type
            when (value) {
                is BoolValue -> {
                    NovaBooleanConfiguration(value)
                }
                is FloatValue -> {
                    NovaFloatConfiguration(value)
                }
                is IntValue -> {
                    NovaIntConfiguration(value)
                }
                is ListValue -> {
                    NovaListConfiguration(value)
                }
                else -> {
                    Text(
                        text = "Unsupported type",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaColors.OnSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NovaBooleanConfiguration(value: BoolValue) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (value.value) "ENABLED" else "DISABLED",
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.value) NovaColors.Secondary else NovaColors.OnSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Switch(
            checked = value.value,
            onCheckedChange = { value.value = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = NovaColors.Secondary,
                checkedTrackColor = NovaColors.Secondary.copy(alpha = 0.5f),
                uncheckedThumbColor = NovaColors.OnSurfaceVariant,
                uncheckedTrackColor = NovaColors.OnSurfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun NovaFloatConfiguration(value: FloatValue) {
    var sliderValue by remember(value.value) { mutableFloatStateOf(value.value) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "%.2f".format(sliderValue),
            style = MaterialTheme.typography.bodyMedium,
            color = NovaColors.Secondary,
            fontWeight = FontWeight.Medium
        )

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { value.value = sliderValue },
            valueRange = value.range,
            colors = SliderDefaults.colors(
                thumbColor = NovaColors.Secondary,
                activeTrackColor = NovaColors.Secondary,
                inactiveTrackColor = NovaColors.OnSurfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.height(32.dp)
        )
    }
}

@Composable
private fun NovaIntConfiguration(value: IntValue) {
    var sliderValue by remember(value.value) { mutableIntStateOf(value.value) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = sliderValue.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = NovaColors.Secondary,
            fontWeight = FontWeight.Medium
        )

        Slider(
            value = sliderValue.toFloat(),
            onValueChange = { sliderValue = it.toInt() },
            onValueChangeFinished = { value.value = sliderValue },
            valueRange = value.range.start.toFloat()..value.range.endInclusive.toFloat(),
            steps = if (value.range.endInclusive - value.range.start > 1) value.range.endInclusive - value.range.start - 1 else 0,
            colors = SliderDefaults.colors(
                thumbColor = NovaColors.Secondary,
                activeTrackColor = NovaColors.Secondary,
                inactiveTrackColor = NovaColors.OnSurfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.height(32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaListConfiguration(value: ListValue) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value.value.name,
            onValueChange = { },
            readOnly = true,
            placeholder = {
                Text(
                    "Select option...",
                    color = NovaColors.OnSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NovaColors.Secondary,
                unfocusedBorderColor = NovaColors.OnSurfaceVariant.copy(alpha = 0.3f),
                cursorColor = NovaColors.Secondary,
                focusedTextColor = NovaColors.OnSurface,
                unfocusedTextColor = NovaColors.OnSurface
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NovaColors.Surface.copy(alpha = 0.95f),
                            NovaColors.SurfaceVariant.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            value.listItems.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.name,
                            color = if (option == value.value) NovaColors.Secondary else NovaColors.OnSurface,
                            fontWeight = if (option == value.value) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        expanded = false
                        if (option != value.value) {
                            value.value = option
                        }
                    }
                )
            }
        }
    }
}
