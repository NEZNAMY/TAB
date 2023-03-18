package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.Scoreboard.DisplaySlot
import me.neznamy.tab.api.TabPlayer
import me.neznamy.tab.api.chat.EnumChatFormat
import me.neznamy.tab.shared.TabScoreboard
import net.kyori.adventure.text.Component
import org.kryptonmc.krypton.packet.out.play.PacketOutDisplayObjective
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateObjectives
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateScore
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateTeams

class KryptonScoreboard(player: TabPlayer) : TabScoreboard(player) {

    override fun setDisplaySlot(slot: DisplaySlot, objective: String) {
        player.sendPacket(PacketOutDisplayObjective(slot.ordinal, objective))
    }

    override fun registerObjective0(objectiveName: String, title: String, hearts: Boolean) {
        player.sendPacket(
            PacketOutUpdateObjectives(
            objectiveName,
            PacketOutUpdateObjectives.Actions.CREATE,
            KryptonPacketBuilder.toComponent(title, player.version),
            if (hearts) 1 else 0)
        )
    }

    override fun unregisterObjective0(objectiveName: String) {
        player.sendPacket(
            PacketOutUpdateObjectives(objectiveName,
                PacketOutUpdateObjectives.Actions.REMOVE, Component.empty(), -1)
        )
    }

    override fun updateObjective0(objectiveName: String, title: String, hearts: Boolean) {
        player.sendPacket(
            PacketOutUpdateObjectives(
            objectiveName,
            PacketOutUpdateObjectives.Actions.UPDATE_TEXT,
            KryptonPacketBuilder.toComponent(title, player.version),
            if (hearts) 1 else 0)
        )
    }

    override fun registerTeam0(
        name: String,
        prefix: String,
        suffix: String,
        visibility: String,
        collision: String,
        players: MutableCollection<String>,
        options: Int
    ) {
        player.sendPacket(
            PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.CREATE,
            createParameters(name, prefix, suffix, visibility, collision, options), players.map(Component::text))
        )
    }

    override fun unregisterTeam0(name: String) {
        player.sendPacket(PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.REMOVE, null, emptyList()))
    }

    override fun updateTeam0(
        name: String,
        prefix: String,
        suffix: String,
        visibility: String,
        collision: String,
        options: Int
    ) {
        player.sendPacket(
            PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.UPDATE_INFO,
            createParameters(name, prefix, suffix, visibility, collision, options), emptyList())
        )
    }

    private fun createParameters(name: String, prefix: String, suffix: String, visibility: String, collision: String, options: Int): PacketOutUpdateTeams.Parameters {
        var finalPrefix = prefix
        var finalSuffix = suffix
        if (player.version.minorVersion < 13) {
            finalPrefix = cutTo(finalPrefix, 16)
            finalSuffix = cutTo(finalSuffix, 16)
        }
        return PacketOutUpdateTeams.Parameters(
            Component.text(name),
            options.toByte(),
            visibility,
            collision,
            EnumChatFormat.lastColorsOf(finalPrefix).ordinal,
            KryptonPacketBuilder.toComponent(finalPrefix, player.version),
            KryptonPacketBuilder.toComponent(finalSuffix, player.version)
        )
    }

    override fun setScore0(objective: String, playerName: String, score: Int) {
        player.sendPacket(PacketOutUpdateScore(playerName, 0, objective, score))
    }

    override fun removeScore0(objective: String, playerName: String) {
        player.sendPacket(PacketOutUpdateScore(playerName, 1, objective, 0))
    }
}