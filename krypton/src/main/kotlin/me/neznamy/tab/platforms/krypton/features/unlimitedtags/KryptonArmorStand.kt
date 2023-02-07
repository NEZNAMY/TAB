package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.ArmorStand
import me.neznamy.tab.api.Property
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.chat.EnumChatFormat
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.platforms.krypton.KryptonPacketBuilder
import org.kryptonmc.api.util.Vec3d
import org.kryptonmc.krypton.entity.KryptonEntityTypes
import org.kryptonmc.krypton.entity.metadata.MetadataHolder
import org.kryptonmc.krypton.entity.metadata.MetadataKeys
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.Packet
import org.kryptonmc.krypton.packet.out.play.PacketOutRemoveEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutSetEntityMetadata
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnEntity
import org.kryptonmc.krypton.packet.out.play.PacketOutTeleportEntity
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class KryptonArmorStand(
    private val manager: KryptonArmorStandManager,
    private val nameTagX: KryptonNameTagX,
    private val owner: TabPlayer,
    propertyName: String,
    private var yOffset: Double,
    private val staticOffset: Boolean
) : ArmorStand {

    private val player = owner.player as KryptonPlayer
    private val entityId = ID_COUNTER.getAndIncrement()
    private val uuid = UUID.randomUUID()
    private var sneaking = player.isSneaking
    private val property = owner.getProperty(propertyName)!!
    private var visible = calculateVisibility()
    private val destroyPacket = PacketOutRemoveEntities(intArrayOf(entityId))

    override fun refresh() {
        visible = calculateVisibility()
        updateMetadata()
    }

    override fun getProperty(): Property = property

    override fun hasStaticOffset(): Boolean = staticOffset

    override fun getOffset(): Double = yOffset

    override fun setOffset(offset: Double) {
        if (yOffset == offset) return
        yOffset = offset
        manager.nearbyPlayers().forEach { viewer -> viewer.sendPacket(getTeleportPacket(viewer)) }
    }

    override fun spawn(viewer: TabPlayer) {
        getSpawnPackets(viewer).forEach { viewer.sendPacket(it) }
    }

    override fun destroy() {
        player.server.connectionManager.sendGroupedPacket(player.server.players, destroyPacket)
    }

    override fun destroy(viewer: TabPlayer) {
        viewer.sendPacket(destroyPacket)
    }

    override fun teleport() {
        manager.nearbyPlayers().forEach { viewer -> viewer.sendPacket(getTeleportPacket(viewer)) }
    }

    override fun teleport(viewer: TabPlayer) {
        if (!manager.isNearby(viewer) && viewer !== owner) {
            manager.spawn(viewer)
        } else {
            (viewer.player as KryptonPlayer).connection.send(getTeleportPacket(viewer))
        }
    }

    override fun sneak(sneaking: Boolean) {
        if (this.sneaking == sneaking) return // idk
        this.sneaking = sneaking

        manager.nearbyPlayers().forEach { viewer ->
            if (viewer.version.minorVersion == 14 && !nameTagX.isArmorStandsAlwaysVisible) {
                // 1.14.x client sided bug, despawning completely
                if (sneaking) {
                    (viewer.player as KryptonPlayer).connection.send(destroyPacket)
                } else {
                    spawn(viewer)
                }
            } else {
                // respawning so there's no animation and it's instant
                respawn(viewer)
            }
        }
    }

    override fun updateVisibility(force: Boolean) {
        val visibility = calculateVisibility()
        if (visible != visibility || force) {
            refresh()
        }
    }

    override fun getEntityId(): Int = entityId

    private fun getTeleportPacket(viewer: TabPlayer): PacketOutTeleportEntity {
        val position = getArmorStandLocationFor(viewer)
        return PacketOutTeleportEntity(entityId, position.x, position.y, position.z, 0F, 0F, false)
    }

    private fun getArmorStandLocationFor(viewer: TabPlayer): Vec3d {
        var position = getPositionForViewer(viewer)
        if (viewer.version.minorVersion == 8 && !nameTagX.isMarkerFor18x) position = position.subtract(0.0, 2.0, 0.0)
        return position
    }

    private fun getPositionForViewer(viewer: TabPlayer): Vec3d {
        var y = getY() + yOffset
        if (sneaking) {
            if (viewer.version.minorVersion >= 15) {
                y += 1.37
            } else if (viewer.version.minorVersion >= 9) {
                y += 1.52
            } else {
                y += 1.7
            }
        } else {
            y += if (viewer.version.minorVersion >= 9) 1.8 else 1.84 // Normal
        }
        return Vec3d.of(player.position.x, y, player.position.z)
    }

    private fun getY(): Double {
        if (player.isSwimming || player.isGliding) return player.position.y - 1.22
        return player.position.y
    }

    private fun updateMetadata() {
        manager.nearbyPlayers().forEach { viewer ->
            val packet = PacketOutSetEntityMetadata(entityId, createMetadata(property.getFormat(viewer), viewer).collectAll())
            viewer.sendPacket(packet)
        }
    }

    private fun calculateVisibility(): Boolean {
        if (nameTagX.isArmorStandsAlwaysVisible) return true
        if (owner.isDisguised || nameTagX.vehicleManager().isOnBoat(owner)) return false
        return !owner.hasInvisibilityPotion() && owner.gamemode != 3 && !nameTagX.hasHiddenNametag(owner) && property.get().isNotEmpty()
    }

    private fun getSpawnPackets(viewer: TabPlayer): Array<Packet> {
        visible = calculateVisibility()
        val data = createMetadata(property.getFormat(viewer), viewer)
        val location = getArmorStandLocationFor(viewer)
        return arrayOf(
            PacketOutSpawnEntity(entityId, uuid, KryptonEntityTypes.ARMOR_STAND, location.x, location.y, location.z, 0F, 0F, 0F, 0, 0, 0, 0),
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

        var flags = 32 // invisible
        if (sneaking) flags += 2
        holder.set(MetadataKeys.Entity.FLAGS, flags.toByte())
        holder.set(MetadataKeys.Entity.CUSTOM_NAME, KryptonPacketBuilder.toComponent(displayName, viewer.version))

        if (isNameVisibilityEmpty(displayName) || nameTagX.hasHiddenNametag(owner, viewer) || nameTagX.hasHiddenNameTagVisibilityView(viewer)) {
            holder.set(MetadataKeys.Entity.CUSTOM_NAME_VISIBILITY, false)
        } else {
            holder.set(MetadataKeys.Entity.CUSTOM_NAME_VISIBILITY, visible)
        }

        if (viewer.version.minorVersion > 8 || nameTagX.isMarkerFor18x) {
            holder.define(MetadataKeys.ArmorStand.FLAGS, 16.toByte())
        }
        return holder
    }

    private fun isNameVisibilityEmpty(displayName: String): Boolean {
        if (displayName.isEmpty()) return true
        if (!displayName.startsWith(EnumChatFormat.COLOR_CHAR) && !displayName.startsWith('&') && !displayName.startsWith('#')) return false
        var text = IChatBaseComponent.fromColoredText(displayName).toRawText()
        if (text.contains(' ')) text = text.replace(" ", "")
        return text.isEmpty()
    }

    override fun respawn(viewer: TabPlayer) {
        viewer.sendPacket(destroyPacket)
        spawn(viewer)
    }

    companion object {

        // entity ID counter to pick (hopefully) unique entity IDs
        private val ID_COUNTER = AtomicInteger(2000000000)
    }
}
