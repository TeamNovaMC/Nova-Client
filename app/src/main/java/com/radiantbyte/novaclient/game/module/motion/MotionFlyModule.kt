package com.radiantbyte.novaclient.game.module.motion

import com.radiantbyte.novaclient.game.InterceptablePacket
import com.radiantbyte.novaclient.game.Module
import com.radiantbyte.novaclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class MotionFlyModule : Module("motion_fly", ModuleCategory.Motion) {

    private val verticalSpeedUp = floatValue("verticalUpSpeed", 11.77f, 1.0f..20.0f)
    private val verticalSpeedDown = floatValue("verticalDownSpeed", 16.55f, 1.0f..20.0f)
    private val motionInterval = floatValue("delay", 472.17f, 100.0f..600.0f)
    private var lastMotionTime = 0L
    private var jitterState = false
    private var canFly = false

    private val flyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(arrayOf(
                Ability.BUILD,
                Ability.MINE,
                Ability.DOORS_AND_SWITCHES,
                Ability.OPEN_CONTAINERS,
                Ability.ATTACK_PLAYERS,
                Ability.ATTACK_MOBS,
                Ability.MAY_FLY,
                Ability.FLY_SPEED,
                Ability.WALK_SPEED,
                Ability.OPERATOR_COMMANDS
            ))
            walkSpeed = 0.1f
            flySpeed = 2.19f
        })
    }

    private val resetAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(arrayOf(
                Ability.BUILD,
                Ability.MINE,
                Ability.DOORS_AND_SWITCHES,
                Ability.OPEN_CONTAINERS,
                Ability.ATTACK_PLAYERS,
                Ability.ATTACK_MOBS,
                Ability.OPERATOR_COMMANDS
            ))
            walkSpeed = 0.1f
            flySpeed = 0f
        })
    }

    private fun handleFlyAbilities(isEnabled: Boolean) {
        if (canFly != isEnabled) {
            flyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            resetAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            if (isEnabled) {
                session.clientBound(flyAbilitiesPacket)
            } else {
                session.clientBound(resetAbilitiesPacket)
            }
            canFly = isEnabled
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            handleFlyAbilities(isEnabled)
            if (isEnabled && System.currentTimeMillis() - lastMotionTime >= motionInterval.value) {
                val vertical = when {
                    packet.inputData.contains(PlayerAuthInputData.WANT_UP) -> verticalSpeedUp.value
                    packet.inputData.contains(PlayerAuthInputData.WANT_DOWN) -> -verticalSpeedDown.value
                    else -> 0f
                }
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(0f, vertical + (if (jitterState) 0.1f else -0.1f), 0f)
                }
                session.clientBound(motionPacket)
                jitterState = !jitterState
                lastMotionTime = System.currentTimeMillis()
            }
        }
    }
}