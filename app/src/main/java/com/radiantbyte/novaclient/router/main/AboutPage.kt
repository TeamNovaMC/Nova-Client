package com.radiantbyte.novaclient.router.main

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.radiantbyte.novaclient.ui.component.*
import com.radiantbyte.novaclient.R
import com.radiantbyte.novaclient.ui.theme.NovaColors
import com.radiantbyte.novaclient.util.LocalSnackbarHostState
import com.radiantbyte.novaclient.util.SnackbarHostStateScope
import androidx.core.net.toUri
import com.radiantbyte.novaclient.ui.component.authId
import com.radiantbyte.novaclient.BuildConfig

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AboutPageContent() {
    SnackbarHostStateScope {
        val snackbarHostState = LocalSnackbarHostState.current
        val context = LocalContext.current

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.about),
                                style = MaterialTheme.typography.headlineMedium,
                                color = NovaColors.OnSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NovaColors.Background,
                        titleContentColor = NovaColors.OnSurface
                    )
                )
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.animateContentSize()
                )
            },
            containerColor = NovaColors.Background
        ) {
            Box(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // App Info Section
                    NovaGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NovaColors.Primary,
                        glowIntensity = 0.4f
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Nova Client",
                                style = MaterialTheme.typography.headlineLarge,
                                color = NovaColors.Primary
                            )
                            Text(
                                "A powerful and feature-rich Minecraft client designed for enhanced gameplay experience.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = NovaColors.OnSurface
                            )
                            Text(
                                "Version: ${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NovaColors.OnSurfaceVariant
                            )
                        }
                    }

                    // License Section
                    NovaGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NovaColors.Secondary,
                        glowIntensity = 0.3f
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "License",
                                style = MaterialTheme.typography.headlineMedium,
                                color = NovaColors.Secondary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "✅ Permitted Uses",
                                style = MaterialTheme.typography.titleSmall,
                                color = NovaColors.Accent
                            )
                            Text(
                                "• Personal use and modification\n• Creating content (e.g., videos or showcases) using Nova Client\n• Redistributing the original or modified source code, provided you include the same GPLv3 license and make the source code available",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaColors.OnSurface
                            )

                            Text(
                                "❌ Prohibited Uses",
                                style = MaterialTheme.typography.titleSmall,
                                color = NovaColors.Error
                            )
                            Text(
                                "• Distributing modified versions without including source code and the GPLv3 license\n• Claiming ownership of the project or its original source code",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaColors.OnSurface
                            )
                        }
                    }

                    NovaGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NovaColors.OnSurfaceVariant,
                        glowIntensity = 0.2f
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "Legal",
                                style = MaterialTheme.typography.headlineMedium,
                                color = NovaColors.Primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                            Text(
                                "Disclaimer of Warranty",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaColors.OnSurface
                            )
                            Text(
                                "This program is distributed under the terms of the GNU General Public License, version 3 (GPLv3). It is provided \"AS IS\", without any warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose, and noninfringement.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaColors.OnSurfaceVariant
                            )

                            Text(
                                "Limitation of Liability",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaColors.OnSurface
                            )
                            Text(
                                "In no event shall the author(s) or copyright holder(s) be liable for any claim, damages, or other liability, whether in an action of contract, tort, or otherwise, arising from, out of, or in connection with the software or the use or other dealings in the software.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaColors.OnSurfaceVariant
                            )

                            Text(
                                "Intended Use",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaColors.OnSurface
                            )
                            Text(
                                "This software is intended solely for educational and research purposes. Any use of this program that violates applicable laws, terms of service, or causes harm to others is strictly unintended and the responsibility of the user.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaColors.OnSurfaceVariant
                            )
                        }
                    }

                    NovaGlassCard(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/mucute-qwq".toUri()
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NovaColors.Primary,
                        glowIntensity = 0.15f
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "🙏 Acknowledgements",
                                style = MaterialTheme.typography.titleMedium,
                                color = NovaColors.OnSurface
                            )
                            Text(
                                "Special thanks to ",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaColors.OnSurfaceVariant
                            )
                            Text(
                                "一剪沐橙 — this project would not have been possible without her contributions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaColors.OnSurfaceVariant
                            )
                            Text(
                                "View on GitHub",
                                style = MaterialTheme.typography.labelSmall,
                                color = NovaColors.Secondary
                            )
                        }
                    }

                    NovaGlassCard(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://github.com/TeamNovaMC/Nova-Client".toUri()
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NovaColors.Secondary,
                        glowIntensity = 0.3f
                    ) {
                        Row(
                            Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "View Source Code",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NovaColors.OnSurface
                                )
                                Text(
                                    "GitHub Repository",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NovaColors.OnSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = NovaColors.Secondary
                            )
                        }
                    }
                }
            }
        }
    }
}