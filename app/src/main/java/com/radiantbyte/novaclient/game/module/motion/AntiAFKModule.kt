package com.radiantbyte.novaclient.game.module.motion

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.random.Random

class AntiAFKModule : Module("anti_afk", ModuleCategory.Motion) {

    private val glitchInterval by intValue("interval", 200, 50..1000)
    private val intensity by floatValue("intensity", 0.5f, 0.1f..2.0f)

    private var lastGlitchTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastGlitchTime >= glitchInterval) {
                lastGlitchTime = currentTime

                val randomOffset = Vector3f.from(
                    (Random.nextFloat() - 0.5f) * intensity,
                    (Random.nextFloat() - 0.5f) * intensity,
                    (Random.nextFloat() - 0.5f) * intensity
                )

                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = randomOffset
                }
                session.clientBound(motionPacket)
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            val resetPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.ZERO
            }
            session.clientBound(resetPacket)
        }
    }
}
