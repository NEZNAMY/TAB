package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
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
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.api.world.GameModes
import org.kryptonmc.krypton.packet.out.play.PacketOutDisplayObjective
import org.kryptonmc.krypton.packet.out.play.PacketOutObjective
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo
import org.kryptonmc.krypton.packet.out.play.PacketOutTeam
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateScore

object KryptonPacketBuilder : PacketBuilder() {

    override fun build(packet: PacketPlayOutBoss, clientVersion: ProtocolVersion?) = packet

    override fun build(packet: PacketPlayOutChat, clientVersion: ProtocolVersion?) = packet

    @Suppress("UNCHECKED_CAST")
    override fun build(packet: PacketPlayOutPlayerInfo, clientVersion: ProtocolVersion?) = PacketOutPlayerInfo(
        PacketOutPlayerInfo.Action.valueOf(packet.action.name),
        packet.entries.map {
            PacketOutPlayerInfo.PlayerData(
                it.uniqueId,
                it.name,
                it.skin as List<ProfileProperty>,
                Registries.GAME_MODES[it.gameMode.ordinal - 1] ?: GameModes.SURVIVAL,
                it.latency,
                GsonComponentSerializer.gson().deserialize(it.displayName.toString())
            )
        }
    )

    override fun build(packet: PacketPlayOutPlayerListHeaderFooter, clientVersion: ProtocolVersion?) = packet

    override fun build(packet: PacketPlayOutScoreboardDisplayObjective, clientVersion: ProtocolVersion?) = PacketOutDisplayObjective(
        packet.slot,
        packet.objectiveName
    )

    override fun build(packet: PacketPlayOutScoreboardObjective, clientVersion: ProtocolVersion) = PacketOutObjective(
        PacketOutObjective.Action.fromId(packet.method)!!,
        packet.objectiveName,
        Component.text(packet.displayName),
        packet.renderType.ordinal
    )

    override fun build(packet: PacketPlayOutScoreboardScore, clientVersion: ProtocolVersion?) = PacketOutUpdateScore(
        PacketOutUpdateScore.Action.fromId(packet.action.ordinal)!!,
        Component.text(packet.player),
        packet.objectiveName,
        packet.score
    )

    override fun build(packet: PacketPlayOutScoreboardTeam, clientVersion: ProtocolVersion?): PacketOutTeam {
        val action = PacketOutTeam.Action.fromId(packet.method)
        val players = packet.players.map { Component.text(it) }
        return PacketOutTeam(
            PacketOutTeam.Action.fromId(packet.method)!!,
            packet.name,
            Component.text(packet.name),
            packet.color.ordinal,
            Component.text(packet.playerPrefix),
            Component.text(packet.playerSuffix),
            packet.options and 1 > 0,
            packet.options and 2 > 0,
            packet.nametagVisibility,
            packet.collisionRule,
            players,
            if (action == PacketOutTeam.Action.ADD_MEMBERS || action == PacketOutTeam.Action.REMOVE_MEMBERS) players else emptySet()
        )
    }

    override fun readPlayerInfo(packet: Any?, clientVersion: ProtocolVersion?): PacketPlayOutPlayerInfo? {
        if (packet !is PacketOutPlayerInfo) return null
        val action = PacketPlayOutPlayerInfo.EnumPlayerInfoAction.valueOf(packet.action.name)
        val listData = packet.players.map {
            val mode = PacketPlayOutPlayerInfo.EnumGamemode.valueOf(it.gameMode.key().value().uppercase())
            val listName = IChatBaseComponent.deserialize(GsonComponentSerializer.gson().serialize(it.displayName))
            PacketPlayOutPlayerInfo.PlayerInfoData(
                it.name,
                it.uuid,
                it.properties,
                it.latency,
                mode,
                listName
            )
        }
        return PacketPlayOutPlayerInfo(action, listData)
    }

    override fun readObjective(packet: Any?, clientVersion: ProtocolVersion?) = null

    override fun readDisplayObjective(packet: Any?, clientVersion: ProtocolVersion?) = null
}
