package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabAPI
import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabFeature
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.packet.EntityPacket
import org.kryptonmc.krypton.packet.MovementPacket
import org.kryptonmc.krypton.packet.`in`.play.PacketInInteract
import org.kryptonmc.krypton.packet.out.play.PacketOutRemoveEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnPlayer
import java.util.concurrent.ConcurrentHashMap

class PacketListener(private val nameTagX: KryptonNameTagX) : TabFeature(nameTagX.featureName, null) {

    private val entityIdMap = ConcurrentHashMap<Int, TabPlayer>()

    override fun load() {
        TabAPI.getInstance().onlinePlayers.forEach { player -> entityIdMap.put((player.player as Player).id, player) }
    }

    override fun onJoin(connectedPlayer: TabPlayer) {
        entityIdMap.put((connectedPlayer.player as Player).id, connectedPlayer)
    }

    override fun onQuit(disconnectedPlayer: TabPlayer) {
        entityIdMap.remove((disconnectedPlayer.player as Player).id, disconnectedPlayer)
    }

    override fun onPacketReceive(sender: TabPlayer, packet: Any): Boolean {
        if (sender.version.minorVersion == 8 && packet is PacketInInteract) {
            val entityId = packet.entityId
            var attacked: TabPlayer? = null
            for (player in TAB.getInstance().onlinePlayers) {
                if (player.isLoaded && nameTagX.getArmorStandManager(player)!!.hasArmorStandWithId(entityId)) {
                    attacked = player
                    break
                }
            }
            if (attacked != null && attacked !== sender) updateEntityId(packet, entityId)
        }
        return false
    }

    override fun onPacketSend(receiver: TabPlayer, packet: Any) {
        if (receiver.version.minorVersion < 8) return
        if (!receiver.isLoaded || nameTagX.isPlayerDisabled(receiver) || nameTagX.disabledUnlimitedPlayers.contains(receiver)) return
        when (packet) {
            is MovementPacket -> {
                if (packet !is EntityPacket) return
                onEntityMove(receiver, (packet as EntityPacket).entityId)
            }
            is PacketOutSpawnPlayer -> onEntitySpawn(receiver, packet.entityId)
            is PacketOutRemoveEntities -> packet.ids.forEach { onEntityDestroy(receiver, it) }
        }
    }

    private fun onEntityMove(receiver: TabPlayer, entityId: Int) {
        val player = entityIdMap.get(entityId)
        if (player != null) {
            if (nameTagX.isPlayerDisabled(player) || !player.isLoaded) return
            TabAPI.getInstance().threadManager.runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE) {
                nameTagX.getArmorStandManager(player)!!.teleport(receiver)
            }
            return
        }

        val vehicles = nameTagX.vehicleManager().vehicles().get(entityId) ?: return
        vehicles.forEach { vehicle ->
            val passenger = entityIdMap.get(vehicle.id)
            if (passenger == null || nameTagX.getArmorStandManager(passenger) == null) return@forEach

            TabAPI.getInstance().threadManager.runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE) {
                nameTagX.getArmorStandManager(passenger)!!.teleport(receiver)
            }
        }
    }

    private fun onEntitySpawn(receiver: TabPlayer, entityId: Int) {
        val spawnedPlayer = entityIdMap.get(entityId)
        if (spawnedPlayer == null || !spawnedPlayer.isLoaded) return

        TabAPI.getInstance().threadManager.runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_SPAWN) {
            nameTagX.getArmorStandManager(spawnedPlayer)!!.spawn(receiver)
        }
    }

    private fun onEntityDestroy(receiver: TabPlayer, entityId: Int) {
        val despawnedPlayer = entityIdMap.get(entityId)
        if (despawnedPlayer == null || !despawnedPlayer.isLoaded) return

        TabAPI.getInstance().threadManager.runMeasuredTask(nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY) {
            nameTagX.getArmorStandManager(despawnedPlayer)!!.destroy(receiver)
        }
    }

    companion object {

        private val INTERACT_ENTITY_ID_FIELD = PacketInInteract::class.java.getDeclaredField("entityId").apply { isAccessible = true }

        @JvmStatic
        private fun updateEntityId(packet: PacketInInteract, id: Int) {
            INTERACT_ENTITY_ID_FIELD.setInt(packet, id)
        }
    }
}
