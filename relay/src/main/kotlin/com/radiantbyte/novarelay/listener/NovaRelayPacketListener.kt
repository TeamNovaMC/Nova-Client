package com.radiantbyte.novarelay.listener

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface NovaRelayPacketListener {

    fun beforeClientBound(packet: BedrockPacket): Boolean {
        return false
    }

    fun beforeServerBound(packet: BedrockPacket): Boolean {
        return false
    }

    fun afterClientBound(packet: BedrockPacket) {}

    fun afterServerBound(packet: BedrockPacket) {}

    fun onDisconnect(reason: String) {}

}