package me.neznamy.tab.platforms.krypton

import me.neznamy.tab.api.ProtocolVersion
import me.neznamy.tab.api.Scoreboard
import me.neznamy.tab.api.tablist.TabList
import me.neznamy.tab.api.bossbar.BarColor
import me.neznamy.tab.api.bossbar.BarStyle
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.Skin
import me.neznamy.tab.api.util.ComponentCache
import me.neznamy.tab.shared.ITabPlayer
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.*
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.network.NettyConnection
import org.kryptonmc.krypton.packet.Packet
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
    private val scoreboard = KryptonScoreboard(this)
    private val tabList = KryptonTabList(this)

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

    override fun getScoreboard(): Scoreboard {
        return scoreboard
    }

    override fun getTabList(): TabList {
        return tabList
    }
}
