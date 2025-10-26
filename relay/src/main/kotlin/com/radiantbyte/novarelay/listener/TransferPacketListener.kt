package com.radiantbyte.novarelay.listener

import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.address.NovaAddress
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket

@Suppress("MemberVisibilityCanBePrivate")
class TransferPacketListener(
    val novaRelaySession: NovaRelaySession
) : NovaRelayPacketListener {

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is TransferPacket) {
            val remoteAddress = NovaAddress(packet.address, packet.port)
            val localAddress = novaRelaySession.novaRelay.localAddress
            novaRelaySession.novaRelay.remoteAddress = remoteAddress
            novaRelaySession.clientBoundImmediately(TransferPacket().apply {
                address = localAddress.hostName
                port = localAddress.port
            })

            novaRelaySession.novaRelay.novaRelaySession = null
            return true
        }
        return false
    }

}