package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.bossbar.BarColor
import me.neznamy.tab.api.bossbar.BarStyle
import me.neznamy.tab.api.chat.EnumChatFormat
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.Skin
import me.neznamy.tab.api.util.ComponentCache
import me.neznamy.tab.shared.ITabPlayer
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.adventure.KryptonAdventure
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.network.NettyConnection
import org.kryptonmc.krypton.packet.Packet
import org.kryptonmc.krypton.packet.out.play.PacketOutDisplayObjective
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateObjectives
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateScore
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateTeams
import java.util.*

class KryptonTabPlayer(
    delegate: Player,
    protocolVersion: Int
) : ITabPlayer(delegate, delegate.uuid, delegate.profile.name, "N/A", delegate.world.name, protocolVersion, true) {

    /** Component cache to save CPU when creating components  */
    private val componentCache = ComponentCache(10000) {
            component: IChatBaseComponent, clientVersion: ProtocolVersion ->
        GsonComponentSerializer.gson().deserialize(component.toString(clientVersion))
    }

    private val delegate = delegate as KryptonPlayer
    private val bossBars = mutableMapOf<UUID, BossBar>()

    fun connection(): NettyConnection = delegate.connection

    override fun hasPermission(permission: String): Boolean = delegate.hasPermission(permission)

    override fun getPing(): Int {
        val latency = delegate.connection.latency()
        if (latency > 10000 || latency < 0) return -1
        return latency
    }

    override fun sendPacket(packet: Any?) {
        if (packet == null) return
        delegate.connection.send(packet as Packet)
    }

    override fun sendMessage(message: IChatBaseComponent) {
        delegate.sendMessage(componentCache.get(message, version))
    }

    override fun hasInvisibilityPotion(): Boolean = false

    override fun isDisguised(): Boolean = false

    override fun getSkin(): Skin {
        val textures = delegate.profile.properties.firstOrNull { it.name == "textures" }
            ?: throw IllegalStateException("User does not have any skin data!")
        return Skin(textures.value, textures.signature)
    }

    override fun getPlayer(): Player = delegate

    override fun isOnline(): Boolean = delegate.isOnline()

    override fun isVanished(): Boolean = false

    override fun getGamemode(): Int = delegate.gameMode.ordinal

    override fun setPlayerListHeaderFooter(header: IChatBaseComponent, footer: IChatBaseComponent) {
        delegate.sendPlayerListHeaderAndFooter(componentCache.get(header, version), componentCache.get(footer, version))
    }

    override fun sendBossBar(id: UUID, title: String, progress: Float, color: BarColor, style: BarStyle) {
        if (bossBars.containsKey(id)) return
        val bar = bossBar(
            componentCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()),
            progress,
            Color.valueOf(color.toString()),
            Overlay.valueOf(style.toString())
        )
        bossBars[id] = bar
        delegate.showBossBar(bar)
    }

    override fun updateBossBar(id: UUID, title: String) {
        bossBars[id]?.name(componentCache.get(IChatBaseComponent.optimizedComponent(title), getVersion()))
    }

    override fun updateBossBar(id: UUID, progress: Float) {
        bossBars[id]?.progress(progress)
    }

    override fun updateBossBar(id: UUID, style: BarStyle) {
        val bar = bossBars[id] ?: return
        bar.overlay(Overlay.valueOf(style.toString()))
    }

    override fun updateBossBar(id: UUID, color: BarColor) {
        val bar = bossBars[id] ?: return
        bar.color(Color.valueOf(color.toString()))
    }

    override fun removeBossBar(id: UUID) {
        delegate.hideBossBar(bossBars[id] ?: return)
        bossBars.remove(id)
    }

    override fun setObjectiveDisplaySlot(slot: Int, objective: String) {
        sendPacket(PacketOutDisplayObjective(slot, objective))
    }

    override fun registerObjective0(objectiveName: String, title: String, hearts: Boolean) {
        sendPacket(PacketOutUpdateObjectives(
            objectiveName,
            PacketOutUpdateObjectives.Actions.CREATE,
            KryptonPacketBuilder.toComponent(title, getVersion()),
            if (hearts) 1 else 0
        ))
    }

    override fun unregisterObjective0(objectiveName: String) {
        sendPacket(PacketOutUpdateObjectives(objectiveName,PacketOutUpdateObjectives.Actions.REMOVE, Component.empty(), -1))
    }

    override fun updateObjectiveTitle0(objectiveName: String, title: String, hearts: Boolean) {
        sendPacket(PacketOutUpdateObjectives(
            objectiveName,
            PacketOutUpdateObjectives.Actions.UPDATE_TEXT,
            KryptonPacketBuilder.toComponent(title, getVersion()),
            if (hearts) 1 else 0
        ))
    }

    override fun registerScoreboardTeam0(
        name: String,
        prefix: String,
        suffix: String,
        visibility: String,
        collision: String,
        players: MutableCollection<String>,
        options: Int
    ) {
        sendPacket(PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.CREATE,
            createParameters(name, prefix, suffix, visibility, collision, options), players.map(Component::text)))
    }

    override fun unregisterScoreboardTeam0(name: String) {
        sendPacket(PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.REMOVE, null, emptyList()))
    }

    override fun updateScoreboardTeam0(
        name: String,
        prefix: String,
        suffix: String,
        visibility: String,
        collision: String,
        options: Int
    ) {
        sendPacket(PacketOutUpdateTeams(name, PacketOutUpdateTeams.Action.UPDATE_INFO,
            createParameters(name, prefix, suffix, visibility, collision, options), emptyList()
        ))
    }

    private fun createParameters(name: String, prefix: String, suffix: String, visibility: String, collision: String, options: Int): PacketOutUpdateTeams.Parameters {
        var finalPrefix = prefix
        var finalSuffix = suffix
        if (getVersion().minorVersion < 13) {
            finalPrefix = KryptonPacketBuilder.cutTo(finalPrefix, 16)
            finalSuffix = KryptonPacketBuilder.cutTo(finalSuffix, 16)
        }
        return PacketOutUpdateTeams.Parameters(
            Component.text(name),
            options,
            visibility,
            collision,
            KryptonAdventure.getColorFromId(EnumChatFormat.lastColorsOf(finalPrefix).ordinal),
            KryptonPacketBuilder.toComponent(finalPrefix, getVersion()),
            KryptonPacketBuilder.toComponent(finalSuffix, getVersion())
        )
    }

    override fun setScoreboardScore0(objective: String, player: String, score: Int) {
        sendPacket(PacketOutUpdateScore(player, 0, objective, score))
    }

    override fun removeScoreboardScore0(objective: String, player: String) {
        sendPacket(PacketOutUpdateScore(player, 1, objective, 0))
    }
}
