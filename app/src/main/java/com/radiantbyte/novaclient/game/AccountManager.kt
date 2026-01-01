package com.radiantbyte.novaclient.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonParser
import com.radiantbyte.novaclient.application.AppContext
import com.radiantbyte.novaclient.service.RealmsManager
import com.radiantbyte.novarelay.util.AuthUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.bedrock.BedrockAuthManager
import java.io.File
import java.util.concurrent.TimeUnit

object AccountManager {

    private const val GAME_VERSION = "1.21.131"

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + CoroutineName("AccountManagerCoroutine"))

    private val _accounts: MutableList<BedrockAuthManager> = mutableStateListOf()

    val accounts: List<BedrockAuthManager>
        get() = _accounts

    var selectedAccount: BedrockAuthManager? by mutableStateOf(null)
        private set

    private val TOKEN_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(30)

    private val TOKEN_REFRESH_THRESHOLD_MS = TimeUnit.HOURS.toMillis(2)

    init {
        val fetchedAccounts = fetchAccounts()

        _accounts.addAll(fetchedAccounts)
        selectedAccount = fetchSelectedAccount()

        RealmsManager.updateSession(selectedAccount)

        startTokenRefreshScheduler()
    }

    fun addAccount(authManager: BedrockAuthManager) {
        val displayName = getDisplayName(authManager)
        val existingAccount = _accounts.find { getDisplayName(it) == displayName }
        if (existingAccount != null) {
            _accounts.remove(existingAccount)
        }

        _accounts.add(authManager)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            try {
                val json = BedrockAuthManager.toJson(authManager)
                file.resolve("$displayName.json")
                    .writeText(AuthUtils.gson.toJson(json))
                println("Successfully saved account: $displayName")
            } catch (e: Exception) {
                println("Failed to save account: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun removeAccount(authManager: BedrockAuthManager) {
        _accounts.remove(authManager)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            val displayName = getDisplayName(authManager)
            file.resolve("$displayName.json").delete()
        }
    }

    fun selectAccount(authManager: BedrockAuthManager?) {
        selectedAccount = authManager

        RealmsManager.updateSession(authManager)

        coroutineScope.launch {
            val file = File(AppContext.instance.cacheDir, "accounts")
            file.mkdirs()

            runCatching {
                val selectedAccountFile = file.resolve("selectedAccount")
                if (authManager != null) {
                    selectedAccountFile.writeText(getDisplayName(authManager))
                } else {
                    selectedAccountFile.delete()
                }
            }
        }
    }

    fun getDisplayName(authManager: BedrockAuthManager): String {
        return try {
            authManager.minecraftCertificateChain.upToDate.identityDisplayName
        } catch (e: Exception) {
            try {
                authManager.minecraftCertificateChain.cached.identityDisplayName
            } catch (e2: Exception) {
                "Unknown"
            }
        }
    }

    private fun fetchAccounts(): List<BedrockAuthManager> {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val accounts = ArrayList<BedrockAuthManager>()
        val listFiles = file.listFiles() ?: emptyArray()
        val httpClient = MinecraftAuth.createHttpClient()
        
        for (child in listFiles) {
            runCatching {
                if (child.isFile && child.extension == "json") {
                    val account = BedrockAuthManager.fromJson(
                        httpClient,
                        GAME_VERSION,
                        JsonParser.parseString(child.readText()).asJsonObject
                    )
                    accounts.add(account)
                    println("Loaded account ${getDisplayName(account)}")
                }
            }.onFailure {
                println("Failed to load account from ${child.name}: ${it.message}")
            }
        }

        return accounts
    }

    private fun fetchSelectedAccount(): BedrockAuthManager? {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        val selectedAccountFile = file.resolve("selectedAccount")
        if (!selectedAccountFile.exists() || selectedAccountFile.isDirectory) {
            return null
        }

        val displayName = selectedAccountFile.readText()
        return accounts.find { getDisplayName(it) == displayName }
    }

    private fun startTokenRefreshScheduler() {
        coroutineScope.launch {
            while (true) {
                try {
                    refreshExpiredTokens()
                } catch (e: Exception) {
                    println("Error during token refresh: ${e.message}")
                    e.printStackTrace()
                }

                delay(TOKEN_REFRESH_INTERVAL_MS)
            }
        }
    }

    private fun refreshExpiredTokens() {
        if (_accounts.isEmpty()) {
            return
        }

        val accountsToRefresh = _accounts.filter { account ->
            shouldRefreshToken(account)
        }

        if (accountsToRefresh.isNotEmpty()) {
            println("Found ${accountsToRefresh.size} accounts that need token refresh")
        }

        accountsToRefresh.forEach { account ->
            try {
                val displayName = getDisplayName(account)
                println("Refreshing token for account: $displayName")

                account.minecraftCertificateChain.refresh()

                saveAccountToDisk(account)

                if (selectedAccount == account) {
                    RealmsManager.updateSession(account)
                }

                println("Successfully refreshed token for: $displayName")
            } catch (e: Exception) {
                println("Failed to refresh token for ${getDisplayName(account)}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun shouldRefreshToken(account: BedrockAuthManager): Boolean {
        val currentTime = System.currentTimeMillis()

        try {
            val msaToken = account.msaToken.cached
            if (msaToken.expireTimeMs - currentTime < TOKEN_REFRESH_THRESHOLD_MS) {
                return true
            }
        } catch (e: Exception) {
            return true
        }

        try {
            val certChain = account.minecraftCertificateChain.cached
            if (certChain.expireTimeMs - currentTime < TOKEN_REFRESH_THRESHOLD_MS) {
                return true
            }
        } catch (e: Exception) {
        }

        try {
            val playFabToken = account.playFabToken.cached
            if (playFabToken.expireTimeMs - currentTime < TOKEN_REFRESH_THRESHOLD_MS) {
                return true
            }
        } catch (e: Exception) {
        }

        return false
    }

    private fun saveAccountToDisk(account: BedrockAuthManager) {
        val file = File(AppContext.instance.cacheDir, "accounts")
        file.mkdirs()

        try {
            val displayName = getDisplayName(account)
            val json = BedrockAuthManager.toJson(account)
            file.resolve("$displayName.json")
                .writeText(AuthUtils.gson.toJson(json))
        } catch (e: Exception) {
            println("Failed to save account: ${e.message}")
            e.printStackTrace()
        }
    }
}
