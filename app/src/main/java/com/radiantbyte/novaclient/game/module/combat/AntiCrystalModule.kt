package com.radiantbyte.novaclient.game.module.combat


import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AntiCrystalModule : Module("anti_crystal", ModuleCategory.Combat) {

    private var ylevel by floatValue("ylevel", 0.4f, 0.1f..1.61f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            packet.position.add(0.0, -ylevel.toDouble(), 0.0)
        }
    }

}