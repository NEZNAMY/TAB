package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.PacketBuilder
import me.neznamy.tab.api.protocol.TabPacket
import me.neznamy.tab.api.util.BiFunctionWithException
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityMetadata
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutSpawnEntityLiving
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.krypton.entity.KryptonEntityType
import org.kryptonmc.krypton.entity.metadata.MetadataHolder
import org.kryptonmc.krypton.packet.out.play.PacketOutSetEntityMetadata
import org.kryptonmc.krypton.packet.out.play.PacketOutSpawnEntity

// All build functions that just return the packet parameter will be passed through to be handled in KryptonTabPlayer.
object KryptonPacketBuilder : PacketBuilder() {

    init {
        buildMap[PacketPlayOutEntityMetadata::class.java] =
            BiFunctionWithException { packet: TabPacket, _: ProtocolVersion? ->
                build(packet as PacketPlayOutEntityMetadata)
            }
        buildMap[PacketPlayOutSpawnEntityLiving::class.java] =
            BiFunctionWithException { packet: TabPacket, _: ProtocolVersion? ->
                build(packet as PacketPlayOutSpawnEntityLiving)
            }
    }

    @JvmStatic
    fun toComponent(text: String?, clientVersion: ProtocolVersion): Component {
        if (text.isNullOrEmpty()) return Component.empty()
        return GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(text).toString(clientVersion))
    }

    private fun build(packet: PacketPlayOutEntityMetadata): Any {
        return PacketOutSetEntityMetadata(packet.entityId, (packet.dataWatcher as MetadataHolder).collectAll())
    }

    private fun build(packet: PacketPlayOutSpawnEntityLiving): Any {
        return PacketOutSpawnEntity(packet.entityId, packet.uniqueId, packet.entityType as KryptonEntityType<*>,
            packet.x, packet.y, packet.z, 0, 0, 0, 0, 0, 0, 0)
    }

    /*override fun readPlayerInfo(packet: Any?, clientVersion: ProtocolVersion?): PacketPlayOutPlayerInfo? {
        if (packet is PacketOutPlayerInfoRemove) {
            return PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, packet.profileIds.map { TabListEntry(it) })
        }
        if (packet !is PacketOutPlayerInfoUpdate) return null

        val actions = EnumSet.noneOf(EnumPlayerInfoAction::class.java)
        packet.actions.forEach { actions.add(EnumPlayerInfoAction.valueOf(it.name)) }

        val entries = packet.entries.map { entry ->
            val displayName = entry.displayName
            val serializedListName = if (displayName != null) GsonComponentSerializer.gson().serialize(displayName) else null
            val textures = entry.profile.properties.firstOrNull { it.name == "textures" } ?: return null

            TabListEntry(
                entry.profile.uuid,
                entry.profile.name,
                Skin(textures.value, textures.signature),
                entry.listed,
                entry.latency,
                entry.gameMode.ordinal,
                if (serializedListName != null) IChatBaseComponent.deserialize(serializedListName) else null,
                entry.chatSession?.sessionId,
                entry.chatSession?.publicKey
            )
        }

        return PacketPlayOutPlayerInfo(actions, entries)
    }*/
}
