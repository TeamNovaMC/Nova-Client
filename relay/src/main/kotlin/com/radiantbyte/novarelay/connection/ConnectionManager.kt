package com.radiantbyte.novarelay.connection

import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.address.inetSocketAddress
import com.radiantbyte.novarelay.client.ClientIdentification
import com.radiantbyte.novarelay.config.ServerConfig
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption
import org.cloudburstmc.protocol.bedrock.BedrockPeer
import org.cloudburstmc.protocol.bedrock.PacketDirection
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer
import io.netty.bootstrap.Bootstrap

class ConnectionManager(
    private val novaRelaySession: NovaRelaySession,
    private val serverConfig: ServerConfig = ServerConfig()
) {

    private var isConnecting = false
    private var eventLoopGroup: NioEventLoopGroup? = null

    fun cleanup() {
        eventLoopGroup?.shutdownGracefully()
        eventLoopGroup = null
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
            var lastException: Exception? = null

            for (attempt in 0 until serverConfig.maxRetryAttempts) {
                try {
                    val clientSession = attemptConnection(remoteAddress, onSessionCreated)
                    return@withContext Result.success(clientSession)
                } catch (e: Exception) {
                    lastException = e
                    if (shouldNotRetry(e)) break
                    if (attempt < serverConfig.maxRetryAttempts - 1) {
                        delay(serverConfig.retryDelay)
                    }
                }
            }

            Result.failure(lastException ?: Exception("Connection failed after ${serverConfig.maxRetryAttempts} attempts"))

        } finally {
            isConnecting = false
        }
    }

    private suspend fun attemptConnection(
        remoteAddress: NovaAddress,
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
            .option(RakChannelOption.RAK_CONNECT_TIMEOUT, serverConfig.connectionTimeout)
            .option(RakChannelOption.RAK_SESSION_TIMEOUT, serverConfig.sessionTimeout)
            .option(RakChannelOption.RAK_COMPATIBILITY_MODE, true)
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
            delay(serverConfig.connectionTimeout + 10000)
            if (!continuation.isCompleted) {
                connectFuture.cancel(true)
                continuation.resumeWithException(Exception("Connection timeout after ${serverConfig.connectionTimeout}ms"))
            }
        }

        connectFuture.addListener { future ->
            timeoutJob.cancel()
            if (!future.isSuccess && !continuation.isCompleted) {
                continuation.resumeWithException(
                    future.cause() ?: Exception("Connection failed")
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