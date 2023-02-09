package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.chat.EnumChatFormat
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.*
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.api.auth.GameProfile
import org.kryptonmc.api.auth.ProfileProperty
import org.kryptonmc.api.world.GameMode
import org.kryptonmc.krypton.adventure.KryptonAdventure
import org.kryptonmc.krypton.entity.player.PlayerPublicKey
import org.kryptonmc.krypton.network.chat.RemoteChatSession
import org.kryptonmc.krypton.packet.out.play.PacketOutDisplayObjective
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoRemove
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfoUpdate
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateObjectives
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateScore
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateTeams
import java.util.EnumSet

// All build functions that just return the packet parameter will be passed through to be handled in KryptonTabPlayer.
object KryptonPacketBuilder : PacketBuilder() {

    private val GAME_MODES = GameMode.values()

    @JvmStatic
    fun toComponent(text: String?, clientVersion: ProtocolVersion): Component {
        if (text.isNullOrEmpty()) return Component.empty()
        return GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(text).toString(clientVersion))
    }

    override fun build(packet: PacketPlayOutBoss, clientVersion: ProtocolVersion?): Any = packet

    override fun build(packet: PacketPlayOutChat, clientVersion: ProtocolVersion?): Any = packet

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

    override fun build(packet: PacketPlayOutPlayerListHeaderFooter, clientVersion: ProtocolVersion?): Any = packet

    override fun build(packet: PacketPlayOutScoreboardDisplayObjective, clientVersion: ProtocolVersion?): Any {
        return PacketOutDisplayObjective(packet.slot, packet.objectiveName)
    }

    override fun build(packet: PacketPlayOutScoreboardObjective, clientVersion: ProtocolVersion): Any {
        return PacketOutUpdateObjectives(
            packet.objectiveName,
            packet.action,
            toComponent(packet.displayName, clientVersion),
            packet.renderType?.ordinal ?: -1
        )
    }

    override fun build(packet: PacketPlayOutScoreboardScore, clientVersion: ProtocolVersion?): Any {
        return PacketOutUpdateScore(packet.player, packet.action.ordinal, packet.objectiveName, packet.score)
    }

    override fun build(packet: PacketPlayOutScoreboardTeam, clientVersion: ProtocolVersion): Any {
        val action = PacketOutUpdateTeams.Action.fromId(packet.action)!!
        val players = packet.players.map(Component::text)
        return PacketOutUpdateTeams(packet.name, action, createParameters(packet, clientVersion), players)
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

    override fun readObjective(packet: Any?): PacketPlayOutScoreboardObjective? = null

    override fun readDisplayObjective(packet: Any?): PacketPlayOutScoreboardDisplayObjective? = null
}
