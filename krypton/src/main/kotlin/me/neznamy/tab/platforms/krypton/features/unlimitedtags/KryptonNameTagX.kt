package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.platforms.krypton.Main
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStand
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStandManager
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.EventNode
import org.kryptonmc.krypton.packet.EntityPacket
import org.kryptonmc.krypton.packet.MovementPacket
import org.kryptonmc.krypton.packet.`in`.play.PacketInInteract
import org.kryptonmc.krypton.packet.out.play.PacketOutRemoveEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnPlayer
import kotlin.math.sqrt

class KryptonNameTagX(private val plugin: Main) : BackendNameTagX() {

    private val eventListener = EventListener(this)

    init {
        val eventNode = EventNode.all("tab_nametagx")
        plugin.eventNode.addChild(eventNode)
        eventNode.registerListeners(eventListener)
    }

    override fun onPacketReceive(sender: TabPlayer, packet: Any): Boolean {
        if (sender.version.minorVersion == 8 && packet is PacketInInteract) {
            val entityId = packet.entityId
            var attacked: TabPlayer? = null
            for (player in TAB.getInstance().onlinePlayers) {
                if (player.isLoaded && getArmorStandManager(player)!!.hasArmorStandWithID(entityId)) {
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
        if (!receiver.isLoaded || isPlayerDisabled(receiver) || disabledUnlimitedPlayers.contains(receiver)) return
        when (packet) {
            is MovementPacket -> {
                if (packet !is EntityPacket) return
                packetListener.onEntityMove(receiver, (packet as EntityPacket).entityId)
            }
            is PacketOutSpawnPlayer -> packetListener.onEntitySpawn(receiver, packet.entityId)
            is PacketOutRemoveEntities -> packet.ids.forEach { packetListener.onEntityDestroy(receiver, it) }
        }
    }

    override fun isOnBoat(player: TabPlayer): Boolean = vehicleManager.isOnBoat(player)

    override fun getDistance(player1: TabPlayer, player2: TabPlayer): Double {
        val pos1 = (player1.player as Player).position
        val pos2 = (player2.player as Player).position
        return sqrt((pos1.x - pos2.x) * (pos1.x - pos2.x) + (pos1.z - pos2.z) * (pos1.z - pos2.z))
    }

    override fun areInSameWorld(player1: TabPlayer, player2: TabPlayer): Boolean {
        return (player1.player as Player).world != (player2.player as Player).world
    }

    override fun canSee(viewer: TabPlayer?, target: TabPlayer?): Boolean {
        return true
    }

    override fun unregisterListener() {
        plugin.eventNode.unregisterListeners(eventListener)
    }

    override fun getPassengers(vehicle: Any): List<Int> {
        return (vehicle as Entity).passengers.map { it.id }
    }

    override fun registerVehiclePlaceholder() {
        TAB.getInstance().placeholderManager.registerPlayerPlaceholder(TabConstants.Placeholder.VEHICLE, 100) {
            (it.player as Player).vehicle.toString()
        }
    }

    override fun getVehicle(player: TabPlayer): Any? {
        return (player.player as Player).vehicle
    }

    override fun getEntityId(entity: Any): Int {
        return (entity as Entity).id
    }

    override fun getEntityType(entity: Any): String {
        return (entity as Entity).type.key().value()
    }

    override fun createArmorStand(asm: BackendArmorStandManager, owner: TabPlayer, lineName: String, yOffset: Double, staticOffset: Boolean): BackendArmorStand {
        return KryptonArmorStand(asm, this, owner, lineName, yOffset, staticOffset)
    }

    companion object {

        private val INTERACT_ENTITY_ID_FIELD = PacketInInteract::class.java.getDeclaredField("entityId").apply { isAccessible = true }

        @JvmStatic
        private fun updateEntityId(packet: PacketInInteract, id: Int) {
            INTERACT_ENTITY_ID_FIELD.setInt(packet, id)
        }
    }
}
