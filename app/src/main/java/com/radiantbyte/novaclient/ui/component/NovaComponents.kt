package com.radiantbyte.novaclient.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.radiantbyte.novaclient.ui.theme.NovaColors

/**
 * Enhanced UI components with Nova aesthetics and liquid glass effects
 */

@Composable
fun NovaGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    glowColor: Color = NovaColors.Primary,
    glowIntensity: Float = 0.3f,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_animation")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = glowIntensity * 0.5f,
        targetValue = glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = glowColor.copy(alpha = glowAlpha),
            spotColor = glowColor.copy(alpha = glowAlpha)
        )
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    NovaColors.Surface.copy(alpha = 0.9f),
                    NovaColors.SurfaceVariant.copy(alpha = 0.7f)
                )
            ),
            shape = shape
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor.copy(alpha = glowAlpha),
                    Color.Transparent,
                    glowColor.copy(alpha = glowAlpha * 0.5f)
                )
            ),
            shape = shape
        )
        .clip(shape)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    }
}

@Composable
fun NovaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = NovaColors.Primary,
        contentColor = NovaColors.OnPrimary,
        disabledContainerColor = NovaColors.SurfaceVariant,
        disabledContentColor = NovaColors.OnSurfaceVariant
    ),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(
        defaultElevation = 6.dp,
        pressedElevation = 2.dp,
        disabledElevation = 0.dp
    ),
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "button_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = NovaColors.Primary.copy(alpha = if (enabled) glowAlpha else 0f),
                spotColor = NovaColors.Primary.copy(alpha = if (enabled) glowAlpha else 0f)
            ),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun NovaFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = NovaColors.Primary,
    contentColor: Color = NovaColors.OnPrimary,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = 8.dp,
        pressedElevation = 4.dp
    ),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = containerColor.copy(alpha = glowAlpha),
                spotColor = containerColor.copy(alpha = glowAlpha)
            ),
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
        content = content
    )
}