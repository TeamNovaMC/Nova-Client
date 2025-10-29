package com.radiantbyte.novaclient.router.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.radiantbyte.novaclient.R
import com.radiantbyte.novaclient.ui.component.NovaNavItem
import com.radiantbyte.novaclient.ui.component.NovaSidebar
import com.radiantbyte.novaclient.ui.theme.NovaColors
import com.radiantbyte.novaclient.viewmodel.MainScreenViewModel

@Immutable
enum class MainScreenPages(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
) {
    HomePage(
        label = "Home",
        icon = Icons.Rounded.Home,
        content = { HomePageContent() }
    ),
    AccountPage(
        label = "Account",
        icon = Icons.Rounded.AccountCircle,
        content = { AccountPageContent() }
    ),
    ServerPage(
        label = "Servers",
        icon = Icons.Rounded.Storage,
        content = { ServerPageContent() }
    ),
    RealmsPage(
        label = "Realms",
        icon = Icons.Rounded.Cloud,
        content = { RealmsPageContent() }
    ),
    SettingsPage(
        label = "Settings",
        icon = Icons.Rounded.Settings,
        content = { SettingsPageContent() }
    ),
    AboutPage(
        label = "About",
        icon = Icons.Rounded.Info,
        content = { AboutPageContent() }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val selectedPage by mainScreenViewModel.selectedPage.collectAsStateWithLifecycle()

    // Create navigation items
    val navItems = remember {
        MainScreenPages.entries.map { page ->
            NovaNavItem(
                page = page,
                label = page.label,
                icon = page.icon
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaColors.Background)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                NovaSidebar(
                    selectedPage = selectedPage,
                    pages = navItems,
                    onPageSelected = { page ->
                        if (selectedPage != page) {
                            mainScreenViewModel.selectPage(page as MainScreenPages)
                        }
                    }
                )
            }

            // Main content area with enhanced animations
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AnimatedContent(
                    targetState = selectedPage,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(400)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(400))
                    },
                    label = "page_transition"
                ) { currentPage ->
                    currentPage.content()
                }
            }
        }
    }
}