package com.radiantbyte.novaclient.overlay

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.radiantbyte.novaclient.game.ModuleCategory
import com.radiantbyte.novaclient.game.ModuleContent
import com.radiantbyte.novaclient.ui.component.NavigationRailX

class OverlayClickGUI : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags =
                flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) {
                blurBehindRadius = 15
            }

            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES

            dimAmount = 0.4f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedModuleCategory by mutableStateOf(ModuleCategory.Combat)

    @Composable
    override fun Content() {
        Column(
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    OverlayManager.dismissOverlayWindow(this)
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedCard(
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(40.dp)
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
            ) {
                Row(Modifier.fillMaxSize()) {
                    NavigationRailX(
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        ModuleCategory.entries.fastForEach { moduleCategory ->
                            NavigationRailItem(
                                selected = selectedModuleCategory === moduleCategory,
                                onClick = {
                                    if (selectedModuleCategory !== moduleCategory) {
                                        selectedModuleCategory = moduleCategory
                                    }
                                },
                                icon = {
                                    Icon(
                                        painterResource(moduleCategory.iconResId),
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(stringResource(moduleCategory.labelResId))
                                },
                                alwaysShowLabel = false
                            )
                        }
                    }
                    VerticalDivider()
                    DialogContent()
                }
            }
        }
    }

    @Composable
    private fun DialogContent() {
        AnimatedContent(
            targetState = selectedModuleCategory,
            label = "animatedPage",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) { moduleCategory ->
            Box(Modifier.fillMaxSize()) {
                ModuleContent(moduleCategory)
            }
        }
    }

}