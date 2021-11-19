package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TabConstants
import me.neznamy.tab.shared.TAB
import org.kryptonmc.krypton.packet.EntityPacket
import org.kryptonmc.krypton.packet.out.play.PacketOutDestroyEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutEntityPosition
import org.kryptonmc.krypton.packet.out.play.PacketOutEntityPositionAndRotation
import org.kryptonmc.krypton.packet.out.play.PacketOutEntityRotation
import org.kryptonmc.krypton.packet.out.play.PacketOutEntityTeleport
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnPlayer

class PacketListener(private val nameTagX: NameTagX) : TabFeature(nameTagX.featureName, null) {

    // TODO: Use the interact packet when we support it
    override fun onPacketReceive(sender: TabPlayer, packet: Any): Boolean = false

    override fun onPacketSend(receiver: TabPlayer, packet: Any) {
        if (receiver.version.minorVersion < 8) return
        if (!receiver.isLoaded || nameTagX.isPlayerDisabled(receiver)) return
        when (packet) {
            is PacketOutEntityPosition, is PacketOutEntityRotation, is PacketOutEntityPositionAndRotation, is PacketOutEntityTeleport -> {
                packet as EntityPacket
                onEntityMove(receiver, packet.entityId)
            }
            is PacketOutSpawnPlayer -> onEntitySpawn(receiver, packet.entityId)
            is PacketOutDestroyEntities -> packet.ids.forEach { onEntityDestroy(receiver, it) }
        }
    }

    private fun onEntityMove(receiver: TabPlayer, entityId: Int) {
        val player = nameTagX.entityIdMap[entityId]
        if (player != null) {
            TAB.getInstance().cpuManager.runMeasuredTask("processing EntityMove", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE) {
                player.armorStandManager.teleport(receiver)
            }
        }
        /*
        nameTagX.vehicles[entityId]?.forEach {
            val passenger = nameTagX.entityIdMap[(it as KryptonEntity).id]
            if (passenger != null && passenger.armorStandManager != null) {
                tab.cpuManager.runMeasuredTask("processing EntityMove", featureType, UsageType.PACKET_ENTITY_MOVE) {
                    passenger.armorStandManager.teleport(receiver)
                }
            }
        }
        */
    }

    private fun onEntitySpawn(receiver: TabPlayer, entityId: Int) {
        val spawnedPlayer = nameTagX.entityIdMap[entityId]
        if (spawnedPlayer != null && spawnedPlayer.isLoaded) {
            TAB.getInstance().cpuManager.runMeasuredTask("processing EntitySpawn", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_SPAWN) {
                spawnedPlayer.armorStandManager.spawn(receiver)
            }
        }
    }

    private fun onEntityDestroy(receiver: TabPlayer, entity: Int) {
        val despawnedPlayer = nameTagX.entityIdMap[entity]
        if (despawnedPlayer != null && despawnedPlayer.isLoaded) {
            TAB.getInstance().cpuManager.runMeasuredTask("processing EntityDestroy", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY) {
                despawnedPlayer.armorStandManager.destroy(receiver)
            }
        }
    }
}
