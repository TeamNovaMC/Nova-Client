package com.radiantbyte.novaclient.game.module.visual

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket

class NoHurtCameraModule : Module("no_hurt_camera", ModuleCategory.Visual) {

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is EntityEventPacket) {
            if (packet.runtimeEntityId == session.localPlayer.runtimeEntityId
                && packet.type == EntityEventType.HURT
            ) {
                interceptablePacket.intercept()
            }
        }
    }

}