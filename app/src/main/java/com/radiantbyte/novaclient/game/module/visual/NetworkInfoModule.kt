package com.radiantbyte.novaclient.game.module.visual

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import com.radiantbyte.novaclient.game.ActionBarManager
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

class NetworkInfoModule : Module("network_info", ModuleCategory.Visual) {

    private var lastDisplayTime = 0L
    private val displayInterval = 500L
    private val colorStyle by boolValue("colored_text", true)
    private val showPacketCounts by boolValue("show_packets", true)

    private var incomingPackets = 0
    private var outgoingPackets = 0
    private var lastPacketCountReset = 0L
    private val packetCountInterval = 1000L

    private var lastPingSentTime = 0L
    private var currentPing = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        incomingPackets++

        if (packet is PlayerAuthInputPacket) {
            if (lastPingSentTime > 0) {
                currentPing = System.currentTimeMillis() - lastPingSentTime
            }

            val currentTime = System.currentTimeMillis()

            if (currentTime - lastPacketCountReset >= packetCountInterval) {
                lastPacketCountReset = currentTime
                incomingPackets = 0
                outgoingPackets = 0
            }

            if (currentTime - lastDisplayTime >= displayInterval) {
                lastDisplayTime = currentTime

                val networkText = if (colorStyle) {
                    buildString {
                        append("§l§b[Network] §r")
                        append("§fPing: §a${currentPing}ms")
                        if (showPacketCounts) {
                            append(" §f| §fPackets: §a↑$outgoingPackets §c↓$incomingPackets")
                        }
                    }
                } else {
                    buildString {
                        append("Network: ")
                        append("Ping: ${currentPing}ms")
                        if (showPacketCounts) {
                            append(" | Packets: ↑$outgoingPackets ↓$incomingPackets")
                        }
                    }
                }

                ActionBarManager.updateModule("network", networkText)
                ActionBarManager.display(session)
            }
        }
    }

    override fun afterPacketBound(packet: BedrockPacket) {
        if (!isEnabled) return

        outgoingPackets++
        lastPingSentTime = System.currentTimeMillis()
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            ActionBarManager.removeModule("network")
            ActionBarManager.display(session)
        }
    }
}