package com.radiantbyte.novaclient.game

import com.radiantbyte.novaclient.game.InterceptablePacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface InterruptiblePacketHandler {

    fun beforePacketBound(interceptablePacket: InterceptablePacket)

    fun afterPacketBound(packet: BedrockPacket) {}

    fun onDisconnect(reason: String) {}

}