package com.radiantbyte.novaclient.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.WindowManager
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.ui.theme.NovaClientTheme
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object NovaOverlayManager {
    var context: Context? = null
        private set
    private var overlayButton: NovaOverlayButton? = null
    private var clickGUI: NovaClickGUI? = null
    private var moduleSettingsOverlay: NovaModuleSettingsOverlay? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        this.context = context
        isInitialized = true
    }

    fun showOverlayButton() {
        if (!isInitialized) return
        
        hideOverlayButton()
        overlayButton = NovaOverlayButton().apply {
            show(context!!)
        }
    }

    fun hideOverlayButton() {
        overlayButton?.hide()
        overlayButton = null
    }

    fun showClickGUI() {
        if (!isInitialized) return
        
        hideClickGUI()
        clickGUI = NovaClickGUI().apply {
            show(context!!)
        }
    }

    fun hideClickGUI() {
        clickGUI?.hide()
        clickGUI = null
    }

    fun showModuleSettings(module: Module) {
        if (!isInitialized) return
        
        hideModuleSettings()
        moduleSettingsOverlay = NovaModuleSettingsOverlay(module).apply {
            show(context!!)
        }
    }

    fun hideModuleSettings() {
        moduleSettingsOverlay?.hide()
        moduleSettingsOverlay = null
    }

    fun hide() {
        hideClickGUI()
        hideModuleSettings()
    }

    fun hideAll() {
        hideOverlayButton()
        hideClickGUI()
        hideModuleSettings()
    }

    fun dismissOverlayWindow(window: OverlayWindow) {
        when (window) {
            is NovaClickGUI -> hideClickGUI()
            is NovaModuleSettingsOverlay -> hideModuleSettings()
            is NovaOverlayButton -> hideOverlayButton()
        }
    }

    fun updateOverlayIcon() {
        overlayButton?.let { button ->
            context?.let { ctx ->
                try {
                    val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    windowManager.updateViewLayout(button.composeView, button.layoutParams)
                } catch (e: Exception) {
                    // Handle exception silently
                }
            }
        }
    }

    fun updateOverlayBorder() {
        overlayButton?.let { button ->
            context?.let { ctx ->
                try {
                    val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    windowManager.updateViewLayout(button.composeView, button.layoutParams)
                } catch (e: Exception) {
                    // Handle exception silently
                }
            }
        }
    }

}

// Extension functions for OverlayWindow
fun OverlayWindow.show(context: Context) {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val layoutParams = this.layoutParams
    val composeView = this.composeView

    composeView.setContent {
        NovaClientTheme {
            this@show.Content()
        }
    }

    val lifecycleOwner = this.lifecycleOwner
    lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    composeView.setViewTreeLifecycleOwner(lifecycleOwner)
    composeView.setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore
            get() = this@show.viewModelStore
    })
    composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
    composeView.compositionContext = this.recomposer

    if (this.firstRun) {
        this.composeScope.launch {
            this@show.recomposer.runRecomposeAndApplyChanges()
        }
        this.firstRun = false
    }

    try {
        windowManager.addView(composeView, layoutParams)
    } catch (_: Exception) {
    }
}

fun OverlayWindow.hide() {
    val context = NovaOverlayManager.context ?: return
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val composeView = this.composeView

    try {
        windowManager.removeView(composeView)
    } catch (_: Exception) {
    }
}
