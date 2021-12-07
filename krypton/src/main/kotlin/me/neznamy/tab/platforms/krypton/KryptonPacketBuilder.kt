package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.chat.EnumChatFormat
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.PacketBuilder
import me.neznamy.tab.api.protocol.PacketPlayOutBoss
import me.neznamy.tab.api.protocol.PacketPlayOutChat
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.api.auth.ProfileProperty
import org.kryptonmc.api.world.GameMode
import org.kryptonmc.krypton.packet.out.play.PacketOutDisplayObjective
import org.kryptonmc.krypton.packet.out.play.PacketOutObjective
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo
import org.kryptonmc.krypton.packet.out.play.PacketOutTeam
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateScore

// All build functions that just return the packet parameter will be passed through to be handled in KryptonTabPlayer.
object KryptonPacketBuilder : PacketBuilder() {

    @JvmStatic
    fun toComponent(text: String?, clientVersion: ProtocolVersion): Component {
        if (text == null || text.isEmpty()) return Component.empty()
        return GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(text).toString(clientVersion))
    }

    override fun build(packet: PacketPlayOutBoss, clientVersion: ProtocolVersion?): Any = packet

    override fun build(packet: PacketPlayOutChat, clientVersion: ProtocolVersion?): Any = packet

    @Suppress("UNCHECKED_CAST")
    override fun build(packet: PacketPlayOutPlayerInfo, clientVersion: ProtocolVersion): Any = PacketOutPlayerInfo(
        PacketOutPlayerInfo.Action.valueOf(packet.action.name),
        packet.entries.map {
            val displayName = it.displayName?.toString(clientVersion)
            PacketOutPlayerInfo.PlayerData(
                it.uniqueId,
                it.name ?: "",
                it.skin as? List<ProfileProperty> ?: emptyList(),
                GameMode.fromId((it.gameMode?.ordinal ?: 0) - 1) ?: GameMode.SURVIVAL,
                it.latency,
                if (displayName != null) GsonComponentSerializer.gson().deserialize(displayName) else Component.empty()
            )
        }
    )

    override fun build(packet: PacketPlayOutPlayerListHeaderFooter, clientVersion: ProtocolVersion?): Any = packet

    override fun build(packet: PacketPlayOutScoreboardDisplayObjective, clientVersion: ProtocolVersion?): Any = PacketOutDisplayObjective(
        packet.slot,
        packet.objectiveName
    )

    override fun build(packet: PacketPlayOutScoreboardObjective, clientVersion: ProtocolVersion): Any = PacketOutObjective(
        PacketOutObjective.Action.fromId(packet.method)!!,
        packet.objectiveName,
        toComponent(packet.displayName, clientVersion),
        packet.renderType?.ordinal ?: -1
    )

    override fun build(packet: PacketPlayOutScoreboardScore, clientVersion: ProtocolVersion?): Any = PacketOutUpdateScore(
        PacketOutUpdateScore.Action.fromId(packet.action.ordinal)!!,
        Component.text(packet.player),
        packet.objectiveName,
        packet.score
    )

    override fun build(packet: PacketPlayOutScoreboardTeam, clientVersion: ProtocolVersion): Any {
        val action = PacketOutTeam.Action.fromId(packet.method)!!
        val players = packet.players.map(Component::text)
        var prefix = packet.playerPrefix
        var suffix = packet.playerSuffix
        if (clientVersion.minorVersion < 13) {
            prefix = cutTo(prefix, 16)
            suffix = cutTo(suffix, 16)
        }
        return PacketOutTeam(
            action,
            packet.name,
            Component.text(packet.name),
            packet.color?.ordinal ?: EnumChatFormat.lastColorsOf(packet.playerPrefix).ordinal,
            toComponent(prefix, clientVersion),
            toComponent(suffix, clientVersion),
            packet.options and 1 > 0,
            packet.options and 2 > 0,
            packet.nameTagVisibility ?: "",
            packet.collisionRule ?: "",
            players,
            if (action == PacketOutTeam.Action.ADD_MEMBERS || action == PacketOutTeam.Action.REMOVE_MEMBERS) players else emptySet()
        )
    }

    override fun readPlayerInfo(packet: Any?, clientVersion: ProtocolVersion?): PacketPlayOutPlayerInfo? {
        if (packet !is PacketOutPlayerInfo) return null
        val action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.valueOf(packet.action.name)
        val listData = packet.players.map {
            val serializedListName = if (it.displayName != null) GsonComponentSerializer.gson().serialize(it.displayName!!) else null
            PacketPlayOutPlayerInfo.PlayerInfoData(
                it.name,
                it.uuid,
                it.properties,
                it.latency,
                PacketPlayOutPlayerInfo.EnumGamemode.values()[it.gameMode.ordinal + 1],
                if (serializedListName != null) IChatBaseComponent.deserialize(serializedListName) else null
            )
        }
        return PacketPlayOutPlayerInfo(action, listData)
    }

    override fun readObjective(packet: Any?): PacketPlayOutScoreboardObjective? = null

    override fun readDisplayObjective(packet: Any?): PacketPlayOutScoreboardDisplayObjective? = null
}
