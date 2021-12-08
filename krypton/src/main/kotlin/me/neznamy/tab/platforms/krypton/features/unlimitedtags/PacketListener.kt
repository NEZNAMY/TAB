package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.packet.EntityPacket
import org.kryptonmc.krypton.packet.MovementPacket
import org.kryptonmc.krypton.packet.`in`.play.PacketInInteract
import org.kryptonmc.krypton.packet.out.play.PacketOutDestroyEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnPlayer

class PacketListener(private val nameTagX: NameTagX) : TabFeature(nameTagX.featureName, null) {

    override fun onPacketReceive(sender: TabPlayer, packet: Any): Boolean {
        if (sender.version.minorVersion == 8 && packet is PacketInInteract) {
            val entityId = packet.entityId
            var attacked: TabPlayer? = null
            for (player in TAB.getInstance().onlinePlayers) {
                if (player.isLoaded && player.armorStandManager.hasArmorStandWithID(entityId)) {
                    attacked = player
                    break
                }
            }
            if (attacked != null && attacked !== sender) packet.updateEntityId(entityId)
        }
        return false
    }

    override fun onPacketSend(receiver: TabPlayer, packet: Any) {
        if (receiver.version.minorVersion < 8) return
        if (!receiver.isLoaded || nameTagX.isPlayerDisabled(receiver)) return
        when (packet) {
            is MovementPacket -> {
                if (packet !is EntityPacket) return
                onEntityMove(receiver, (packet as EntityPacket).entityId)
            }
            is PacketOutSpawnPlayer -> onEntitySpawn(receiver, packet.entityId)
            is PacketOutDestroyEntities -> packet.ids.forEach { onEntityDestroy(receiver, it) }
        }
    }

    private fun onEntityMove(receiver: TabPlayer, entityId: Int) {
        val player = nameTagX.entityIdMap[entityId]
        if (player != null) {
            TAB.getInstance().cpuManager.runMeasuredTask("processing EntityMove", nameTagX, CpuUsageCategory.PACKET_ENTITY_MOVE) {
                player.armorStandManager.teleport(receiver)
            }
            return
        }
        nameTagX.vehicleManager.vehicles(entityId).forEach {
            val passenger = nameTagX.entityIdMap[(it as KryptonEntity).id]
            if (passenger != null && passenger.armorStandManager != null) {
                TAB.getInstance().cpuManager.runMeasuredTask("processing EntityMove", nameTagX, CpuUsageCategory.PACKET_ENTITY_MOVE) {
                    passenger.armorStandManager.teleport(receiver)
                }
            }
        }
    }

    private fun onEntitySpawn(receiver: TabPlayer, entityId: Int) {
        val spawnedPlayer = nameTagX.entityIdMap[entityId]
        if (spawnedPlayer != null && spawnedPlayer.isLoaded) {
            TAB.getInstance().cpuManager.runMeasuredTask("processing EntitySpawn", nameTagX, CpuUsageCategory.PACKET_ENTITY_SPAWN) {
                spawnedPlayer.armorStandManager.spawn(receiver)
            }
        }
    }

    private fun onEntityDestroy(receiver: TabPlayer, entity: Int) {
        val deSpawnedPlayer = nameTagX.entityIdMap[entity]
        if (deSpawnedPlayer != null && deSpawnedPlayer.isLoaded) {
            TAB.getInstance().cpuManager.runMeasuredTask("processing EntityDestroy", nameTagX, CpuUsageCategory.PACKET_ENTITY_DESTROY) {
                deSpawnedPlayer.armorStandManager.destroy(receiver)
            }
        }
    }

    companion object {

        private val INTERACT_ENTITY_ID_FIELD = PacketInInteract::class.java.getDeclaredField("entityId").apply { isAccessible = true }

        private fun PacketInInteract.updateEntityId(id: Int) {
            INTERACT_ENTITY_ID_FIELD.setInt(this, id)
        }
    }
}
