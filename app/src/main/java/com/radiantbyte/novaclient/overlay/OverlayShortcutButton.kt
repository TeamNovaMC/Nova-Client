package com.radiantbyte.novaclient.overlay

import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.util.translatedSelf
import kotlin.math.min

class OverlayShortcutButton(
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
        val color by animateColorAsState(
            targetValue = if (module.isEnabled) MaterialTheme.colorScheme.primary else contentColorFor(
                MaterialTheme.colorScheme.surfaceContainerLow
            ),
            label = "animateColor"
        )

        LaunchedEffect(isLandScape) {
            _layoutParams.x = min(width, _layoutParams.x)
            _layoutParams.y = min(height, _layoutParams.y)
            windowManager.updateViewLayout(composeView, _layoutParams)
            updateShortcut()
        }

        ElevatedCard(
            onClick = {
                module.isEnabled = !module.isEnabled
            },
            shape = CircleShape,
            modifier = Modifier
                .padding(5.dp)
//                .border(
//                    width = if (module.isEnabled) 2.dp else 0.dp, // Show border only when enabled
//                    color = if (module.isEnabled) Color.Cyan else Color.Transparent, // Cyan border when enabled
//                    shape = CircleShape
//                )
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
            Text(
                module.name.translatedSelf,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                modifier = Modifier
                    .padding(10.dp)
            )
        }
    }

    private fun updateShortcut() {
        module.shortcutX = _layoutParams.x
        module.shortcutY = _layoutParams.y
    }
}