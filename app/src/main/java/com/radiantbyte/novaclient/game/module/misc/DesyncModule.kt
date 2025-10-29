package com.radiantbyte.novaclient.game.module.misc

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random

class DesyncModule : Module("desync", ModuleCategory.Misc) {

    private var isDesynced = false
    private val storedPackets = ConcurrentLinkedQueue<PlayerAuthInputPacket>()
    private val updateDelay = 1000L
    private val minResendInterval = 100L
    private val maxResendInterval = 300L

    override fun onEnabled() {
        super.onEnabled()
        isDesynced = true
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDisabled() {
        super.onDisabled()
        isDesynced = false

        GlobalScope.launch {
            delay(updateDelay)
            while (storedPackets.isNotEmpty()) {
                val packet = storedPackets.poll()
                if (packet != null) {
                    session.clientBound(packet)
                }
                delay(Random.nextLong(minResendInterval, maxResendInterval))
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isDesynced) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            storedPackets.add(packet)
            interceptablePacket.intercept()
        }
    }

}