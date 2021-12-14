package me.neznamy.tab.platforms.krypton.features.unlimitedtags

import me.neznamy.tab.api.ArmorStand
import me.neznamy.tab.api.Property
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.chat.EnumChatFormat
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.platforms.krypton.KryptonPacketBuilder
import me.neznamy.tab.shared.TabConstants
import me.neznamy.tab.shared.TAB
import org.kryptonmc.api.entity.EntityTypes
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.krypton.entity.metadata.MetadataHolder
import org.kryptonmc.krypton.entity.metadata.MetadataKeys
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.Packet
import org.kryptonmc.krypton.packet.out.play.PacketOutDestroyEntities
import org.kryptonmc.krypton.packet.out.play.PacketOutEntityTeleport
import org.kryptonmc.krypton.packet.out.play.PacketOutMetadata
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnLivingEntity
import org.spongepowered.math.vector.Vector2f
import org.spongepowered.math.vector.Vector3d
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class KryptonArmorStand(
    private val owner: TabPlayer,
    private val property: Property,
    private var yOffset: Double,
    private val staticOffset: Boolean
) : ArmorStand {

    private val manager = TAB.getInstance().featureManager.getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS) as NameTagX
    private val player = owner.player as Player
    private val entityId = ID_COUNTER.incrementAndGet()
    private val uuid = UUID.randomUUID()
    private var sneaking = false
    private var visible = calculateVisibility()
    private val destroyPacket = PacketOutDestroyEntities(entityId)
    private val location: Vector3d
        get() {
            val x = player.location.x()
            var y = calculateY() + yOffset + 2
            val z = player.location.z()
            y -= if (player.sleepingPosition != null) 1.76 else if (sneaking) 0.45 else 0.18
            return Vector3d(x, y, z)
        }

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
        owner.armorStandManager.nearbyPlayers.forEach {
            it.sendPacket(getTeleportPacket(it), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_OFFSET_CHANGE)
        }
    }

    override fun spawn(viewer: TabPlayer) {
        getSpawnPackets(viewer).forEach { viewer.sendPacket(it, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_SPAWN) }
    }

    override fun destroy() {
        TAB.getInstance().onlinePlayers.forEach { it.sendPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN) }
    }

    override fun destroy(viewer: TabPlayer) {
        viewer.sendPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN)
    }

    override fun teleport() {
        owner.armorStandManager.nearbyPlayers.forEach {
            it.sendPacket(getTeleportPacket(it), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_TELEPORT)
        }
    }

    override fun teleport(viewer: TabPlayer) {
        if (!owner.armorStandManager.isNearby(viewer) && viewer !== owner) {
            owner.armorStandManager.spawn(viewer)
            return
        }
        viewer.sendPacket(getTeleportPacket(viewer), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_TELEPORT)
    }

    override fun sneak(sneaking: Boolean) {
        if (this.sneaking == sneaking) return // idk
        this.sneaking = sneaking
        owner.armorStandManager.nearbyPlayers.forEach {
            if (it.version.minorVersion == 14 && !ALWAYS_VISIBLE) {
                // 1.14.x client sided bug, despawning completely
                if (sneaking) {
                    it.sendPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_SNEAK)
                } else {
                    spawn(it)
                }
            } else {
                // respawning so there's no animation and it's instant
                respawn(it)
            }
        }
    }

    override fun updateVisibility(force: Boolean) {
        val visibility = calculateVisibility()
        if (visible != visibility || force) {
            visible = visibility
            updateMetadata()
        }
    }

    override fun getEntityId(): Int = entityId

    private fun updateMetadata() {
        owner.armorStandManager.nearbyPlayers.forEach {
            val packet = PacketOutMetadata(entityId, createMetadata(property.getFormat(it), it).all)
            it.sendPacket(packet, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_METADATA)
        }
    }

    private fun getTeleportPacket(viewer: TabPlayer): PacketOutEntityTeleport = PacketOutEntityTeleport(
        entityId,
        armorStandLocationFor(viewer),
        Vector2f.ZERO,
        false
    )

    // Using a sequence here makes sense because we only need the iteration behaviour, which is exactly what sequences are for.
    // For those reading this from Java, a Sequence is kind of Kotlin's equivalent to Java's Stream.
    private fun getSpawnPackets(viewer: TabPlayer): Sequence<Packet> {
        visible = calculateVisibility()
        val data = createMetadata(property.getFormat(viewer), viewer)
        val location = armorStandLocationFor(viewer)
        return sequenceOf(
            PacketOutSpawnLivingEntity(
                entityId,
                uuid,
                TYPE,
                location.x(),
                location.y(),
                location.z(),
                0F,
                0F,
                0F,
                0,
                0,
                0
            ),
            PacketOutMetadata(entityId, data.all)
        )
    }

    private fun createMetadata(displayName: String, viewer: TabPlayer): MetadataHolder {
        val viewerPlayer = viewer.player as KryptonPlayer
        val holder = MetadataHolder(viewerPlayer).apply {
            add(MetadataKeys.FLAGS)
            add(MetadataKeys.CUSTOM_NAME)
            add(MetadataKeys.CUSTOM_NAME_VISIBILITY)
        }

        var flags = 32 // invisible
        if (sneaking) flags += 2
        holder[MetadataKeys.FLAGS] = flags.toByte()
        holder[MetadataKeys.CUSTOM_NAME] = KryptonPacketBuilder.toComponent(displayName, viewer.version)

        if (isNameVisibilityEmpty(displayName) || !viewerPlayer.canSee(player) || manager.hasHiddenNametag(owner, viewer)) {
            holder[MetadataKeys.CUSTOM_NAME_VISIBILITY] = false
        } else {
            holder[MetadataKeys.CUSTOM_NAME_VISIBILITY] = visible
        }

        if (viewer.version.minorVersion > 8 || manager.markerFor18x) {
            holder.add(MetadataKeys.ARMOR_STAND.FLAGS)
            holder[MetadataKeys.ARMOR_STAND.FLAGS] = 16.toByte()
        }
        return holder
    }

    private fun calculateVisibility(): Boolean {
        if (owner.isDisguised || manager.vehicleManager.isOnBoat(owner)) return false
        if (ALWAYS_VISIBLE) return true
        return !owner.hasInvisibilityPotion() && owner.gamemode != 3 && !manager.hasHiddenNametag(owner) && property.get().isNotEmpty()
    }

    private fun calculateY(): Double {
        // 1.14+ server sided bug
        val vehicle = player.vehicle
        if (vehicle != null) {
            val key = vehicle.type.key().asString()
            when {
                key.contains("horse") -> return vehicle.location.y() + 0.85
                key.contains("donkey") -> return vehicle.location.y() + 0.525
                vehicle.type === EntityTypes.PIG -> return vehicle.location.y() + 0.325
                key.contains("strider") -> vehicle.location.y() + 1.15
            }
        }
        if (player.isSwimming || player.isGliding) return player.location.y() - 1.22
        return player.location.y()
    }

    private fun armorStandLocationFor(viewer: TabPlayer): Vector3d {
        if (viewer.version.minorVersion == 8 && !manager.markerFor18x) return location.add(0.0, -2.0, 0.0)
        return location
    }

    private fun isNameVisibilityEmpty(displayName: String): Boolean {
        if (displayName.isEmpty()) return true
        if (!displayName.startsWith(EnumChatFormat.COLOR_CHAR) && !displayName.startsWith('&') && !displayName.startsWith('#')) return false
        var text = IChatBaseComponent.fromColoredText(displayName).toRawText()
        if (text.contains(' ')) text = text.replace(" ", "")
        return text.isEmpty()
    }

    override fun respawn(viewer: TabPlayer) {
        viewer.sendPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN)
        val spawn = Runnable { spawn(viewer) }
        if (viewer.version.minorVersion == 8) {
            // 1.8.8 client sided bug
            TAB.getInstance().cpuManager.runTaskLater(
                50,
                "compensating for 1.8.0 bugs",
                manager,
                TabConstants.CpuUsageCategory.V1_8_0_BUG_COMPENSATION,
                spawn
            )
        } else {
            spawn.run()
        }
    }

    companion object {

        private val ALWAYS_VISIBLE = TAB.getInstance().configuration.isArmorStandsAlwaysVisible
        // entity ID counter to pick (hopefully) unique entity IDs
        private val ID_COUNTER = AtomicInteger(2000000000)
        private val TYPE = Registries.ENTITY_TYPE.idOf(EntityTypes.ARMOR_STAND)
    }
}
