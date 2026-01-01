package com.radiantbyte.novarelay.util

import com.radiantbyte.novarelay.NovaRelay
import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.codec.CodecRegistry
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.bedrock.BedrockAuthManager
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService
import org.cloudburstmc.protocol.bedrock.BedrockPong
import java.io.File
import java.nio.file.Paths
import java.util.function.Consumer

fun captureGamePacket(
    advertisement: BedrockPong = NovaRelay.DefaultAdvertisement,
    localAddress: NovaAddress = NovaAddress("0.0.0.0", 19132),
    remoteAddress: NovaAddress,
    onSessionCreated: NovaRelaySession.() -> Unit
): NovaRelay {
    CodecRegistry.getLatestCodec()
    
    return NovaRelay(
        localAddress = localAddress,
        advertisement = advertisement
    ).capture(
        remoteAddress = remoteAddress,
        onSessionCreated = onSessionCreated
    )
}

fun authorize(
    cache: Boolean = true,
    file: File? = Paths.get(".").resolve("bedrockSession.json").toFile(),
    gameVersion: String = "1.21.131",
    msaDeviceCodeCallback: Consumer<MsaDeviceCode> = Consumer {
        println("Go to ${it.directVerificationUri}")
    }
): BedrockAuthManager {
    val httpClient = MinecraftAuth.createHttpClient()

    if (cache && file != null && file.exists()) {
        val json = com.google.gson.JsonParser.parseString(file.readText()).asJsonObject
        return BedrockAuthManager.fromJson(httpClient, gameVersion, json)
    }

    val authManager = BedrockAuthManager.create(httpClient, gameVersion)
        .login({ client, config, callback -> DeviceCodeMsaAuthService(client, config, callback) }, msaDeviceCodeCallback)

    if (cache && file != null && !file.isDirectory) {
        val json = AuthUtils.gson.toJson(BedrockAuthManager.toJson(authManager))
        file.writeText(json)
    }

    return authManager
}

fun BedrockAuthManager.refresh(): BedrockAuthManager {
    this.minecraftCertificateChain.refresh()
    return this
}
