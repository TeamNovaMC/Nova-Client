package com.radiantbyte.novaclient.game.module.world

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import com.radiantbyte.novaclient.game.data.Effect
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket

class AntiDebuffModule : Module("anti_debuff", ModuleCategory.World) {

    private val badEffects = listOf(
        Effect.POISON,
        Effect.WITHER,
        Effect.FATAL_POISON,
        Effect.BLINDNESS,
        Effect.NAUSEA,
        Effect.WEAKNESS,
        Effect.MINING_FATIGUE,
        Effect.HUNGER,
        Effect.INSTANT_DAMAGE,
        Effect.SLOWNESS,
        Effect.DARKNESS,
        Effect.BAD_OMEN
    )

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is MobEffectPacket) {
            if (packet.runtimeEntityId == session.localPlayer.runtimeEntityId &&
                packet.event == MobEffectPacket.Event.ADD &&
                packet.effectId in badEffects) {
                interceptablePacket.intercept()
                
                session.clientBound(MobEffectPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    event = MobEffectPacket.Event.REMOVE
                    effectId = packet.effectId
                })
            }
        }
    }
}
