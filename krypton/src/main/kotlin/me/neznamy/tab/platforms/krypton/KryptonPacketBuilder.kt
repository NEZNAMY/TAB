package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.chat.EnumChatFormat
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.*
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData
import me.neznamy.tab.api.util.BiFunctionWithException
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityMetadata
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutSpawnEntityLiving
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.api.auth.GameProfile
import org.kryptonmc.api.auth.ProfileProperty
import org.kryptonmc.api.world.GameMode
import org.kryptonmc.krypton.adventure.KryptonAdventure
import org.kryptonmc.krypton.entity.KryptonEntityType
import org.kryptonmc.krypton.entity.metadata.MetadataHolder
import org.kryptonmc.krypton.entity.player.PlayerPublicKey
import org.kryptonmc.krypton.network.chat.RemoteChatSession
import org.kryptonmc.krypton.packet.out.play.*
import java.util.*

// All build functions that just return the packet parameter will be passed through to be handled in KryptonTabPlayer.
object KryptonPacketBuilder : PacketBuilder() {

    private val GAME_MODES = GameMode.values()

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

    @Suppress("UNCHECKED_CAST")
    override fun build(packet: PacketPlayOutPlayerInfo, clientVersion: ProtocolVersion): Any {
        if (packet.actions.contains(EnumPlayerInfoAction.REMOVE_PLAYER)) {
            return PacketOutPlayerInfoRemove(packet.entries.map { it.uniqueId })
        }

        val actions = EnumSet.noneOf(PacketOutPlayerInfoUpdate.Action::class.java)
        packet.actions.forEach { PacketOutPlayerInfoUpdate.Action.fromId(it.ordinal) }

        val entries = packet.entries.map { data ->
            val displayName = data.displayName?.toString(clientVersion)
            val skin = if (data.skin != null) listOf(ProfileProperty.of("textures", data.skin!!.value, data.skin!!.signature)) else emptyList()

            val sessionId = data.chatSessionId
            val publicKey = data.profilePublicKey as? PlayerPublicKey.Data
            val session = if (sessionId != null && publicKey != null) RemoteChatSession.Data(sessionId, publicKey) else null

            PacketOutPlayerInfoUpdate.Entry(
                data.uniqueId,
                GameProfile.of(data.name ?: "", data.uniqueId, skin),
                data.isListed,
                data.latency,
                GAME_MODES[data.gameMode.ordinal - 1],
                displayName?.let { GsonComponentSerializer.gson().deserialize(it) },
                session
            )
        }

        return PacketOutPlayerInfoUpdate(actions, entries)
    }

    override fun build(packet: PacketPlayOutScoreboardTeam, clientVersion: ProtocolVersion): Any {
        val action = PacketOutUpdateTeams.Action.fromId(packet.action)!!
        val players = packet.players.map(Component::text)
        return PacketOutUpdateTeams(packet.name, action, createParameters(packet, clientVersion), players)
    }

    private fun build(packet: PacketPlayOutEntityMetadata): Any {
        return PacketOutSetEntityMetadata(packet.entityId, (packet.dataWatcher as MetadataHolder).collectAll())
    }

    private fun build(packet: PacketPlayOutSpawnEntityLiving): Any {
        return PacketOutSpawnEntity(packet.entityId, packet.uniqueId, packet.entityType as KryptonEntityType<*>,
            packet.x, packet.y, packet.z, 0, 0, 0, 0, 0, 0, 0)
    }

    private fun createParameters(packet: PacketPlayOutScoreboardTeam, clientVersion: ProtocolVersion): PacketOutUpdateTeams.Parameters? {
        if (packet.action != 0 && packet.action != 2) return null
        var prefix = packet.playerPrefix
        var suffix = packet.playerSuffix
        if (clientVersion.minorVersion < 13) {
            prefix = cutTo(prefix, 16)
            suffix = cutTo(suffix, 16)
        }
        return PacketOutUpdateTeams.Parameters(
            Component.text(packet.name),
            packet.options,
            packet.nameTagVisibility,
            packet.collisionRule,
            KryptonAdventure.getColorFromId(packet.color?.ordinal ?: EnumChatFormat.lastColorsOf(packet.playerPrefix).ordinal),
            toComponent(prefix, clientVersion),
            toComponent(suffix, clientVersion)
        )
    }

    override fun readPlayerInfo(packet: Any?, clientVersion: ProtocolVersion?): PacketPlayOutPlayerInfo? {
        if (packet is PacketOutPlayerInfoRemove) {
            return PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, packet.profileIds.map { PlayerInfoData(it) })
        }
        if (packet !is PacketOutPlayerInfoUpdate) return null

        val actions = EnumSet.noneOf(EnumPlayerInfoAction::class.java)
        packet.actions.forEach { actions.add(EnumPlayerInfoAction.valueOf(it.name)) }

        val entries = packet.entries.map { entry ->
            val displayName = entry.displayName
            val serializedListName = if (displayName != null) GsonComponentSerializer.gson().serialize(displayName) else null
            val textures = entry.profile.properties.firstOrNull { it.name == "textures" } ?: return null

            PlayerInfoData(
                entry.profile.name,
                entry.profile.uuid,
                Skin(textures.value, textures.signature),
                entry.listed,
                entry.latency,
                PacketPlayOutPlayerInfo.EnumGamemode.VALUES[entry.gameMode.ordinal + 1],
                if (serializedListName != null) IChatBaseComponent.deserialize(serializedListName) else null,
                entry.chatSession?.sessionId,
                entry.chatSession?.publicKey
            )
        }

        return PacketPlayOutPlayerInfo(actions, entries)
    }
}
