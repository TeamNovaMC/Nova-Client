package com.radiantbyte.novaclient.game.world

import com.radiantbyte.novaclient.game.GameSession
import com.radiantbyte.novaclient.game.entity.Entity
import com.radiantbyte.novaclient.game.entity.EntityUnknown
import com.radiantbyte.novaclient.game.entity.Item
import com.radiantbyte.novaclient.game.entity.Player
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.packet.TakeItemEntityPacket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Suppress("MemberVisibilityCanBePrivate")
class Level(val session: GameSession) {

    val entityMap = ConcurrentHashMap<Long, Entity>()

    val playerMap = ConcurrentHashMap<UUID, PlayerListPacket.Entry>()

    fun onDisconnect() {
        entityMap.clear()
        playerMap.clear()
    }

    fun onPacketBound(packet: BedrockPacket) {
        when (packet) {
            is StartGamePacket -> {
                entityMap.clear()
                playerMap.clear()
            }

            is AddEntityPacket -> {
                val entity = EntityUnknown(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.identifier
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                    handleSetAttribute(packet.attributes)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is AddItemEntityPacket -> {
                val entity = Item(packet.runtimeEntityId, packet.uniqueEntityId).apply {
                    move(packet.position)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is AddPlayerPacket -> {
                val entity = Player(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.uuid,
                    packet.username
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is RemoveEntityPacket -> {
                val entityToRemove =
                    entityMap.values.find { it.uniqueEntityId == packet.uniqueEntityId } ?: return
                entityMap.remove(entityToRemove.runtimeEntityId)
            }

            is TakeItemEntityPacket -> {
                entityMap.remove(packet.itemRuntimeEntityId)
            }

            is PlayerListPacket -> {
                val add = packet.action == PlayerListPacket.Action.ADD
                packet.entries.forEach {
                    if (add) {
                        playerMap[it.uuid] = it
                    } else {
                        playerMap.remove(it.uuid)
                    }
                }
            }

            else -> {
                entityMap.values.forEach { entity ->
                    entity.onPacketBound(packet)
                }
            }
        }
    }

}