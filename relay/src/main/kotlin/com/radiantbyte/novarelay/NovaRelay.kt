package com.radiantbyte.novarelay

import com.radiantbyte.novarelay.NovaRelaySession.ClientSession
import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.address.inetSocketAddress
import com.radiantbyte.novarelay.config.EnhancedServerConfig
import com.radiantbyte.novarelay.connection.ConnectionManager
import com.radiantbyte.novarelay.util.ServerCompatUtils
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import kotlinx.coroutines.*
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption
import org.cloudburstmc.netty.handler.codec.raknet.server.RakServerRateLimiter
import org.cloudburstmc.protocol.bedrock.BedrockPeer
import org.cloudburstmc.protocol.bedrock.BedrockPong
import org.cloudburstmc.protocol.bedrock.PacketDirection
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec
import org.cloudburstmc.protocol.bedrock.codec.v844.Bedrock_v844
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer
import kotlin.random.Random

class NovaRelay(
    val localAddress: NovaAddress = NovaAddress("0.0.0.0", 19132),
    val advertisement: BedrockPong = DefaultAdvertisement,
    val serverConfig: EnhancedServerConfig = EnhancedServerConfig.DEFAULT
) {

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {

        val DefaultCodec: BedrockCodec = Bedrock_v844.CODEC

        val DefaultAdvertisement: BedrockPong = BedrockPong()
            .edition("MCPE")
            .gameType("Survival")
            .version(DefaultCodec.minecraftVersion)
            .protocolVersion(DefaultCodec.protocolVersion)
            .motd("NovaRelay")
            .playerCount(0)
            .maximumPlayerCount(20)
            .subMotd("Nova Relay")
            .nintendoLimited(false)

    }

    @Suppress("MemberVisibilityCanBePrivate")
    val isRunning: Boolean
        get() = channelFuture != null

    private var channelFuture: ChannelFuture? = null

    var novaRelaySession: NovaRelaySession? = null
        internal set
    internal var connectionManager: ConnectionManager? = null

    var remoteAddress: NovaAddress? = null
        internal set

    fun capture(
        remoteAddress: NovaAddress = NovaAddress("geo.hivebedrock.network", 19132),
        onSessionCreated: NovaRelaySession.() -> Unit
    ): NovaRelay {
        if (isRunning) {
            return this
        }

        this.remoteAddress = remoteAddress

        if (ServerCompatUtils.isProtectedServer(remoteAddress)) {
            println("Protected server detected: ${remoteAddress.hostName}")
            val tips = ServerCompatUtils.getConnectionTips(remoteAddress)
            tips.forEach { println("  - $it") }

            val serverInfo = ServerCompatUtils.extractServerInfo(remoteAddress.hostName)
            if (serverInfo != null) {
                println("  - Server ID: ${serverInfo.serverId}")
                println("  - Domain: ${serverInfo.domain}")
            }
        }

        advertisement
            .ipv4Port(localAddress.port)
            .ipv6Port(localAddress.port)

        ServerBootstrap()
            .group(NioEventLoopGroup())
            .channelFactory(RakChannelFactory.server(NioDatagramChannel::class.java))
            .option(RakChannelOption.RAK_ADVERTISEMENT, advertisement.toByteBuf())
            .option(RakChannelOption.RAK_GUID, Random.nextLong())
            .childHandler(object : BedrockChannelInitializer<NovaRelaySession.ServerSession>() {

                override fun createSession0(peer: BedrockPeer, subClientId: Int): NovaRelaySession.ServerSession {
                    return NovaRelaySession(peer, subClientId, this@NovaRelay)
                        .also {
                            novaRelaySession = it
                            val config = if (remoteAddress != null && ServerCompatUtils.isProtectedServer(remoteAddress!!)) {
                                ServerCompatUtils.getRecommendedConfig(remoteAddress!!)
                            } else {
                                serverConfig
                            }
                            connectionManager = ConnectionManager(it, config)
                            it.onSessionCreated()
                        }
                        .server
                }

                override fun initSession(session: NovaRelaySession.ServerSession) {}

                override fun preInitChannel(channel: Channel) {
                    channel.attr(PacketDirection.ATTRIBUTE).set(PacketDirection.CLIENT_BOUND)
                    super.preInitChannel(channel)
                }

            })
            .localAddress(localAddress.inetSocketAddress)
            .bind()
            .awaitUninterruptibly()
            .also {
                it.channel().pipeline().remove(RakServerRateLimiter.NAME)
                channelFuture = it
            }

        return this
    }

    internal fun connectToServer(onSessionCreated: ClientSession.() -> Unit) {
        val manager = connectionManager ?: throw IllegalStateException("Connection manager not initialized")
        val address = remoteAddress ?: throw IllegalStateException("Remote address not set")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = manager.connectToServer(address, onSessionCreated)
                if (result.isFailure) {
                    println("Failed to connect to server: ${result.exceptionOrNull()?.message}")
                    result.exceptionOrNull()?.printStackTrace()
                    novaRelaySession?.server?.disconnect("Failed to connect to server: ${result.exceptionOrNull()?.message}")
                    novaRelaySession?.listeners?.forEach { listener ->
                        runCatching {
                            listener.onDisconnect("Connection failed: ${result.exceptionOrNull()?.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error during connection: ${e.message}")
                e.printStackTrace()
                novaRelaySession?.server?.disconnect("Connection error: ${e.message}")
            }
        }
    }

    suspend fun connectToServerAsync(onSessionCreated: ClientSession.() -> Unit): Result<ClientSession> {
        val manager = connectionManager ?: return Result.failure(IllegalStateException("Connection manager not initialized"))
        val address = remoteAddress ?: return Result.failure(IllegalStateException("Remote address not set"))

        return manager.connectToServer(address, onSessionCreated)
    }

    fun stop() {
        try {
            connectionManager?.cleanup()
            novaRelaySession?.client?.disconnect()
            novaRelaySession?.server?.disconnect()
            channelFuture?.channel()?.close()?.sync()
            channelFuture = null
            novaRelaySession = null
            connectionManager = null
        } catch (e: Exception) {
            println("Error stopping NovaRelay: ${e.message}")
        }
    }
}
