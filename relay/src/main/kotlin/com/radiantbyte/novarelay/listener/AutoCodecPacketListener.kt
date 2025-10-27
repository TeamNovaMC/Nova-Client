package com.radiantbyte.novarelay.listener

import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.codec.CodecRegistry
import com.radiantbyte.novarelay.codec.VersionDetector
import com.radiantbyte.novarelay.definition.Definitions
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec
import org.cloudburstmc.protocol.bedrock.codec.v729.serializer.InventoryContentSerializer_v729
import org.cloudburstmc.protocol.bedrock.codec.v729.serializer.InventorySlotSerializer_v729
import org.cloudburstmc.protocol.bedrock.data.EncodingSettings
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm
import org.cloudburstmc.protocol.bedrock.packet.*

@Suppress("MemberVisibilityCanBePrivate")
class AutoCodecPacketListener(
    val novaRelaySession: NovaRelaySession,
    val patchCodec: Boolean = true
) : NovaRelayPacketListener {

    companion object {

        private fun fetchCodecForProtocol(protocolVersion: Int): BedrockCodec {
            val codec = CodecRegistry.getClosestCodec(protocolVersion)
            
            val minecraftVersion = CodecRegistry.getMinecraftVersion(codec.protocolVersion)
            val supportedVersions = VersionDetector.getSupportedVersionsForProtocol(codec.protocolVersion)
            
            println("Selected codec for protocol $protocolVersion:")
            println("  - Codec protocol: ${codec.protocolVersion}")
            println("  - Minecraft version: ${codec.minecraftVersion}")
            if (minecraftVersion != null) {
                println("  - Primary version: $minecraftVersion")
            }
            if (supportedVersions.isNotEmpty()) {
                println("  - Supported versions: ${supportedVersions.joinToString(", ")}")
            }
            
            return codec
        }

    }

    private fun patchCodecIfNeeded(codec: BedrockCodec): BedrockCodec {
        return if (patchCodec && codec.protocolVersion > 729) {
            codec.toBuilder()
                .updateSerializer(InventoryContentPacket::class.java, InventoryContentSerializer_v729.INSTANCE)
                .updateSerializer(InventorySlotPacket::class.java, InventorySlotSerializer_v729.INSTANCE)
                .build()
        } else {
            codec
        }
    }

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is RequestNetworkSettingsPacket) {
            try {
                val protocolVersion = packet.protocolVersion
                val bedrockCodec = patchCodecIfNeeded(fetchCodecForProtocol(protocolVersion))
                println("Using bedrock codec: ${bedrockCodec.protocolVersion} for client protocol: $protocolVersion")

                novaRelaySession.server.codec = bedrockCodec
                novaRelaySession.server.peer.codecHelper.apply {
                    itemDefinitions = Definitions.itemDefinitions
                    blockDefinitions = Definitions.blockDefinitions
                    cameraPresetDefinitions = Definitions.cameraPresetDefinitions
                    encodingSettings = EncodingSettings.builder()
                        .maxListSize(Int.MAX_VALUE)
                        .maxByteArraySize(Int.MAX_VALUE)
                        .maxNetworkNBTSize(Int.MAX_VALUE)
                        .maxItemNBTSize(Int.MAX_VALUE)
                        .maxStringLength(Int.MAX_VALUE)
                        .build()
                }

                val networkSettingsPacket = NetworkSettingsPacket()
                networkSettingsPacket.compressionThreshold = 1
                networkSettingsPacket.compressionAlgorithm = PacketCompressionAlgorithm.ZLIB

                novaRelaySession.clientBoundImmediately(networkSettingsPacket)
                novaRelaySession.server.setCompression(PacketCompressionAlgorithm.ZLIB)
                println("Client enabled compression: ZLIB with threshold 1")
            } catch (e: Exception) {
                println("Failed to process network settings: ${e.message}")
                e.printStackTrace()
                novaRelaySession.server.disconnect("Failed to setup network settings: ${e.message}")
                return true
            }
            return true
        }
        return false
    }

}