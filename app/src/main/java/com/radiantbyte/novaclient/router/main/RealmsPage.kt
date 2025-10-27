package com.radiantbyte.novaclient.router.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.radiantbyte.novaclient.game.AccountManager
import com.radiantbyte.novaclient.service.RealmsManager
import com.radiantbyte.novaclient.ui.component.NovaRealmsSection
import com.radiantbyte.novaclient.ui.theme.NovaColors
import com.radiantbyte.novaclient.viewmodel.MainScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealmsPageContent() {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val realmsState by RealmsManager.realmsState.collectAsStateWithLifecycle()

    LaunchedEffect(AccountManager.selectedAccount) {
        val selectedAccount = AccountManager.selectedAccount
        println("RealmsPage: Selected account changed: ${selectedAccount?.mcChain?.displayName}")
        println("RealmsPage: Account has Realms support: ${selectedAccount?.realmsXsts != null}")
        RealmsManager.updateSession(selectedAccount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Realms",
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NovaColors.Background
                )
            )
        },
        containerColor = NovaColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NovaRealmsSection(
                realmsState = realmsState,
                onRealmSelect = { host, port ->
                    val portInt = port.toIntOrNull() ?: 19132
                    println("RealmsPage: Connecting to Realm at $host:$portInt")

                    val currentModel = mainScreenViewModel.captureModeModel.value
                    val updatedModel = currentModel.copy(
                        serverHostName = host,
                        serverPort = portInt
                    ).withAutoDetectedServerConfig()
                    mainScreenViewModel.selectCaptureModeModel(updatedModel)

                    println("RealmsPage: Updated game settings - Host: $host, Port: $portInt")
                },
                onRefresh = {
                    RealmsManager.refreshRealms()
                }
            )
        }
    }
}