package com.radiantbyte.novaclient.game

import com.radiantbyte.novaclient.game.InterceptablePacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface InterruptiblePacketHandler {

    fun beforePacketBound(interceptablePacket: InterceptablePacket)

    fun afterPacketBound(packet: BedrockPacket) {}

    fun beforeClientBound(interceptablePacket: InterceptablePacket) {
        beforePacketBound(interceptablePacket)
    }

    fun beforeServerBound(interceptablePacket: InterceptablePacket) {
        beforePacketBound(interceptablePacket)
    }

    fun afterClientBound(packet: BedrockPacket) {
        afterPacketBound(packet)
    }

    fun afterServerBound(packet: BedrockPacket) {
        afterPacketBound(packet)
    }

    fun onDisconnect(reason: String) {}

}