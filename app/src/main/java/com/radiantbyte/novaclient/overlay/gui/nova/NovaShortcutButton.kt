package com.radiantbyte.novaclient.overlay.gui.nova

import android.content.Context
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.overlay.OverlayWindow
import com.radiantbyte.novaclient.ui.theme.NovaColors
import com.radiantbyte.novaclient.util.translatedSelf
import kotlin.math.min

class NovaShortcutButton(
    private val module: Module
) : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            windowAnimations = android.R.style.Animation_Toast
            x = module.shortcutX
            y = module.shortcutY
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        val configuration = LocalConfiguration.current
        val isLandScape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        LaunchedEffect(isLandScape) {
            _layoutParams.x = min(width, _layoutParams.x)
            _layoutParams.y = min(height, _layoutParams.y)
            windowManager.updateViewLayout(composeView, _layoutParams)
            updateShortcut()
        }

        val infiniteTransition = rememberInfiniteTransition(label = "shortcut_glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )

        ElevatedCard(
            onClick = {
                module.isEnabled = !module.isEnabled
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(5.dp)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        _layoutParams.x += (dragAmount.x).toInt()
                        _layoutParams.y += (dragAmount.y).toInt()
                        windowManager.updateViewLayout(
                            composeView, _layoutParams
                        )
                        updateShortcut()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = if (module.isEnabled) {
                            Brush.linearGradient(
                                colors = listOf(
                                    NovaColors.Primary.copy(alpha = 0.2f),
                                    NovaColors.Secondary.copy(alpha = 0.15f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    NovaColors.Surface.copy(alpha = 0.9f),
                                    NovaColors.SurfaceVariant.copy(alpha = 0.8f)
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (module.isEnabled) {
                            NovaColors.Primary.copy(alpha = glowAlpha)
                        } else {
                            NovaColors.OnSurfaceVariant.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = module.name.translatedSelf,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (module.isEnabled) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (module.isEnabled) NovaColors.Primary else NovaColors.OnSurface
                )
            }
        }
    }

    private fun updateShortcut() {
        module.shortcutX = _layoutParams.x
        module.shortcutY = _layoutParams.y
    }
}
