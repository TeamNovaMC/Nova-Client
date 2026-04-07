package com.radiantbyte.novarelay.connection

import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.address.inetSocketAddress
import com.radiantbyte.novarelay.client.ClientIdentification
import com.radiantbyte.novarelay.config.ServerConfig
import com.radiantbyte.novarelay.util.ServerCompatUtils
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption
import org.cloudburstmc.protocol.bedrock.BedrockPeer
import org.cloudburstmc.protocol.bedrock.PacketDirection
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer
import io.netty.bootstrap.Bootstrap

class ConnectionManager(
    private val novaRelaySession: NovaRelaySession,
    private val serverConfig: ServerConfig = ServerConfig.DEFAULT
) {

    private val connectionAttempts = mutableMapOf<String, Long>()
    private val connectionCounts = mutableMapOf<String, Int>()
    private val rateLimitResetTime = mutableMapOf<String, Long>()
    private var isConnecting = false
    private var eventLoopGroup: NioEventLoopGroup? = null

    companion object {
        private const val RATE_LIMIT_WINDOW_MS = 60000L
        private const val MAX_CONNECTIONS_PER_WINDOW = 3
    }

    fun cleanup() {
        eventLoopGroup?.shutdownGracefully()
        eventLoopGroup = null
        connectionAttempts.clear()
        connectionCounts.clear()
        rateLimitResetTime.clear()
        isConnecting = false
    }

    suspend fun connectToServer(
        remoteAddress: NovaAddress,
        onSessionCreated: NovaRelaySession.ClientSession.() -> Unit
    ): Result<NovaRelaySession.ClientSession> = withContext(Dispatchers.IO) {

        if (isConnecting) {
            return@withContext Result.failure(IllegalStateException("Already connecting to a server"))
        }

        isConnecting = true

        try {
            val isProtected = ServerCompatUtils.isProtectedServer(remoteAddress)
            val config = if (isProtected) serverConfig else ServerConfig.FAST

            if (isProtected && config.enableConnectionThrottling) {
                applyConnectionThrottling(remoteAddress.hostName, config)
                applyRateLimiting(remoteAddress.hostName)
            }

            if (config.initialConnectionDelay > 0) {
                delay(config.initialConnectionDelay)
            }

            var lastException: Exception? = null

            for (attempt in 0 until config.maxRetryAttempts) {
                try {
                    val clientSession = attemptConnection(remoteAddress, config, onSessionCreated)
                    return@withContext Result.success(clientSession)
                } catch (e: Exception) {
                    lastException = e
                    if (shouldNotRetry(e)) break
                    if (attempt < config.maxRetryAttempts - 1) {
                        delay(config.calculateRetryDelay(attempt))
                    }
                }
            }

            Result.failure(lastException ?: Exception("Connection failed after ${config.maxRetryAttempts} attempts"))

        } finally {
            isConnecting = false
        }
    }

    private suspend fun applyConnectionThrottling(hostname: String, config: ServerConfig) {
        val lastAttempt = connectionAttempts[hostname]
        if (lastAttempt != null) {
            val timeSinceLastAttempt = System.currentTimeMillis() - lastAttempt
            if (timeSinceLastAttempt < config.timeBetweenConnectionAttempts) {
                delay(config.timeBetweenConnectionAttempts - timeSinceLastAttempt)
            }
        }
        connectionAttempts[hostname] = System.currentTimeMillis()
    }

    private suspend fun applyRateLimiting(hostname: String) {
        val currentTime = System.currentTimeMillis()
        val resetTime = rateLimitResetTime[hostname] ?: 0L

        if (currentTime > resetTime) {
            connectionCounts[hostname] = 0
            rateLimitResetTime[hostname] = currentTime + RATE_LIMIT_WINDOW_MS
        }

        val currentCount = connectionCounts[hostname] ?: 0

        if (currentCount >= MAX_CONNECTIONS_PER_WINDOW) {
            val waitTime = rateLimitResetTime[hostname]!! - currentTime
            if (waitTime > 0) {
                delay(waitTime)
                connectionCounts[hostname] = 0
                rateLimitResetTime[hostname] = System.currentTimeMillis() + RATE_LIMIT_WINDOW_MS
            }
        }

        connectionCounts[hostname] = currentCount + 1
    }

    private suspend fun attemptConnection(
        remoteAddress: NovaAddress,
        config: ServerConfig,
        onSessionCreated: NovaRelaySession.ClientSession.() -> Unit
    ): NovaRelaySession.ClientSession = suspendCancellableCoroutine { continuation ->

        if (eventLoopGroup == null || eventLoopGroup!!.isShuttingDown || eventLoopGroup!!.isShutdown) {
            eventLoopGroup = NioEventLoopGroup()
        }

        val bootstrap = Bootstrap()
            .group(eventLoopGroup)
            .channelFactory(RakChannelFactory.client(NioDatagramChannel::class.java))
            .option(RakChannelOption.RAK_PROTOCOL_VERSION, 11)
            .option(RakChannelOption.RAK_GUID, ClientIdentification.generateGUID())
            .option(RakChannelOption.RAK_CONNECT_TIMEOUT, config.connectionTimeout)
            .option(RakChannelOption.RAK_SESSION_TIMEOUT, config.sessionTimeout)
            .option(RakChannelOption.RAK_COMPATIBILITY_MODE, true)
            .option(RakChannelOption.RAK_UNCONNECTED_MAGIC, ClientIdentification.createUnconnectedMagic())
            .option(RakChannelOption.RAK_MTU, 1400)
            .handler(object : BedrockChannelInitializer<NovaRelaySession.ClientSession>() {

                override fun createSession0(peer: BedrockPeer, subClientId: Int): NovaRelaySession.ClientSession {
                    return novaRelaySession.ClientSession(peer, subClientId)
                }

                override fun initSession(clientSession: NovaRelaySession.ClientSession) {
                    novaRelaySession.client = clientSession
                    if (!continuation.isCompleted) {
                        continuation.resume(clientSession) {}
                    }
                    onSessionCreated(clientSession)
                }

                override fun preInitChannel(channel: Channel) {
                    channel.attr(PacketDirection.ATTRIBUTE).set(PacketDirection.SERVER_BOUND)
                    super.preInitChannel(channel)
                }
            })
            .remoteAddress(remoteAddress.inetSocketAddress)

        val connectFuture = bootstrap.connect()

        val timeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(config.connectionTimeout + 10000)
            if (!continuation.isCompleted) {
                connectFuture.cancel(true)
                continuation.resumeWithException(Exception("Connection timeout after ${config.connectionTimeout}ms"))
            }
        }

        connectFuture.addListener { future ->
            timeoutJob.cancel()
            if (!future.isSuccess && !continuation.isCompleted) {
                continuation.resumeWithException(
                    future.cause() ?: Exception("Connection failed for unknown reason")
                )
            }
        }

        continuation.invokeOnCancellation {
            timeoutJob.cancel()
            connectFuture.cancel(true)
        }
    }

    private fun shouldNotRetry(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("incompatible") || message.contains("already connected")
    }
}