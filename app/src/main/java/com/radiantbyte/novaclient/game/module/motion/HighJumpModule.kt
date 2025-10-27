package com.radiantbyte.novaclient.game.module.motion

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class HighJumpModule : Module("high_jump", ModuleCategory.Motion) {

    private val jumpHeight by floatValue("jumpHeight", 0.85f, 0.4f..3.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {

            if (packet.motion.length() > 0.0 && packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {
                if (packet.inputData.contains(PlayerAuthInputData.JUMP_DOWN)) {
                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        motion = Vector3f.from(
                            session.localPlayer.motionX,
                            jumpHeight,
                            session.localPlayer.motionZ
                        )
                    }
                    session.clientBound(motionPacket)
                }
            }
        }
    }
}