package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabConstants
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.platforms.krypton.KryptonPacketBuilder
import me.neznamy.tab.platforms.krypton.Main
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.backend.BackendTabPlayer
import me.neznamy.tab.shared.backend.EntityData
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.event.EventNode
import org.kryptonmc.krypton.entity.KryptonEntityTypes
import org.kryptonmc.krypton.entity.metadata.MetadataHolder
import org.kryptonmc.krypton.entity.metadata.MetadataKeys
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.EntityPacket
import org.kryptonmc.krypton.packet.MovementPacket
import org.kryptonmc.krypton.packet.`in`.play.PacketInInteract
import org.kryptonmc.krypton.packet.out.play.PacketOutRemoveEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnPlayer
import kotlin.math.sqrt

class KryptonNameTagX(private val plugin: Main) : BackendNameTagX() {

    private val eventListener = EventListener(this)

    override fun load() {
        val eventNode = EventNode.all("tab_nametagx")
        plugin.eventNode.addChild(eventNode)
        eventNode.registerListeners(eventListener)
        super.load()
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
                packetListener.onEntityMove(receiver as? BackendTabPlayer, (packet as EntityPacket).entityId)
            }
            is PacketOutSpawnPlayer -> packetListener.onEntitySpawn(receiver as? BackendTabPlayer, packet.entityId)
            is PacketOutRemoveEntities -> packet.ids.forEach { packetListener.onEntityDestroy(receiver as BackendTabPlayer, it) }
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

    override fun isSneaking(player: TabPlayer): Boolean {
        return (player.player as Player).isSneaking
    }

    override fun isSwimming(player: TabPlayer): Boolean {
        return (player.player as Player).isSwimming
    }

    override fun isGliding(player: TabPlayer): Boolean {
        return (player.player as Player).isGliding
    }

    override fun isSleeping(player: TabPlayer?): Boolean {
        return false
    }

    override fun getArmorStandType(): Any {
        return KryptonEntityTypes.ARMOR_STAND
    }

    override fun getX(player: TabPlayer): Double {
        return (player.player as Player).position.x
    }

    override fun getY(entity: Any): Double {
        return (entity as Entity).position.y
    }

    override fun getZ(player: TabPlayer): Double {
        return (player.player as Player).position.z
    }

    override fun createDataWatcher(viewer: TabPlayer, flags: Byte, displayName: String, nameVisible: Boolean, markerFlag: Boolean): EntityData {
        val viewerPlayer = viewer.player as KryptonPlayer
        val holder = MetadataHolder(viewerPlayer).apply {
            define(MetadataKeys.Entity.FLAGS, 0)
            define(MetadataKeys.Entity.CUSTOM_NAME, null)
            define(MetadataKeys.Entity.CUSTOM_NAME_VISIBILITY, false)
        }
        holder.set(MetadataKeys.Entity.FLAGS, flags)
        holder.set(MetadataKeys.Entity.CUSTOM_NAME, KryptonPacketBuilder.toComponent(displayName, viewer.version))
        holder.set(MetadataKeys.Entity.CUSTOM_NAME_VISIBILITY, nameVisible)
        if (markerFlag) holder.define(MetadataKeys.ArmorStand.FLAGS, 16.toByte())
        return WrappedEntityData(holder)
    }

    companion object {

        private val INTERACT_ENTITY_ID_FIELD = PacketInInteract::class.java.getDeclaredField("entityId").apply { isAccessible = true }

        @JvmStatic
        private fun updateEntityId(packet: PacketInInteract, id: Int) {
            INTERACT_ENTITY_ID_FIELD.setInt(packet, id)
        }
    }
}
