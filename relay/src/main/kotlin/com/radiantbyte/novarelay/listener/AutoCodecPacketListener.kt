package com.radiantbyte.novarelay.listener

import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.codec.CodecRegistry
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
            return CodecRegistry.getClosestCodec(protocolVersion)
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
                val bedrockCodec = patchCodecIfNeeded(fetchCodecForProtocol(packet.protocolVersion))

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
            } catch (e: Exception) {
                e.printStackTrace()
                novaRelaySession.server.disconnect("Failed to setup network settings: ${e.message}")
                return true
            }
            return true
        }
        return false
    }
}