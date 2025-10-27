package com.radiantbyte.novaclient.game.module.particle

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import net.kyori.adventure.text.Component
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.LevelEvent
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class BubbleParticleModule : Module("bubble_particle", ModuleCategory.Particle) {

    private val intervalValue by intValue("interval", 1500, 1000..2000)
    private val offsetY by floatValue("height_offset", 1.0f, -2.0f..5.0f)
    private val particleSize by floatValue("size", 1.0f, 0.1f..2.0f)
    private val particleCount by intValue("count", 1, 1..5)
    private val randomOffset by boolValue("random_offset", false)
    private val offsetRadius by floatValue("offset_radius", 1.0f, 0.1f..5.0f)

    private var lastParticleTime = 0L
    private var warningShown = false

    override fun onEnabled() {
        super.onEnabled()
        if (isSessionCreated && !warningShown) {
            sendMessage("§bThis particle effect only works when you are underwater!")
            warningShown = true
        }
    }

    private fun sendMessage(msg: String) {
        if (!isSessionCreated) return

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            message = msg
            xuid = ""
            sourceName = ""
        }
        session.clientBound(textPacket)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastParticleTime >= intervalValue) {
                lastParticleTime = currentTime

                repeat(particleCount) {
                    val offsetX = if (randomOffset) (Math.random() * 2 - 1) * offsetRadius else 0.0
                    val offsetZ = if (randomOffset) (Math.random() * 2 - 1) * offsetRadius else 0.0

                    session.clientBound(LevelEventPacket().apply {
                        type = LevelEvent.PARTICLE_BUBBLES
                        position = Vector3f.from(
                            packet.position.x + offsetX.toFloat(),
                            packet.position.y + offsetY,
                            packet.position.z + offsetZ.toFloat()
                        )
                        data = (particleSize * 1000).toInt()
                    })
                }
            }
        }
    }
}