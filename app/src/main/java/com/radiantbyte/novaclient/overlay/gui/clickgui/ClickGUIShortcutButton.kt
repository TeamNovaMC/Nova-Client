package com.radiantbyte.novaclient.overlay.gui.clickgui

import android.content.Context
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.overlay.OverlayWindow
import com.radiantbyte.novaclient.ui.theme.ClickGUIColors
import com.radiantbyte.novaclient.util.translatedSelf
import kotlin.math.min

class ClickGUIShortcutButton(
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

        val backgroundColor by animateColorAsState(
            targetValue = if (module.isEnabled) {
                ClickGUIColors.ModuleEnabled.copy(alpha = 0.3f)
            } else {
                ClickGUIColors.PanelBackground
            },
            label = "bg_color"
        )

        val borderColor by animateColorAsState(
            targetValue = if (module.isEnabled) {
                ClickGUIColors.AccentColor
            } else {
                ClickGUIColors.PanelBorder
            },
            label = "border_color"
        )

        val textColor by animateColorAsState(
            targetValue = if (module.isEnabled) {
                ClickGUIColors.AccentColor
            } else {
                ClickGUIColors.PrimaryText
            },
            label = "text_color"
        )

        ElevatedCard(
            onClick = {
                module.isEnabled = !module.isEnabled
            },
            shape = RoundedCornerShape(8.dp),
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
                        color = backgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = module.name.translatedSelf,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 13.sp,
                    fontWeight = if (module.isEnabled) FontWeight.SemiBold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }

    private fun updateShortcut() {
        module.shortcutX = _layoutParams.x
        module.shortcutY = _layoutParams.y
    }
}
