package com.radiantbyte.novaclient.game

import com.radiantbyte.novarelay.listener.NovaRelayPacketListener
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface ComposedPacketHandler : NovaRelayPacketListener {

    fun beforePacketBound(packet: BedrockPacket): Boolean

    fun afterPacketBound(packet: BedrockPacket) {}

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        return beforePacketBound(packet)
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        return beforePacketBound(packet)
    }

    override fun afterClientBound(packet: BedrockPacket) {
        afterPacketBound(packet)
    }

    override fun afterServerBound(packet: BedrockPacket) {
        afterPacketBound(packet)
    }

}