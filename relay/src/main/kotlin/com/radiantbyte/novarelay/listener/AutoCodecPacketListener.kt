package com.radiantbyte.novarelay.listener

import com.radiantbyte.novarelay.NovaRelay
import com.radiantbyte.novarelay.NovaRelaySession
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

        private val protocols = AutoCodecPacketListener::class.java
            .getResourceAsStream("protocol_mapping.txt")
            ?.bufferedReader()
            ?.use {
                it.readLines()
                    .map { version -> version.toInt() }
            } ?: emptyList()

        private fun fetchCodecIfClosest(
            protocolVersion: Int
        ): BedrockCodec {
            val closestProtocolVersion = protocols.findLast { currentProtocolVersion ->
                protocolVersion >= currentProtocolVersion
            } ?: NovaRelay.DefaultCodec.protocolVersion

            val bedrockCodecClass = Class.forName("org.cloudburstmc.protocol.bedrock.codec.v$closestProtocolVersion.Bedrock_v$closestProtocolVersion")
            val bedrockCodecField = bedrockCodecClass.getDeclaredField("CODEC")
            bedrockCodecField.isAccessible = true

            return bedrockCodecField.get(null) as BedrockCodec
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
                val bedrockCodec = patchCodecIfNeeded(fetchCodecIfClosest(protocolVersion))
                println("Fetched bedrock codec: ${bedrockCodec.protocolVersion} for protocol: $protocolVersion")

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