package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.platforms.krypton.KryptonPacketBuilder
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStand
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStandManager
import org.kryptonmc.krypton.entity.KryptonEntityTypes
import org.kryptonmc.krypton.entity.metadata.MetadataHolder
import org.kryptonmc.krypton.entity.metadata.MetadataKeys
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.Packet
import org.kryptonmc.krypton.packet.out.play.PacketOutRemoveEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutSetEntityMetadata
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnEntity
import org.kryptonmc.krypton.packet.out.play.PacketOutTeleportEntity

class KryptonArmorStand(
    manager: BackendArmorStandManager,
    nameTagX: KryptonNameTagX,
    owner: TabPlayer,
    propertyName: String,
    yOffset: Double,
    staticOffset: Boolean
) : BackendArmorStand(nameTagX, manager, owner, propertyName, yOffset, staticOffset) {

    private val player = owner.player as KryptonPlayer
    private val destroyPacket = PacketOutRemoveEntities(intArrayOf(entityId))

    override fun spawn(viewer: TabPlayer) {
        getSpawnPackets(viewer).forEach { viewer.sendPacket(it) }
    }

    override fun destroy(viewer: TabPlayer) {
        viewer.sendPacket(destroyPacket)
    }

    private fun getArmorStandYFor(viewer: TabPlayer): Double {
        var y = player.position.y
        if (player.isSwimming || player.isGliding) y -= 1.22
        y += getYAdd(false, sneaking, viewer)
        return y
    }

    override fun updateMetadata(viewer: TabPlayer) {
        viewer.sendPacket(PacketOutSetEntityMetadata(entityId, createMetadata(property.getFormat(viewer), viewer).collectAll()))
    }

    override fun sendTeleportPacket(viewer: TabPlayer) {
        viewer.sendPacket(PacketOutTeleportEntity(entityId, player.position.x, getArmorStandYFor(viewer), player.position.z, 0F, 0F, false))
    }

    private fun getSpawnPackets(viewer: TabPlayer): Array<Packet> {
        visible = calculateVisibility()
        val data = createMetadata(property.getFormat(viewer), viewer)
        return arrayOf(
            PacketOutSpawnEntity(entityId, uuid, KryptonEntityTypes.ARMOR_STAND, player.position.x, getArmorStandYFor(viewer),
                player.position.z, 0F, 0F, 0F, 0, 0, 0, 0),
            PacketOutSetEntityMetadata(entityId, data.collectAll())
        )
    }

    private fun createMetadata(displayName: String, viewer: TabPlayer): MetadataHolder {
        val viewerPlayer = viewer.player as KryptonPlayer
        val holder = MetadataHolder(viewerPlayer).apply {
            define(MetadataKeys.Entity.FLAGS, 0)
            define(MetadataKeys.Entity.CUSTOM_NAME, null)
            define(MetadataKeys.Entity.CUSTOM_NAME_VISIBILITY, false)
        }
        holder.set(MetadataKeys.Entity.FLAGS, (if (sneaking) 34 else 32).toByte())
        holder.set(MetadataKeys.Entity.CUSTOM_NAME, KryptonPacketBuilder.toComponent(displayName, viewer.version))
        holder.set(MetadataKeys.Entity.CUSTOM_NAME_VISIBILITY, !shouldBeInvisibleFor(viewer, displayName) && visible)
        if (viewer.version.minorVersion > 8 || manager.isMarkerFor18x) {
            holder.define(MetadataKeys.ArmorStand.FLAGS, 16.toByte())
        }
        return holder
    }
}
