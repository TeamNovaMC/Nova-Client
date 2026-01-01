package com.radiantbyte.novaclient.service

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.bedrock.BedrockAuthManager
import net.raphimc.minecraftauth.extra.realms.service.impl.BedrockRealmsService
import com.radiantbyte.novaclient.model.RealmWorld
import com.radiantbyte.novaclient.model.RealmConnectionDetails
import com.radiantbyte.novaclient.model.RealmState
import com.radiantbyte.novaclient.model.RealmsLoadingState
import java.util.concurrent.ConcurrentHashMap

object RealmsManager {

    private const val TAG = "RealmsManager"
    private const val CLIENT_VERSION = "1.21.120"

    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("RealmsManagerCoroutine"))

    private val _realmsState = MutableStateFlow<RealmsLoadingState>(RealmsLoadingState.NoAccount)
    val realmsState: StateFlow<RealmsLoadingState> = _realmsState.asStateFlow()

    private val connectionCache = ConcurrentHashMap<Long, RealmConnectionDetails>()

    private var currentAuthManager: BedrockAuthManager? = null
    private var realmsService: BedrockRealmsService? = null

    fun updateSession(authManager: BedrockAuthManager?) {
        currentAuthManager = authManager

        val displayName = try {
            authManager?.minecraftCertificateChain?.cached?.identityDisplayName
        } catch (e: Exception) {
            null
        }
        Log.d(TAG, "updateSession called with session: $displayName")

        if (authManager == null) {
            Log.w(TAG, "No auth manager available")
            realmsService = null
            _realmsState.value = RealmsLoadingState.NoAccount
            return
        }

        coroutineScope.launch {
            try {
                Log.d(TAG, "Initializing Realms service with client version: $CLIENT_VERSION")
                val httpClient = MinecraftAuth.createHttpClient()
                httpClient.connectTimeout = 10000
                httpClient.readTimeout = 10000

                realmsService = BedrockRealmsService(httpClient, CLIENT_VERSION, authManager.realmsXstsToken)
                Log.d(TAG, "Realms service initialized successfully")

                refreshRealmsInternal()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Realms service", e)
                _realmsState.value = RealmsLoadingState.Error("Failed to initialize Realms service: ${e.message}")
            }
        }
    }

    fun refreshRealms() {
        coroutineScope.launch {
            refreshRealmsInternal()
        }
    }

    private suspend fun refreshRealmsInternal() {
        val service = realmsService
        if (service == null) {
            Log.w(TAG, "refreshRealms called but realmsService is null")
            _realmsState.value = RealmsLoadingState.NoAccount
            return
        }

        Log.d(TAG, "Starting Realms refresh with client version: $CLIENT_VERSION")
        _realmsState.value = RealmsLoadingState.Loading

        try {
            Log.d(TAG, "Fetching Realms worlds...")
            val realmsServers = withContext(Dispatchers.IO) {
                service.worlds
            }
            val realmWorldList = realmsServers.map { RealmWorld.fromRealmsServer(it) }

            _realmsState.value = RealmsLoadingState.Success(realmWorldList)

            Log.d(TAG, "Successfully fetched ${realmWorldList.size} Realms")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch Realms", e)
            e.printStackTrace()
            
            val errorMessage = when {
                e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true ->
                    "Authentication failed. Please reconnect your Microsoft account."
                e.message?.contains("403") == true || e.message?.contains("Forbidden") == true ->
                    "Access denied. You may not have Realms access."
                e.message?.contains("timeout") == true || e.message?.contains("timed out") == true ->
                    "Connection timed out. Please check your internet connection."
                e.message?.contains("network") == true || e.message?.contains("connection") == true ->
                    "Network error. Please check your internet connection."
                e.message?.contains("Can't refresh") == true || e.message?.contains("sign in again") == true ->
                    "Session expired. Please re-login to your Microsoft account."
                else -> "Failed to fetch Realms: ${e.message ?: "Unknown error"}"
            }
            _realmsState.value = RealmsLoadingState.Error(errorMessage)
        }
    }

    fun getRealmConnectionDetails(realmId: Long, callback: (RealmConnectionDetails) -> Unit) {
        val service = realmsService
        if (service == null) {
            callback(RealmConnectionDetails.loading().withError("Realms service not available"))
            return
        }

        val cached = connectionCache[realmId]
        if (cached != null && !cached.isExpired() && cached.error == null) {
            callback(cached)
            return
        }

        val loadingDetails = RealmConnectionDetails.loading()
        connectionCache[realmId] = loadingDetails
        callback(loadingDetails)

        coroutineScope.launch {
            try {
                val currentState = _realmsState.value
                if (currentState !is RealmsLoadingState.Success) {
                    throw IllegalStateException("Realms not loaded")
                }

                val realm = currentState.realms.find { it.id == realmId }
                    ?: throw IllegalArgumentException("Realm not found")

                if (realm.expired) {
                    throw IllegalStateException("Realm has expired")
                }

                if (realm.state != RealmState.OPEN) {
                    throw IllegalStateException("Realm is not open (current state: ${realm.state})")
                }

                val realmsServers = withContext(Dispatchers.IO) {
                    service.worlds
                }
                val realmsServer = realmsServers.find { it.id == realmId }
                    ?: throw IllegalArgumentException("Realm not found in service")

                Log.d(TAG, "Requesting connection details for Realm ${realm.name} (ID: $realmId)")
                val joinInfo = withContext(Dispatchers.IO) {
                    service.joinWorld(realmsServer)
                }

                Log.d(TAG, "Received raw address from Realms service: '${joinInfo.address}'")

                if (joinInfo.address.isBlank()) {
                    throw IllegalStateException("Received empty address from Realms service")
                }

                val connectionDetails = try {
                    RealmConnectionDetails.fromAddress(joinInfo.address)
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, "Failed to parse address '${joinInfo.address}': ${e.message}")
                    throw IllegalStateException("Invalid address format received: ${joinInfo.address}")
                }

                connectionCache[realmId] = connectionDetails
                callback(connectionDetails)

                Log.d(TAG, "Successfully got connection details for Realm $realmId: ${connectionDetails.address}:${connectionDetails.port}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get connection details for Realm $realmId", e)
                val errorMessage = when {
                    e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true ->
                        "Authentication failed"
                    e.message?.contains("403") == true || e.message?.contains("Forbidden") == true ->
                        "Access denied to this Realm"
                    e.message?.contains("404") == true || e.message?.contains("Not Found") == true ->
                        "Realm not found or no longer available"
                    e.message?.contains("timeout") == true || e.message?.contains("timed out") == true ->
                        "Connection timed out"
                    e.message?.contains("network") == true || e.message?.contains("connection") == true ->
                        "Network error"
                    e is IllegalStateException -> e.message ?: "Realms not loaded"
                    e is IllegalArgumentException -> e.message ?: "Invalid realm"
                    else -> "Connection failed: ${e.message ?: "Unknown error"}"
                }
                val errorDetails = RealmConnectionDetails.loading().withError(errorMessage)
                connectionCache[realmId] = errorDetails
                callback(errorDetails)
            }
        }
    }
}
