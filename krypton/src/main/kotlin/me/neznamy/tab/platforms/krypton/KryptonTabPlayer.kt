package me.neznamy.tab.platforms.krypton

import io.netty.channel.Channel
import me.neznamy.tab.api.chat.IChatBaseComponent
import me.neznamy.tab.api.protocol.PacketPlayOutBoss
import me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action
import me.neznamy.tab.api.protocol.PacketPlayOutChat
import me.neznamy.tab.api.protocol.PacketPlayOutChat.ChatMessageType
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter
import me.neznamy.tab.api.protocol.Skin
import me.neznamy.tab.shared.ITabPlayer
import me.neznamy.tab.shared.TAB
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Color
import net.kyori.adventure.bossbar.BossBar.Flag
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.network.NettyConnection
import org.kryptonmc.krypton.packet.Packet
import java.util.UUID

class KryptonTabPlayer(
    delegate: Player,
    protocolVersion: Int
) : ITabPlayer(delegate, delegate.uuid, delegate.profile.name, "N/A", delegate.world.name, protocolVersion, true) {

    private val delegate = delegate as KryptonPlayer
    private val bossBars = mutableMapOf<UUID, BossBar>()

    init {
        val connection = this.delegate.connection
        channel = NettyConnection::class.java.getDeclaredField("channel").apply { isAccessible = true }.get(connection) as Channel
    }

    override fun hasPermission(permission: String): Boolean = delegate.hasPermission(permission)

    override fun getPing(): Int {
        val latency = delegate.connection.latency()
        if (latency > 10000 || latency < 0) return -1
        return latency
    }

    override fun sendPacket(packet: Any?) {
        if (packet == null) return
        try {
            if (packet is Packet) {
                delegate.connection.send(packet)
                return
            }
            when (packet) {
                is PacketPlayOutBoss -> handle(packet)
                is PacketPlayOutChat -> handle(packet)
                is PacketPlayOutPlayerListHeaderFooter -> handle(packet)
            }
        } catch (exception: Exception) {
            TAB.getInstance().errorManager.printError("An error occurred when sending ${packet.javaClass.simpleName}", exception)
        }
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

    private fun handle(packet: PacketPlayOutBoss) {
        when (packet.action) {
            Action.ADD -> {
                if (bossBars.containsKey(packet.id)) return
                val bar = BossBar.bossBar(
                    GsonComponentSerializer.gson().deserialize(IChatBaseComponent.optimizedComponent(packet.name).toString(getVersion())),
                    packet.pct,
                    Color.valueOf(packet.color.toString()),
                    Overlay.valueOf(packet.overlay.toString())
                )
                if (packet.isCreateWorldFog) bar.addFlag(Flag.CREATE_WORLD_FOG)
                if (packet.isDarkenScreen) bar.addFlag(Flag.DARKEN_SCREEN)
                if (packet.isPlayMusic) bar.addFlag(Flag.PLAY_BOSS_MUSIC)
                bossBars.put(packet.id, bar)
                delegate.showBossBar(bar)
            }
            Action.REMOVE -> {
                delegate.hideBossBar(bossBars.get(packet.id) ?: return)
                bossBars.remove(packet.id)
            }
            Action.UPDATE_PCT -> bossBars.get(packet.id)?.progress(packet.pct)
            Action.UPDATE_NAME -> bossBars.get(packet.id)?.name(LegacyComponentSerializer.legacySection().deserialize(packet.name))
            Action.UPDATE_STYLE -> {
                val bar = bossBars.get(packet.id) ?: return
                bar.overlay(Overlay.valueOf(packet.overlay.toString()))
                bar.color(Color.valueOf(packet.color.toString()))
            }
            Action.UPDATE_PROPERTIES -> {
                val bar = bossBars.get(packet.id) ?: return
                processFlag(bar, packet.isCreateWorldFog, Flag.CREATE_WORLD_FOG)
                processFlag(bar, packet.isDarkenScreen, Flag.DARKEN_SCREEN)
                processFlag(bar, packet.isPlayMusic, Flag.PLAY_BOSS_MUSIC)
            }
            else -> Unit
        }
    }

    private fun handle(packet: PacketPlayOutChat) {
        val message = GsonComponentSerializer.gson().deserialize(packet.message.toString(version))
        when (packet.type) {
            ChatMessageType.CHAT -> delegate.sendMessage(message, ChatType.CHAT.bind(getPlayer().displayName))
            ChatMessageType.SYSTEM -> delegate.sendMessage(message)
            ChatMessageType.GAME_INFO -> delegate.sendActionBar(message)
        }
    }

    private fun handle(packet: PacketPlayOutPlayerListHeaderFooter) {
        val header = GsonComponentSerializer.gson().deserialize(packet.header.toString(version))
        val footer = GsonComponentSerializer.gson().deserialize(packet.footer.toString(version))
        delegate.sendPlayerListHeaderAndFooter(header, footer)
    }

    private fun processFlag(bar: BossBar, targetValue: Boolean, flag: Flag) {
        if (targetValue) {
            if (!bar.hasFlag(flag)) bar.addFlag(flag)
        } else {
            if (bar.hasFlag(flag)) bar.removeFlag(flag)
        }
    }
}
